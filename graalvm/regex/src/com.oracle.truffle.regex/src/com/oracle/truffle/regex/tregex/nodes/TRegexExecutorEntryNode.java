/*
 * Copyright (c) 2018, 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.oracle.truffle.regex.tregex.nodes;

import static com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

import java.lang.reflect.Field;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.profiles.ValueProfile;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.regex.RegexLanguage;
import com.oracle.truffle.regex.RegexRootNode;
import com.oracle.truffle.regex.tregex.TRegexOptions;
import com.oracle.truffle.regex.util.TRegexGuards;

import sun.misc.Unsafe;

/**
 * This class wraps {@link TRegexExecutorNode} and specializes on the type of the input strings
 * provided to {@link TRegexExecNode}.
 */
@ImportStatic({TRegexGuards.class, TruffleString.CodeRange.class})
public abstract class TRegexExecutorEntryNode extends Node {

    private static final sun.misc.Unsafe UNSAFE;
    private static final long coderFieldOffset;

    static {
        String javaVersion = System.getProperty("java.specification.version");
        if (javaVersion != null && javaVersion.compareTo("1.9") < 0) {
            // UNSAFE is needed for detecting compact strings, which are not implemented prior to
            // java9
            UNSAFE = null;
            coderFieldOffset = 0;
        } else {
            UNSAFE = getUnsafe();
            Field coderField;
            try {
                coderField = String.class.getDeclaredField("coder");
            } catch (NoSuchFieldException e) {
                throw new RuntimeException("failed to get coder field offset", e);
            }
            coderFieldOffset = UNSAFE.objectFieldOffset(coderField);
        }
    }

    private static Unsafe getUnsafe() {
        try {
            return Unsafe.getUnsafe();
        } catch (SecurityException e1) {
            try {
                Field theUnsafeInstance = Unsafe.class.getDeclaredField("theUnsafe");
                theUnsafeInstance.setAccessible(true);
                return (Unsafe) theUnsafeInstance.get(Unsafe.class);
            } catch (Exception e2) {
                throw new RuntimeException("exception while trying to get Unsafe.theUnsafe via reflection:", e2);
            }
        }
    }

    private static final class TRegexExecutorRootNode extends RootNode {

        @Child TRegexExecutorNode executor;
        private final TruffleString.CodeRange codeRange;
        private final boolean isTString;

        private TRegexExecutorRootNode(RegexLanguage language, TRegexExecutorNode executor, TruffleString.CodeRange codeRange, boolean isTString) {
            super(language, RegexRootNode.SHARED_EMPTY_FRAMEDESCRIPTOR);
            this.executor = insert(executor);
            this.codeRange = codeRange;
            this.isTString = isTString;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            Object[] arguments = frame.getArguments();
            Object input = arguments[0];
            int fromIndex = (int) arguments[1];
            int index = (int) arguments[2];
            int maxIndex = (int) arguments[3];
            return executor.execute(frame, executor.createLocals(input, fromIndex, index, maxIndex), codeRange, isTString);
        }

        @TruffleBoundary
        @Override
        public String toString() {
            String src = executor.getSource().toStringEscaped();
            return "tregex " + executor.getName() + " " + codeRange + ": " + (src.length() > 30 ? src.substring(0, 30) + "..." : src);
        }
    }

    private final RegexLanguage language;
    @Child TRegexExecutorBaseNode executor;

    public TRegexExecutorEntryNode(RegexLanguage language, TRegexExecutorNode executor) {
        this.language = language;
        this.executor = executor;
    }

    public static TRegexExecutorEntryNode create(RegexLanguage language, TRegexExecutorNode executor) {
        if (executor == null) {
            return null;
        }
        return TRegexExecutorEntryNodeGen.create(language, executor);
    }

    public TRegexExecutorNode getExecutor() {
        return (TRegexExecutorNode) (executor instanceof TRegexExecutorBaseNodeWrapper ? ((TRegexExecutorBaseNodeWrapper) executor).getDelegateNode() : executor);
    }

    public abstract Object execute(VirtualFrame frame, Object input, int fromIndex, int index, int maxIndex);

