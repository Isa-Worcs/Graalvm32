/*
 * Copyright (c) 2022, 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.svm.hosted.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.graalvm.compiler.debug.GraalError;

import com.oracle.svm.hosted.annotation.AnnotationMetadata.AnnotationExtractionError;
import com.oracle.svm.util.AnnotationExtracter;
import com.oracle.svm.util.AnnotationWrapper;
import com.oracle.svm.util.GuardedAnnotationAccess;
import com.oracle.svm.util.ReflectionUtil;

import jdk.internal.reflect.ConstantPool;
import jdk.vm.ci.hotspot.HotSpotJVMCIRuntime;
import jdk.vm.ci.hotspot.HotSpotResolvedJavaField;
import jdk.vm.ci.hotspot.HotSpotResolvedJavaMethod;
import jdk.vm.ci.hotspot.HotSpotResolvedJavaType;
import jdk.vm.ci.hotspot.HotSpotResolvedObjectType;
import jdk.vm.ci.meta.ResolvedJavaMethod;
import sun.reflect.annotation.AnnotationParser;

/**
 * This class wraps all annotation accesses during the Native Image build. This is necessary to
 * avoid initializing classes that should be initialized at run-time, since looking up annotations
 * through the JDK's {@link AnnotationParser} initializes the class of every annotation on the
 * queried element.
 *
 * When queried, the extracter looks for the root of the provided element, which can be a
 * {@link Field}, {@link Method}, {@link Constructor} or {@link Class} object, as well as a record
 * component on JDK 17. It then looks into the byte arrays representing annotations in the root
 * object and outputs wrapper classes containing all the information necessary to reconstruct the
 * annotation on demand in an {@link AnnotationValue} or {@link TypeAnnotationValue} object or any
 * subclass of {@link AnnotationMemberValue}. The actual annotation can then be created using the
 * {@link AnnotationMemberValue#get(Class)} method.
 *
 * The {@link SubstrateAnnotationExtracter} is tightly coupled with {@link GuardedAnnotationAccess},
 * which provides implementations of {@link AnnotatedElement#isAnnotationPresent(Class)} and
 * {@link AnnotatedElement#getAnnotation(Class)}. {@link AnnotatedElement#getAnnotations()} should
 * in principle not be used during Native Image generation.
 */
public class SubstrateAnnotationExtracter implements AnnotationExtracter {
    private final Map<Class<?>, AnnotationValue[]> annotationCache = new ConcurrentHashMap<>();
    private final Map<AnnotatedElement, AnnotationValue[]> declaredAnnotationCache = new ConcurrentHashMap<>();
    private final Map<Executable, AnnotationValue[][]> parameterAnnotationCache = new ConcurrentHashMap<>();
    private final Map<AnnotatedElement, TypeAnnotationValue[]> typeAnnotationCache = new ConcurrentHashMap<>();
    private final Map<Method, AnnotationMemberValue> annotationDefaultCache = new ConcurrentHashMap<>();
    private final Map<AnnotationValue, Annotation> resolvedAnnotationsCache = new ConcurrentHashMap<>();

    private static final AnnotationValue[] NO_ANNOTATIONS = new AnnotationValue[0];
    private static final AnnotationValue[][] NO_PARAMETER_ANNOTATIONS = new AnnotationValue[0][0];
    private static final TypeAnnotationValue[] NO_TYPE_ANNOTATIONS = new TypeAnnotationValue[0];

