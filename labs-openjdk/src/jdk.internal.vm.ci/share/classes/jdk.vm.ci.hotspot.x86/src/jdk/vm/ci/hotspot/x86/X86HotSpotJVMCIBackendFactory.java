/*
 * Copyright (c) 2012, 2016, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
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
package jdk.vm.ci.hotspot.x86;

import static jdk.vm.ci.common.InitTimer.timer;

import java.util.EnumSet;

import jdk.vm.ci.x86.X86;
import jdk.vm.ci.code.Architecture;
import jdk.vm.ci.code.RegisterConfig;
import jdk.vm.ci.code.TargetDescription;
import jdk.vm.ci.code.stack.StackIntrospection;
import jdk.vm.ci.common.InitTimer;
import jdk.vm.ci.hotspot.HotSpotCodeCacheProvider;
import jdk.vm.ci.hotspot.HotSpotConstantReflectionProvider;
import jdk.vm.ci.hotspot.HotSpotJVMCIBackendFactory;
import jdk.vm.ci.hotspot.HotSpotJVMCIRuntime;
import jdk.vm.ci.hotspot.HotSpotMetaAccessProvider;
import jdk.vm.ci.hotspot.HotSpotStackIntrospection;
import jdk.vm.ci.meta.ConstantReflectionProvider;
import jdk.vm.ci.runtime.JVMCIBackend;

public class X86HotSpotJVMCIBackendFactory implements HotSpotJVMCIBackendFactory {

    private static EnumSet<X86.CPUFeature> computeFeatures(X86HotSpotVMConfig config) {
        // Configure the feature set using the HotSpot flag settings.
        EnumSet<X86.CPUFeature> features = EnumSet.noneOf(X86.CPUFeature.class);
        if ((config.vmVersionFeatures & config.amd643DNOWPREFETCH) != 0) {
            features.add(X86.CPUFeature.AMD_3DNOW_PREFETCH);
        }
        assert config.useSSE >= 2 : "minimum config for x64";
        features.add(X86.CPUFeature.SSE);
        features.add(X86.CPUFeature.SSE2);
        if ((config.vmVersionFeatures & config.amd64SSE3) != 0) {
            features.add(X86.CPUFeature.SSE3);
        }
        if ((config.vmVersionFeatures & config.amd64SSSE3) != 0) {
            features.add(X86.CPUFeature.SSSE3);
        }
        if ((config.vmVersionFeatures & config.amd64SSE4A) != 0) {
            features.add(X86.CPUFeature.SSE4A);
        }
        if ((config.vmVersionFeatures & config.amd64SSE41) != 0) {
            features.add(X86.CPUFeature.SSE4_1);
        }
        if ((config.vmVersionFeatures & config.amd64SSE42) != 0) {
            features.add(X86.CPUFeature.SSE4_2);
        }
        if ((config.vmVersionFeatures & config.amd64POPCNT) != 0) {
            features.add(X86.CPUFeature.POPCNT);
        }
        if ((config.vmVersionFeatures & config.amd64LZCNT) != 0) {
            features.add(X86.CPUFeature.LZCNT);
        }
        if ((config.vmVersionFeatures & config.amd64ERMS) != 0) {
            features.add(X86.CPUFeature.ERMS);
        }
        if ((config.vmVersionFeatures & config.amd64AVX) != 0) {
            features.add(X86.CPUFeature.AVX);
        }
        if ((config.vmVersionFeatures & config.amd64AVX2) != 0) {
            features.add(X86.CPUFeature.AVX2);
        }
        if ((config.vmVersionFeatures & config.amd64AES) != 0) {
            features.add(X86.CPUFeature.AES);
        }
        if ((config.vmVersionFeatures & config.amd643DNOWPREFETCH) != 0) {
            features.add(X86.CPUFeature.AMD_3DNOW_PREFETCH);
        }
        if ((config.vmVersionFeatures & config.amd64BMI1) != 0) {
            features.add(X86.CPUFeature.BMI1);
        }
        if ((config.vmVersionFeatures & config.amd64BMI2) != 0) {
            features.add(X86.CPUFeature.BMI2);
        }
        if ((config.vmVersionFeatures & config.amd64RTM) != 0) {
            features.add(X86.CPUFeature.RTM);
        }
        if ((config.vmVersionFeatures & config.amd64ADX) != 0) {
            features.add(X86.CPUFeature.ADX);
        }
        if ((config.vmVersionFeatures & config.amd64AVX512F) != 0) {
            features.add(X86.CPUFeature.AVX512F);
        }
        if ((config.vmVersionFeatures & config.amd64AVX512DQ) != 0) {
            features.add(X86.CPUFeature.AVX512DQ);
        }
        if ((config.vmVersionFeatures & config.amd64AVX512PF) != 0) {
            features.add(X86.CPUFeature.AVX512PF);
        }
        if ((config.vmVersionFeatures & config.amd64AVX512ER) != 0) {
            features.add(X86.CPUFeature.AVX512ER);
        }
        if ((config.vmVersionFeatures & config.amd64AVX512CD) != 0) {
            features.add(X86.CPUFeature.AVX512CD);
        }
        if ((config.vmVersionFeatures & config.amd64AVX512BW) != 0) {
            features.add(X86.CPUFeature.AVX512BW);
        }
        if ((config.vmVersionFeatures & config.amd64AVX512VL) != 0) {
            features.add(X86.CPUFeature.AVX512VL);
        }
        if ((config.vmVersionFeatures & config.amd64SHA) != 0) {
            features.add(X86.CPUFeature.SHA);
        }
        if ((config.vmVersionFeatures & config.amd64FMA) != 0) {
            features.add(X86.CPUFeature.FMA);
        }
        if ((config.vmVersionFeatures & config.amd64CLMUL) != 0) {
            features.add(X86.CPUFeature.CLMUL);
        }
        return features;
    }

    private static EnumSet<X86.Flag> computeFlags(X86HotSpotVMConfig config) {
        EnumSet<X86.Flag> flags = EnumSet.noneOf(X86.Flag.class);
        if (config.useCountLeadingZerosInstruction) {
            flags.add(X86.Flag.UseCountLeadingZerosInstruction);
        }
        if (config.useCountTrailingZerosInstruction) {
            flags.add(X86.Flag.UseCountTrailingZerosInstruction);
        }
        return flags;
    }

    private static TargetDescription createTarget(X86HotSpotVMConfig config) {
        final int stackFrameAlignment = 16;
        final int implicitNullCheckLimit = 4096;
        final boolean inlineObjects = true;
        Architecture arch = new X86(computeFeatures(config), computeFlags(config));
        return new TargetDescription(arch, true, stackFrameAlignment, implicitNullCheckLimit, inlineObjects);
    }

    protected HotSpotConstantReflectionProvider createConstantReflection(HotSpotJVMCIRuntime runtime) {
        return new HotSpotConstantReflectionProvider(runtime);
    }

    private static RegisterConfig createRegisterConfig(X86HotSpotVMConfig config, TargetDescription target) {
        return new X86HotSpotRegisterConfig(target, false, config.windowsOs);
    }

    protected HotSpotCodeCacheProvider createCodeCache(HotSpotJVMCIRuntime runtime, TargetDescription target, RegisterConfig regConfig) {
        return new HotSpotCodeCacheProvider(runtime, target, regConfig);
    }

    protected HotSpotMetaAccessProvider createMetaAccess(HotSpotJVMCIRuntime runtime) {
        return new HotSpotMetaAccessProvider(runtime);
    }

    @Override
    public String getArchitecture() {
        return "X86";
    }

    @Override
    public String toString() {
        return "JVMCIBackend:" + getArchitecture();
    }

    @Override
    @SuppressWarnings("try")
    public JVMCIBackend createJVMCIBackend(HotSpotJVMCIRuntime runtime, JVMCIBackend host) {
        assert host == null;
        X86HotSpotVMConfig config = new X86HotSpotVMConfig(runtime.getConfigStore());
        TargetDescription target = createTarget(config);

        RegisterConfig regConfig;
        HotSpotCodeCacheProvider codeCache;
        ConstantReflectionProvider constantReflection;
        HotSpotMetaAccessProvider metaAccess;
        StackIntrospection stackIntrospection;
        try (InitTimer t = timer("create providers")) {
            try (InitTimer rt = timer("create MetaAccess provider")) {
                metaAccess = createMetaAccess(runtime);
            }
            try (InitTimer rt = timer("create RegisterConfig")) {
                regConfig = createRegisterConfig(config, target);
            }
            try (InitTimer rt = timer("create CodeCache provider")) {
                codeCache = createCodeCache(runtime, target, regConfig);
            }
            try (InitTimer rt = timer("create ConstantReflection provider")) {
                constantReflection = createConstantReflection(runtime);
            }
            try (InitTimer rt = timer("create StackIntrospection provider")) {
                stackIntrospection = new HotSpotStackIntrospection(runtime);
            }
        }
        try (InitTimer rt = timer("instantiate backend")) {
            return createBackend(metaAccess, codeCache, constantReflection, stackIntrospection);
        }
    }

    protected JVMCIBackend createBackend(HotSpotMetaAccessProvider metaAccess, HotSpotCodeCacheProvider codeCache, ConstantReflectionProvider constantReflection,
                    StackIntrospection stackIntrospection) {
        return new JVMCIBackend(metaAccess, codeCache, constantReflection, stackIntrospection);
    }
}