    @Specialization
    Object doByteArray(VirtualFrame frame, byte[] input, int fromIndex, int index, int maxIndex,
                    @Cached("createCallTarget(BROKEN, false)") DirectCallNode callNode) {
        return runExecutor(frame, input, fromIndex, index, maxIndex, callNode, TruffleString.CodeRange.BROKEN, false);
    }

    @Specialization(guards = "isCompactString(input)")
    Object doStringCompact(VirtualFrame frame, String input, int fromIndex, int index, int maxIndex,
                    @Cached("createCallTarget(LATIN_1, false)") DirectCallNode callNode) {
        return runExecutor(frame, input, fromIndex, index, maxIndex, callNode, TruffleString.CodeRange.LATIN_1, false);
    }

    @Specialization(guards = "!isCompactString(input)")
    Object doStringNonCompact(VirtualFrame frame, String input, int fromIndex, int index, int maxIndex,
                    @Cached("createCallTarget(BROKEN, false)") DirectCallNode callNode) {
        return runExecutor(frame, input, fromIndex, index, maxIndex, callNode, TruffleString.CodeRange.BROKEN, false);
    }

    @Specialization(guards = "codeRangeEqualsNode.execute(input, cachedCodeRange)", limit = "5")
    Object doTString(VirtualFrame frame, TruffleString input, int fromIndex, int index, int maxIndex,
                    @Cached TruffleString.MaterializeNode materializeNode,
                    @Cached @SuppressWarnings("unused") TruffleString.GetCodeRangeNode codeRangeNode,
                    @Cached @SuppressWarnings("unused") TruffleString.CodeRangeEqualsNode codeRangeEqualsNode,
                    @Cached("codeRangeNode.execute(input, getExecutor().getEncoding().getTStringEncoding())") TruffleString.CodeRange cachedCodeRange,
                    @Cached("createCallTarget(cachedCodeRange, true)") DirectCallNode callNode) {
        materializeNode.execute(input, getExecutor().getEncoding().getTStringEncoding());
        return runExecutor(frame, input, fromIndex, index, maxIndex, callNode, cachedCodeRange, true);
    }

    @Specialization(guards = "neitherByteArrayNorString(input)")
    Object doTruffleObject(VirtualFrame frame, Object input, int fromIndex, int index, int maxIndex,
                    @Cached("createClassProfile()") ValueProfile inputClassProfile,
                    @Cached("createCallTarget(BROKEN, false)") DirectCallNode callNode) {
        // conservatively disable compact string optimizations.
        // TODO: maybe add an interface for TruffleObjects to announce if they are compact / ascii
        // strings?
        return runExecutor(frame, inputClassProfile.profile(input), fromIndex, index, maxIndex, callNode, TruffleString.CodeRange.BROKEN, false);
    }

    DirectCallNode createCallTarget(TruffleString.CodeRange codeRange, boolean isTString) {
        if (getExecutor().getNumberOfTransitions() <= TRegexOptions.TRegexMaxTransitionsInTrivialExecutor) {
            return null;
        } else {
            return DirectCallNode.create(new TRegexExecutorRootNode(language, getExecutor().shallowCopy(), codeRange, isTString).getCallTarget());
        }
    }

    private Object runExecutor(VirtualFrame frame, Object input, int fromIndex, int index, int maxIndex, DirectCallNode callNode, TruffleString.CodeRange cachedCodeRange, boolean isTString) {
        CompilerAsserts.partialEvaluationConstant(cachedCodeRange);
        CompilerAsserts.partialEvaluationConstant(isTString);
        CompilerAsserts.partialEvaluationConstant(callNode);
        if (callNode == null) {
            return executor.execute(frame, getExecutor().createLocals(input, fromIndex, index, maxIndex), cachedCodeRange, isTString);
        } else {
            return callNode.call(input, fromIndex, index, maxIndex);
        }
    }

    static boolean isCompactString(String str) {
        return UNSAFE != null && UNSAFE.getByte(str, coderFieldOffset) == 0;
    }
}