    private static final Method classGetRawAnnotations = ReflectionUtil.lookupMethod(Class.class, "getRawAnnotations");
    private static final Method classGetRawTypeAnnotations = ReflectionUtil.lookupMethod(Class.class, "getRawTypeAnnotations");
    private static final Method classGetConstantPool = ReflectionUtil.lookupMethod(Class.class, "getConstantPool");
    private static final Field fieldAnnotations = ReflectionUtil.lookupField(Field.class, "annotations");
    private static final Method fieldGetTypeAnnotationBytes = ReflectionUtil.lookupMethod(Field.class, "getTypeAnnotationBytes0");
    private static final Method executableGetAnnotationBytes = ReflectionUtil.lookupMethod(Executable.class, "getAnnotationBytes");
    private static final Method executableGetTypeAnnotationBytes = ReflectionUtil.lookupMethod(Executable.class, "getTypeAnnotationBytes");
    private static final Field methodParameterAnnotations = ReflectionUtil.lookupField(Method.class, "parameterAnnotations");
    private static final Field methodAnnotationDefault = ReflectionUtil.lookupField(Method.class, "annotationDefault");
    private static final Field constructorParameterAnnotations = ReflectionUtil.lookupField(Constructor.class, "parameterAnnotations");
    private static final Class<?> recordComponentClass;
    private static final Field recordComponentAnnotations;
    private static final Field recordComponentTypeAnnotations;
    private static final Method recordComponentGetDeclaringRecord;
    private static final Method packageGetPackageInfo = ReflectionUtil.lookupMethod(Package.class, "getPackageInfo");
    private static final Object hotSpotJVMCIRuntimeReflection;
    private static final Method hotSpotJDKReflectionGetMirror;
    private static final Method hotSpotJDKReflectionGetMethod;
    private static final Method hotSpotJDKReflectionGetField;
    private static final boolean isHotSpotJDKReflectionAvailable;
    private static final Method hotSpotResolvedObjectTypeImplMirror;
    private static final Method hotSpotResolvedPrimitiveTypeMirror;
    private static final Method hotSpotResolvedJavaMethodImplToJava;
    private static final Method hotSpotResolvedJavaFieldImplToJava;

    static {
        recordComponentClass = ReflectionUtil.lookupClass(true, "java.lang.reflect.RecordComponent");
        recordComponentAnnotations = recordComponentClass != null ? ReflectionUtil.lookupField(recordComponentClass, "annotations") : null;
        recordComponentTypeAnnotations = recordComponentClass != null ? ReflectionUtil.lookupField(recordComponentClass, "typeAnnotations") : null;
        recordComponentGetDeclaringRecord = recordComponentClass != null ? ReflectionUtil.lookupMethod(recordComponentClass, "getDeclaringRecord") : null;

        Object temporaryHotSpotJVMCIRuntimeReflection = null;
        Method temporaryHotSpotJDKReflectionGetMirror = null;
        Method temporaryHotSpotJDKReflectionGetMethod = null;
        Method temporaryHotSpotJDKReflectionGetField = null;
        boolean temporaryIsHotSpotJDKReflectionAvailable = true;

        try {
            Object hotSpotJVMCIRuntime = ReflectionUtil.lookupMethod(HotSpotJVMCIRuntime.class, "runtime").invoke(null);
            temporaryHotSpotJVMCIRuntimeReflection = ReflectionUtil.lookupMethod(HotSpotJVMCIRuntime.class, "getReflection").invoke(hotSpotJVMCIRuntime);
            temporaryHotSpotJDKReflectionGetMirror = ReflectionUtil.lookupMethod(Class.forName("jdk.vm.ci.hotspot.HotSpotJDKReflection"), "getMirror", HotSpotResolvedJavaType.class);
            temporaryHotSpotJDKReflectionGetMethod = ReflectionUtil.lookupMethod(Class.forName("jdk.vm.ci.hotspot.HotSpotJDKReflection"), "getMethod",
                            Class.forName("jdk.vm.ci.hotspot.HotSpotResolvedJavaMethodImpl"));
            temporaryHotSpotJDKReflectionGetField = ReflectionUtil.lookupMethod(Class.forName("jdk.vm.ci.hotspot.HotSpotJDKReflection"), "getField",
                            Class.forName("jdk.vm.ci.hotspot.HotSpotResolvedJavaFieldImpl"));
        } catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException | ReflectionUtil.ReflectionUtilError ex) {
            temporaryIsHotSpotJDKReflectionAvailable = false;
        }

        isHotSpotJDKReflectionAvailable = temporaryIsHotSpotJDKReflectionAvailable;
        hotSpotJVMCIRuntimeReflection = temporaryHotSpotJVMCIRuntimeReflection;
        hotSpotJDKReflectionGetMirror = temporaryHotSpotJDKReflectionGetMirror;
        hotSpotJDKReflectionGetMethod = temporaryHotSpotJDKReflectionGetMethod;
        hotSpotJDKReflectionGetField = temporaryHotSpotJDKReflectionGetField;

