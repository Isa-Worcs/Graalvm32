#
# Copyright (c) 2018, 2022, Oracle and/or its affiliates. All rights reserved.
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
#
# The Universal Permissive License (UPL), Version 1.0
#
# Subject to the condition set forth below, permission is hereby granted to any
# person obtaining a copy of this software, associated documentation and/or
# data (collectively the "Software"), free of charge and under any and all
# copyright rights in the Software, and any and all patent rights owned or
# freely licensable by each licensor hereunder covering either (i) the
# unmodified Software as contributed to or provided by such licensor, or (ii)
# the Larger Works (as defined below), to deal in both
#
# (a) the Software, and
#
# (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
# one is included with the Software each a "Larger Work" to which the Software
# is contributed by such licensors),
#
# without restriction, including without limitation the rights to copy, create
# derivative works of, display, perform, and distribute the Software and make,
# use, sell, offer for sale, import, export, have made, and have sold the
# Software and the Larger Work(s), and to sublicense the foregoing rights on
# either these or other terms.
#
# This license is subject to the following condition:
#
# The above copyright notice and either this complete permission notice or at a
# minimum a reference to the UPL must be included in all copies or substantial
# portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.
#
suite = {
  "mxversion" : "6.7.0",
  "name" : "sdk",
  "version" : "22.3.2",
  "release" : True,
  "sourceinprojectwhitelist" : [],
  "url" : "https://github.com/oracle/graal",
  "groupId" : "org.graalvm.sdk",
  "developer" : {
    "name" : "GraalVM Development",
    "email" : "graalvm-dev@oss.oracle.com",
    "organization" : "Oracle Corporation",
    "organizationUrl" : "http://www.graalvm.org/",
  },
  "scm" : {
    "url" : "https://github.com/oracle/graal",
    "read" : "https://github.com/oracle/graal.git",
    "write" : "git@github.com:oracle/graal.git",
  },
  "repositories" : {
    "lafo-snapshots" : {
      "url" : "https://curio.ssw.jku.at/nexus/content/repositories/snapshots",
      "licenses" : ["GPLv2-CPE", "UPL", "BSD-new", "NCSA"]
    },
    "lafo" : {
      "snapshotsUrl" : "https://curio.ssw.jku.at/nexus/content/repositories/snapshots",
      "releasesUrl": "https://curio.ssw.jku.at/nexus/content/repositories/releases",
      "licenses" : ["GPLv2-CPE", "UPL", "BSD-new", "MIT", "NCSA"],
    },
    "lafo-maven" : {
      "snapshotsUrl" : "https://curio.ssw.jku.at/nexus/content/repositories/maven-snapshots",
      "releasesUrl": "https://curio.ssw.jku.at/nexus/content/repositories/maven-releases",
      "licenses" : ["GPLv2-CPE", "UPL", "BSD-new", "MIT", "NCSA"],
      "mavenId" : "lafo",
    },
  },
  "snippetsPattern" : ".*(Snippets|doc-files).*",
  "defaultLicense" : "UPL",
  "imports": {
    "suites": [
      {
        "name": "docs",
        "subdir": True,
      },
    ]
  },
  "ignore_suite_commit_info": True,
  "libraries" : {
    "WRK_MULTIARCH": {
      "urls": ["https://lafo.ssw.uni-linz.ac.at/pub/graal-external-deps/wrk-a211dd5-multiarch.tar.gz"],
      "sha1": "48f9df44d53b3fef8515e6d463a885aa00b5a954",
      "packedResource": True,
      "license": "Apache-2.0-wrk-a211dd5",
    },
    "WRK2_MULTIARCH": {
      "urls": ["https://lafo.ssw.uni-linz.ac.at/pub/graal-external-deps/wrk2-multiarch.tar.gz"],
      "sha1": "04a75fe3362e3b708c0c44db81fa73029d9fb604",
      "packedResource": True,
      "license": "Apache-2.0",
    },
    "APACHE_JMETER_5.3": {
      "urls": ["https://lafo.ssw.uni-linz.ac.at/pub/graal-external-deps/apache-jmeter-5.3.zip"],
      "sha1": "17480a0905d9d485bc8ce8e7be9daec2de98c251",
      "packedResource": True,
      "license": "Apache-2.0",
    },
    "UPX": {
      "packedResource": True,
      "os_arch" : {
        "linux" : {
          "i386" : {
            "sha1": "bbec782ba0864d71fe49ffbba85fbb1b1f8fb734",
            "urls": ["https://lafo.ssw.uni-linz.ac.at/pub/graal-external-deps/upx/upx-3.96-amd64_linux.tar.gz"],
          },
          "amd64" : {
            "sha1": "bbec782ba0864d71fe49ffbba85fbb1b1f8fb734",
            "urls": ["https://lafo.ssw.uni-linz.ac.at/pub/graal-external-deps/upx/upx-3.96-amd64_linux.tar.gz"],
          },
          "aarch64" : {
            "sha1" : "91acc0b24ae5fe9b13d1d313c851b7b308b66aa6",
            "urls": ["https://lafo.ssw.uni-linz.ac.at/pub/graal-external-deps/upx/upx-3.96-arm64_linux.tar.gz"],
          }
        },
        "windows" : {
          "amd64" : {
            "sha1": "7db7a58c4edd9f772f1d9dda77ec18adc6b8ce78",
            "urls": ["https://lafo.ssw.uni-linz.ac.at/pub/graal-external-deps/upx/upx-3.96-win64.zip"],
          }
        },
        "<others>" : {
          "<others>" : {
            "optional": True,
          }
        }
      }
    },
    "JLINE" : {
      "sha1" : "c3aeac59c022bdc497c8c48ed86fa50450e4896a",
      "maven" : {
        "groupId" : "jline",
        "artifactId" : "jline",
        "version" : "2.14.6",
      }
    },
    "JLINE3" : {
      "sha1" : "78571ca051ec786f4140c91661058ce56a45d433",
      "version" : "3.16.0.3",
      "urls" : ["https://lafo.ssw.uni-linz.ac.at/pub/graal-external-deps/jline3-shadowed-{version}.jar"],
      "license" : "BSD-new",
      "requires" : ["java.logging"],
      "exports" : [
        "org.graalvm.shadowed.org.fusesource.hawtjni.runtime",
        "org.graalvm.shadowed.org.fusesource.jansi",
        "org.graalvm.shadowed.org.fusesource.jansi.internal",
        "org.graalvm.shadowed.org.jline.builtins",
        "org.graalvm.shadowed.org.jline.builtins.ssh",
        "org.graalvm.shadowed.org.jline.builtins.telnet",
        "org.graalvm.shadowed.org.jline.console",
        "org.graalvm.shadowed.org.jline.console.impl",
        "org.graalvm.shadowed.org.jline.keymap",
        "org.graalvm.shadowed.org.jline.reader",
        "org.graalvm.shadowed.org.jline.reader.impl",
        "org.graalvm.shadowed.org.jline.reader.impl.completer",
        "org.graalvm.shadowed.org.jline.reader.impl.history",
        "org.graalvm.shadowed.org.jline.style",
        "org.graalvm.shadowed.org.jline.terminal",
        "org.graalvm.shadowed.org.jline.terminal.impl",
        "org.graalvm.shadowed.org.jline.terminal.impl.jansi",
        "org.graalvm.shadowed.org.jline.terminal.impl.jansi.freebsd",
        "org.graalvm.shadowed.org.jline.terminal.impl.jansi.linux",
        "org.graalvm.shadowed.org.jline.terminal.impl.jansi.osx",
        "org.graalvm.shadowed.org.jline.terminal.impl.jansi.solaris",
        "org.graalvm.shadowed.org.jline.terminal.impl.jansi.win",
        "org.graalvm.shadowed.org.jline.terminal.impl.jna",
        "org.graalvm.shadowed.org.jline.terminal.impl.jna.freebsd",
        "org.graalvm.shadowed.org.jline.terminal.impl.jna.linux",
        "org.graalvm.shadowed.org.jline.terminal.impl.jna.osx",
        "org.graalvm.shadowed.org.jline.terminal.impl.jna.solaris",
        "org.graalvm.shadowed.org.jline.terminal.impl.jna.win",
        "org.graalvm.shadowed.org.jline.terminal.spi",
        "org.graalvm.shadowed.org.jline.utils",
        "org.graalvm.shadowed.org.jline.widget",
      ],
    },
    "LLVM_ORG" : {
      "version" : "14.0.6-3-gc7a4a53c32-bgc5e298fd27",
      "host" : "https://lafo.ssw.uni-linz.ac.at/pub/llvm-org",
      "os_arch" : {
        "linux" : {
          "i386" : {
            "urls" : ["{host}/llvm-llvmorg-{version}-linux-amd64.tar.gz"],
            "sha1" : "4cd45eff8e914189dd8bebcfaf46271c412c57fa",
          },
          "amd64" : {
            "urls" : ["{host}/llvm-llvmorg-{version}-linux-amd64.tar.gz"],
            "sha1" : "4cd45eff8e914189dd8bebcfaf46271c412c57fa",
          },
          "aarch64" : {
            "urls" : ["{host}/llvm-llvmorg-{version}-linux-aarch64.tar.gz"],
            "sha1" : "bf95d0cb96d29d061e2106f221f9535d38d37daf",
          },
          "riscv64": {
            "urls" : ["{host}/llvm-llvmorg-{version}-linux-riscv64.tar.gz"],
            "sha1" : "4a75da563e277f5d222778f2b814b8e5f7e82609",
          },
        },
        "darwin" : {
          "amd64" : {
            "urls" : ["{host}/llvm-llvmorg-{version}-darwin-amd64.tar.gz"],
            "sha1" : "61960f183a08436c16652c6bc8b0de67468899fb",
          },
          "aarch64" : {
            "urls" : ["{host}/llvm-llvmorg-{version}-darwin-aarch64.tar.gz"],
            "sha1" : "f8a11403a5a975ff7eb231c785979a37000cbf62",
          }
        },
        "windows" : {
          "amd64" : {
            "urls" : ["{host}/llvm-llvmorg-{version}-windows-amd64.tar.gz"],
            "sha1" : "3e4cae36505ba566a983b3beacdb3523ccbf114e",
          }
        },
        "<others>": {
          "<others>": {
            "optional": True,
          }
        },
      },
      "license" : "Apache-2.0-LLVM",
    },
    "LLVM_ORG_COMPILER_RT_LINUX" : {
      "version" : "14.0.6-3-gc7a4a53c32-bgc5e298fd27",
      "host" : "https://lafo.ssw.uni-linz.ac.at/pub/llvm-org",
      # we really want linux-amd64, also on non-linux and non-amd64 platforms for cross-compilation
      "urls" : ["{host}/compiler-rt-llvmorg-{version}-linux-amd64.tar.gz"],
      "sha1" : "e214d63812b9276880e5c4e1849a493653f1e269",
      "license" : "Apache-2.0-LLVM",
    },
    "LLVM_ORG_SRC" : {
      "version" : "14.0.6-3-gc7a4a53c32-bgc5e298fd27",
      "host" : "https://lafo.ssw.uni-linz.ac.at/pub/llvm-org",
      "packedResource" : True,
      "urls" : ["{host}/llvm-src-llvmorg-{version}.tar.gz"],
      "sha1" : "4b631ecd732e38d491ff6f41da796e393cb1d874",
      "license" : "Apache-2.0-LLVM",
    },
  },
  "projects" : {
    "org.graalvm.options" : {
      "subDir" : "src",
      "sourceDirs" : ["src"],
      "dependencies" : [],
      "checkstyle" : "org.graalvm.word",
      "javaCompliance" : "11+",
      "workingSets" : "API,SDK",
    },
    "org.graalvm.polyglot" : {
      "subDir" : "src",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "org.graalvm.collections",
        "org.graalvm.home",
      ],
      "requires" : [
        "java.logging",
      ],
      "checkstyle" : "org.graalvm.word",
      "javaCompliance" : "11+",
      "workingSets" : "API,SDK",
    },

    "org.graalvm.word" : {
      "subDir" : "src",
      "sourceDirs" : ["src"],
      "dependencies" : [],
      "checkstyle" : "org.graalvm.word",
      "javaCompliance" : "11+",
      "checkstyleVersion" : "8.36.1",
      "workingSets" : "API,SDK",
    },

    "org.graalvm.nativeimage" : {
      "subDir" : "src",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "org.graalvm.word",
        "org.graalvm.options",
      ],
      "checkstyle" : "org.graalvm.word",
      "javaCompliance" : "11+",
      "workingSets" : "API,SDK",
    },
    "com.oracle.svm.core.annotate" : {
      "subDir" : "src",
      "sourceDirs" : ["src"],
      "dependencies" : [
         "org.graalvm.nativeimage",
      ],
      "checkstyle" : "org.graalvm.word",
      "javaCompliance" : "11+",
      "workingSets" : "API,SDK",
    },
    "org.graalvm.nativeimage.test" : {
      "subDir" : "src",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "mx:JUNIT",
        "org.graalvm.nativeimage"
      ],
      "javaCompliance" : "11+",
      "workingSets" : "SDK",
      "checkstyle" : "org.graalvm.word",
    },
    "org.graalvm.launcher" : {
      "subDir" : "src",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "org.graalvm.polyglot",
        "JLINE",
        "JLINE3",
      ],
      "requires" : [
        "java.logging",
      ],
      "javaCompliance" : "11+",
      "workingSets" : "Truffle,Tools",
      "checkstyle" : "org.graalvm.word",
    },
    "org.graalvm.launcher.test" : {
      "subDir" : "src",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "mx:JUNIT",
        "org.graalvm.launcher"
      ],
      "javaCompliance" : "11+",
      "workingSets" : "Truffle,Tools,Test",
      "checkstyle" : "org.graalvm.word",
    },
    "org.graalvm.polyglot.tck" : {
      "subDir" : "src",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "org.graalvm.polyglot",
      ],
      "checkstyle" : "org.graalvm.word",
      "javaCompliance" : "11+",
      "workingSets" : "API,SDK,Test",
    },
    "org.graalvm.collections" : {
      "subDir" : "src",
      "sourceDirs" : ["src"],
      "checkstyle" : "org.graalvm.word",
      "javaCompliance" : "11+",
      "workingSets" : "API,SDK",
    },
    "org.graalvm.collections.test" : {
      "subDir" : "src",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "mx:JUNIT",
        "org.graalvm.collections",
      ],
      "checkstyle" : "org.graalvm.word",
      "javaCompliance" : "11+",
      "workingSets" : "API,SDK,Test",
    },
    "org.graalvm.home" : {
      "subDir" : "src",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "org.graalvm.nativeimage",
      ],
      "checkstyle" : "org.graalvm.word",
      "javaCompliance" : "11+",
      "workingSets" : "API,SDK",
    },
    "org.graalvm.home.test" : {
      "subDir" : "src",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "mx:JUNIT",
        "org.graalvm.home",
      ],
      "checkstyle" : "org.graalvm.word",
      "javaCompliance" : "11+",
      "workingSets" : "API,SDK",
    },
  },
  "licenses" : {
    "UPL" : {
      "name" : "Universal Permissive License, Version 1.0",
      "url" : "http://opensource.org/licenses/UPL",
    },
    "NCSA" : {
      "name" : "University of Illinois/NCSA Open Source License",
      "url" : "https://releases.llvm.org/8.0.0/LICENSE.TXT"
    },
    "Apache-2.0-LLVM" : {
      "name" : "Apache License 2.0 with LLVM Exceptions",
      "url" : "http://releases.llvm.org/9.0.0/LICENSE.TXT"
    },
    "Apache-2.0-wrk-a211dd5" : {
      "name" : "Modified Apache 2.0 License",
      "url" : "https://raw.githubusercontent.com/wg/wrk/a211dd5a7050b1f9e8a9870b95513060e72ac4a0/LICENSE"
    },
},

  # ------------- Distributions -------------
  "distributions" : {
    "GRAAL_SDK" : {
      "subDir" : "src",
      "dependencies" : [
        "org.graalvm.polyglot",
        "org.graalvm.nativeimage",
        "com.oracle.svm.core.annotate",
        "org.graalvm.collections",
        "org.graalvm.home",
      ],
      "distDependencies" : [],
      "javadocType": "api",
      "moduleInfo" : {
        "name" : "org.graalvm.sdk",
        "requires" : ["java.logging"],
        "exports" : [
          "org.graalvm.collections",
          "org.graalvm.home",
          "org.graalvm.home.impl",
          "com.oracle.svm.core.annotate",
          "org.graalvm.nativeimage.hosted",
          "org.graalvm.nativeimage.c.function",
          "org.graalvm.nativeimage.c.struct",
          "org.graalvm.nativeimage.c.type",
          "org.graalvm.nativeimage.c.constant",
          "org.graalvm.nativeimage.c",
          "org.graalvm.nativeimage",
          "org.graalvm.polyglot.proxy",
          "org.graalvm.polyglot.io",
          "org.graalvm.polyglot.management",
          "org.graalvm.polyglot",
          "org.graalvm.options",
          "org.graalvm.word",
          "org.graalvm.polyglot.impl to org.graalvm.truffle, com.oracle.graal.graal_enterprise",
          "org.graalvm.word.impl to jdk.internal.vm.compiler",
          "org.graalvm.nativeimage.impl to org.graalvm.nativeimage.builder,org.graalvm.nativeimage.configure,com.oracle.svm.svm_enterprise",
          "org.graalvm.nativeimage.impl.clinit to org.graalvm.nativeimage.builder",
        ],
        "uses" : [
          "org.graalvm.polyglot.impl.AbstractPolyglotImpl"
        ],
        "opens" : [
          "org.graalvm.polyglot to org.graalvm.truffle"
        ],
      },
      "description" : "GraalVM is an ecosystem for compiling and running applications written in multiple languages.\nGraalVM removes the isolation between programming languages and enables interoperability in a shared runtime.",
    },
    "SDK_TEST" : {
      "subDir" : "src",
      "dependencies" : [
        "org.graalvm.collections.test",
        "org.graalvm.nativeimage.test",
        "org.graalvm.launcher.test",
        "org.graalvm.home.test",
      ],
      "distDependencies" : [
        "GRAAL_SDK",
        "LAUNCHER_COMMON"
      ],
      "maven" : False,
    },
    "LAUNCHER_COMMON" : {
      "subDir" : "src",
      "moduleInfo" : {
        "name" : "org.graalvm.launcher",
        "exports" : [
          "org.graalvm.launcher",
        ],
      },
      "dependencies" : [
        "org.graalvm.launcher",
      ],
      "distDependencies" : [
        "GRAAL_SDK",
      ],
      "description" : "Common infrastructure to create language launchers using the Polyglot API.",
      "allowsJavadocWarnings": True,
    },
    "POLYGLOT_TCK" : {
      "subDir" : "src",
      "moduleInfo" : {
        "name" : "org.graalvm.polyglot_tck",
        "exports" : [
          "org.graalvm.polyglot.tck"
        ],
      },
      "dependencies" : [
        "org.graalvm.polyglot.tck",
      ],
      "distDependencies" : [
        "GRAAL_SDK",
      ],
      "javadocType": "api",
      "description" : """GraalVM TCK SPI""",
    },
    "LLVM_ORG_FILTERED": {
      "native": True,
      "description": "LLVM_ORG build with some things removed that we don't want to redistribute",
      "os_arch": {
        "windows": {
          "<others>": {
            "layout": {
              "./": {
                # Starting with LLVM 13, the LLVM build tries to create symlinks if possible.
                # On Windows, symlinks are only supported when developer mode is enabled.
                # Get rid of the symlinks here, so our users don't need to enable developer mode.
                "dereference": "always"
              },
            },
          },
        },
        "<others>": {
          "<others>": {
            "layout": {
              "./": {
                "dereference": "never"
              },
            },
          },
        },
      },
      "layout": {
        "./": {
          "source_type": "extracted-dependency",
          "dependency": "LLVM_ORG",
          "path": "*",
          "exclude": [
            "bin/bugpoint*",
            "bin/c-index-test*",
            "bin/clang-check*",
            "bin/clang-extdef-mapping*",
            "bin/clang-import-test*",
            "bin/clang-offload-*",
            "bin/clang-refactor*",
            "bin/clang-rename*",
            "bin/clang-scan-deps*",
            "bin/diagtool*",
            "bin/git-clang-format",
            "bin/hmaptool",
            "bin/llvm-addr2line*",
            "bin/llvm-bcanalyzer*",
            "bin/llvm-cat*",
            "bin/llvm-cfi-verify*",
            "bin/llvm-cov*",
            "bin/llvm-c-test*",
            "bin/llvm-cvtres*",
            "bin/llvm-cxxdump*",
            "bin/llvm-cxxfilt*",
            "bin/llvm-cxxmap*",
            "bin/llvm-dwp*",
            "bin/llvm-elfabi*",
            "bin/llvm-exegesis*",
            "bin/llvm-jitlink*",
            "bin/llvm-lipo*",
            "bin/llvm-lto*",
            "bin/llvm-lto2*",
            "bin/llvm-mc*",
            "bin/llvm-mca*",
            "bin/llvm-modextract*",
            "bin/llvm-mt*",
            "bin/llvm-opt-report*",
            "bin/llvm-pdbutil*",
            "bin/llvm-profdata*",
            "bin/llvm-rc*",
            "bin/llvm-rtdyld*",
            "bin/llvm-size*",
            "bin/llvm-split*",
            "bin/llvm-stress*",
            "bin/llvm-strings*",
            "bin/llvm-symbolizer*",
            "bin/llvm-tblgen*",
            "bin/llvm-undname*",
            "bin/llvm-windres*", # symlink to llvm-rc
            "bin/llvm-xray*",
            "bin/obj2yaml*",
            "bin/sancov*",
            "bin/sanstats*",
            "bin/scan-build*",
            "bin/scan-view*",
            "bin/verify-uselistorder*",
            "bin/yaml2obj*",
            "bin/set-xcode-analyzer",
            "share",
            "include/clang",
            "include/clang-c",
            "include/lld",
            "include/llvm",
            "include/llvm-c",
            "lib/cmake",
            "lib/Checker*",
            "lib/Sample*",
            "lib/libRemarks*",
            "lib/libLLVM*.a",
            "lib/libclang.so*",
            "lib/libclang.dylib*",
            "lib/libclang*.a",
            "lib/liblld*.a",
            "libexec",
            # the following is added by COMPILER_RT
            "lib/clang/*/lib/linux/*clang_rt*",
            # Windows libarary excludes
            "lib/*.lib",
          ]
        },
      },
    },
    "LLVM_TOOLCHAIN": {
      "native": True,
      "description": "LLVM with general purpose patches used by Sulong and Native Image",
      "layout": {
        "./": [
          "extracted-dependency:LLVM_ORG_FILTERED",
          "extracted-dependency:LLVM_ORG_COMPILER_RT_LINUX",
          "file:3rd_party_license_llvm-toolchain.txt",
        ],
        "./patches/" : "file:llvm-patches/*",
      },
      "platformDependent" : True,
      "maven": False,
      "license" : "Apache-2.0-LLVM",
    },
    "LLVM_TOOLCHAIN_FULL": {
      "description": "Distribution including all of LLVM. Use only for building/testing. Only the content of LLVM_TOOLCHAIN will be included in the llvm-toolchain installable.",
      "native": True,
      "layout": {
        "./": [
          {
            "source_type": "extracted-dependency",
            "dependency": "LLVM_ORG",
            "path": "*",
            "dereference": "never",
          },
        ],
      },
      "platformDependent" : True,
      "maven": False,
      "license" : "Apache-2.0-LLVM",
      "defaultBuild" : False,
    },
  },
}
