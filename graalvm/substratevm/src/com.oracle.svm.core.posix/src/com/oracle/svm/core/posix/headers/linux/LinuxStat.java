/*
 * Copyright (c) 2013, 2017, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.svm.core.posix.headers.linux;

import static org.graalvm.nativeimage.c.function.CFunction.Transition.NO_TRANSITION;

import org.graalvm.nativeimage.c.CContext;
import org.graalvm.nativeimage.c.function.CFunction;
import org.graalvm.nativeimage.c.struct.AllowWideningCast;
import org.graalvm.nativeimage.c.struct.CField;
import org.graalvm.nativeimage.c.struct.CStruct;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.word.PointerBase;
import org.graalvm.word.UnsignedWord;

import com.oracle.svm.core.c.CConst;
import com.oracle.svm.core.posix.headers.PosixDirectives;

// Checkstyle: stop

/**
 * Definitions manually translated from the C header file sys/stat.h.
 */
@CContext(PosixDirectives.class)
public class LinuxStat {

    @CStruct(addStructKeyword = true)
    public interface stat64 extends PointerBase {
        @CField
        long st_ino();

        @CField
        @AllowWideningCast
        UnsignedWord st_mode();

        @CField
        int st_uid();

        @CField
        long st_size();

        @CField
        @AllowWideningCast
        UnsignedWord st_nlink();
    }

    public static class NoTransitions {
        @CFunction(transition = NO_TRANSITION)
        public static native int fstat64(int fd, stat64 buf);

        @CFunction(transition = NO_TRANSITION)
        public static native int lstat64(@CConst CCharPointer path, stat64 buf);
    }
}