        if (isHotSpotJDKReflectionAvailable) {
            hotSpotResolvedObjectTypeImplMirror = null;
            hotSpotResolvedPrimitiveTypeMirror = null;
            hotSpotResolvedJavaMethodImplToJava = null;
            hotSpotResolvedJavaFieldImplToJava = null;
        } else {
            try {
                hotSpotResolvedObjectTypeImplMirror = ReflectionUtil.lookupMethod(Class.forName("jdk.vm.ci.hotspot.HotSpotResolvedObjectTypeImpl"), "mirror");
                hotSpotResolvedPrimitiveTypeMirror = ReflectionUtil.lookupMethod(Class.forName("jdk.vm.ci.hotspot.HotSpotResolvedPrimitiveType"), "mirror");
                hotSpotResolvedJavaMethodImplToJava = ReflectionUtil.lookupMethod(Class.forName("jdk.vm.ci.hotspot.HotSpotResolvedJavaMethodImpl"), "toJava");
                hotSpotResolvedJavaFieldImplToJava = ReflectionUtil.lookupMethod(Class.forName("jdk.vm.ci.hotspot.HotSpotResolvedJavaFieldImpl"), "toJava");
            } catch (ClassNotFoundException e) {
                throw GraalError.shouldNotReachHere(e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Annotation> T extractAnnotation(AnnotatedElement element, Class<T> annotationType, boolean declaredOnly) {
        for (AnnotationValue annotation : getAnnotationData(element, declaredOnly)) {
            if (annotation.type != null && annotation.type.equals(annotationType)) {
                return (T) resolvedAnnotationsCache.computeIfAbsent(annotation, value -> (Annotation) value.get(annotationType));
            }
        }
        return null;
    }

    @Override
    public boolean hasAnnotation(AnnotatedElement element, Class<? extends Annotation> annotationType) {
        for (AnnotationValue annotation : getAnnotationData(element, false)) {
            if (annotation.type != null && annotation.type.equals(annotationType)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends Annotation>[] getAnnotationTypes(AnnotatedElement element) {
        return Arrays.stream(getAnnotationData(element, false)).map(AnnotationValue::getType).filter(t -> t != null).toArray(Class[]::new);
    }

    public AnnotationValue[] getDeclaredAnnotationData(AnnotatedElement element) {
        return getAnnotationData(element, true);
    }

    private AnnotationValue[] getAnnotationData(AnnotatedElement element, boolean declaredOnly) {
        AnnotatedElement root = getRoot(element);
        AnnotatedElement secondaryRoot = getSecondaryRoot(element);
        List<Annotation> injectedAnnotations = getInjectedAnnotations(element);
        List<Class<? extends Annotation>> ignoredAnnotations = getIgnoredAnnotations(element);

        List<AnnotationValue> data = new ArrayList<>();
        for (Annotation annotation : injectedAnnotations) {
            data.add(new AnnotationValue(annotation));
        }
        if (root != null) {
            data.addAll(Arrays.asList(declaredOnly ? getDeclaredAnnotationDataFromRoot(root) : getAnnotationDataFromRoot(root)));
        }
        if (secondaryRoot != null) {
            data.addAll(Arrays.asList(declaredOnly ? getDeclaredAnnotationDataFromRoot(secondaryRoot) : getAnnotationDataFromRoot(secondaryRoot)));
        }
        if (ignoredAnnotations.size() > 0) {
            data.removeIf(annotation -> ignoredAnnotations.contains(annotation.type));
        }
        return data.toArray(NO_ANNOTATIONS);
    }

    private AnnotationValue[] getAnnotationDataFromRoot(AnnotatedElement rootElement) {
        if (!(rootElement instanceof Class<?>)) {
            return getDeclaredAnnotationDataFromRoot(rootElement);
        }

        Class<?> clazz = (Class<?>) rootElement;
        List<AnnotationValue> inheritedAnnotations = new ArrayList<>();
        if (!annotationCache.containsKey(clazz)) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null) {
                for (AnnotationValue superclassAnnotation : getAnnotationDataFromRoot(superClass)) {
                    if (hasAnnotation(superclassAnnotation.type, Inherited.class)) {
                        inheritedAnnotations.add(superclassAnnotation);
                    }
                }
            }
        }

        return annotationCache.computeIfAbsent(clazz, element -> {
            AnnotationValue[] declaredAnnotations = getDeclaredAnnotationDataFromRoot(element);
            Map<Class<? extends Annotation>, AnnotationValue> annotations = new LinkedHashMap<>();
            for (AnnotationValue declaredAnnotation : declaredAnnotations) {
                annotations.put(declaredAnnotation.type, declaredAnnotation);
            }
            boolean modified = false;
            for (AnnotationValue inheritedAnnotation : inheritedAnnotations) {
                if (!annotations.containsKey(inheritedAnnotation.type)) {
                    annotations.put(inheritedAnnotation.type, inheritedAnnotation);
                    modified = true;
                }
            }
            return modified ? annotations.values().toArray(NO_ANNOTATIONS) : declaredAnnotations;
        });
    }

    private AnnotationValue[] getDeclaredAnnotationDataFromRoot(AnnotatedElement rootElement) {
        return declaredAnnotationCache.computeIfAbsent(rootElement, element -> {
            byte[] rawAnnotations = getRawAnnotations(element);
            if (rawAnnotations == null) {
                return NO_ANNOTATIONS;
            }
            ByteBuffer buf = ByteBuffer.wrap(rawAnnotations);
            try {
                List<AnnotationValue> annotations = new ArrayList<>();
                int numAnnotations = buf.getShort() & 0xFFFF;
                for (int i = 0; i < numAnnotations; i++) {
                    AnnotationValue annotation = AnnotationValue.extract(buf, getConstantPool(element), getContainer(element), false, false);
                    if (annotation != null) {
                        annotations.add(annotation);
                    }
                }
                return annotations.toArray(NO_ANNOTATIONS);
            } catch (IllegalArgumentException | BufferUnderflowException ex) {
                return new AnnotationValue[]{AnnotationValue.forAnnotationFormatException()};
            }
        });
    }

    public AnnotationValue[][] getParameterAnnotationData(AnnotatedElement element) {
        AnnotatedElement root = getRoot(element);
        return root != null ? getParameterAnnotationDataFromRoot((Executable) root) : NO_PARAMETER_ANNOTATIONS;
    }

    private AnnotationValue[][] getParameterAnnotationDataFromRoot(Executable rootElement) {
        return parameterAnnotationCache.computeIfAbsent(rootElement, element -> {
            byte[] rawParameterAnnotations = getRawParameterAnnotations(element);
            if (rawParameterAnnotations == null) {
                return NO_PARAMETER_ANNOTATIONS;
            }
            ByteBuffer buf = ByteBuffer.wrap(rawParameterAnnotations);
            try {
                int numParameters = buf.get() & 0xFF;
                AnnotationValue[][] parameterAnnotations = new AnnotationValue[numParameters][];
                for (int i = 0; i < numParameters; i++) {
                    List<AnnotationValue> parameterAnnotationList = new ArrayList<>();
                    int numAnnotations = buf.getShort() & 0xFFFF;
                    for (int j = 0; j < numAnnotations; j++) {
                        AnnotationValue parameterAnnotation = AnnotationValue.extract(buf, getConstantPool(element), getContainer(element), false, false);
                        if (parameterAnnotation != null) {
                            parameterAnnotationList.add(parameterAnnotation);
                        }
                    }
                    parameterAnnotations[i] = parameterAnnotationList.toArray(NO_ANNOTATIONS);
                }
                return parameterAnnotations;
            } catch (IllegalArgumentException | BufferUnderflowException ex) {
                return new AnnotationValue[][]{new AnnotationValue[]{AnnotationValue.forAnnotationFormatException()}};
            }
        });
    }

    public TypeAnnotationValue[] getTypeAnnotationData(AnnotatedElement element) {
        AnnotatedElement root = getRoot(element);
        return root != null ? getTypeAnnotationDataFromRoot(root) : NO_TYPE_ANNOTATIONS;
    }

    private TypeAnnotationValue[] getTypeAnnotationDataFromRoot(AnnotatedElement rootElement) {
        return typeAnnotationCache.computeIfAbsent(rootElement, element -> {
            byte[] rawTypeAnnotations = getRawTypeAnnotations(element);
            if (rawTypeAnnotations == null) {
                return NO_TYPE_ANNOTATIONS;
            }
            ByteBuffer buf = ByteBuffer.wrap(rawTypeAnnotations);
            try {
                int annotationCount = buf.getShort() & 0xFFFF;
                TypeAnnotationValue[] typeAnnotationValues = new TypeAnnotationValue[annotationCount];
                for (int i = 0; i < annotationCount; i++) {
                    typeAnnotationValues[i] = TypeAnnotationValue.extract(buf, getConstantPool(element), getContainer(element));
                }
                return typeAnnotationValues;
            } catch (IllegalArgumentException | BufferUnderflowException ex) {
                /*
                 * The byte[] arrrays in the TypeAnnotationValue are structurally correct, but have
                 * an illegal first targetInfo byte that will throw an AnnotationFormatException
                 * during parsing.
                 */
                return new TypeAnnotationValue[]{new TypeAnnotationValue(new byte[]{0x77}, new byte[]{0}, AnnotationValue.forAnnotationFormatException())};
            }
        });
    }

    public AnnotationMemberValue getAnnotationDefaultData(AnnotatedElement element) {
        AnnotatedElement root = getRoot(element);
        return root != null ? getAnnotationDefaultDataFromRoot((Method) root) : null;
    }

    private AnnotationMemberValue getAnnotationDefaultDataFromRoot(Method accessorMethod) {
        return annotationDefaultCache.computeIfAbsent(accessorMethod, method -> {
            byte[] rawAnnotationDefault = getRawAnnotationDefault(method);
            if (rawAnnotationDefault == null) {
                return null;
            }
            ByteBuffer buf = ByteBuffer.wrap(rawAnnotationDefault);
            try {
                return AnnotationMemberValue.extract(buf, getConstantPool(method), getContainer(method), false);
            } catch (IllegalArgumentException | BufferUnderflowException ex) {
                return AnnotationValue.forAnnotationFormatException();
            }
        });
    }

    private static byte[] getRawAnnotations(AnnotatedElement rootElement) {
        try {
            if (rootElement instanceof Class<?>) {
                return (byte[]) classGetRawAnnotations.invoke(rootElement);
            } else if (rootElement instanceof Field) {
                return (byte[]) fieldAnnotations.get(rootElement);
            } else if (rootElement instanceof Executable) {
                return (byte[]) executableGetAnnotationBytes.invoke(rootElement);
            } else if (recordComponentClass != null && recordComponentClass.isInstance(rootElement)) {
                return (byte[]) recordComponentAnnotations.get(rootElement);
            } else {
                throw new AnnotationExtractionError("Unexpected annotated element type: " + rootElement.getClass());
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new AnnotationExtractionError(e);
        }
    }

    private static byte[] getRawParameterAnnotations(Executable rootElement) {
        try {
            if (rootElement instanceof Method) {
                return (byte[]) methodParameterAnnotations.get(rootElement);
            } else if (rootElement instanceof Constructor<?>) {
                return (byte[]) constructorParameterAnnotations.get(rootElement);
            } else {
                throw new AnnotationExtractionError("Unexpected annotated element type: " + rootElement.getClass());
            }
        } catch (IllegalAccessException e) {
            throw new AnnotationExtractionError(e);
        }
    }

    private static byte[] getRawTypeAnnotations(AnnotatedElement rootElement) {
        try {
            if (rootElement instanceof Class<?>) {
                return (byte[]) classGetRawTypeAnnotations.invoke(rootElement);
            } else if (rootElement instanceof Field) {
                return (byte[]) fieldGetTypeAnnotationBytes.invoke(rootElement);
            } else if (rootElement instanceof Executable) {
                return (byte[]) executableGetTypeAnnotationBytes.invoke(rootElement);
            } else if (recordComponentClass != null && recordComponentClass.isInstance(rootElement)) {
                return (byte[]) recordComponentTypeAnnotations.get(rootElement);
            } else {
                throw new AnnotationExtractionError("Unexpected annotated element type: " + rootElement.getClass());
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new AnnotationExtractionError(e);
        }
    }

    private static byte[] getRawAnnotationDefault(Method method) {
        try {
            return (byte[]) methodAnnotationDefault.get(method);
        } catch (IllegalAccessException e) {
            throw new AnnotationExtractionError(e);
        }
    }

    private static ConstantPool getConstantPool(AnnotatedElement rootElement) {
        Class<?> container = getContainer(rootElement);
        try {
            return (ConstantPool) classGetConstantPool.invoke(container);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new AnnotationExtractionError(e);
        }
    }

    private static Class<?> getContainer(AnnotatedElement rootElement) {
        if (rootElement instanceof Class<?>) {
            return (Class<?>) rootElement;
        } else if (rootElement instanceof Field) {
            return ((Field) rootElement).getDeclaringClass();
        } else if (rootElement instanceof Executable) {
            return ((Executable) rootElement).getDeclaringClass();
        } else if (recordComponentClass != null && recordComponentClass.isInstance(rootElement)) {
            try {
                return (Class<?>) recordComponentGetDeclaringRecord.invoke(rootElement);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new AnnotationExtractionError(e);
            }
        } else {
            throw new AnnotationExtractionError("Unexpected annotated element type: " + rootElement.getClass());
        }
    }

    private static AnnotatedElement getRoot(AnnotatedElement element) {
        try {
            if (element instanceof Package) {
                return (Class<?>) packageGetPackageInfo.invoke(element);
            } else if (element instanceof HotSpotResolvedObjectType || element instanceof HotSpotResolvedJavaType) {
                if (isHotSpotJDKReflectionAvailable) {
                    return (AnnotatedElement) hotSpotJDKReflectionGetMirror.invoke(hotSpotJVMCIRuntimeReflection, element);
                } else {
                    if (element instanceof HotSpotResolvedObjectType) {
                        return (AnnotatedElement) hotSpotResolvedObjectTypeImplMirror.invoke(element);
                    } else {
                        return (AnnotatedElement) hotSpotResolvedPrimitiveTypeMirror.invoke(element);
                    }
                }
            } else if (element instanceof HotSpotResolvedJavaMethod) {
                if (((ResolvedJavaMethod) element).isClassInitializer()) {
                    return null;
                }
                if (isHotSpotJDKReflectionAvailable) {
                    return (AnnotatedElement) hotSpotJDKReflectionGetMethod.invoke(hotSpotJVMCIRuntimeReflection, element);
                } else {
                    return (AnnotatedElement) hotSpotResolvedJavaMethodImplToJava.invoke(element);
                }
            } else if (element instanceof HotSpotResolvedJavaField) {
                if (isHotSpotJDKReflectionAvailable) {
                    return (AnnotatedElement) hotSpotJDKReflectionGetField.invoke(hotSpotJVMCIRuntimeReflection, element);
                } else {
                    return (AnnotatedElement) hotSpotResolvedJavaFieldImplToJava.invoke(element);
                }
            } else if (element instanceof AnnotationWrapper) {
                return getRoot(((AnnotationWrapper) element).getAnnotationRoot());
            }
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            if (targetException instanceof LinkageError) {
                throw (LinkageError) targetException;
            } else if (targetException instanceof IllegalArgumentException) {
                return null;
            }
            throw new AnnotationExtractionError(e);
        } catch (IllegalAccessException e) {
            throw new AnnotationExtractionError(e);
        }
        return element;
    }

    private static AnnotatedElement getSecondaryRoot(AnnotatedElement element) {
        if (element instanceof AnnotationWrapper) {
            return getRoot(((AnnotationWrapper) element).getSecondaryAnnotationRoot());
        }
        return null;
    }

    private static List<Annotation> getInjectedAnnotations(AnnotatedElement element) {
        List<Annotation> injectedAnnotations = new ArrayList<>();
        if (element instanceof AnnotationWrapper) {
            AnnotationWrapper wrapper = (AnnotationWrapper) element;
            Annotation[] wrapperInjectedAnnotations = wrapper.getInjectedAnnotations();
            if (wrapperInjectedAnnotations != null) {
                injectedAnnotations.addAll(Arrays.asList(wrapperInjectedAnnotations));
            }
            AnnotatedElement root = wrapper.getAnnotationRoot();
            if (root != null) {
                injectedAnnotations.addAll(getInjectedAnnotations(root));
            }
            AnnotatedElement secondaryRoot = wrapper.getSecondaryAnnotationRoot();
            if (secondaryRoot != null) {
                injectedAnnotations.addAll(getInjectedAnnotations(secondaryRoot));
            }
        }
        return injectedAnnotations;
    }

    private static List<Class<? extends Annotation>> getIgnoredAnnotations(AnnotatedElement element) {
        List<Class<? extends Annotation>> ignoredAnnotations = new ArrayList<>();
        if (element instanceof AnnotationWrapper) {
            AnnotationWrapper wrapper = (AnnotationWrapper) element;
            Class<? extends Annotation>[] wrapperIgnoredAnnotations = wrapper.getIgnoredAnnotations();
            if (wrapperIgnoredAnnotations != null) {
                ignoredAnnotations.addAll(Arrays.asList(wrapperIgnoredAnnotations));
            }
            AnnotatedElement root = wrapper.getAnnotationRoot();
            if (root != null) {
                ignoredAnnotations.addAll(getIgnoredAnnotations(root));
            }
            AnnotatedElement secondaryRoot = wrapper.getSecondaryAnnotationRoot();
            if (secondaryRoot != null) {
                ignoredAnnotations.addAll(getIgnoredAnnotations(secondaryRoot));
            }
        }
        return ignoredAnnotations;
    }

}
