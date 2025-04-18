# pylint: disable=line-too-long
suite = {
    "mxversion" : "6.5.5",
    "name": "substratevm",
    "version" : "22.3.2",
    "release" : True,
    "url" : "https://github.com/oracle/graal/tree/master/substratevm",

    "groupId" : "org.graalvm.nativeimage",
    "developer": {
        "name": "GraalVM Development",
        "email": "graalvm-dev@oss.oracle.com",
        "organization": "Oracle Corporation",
        "organizationUrl": "http://www.graalvm.org/",
    },
    "scm" : {
        "url" : "https://github.com/oracle/graal",
        "read" : "https://github.com/oracle/graal.git",
        "write" : "git@github.com:oracle/graal.git",
    },

    "defaultLicense" : "GPLv2-CPE",

    "versionConflictResolution": "latest",

    "javac.lint.overrides": "-path",

    "imports": {
        "suites": [
            {
                "name": "compiler",
                "subdir": True,
                "urls" : [
                    {"url" : "https://curio.ssw.jku.at/nexus/content/repositories/snapshots", "kind" : "binary"},
                ]
            },
        ]
    },

    "libraries" : {
        "RENAISSANCE_HARNESS_v0.9" : {
            "urls" : ["https://lafo.ssw.uni-linz.ac.at/pub/graal-external-deps/renaissance/renaissance-harness_v0.9.0.tar.gz"],
            "sha1" : "0bef46df4699d896034005d6f3f0422a7075482b",
            "packedResource": True,
        },
        "RENAISSANCE_HARNESS_v0.10" : {
            "urls" : ["https://lafo.ssw.uni-linz.ac.at/pub/graal-external-deps/renaissance/renaissance-harness_v0.10.0.tar.gz"],
            "sha1" : "842e60f56d9871a1fa5700dcc446acbd041e875b",
            "packedResource": True,
        },
        "RENAISSANCE_HARNESS_v0.11" : {
            "urls" : ["https://lafo.ssw.uni-linz.ac.at/pub/graal-external-deps/renaissance/renaissance-harness_v0.11.0.tar.gz"],
            "sha1" : "8d402c1e7c972badfcffdd6c64ed4e791b0dea02",
            "packedResource": True,
        },
        "RENAISSANCE_HARNESS_v0.12" : {
            "urls" : ["https://lafo.ssw.uni-linz.ac.at/pub/graal-external-deps/renaissance/renaissance-harness_v0.12.0.tar.gz"],
            "sha1" : "84592bedd6f0afa842aadb8813d395317b1fa385",
            "packedResource": True,
        },
        "RENAISSANCE_HARNESS_v0.13" : {
            "urls" : ["https://lafo.ssw.uni-linz.ac.at/pub/graal-external-deps/renaissance/renaissance-harness_v0.13.0.tar.gz"],
            "sha1" : "8edc1db5c7ea977e2a9c037b5324bb4cbee40082",
            "packedResource": True,
        },
        "SPARK_BREEZE_PATCHED" : {
            "urls" : ["https://lafo.ssw.uni-linz.ac.at/pub/graal-external-deps/breeze_2.11-0.11.2-patched.jar"],
            "sha1" : "e3327f5d890b5af0f7363a8b3cd95b6ce24bc1ea",
        },
        "XERCES_IMPL" : {
            "sha1" : "006898f2bdfeca5ac996cfff1b76ef98af5aa6f2",
            "maven" : {
                "groupId" : "xerces",
                "artifactId" : "xercesImpl",
                "version" : "2.6.2-jaxb-1.0.6",
           },
        },
        "LLVM_WRAPPER_SHADOWED": {
            "sha1" : "6e2ccf2127750962ac10fbedee7476fb392d967a",
            "sourceSha1" : "f3e062834bd7eac4e7d7ec039e2961d07556a87b",
            "dependencies" : ["JAVACPP_SHADOWED"],
            "urlbase": "https://lafo.ssw.uni-linz.ac.at/pub/graal-external-deps/native-image",
            "urls": ["{urlbase}/llvm-shadowed-13.0.1-1.5.7.jar"],
            "sourceUrls": ["{urlbase}/llvm-shadowed-13.0.1-1.5.7-sources.jar"],
            "license" : "GPLv2-CPE",
            "moduleName" : "com.oracle.svm.shadowed.org.bytedeco.llvm"
        },
        "JAVACPP_SHADOWED": {
            "sha1" : "85ba34efaf9b0ce71b4d2a426637ba37b89cf05c",
            "sourceSha1" : "99c5aaba05b0772577722f51ba3c9eb28ae6a435",
            "urlbase": "https://lafo.ssw.uni-linz.ac.at/pub/graal-external-deps/native-image",
            "urls": ["{urlbase}/javacpp-shadowed-1.5.7.jar"],
            "sourceUrls": ["{urlbase}/javacpp-shadowed-1.5.7-sources.jar"],
            "license" : "GPLv2-CPE",
            "moduleName" : "com.oracle.svm.shadowed.org.bytedeco.javacpp"
        },
        "LLVM_PLATFORM_SPECIFIC_SHADOWED": {
            "urlbase": "https://lafo.ssw.uni-linz.ac.at/pub/graal-external-deps/native-image",
            "os_arch": {
                "linux": {
                    "amd64": {
                        "sha1": "2ee54f786d4f4bb8f3e74106b33474203d9f09c1",
                        "urls": ["{urlbase}/llvm-shadowed-13.0.1-1.5.7_1-linux-x86_64.jar"],
                        "moduleName" : "com.oracle.svm.shadowed.org.bytedeco.llvm.linux.x86_64"
                    },
                    "i386": {
                        "sha1": "2ee54f786d4f4bb8f3e74106b33474203d9f09c1",
                        "urls": ["{urlbase}/llvm-shadowed-13.0.1-1.5.7_1-linux-x86_64.jar"],
                        "moduleName" : "com.oracle.svm.shadowed.org.bytedeco.llvm.linux.x86_64"
                    },
                    "aarch64": {
                        "sha1": "0aac89ebd1682f03372e566f00a8cd97af2eb75a",
                        "urls": ["{urlbase}/llvm-shadowed-13.0.1-1.5.7_1-linux-arm64.jar"],
                        "moduleName" : "com.oracle.svm.shadowed.org.bytedeco.llvm.linux.arm64"
                    },
                    "<others>": {
                        "optional": True,
                    },
                },
                "darwin": {
                    "amd64": {
                        "sha1": "a9b5fccebfb3110154cc3302ace53f86d00137f9",
                        "urls": ["{urlbase}/llvm-shadowed-13.0.1-1.5.7_1-macosx-x86_64.jar"],
                        "moduleName" : "com.oracle.svm.shadowed.org.bytedeco.llvm.macosx.x86_64"
                    },
                    "aarch64": {
                        # GR-34811
                        "optional": True,
                    },
                },
                "<others>": {
                    "<others>": {
                        "optional": True,
                    }
                }
            },
        },
        "JAVACPP_PLATFORM_SPECIFIC_SHADOWED": {
            "urlbase": "https://lafo.ssw.uni-linz.ac.at/pub/graal-external-deps/native-image",
            "os_arch": {
                "linux": {
                    "amd64": {
                        "sha1": "9b787a3e5422d06283f0138b937fd3054609cea0",
                        "urls": ["{urlbase}/javacpp-shadowed-1.5.7_1-linux-x86_64.jar"],
                        "moduleName" : "com.oracle.svm.shadowed.org.bytedeco.javacpp.linux.x86_64"
                    },
                    "i386": {
                        "sha1": "9b787a3e5422d06283f0138b937fd3054609cea0",
                        "urls": ["{urlbase}/javacpp-shadowed-1.5.7_1-linux-x86_64.jar"],
                        "moduleName" : "com.oracle.svm.shadowed.org.bytedeco.javacpp.linux.x86_64"
                    },
                    "aarch64": {
                        "sha1": "412d83d75de5660487c32eb4174dd8a1339ed701",
                        "urls": ["{urlbase}/javacpp-shadowed-1.5.7_1-linux-arm64.jar"],
                        "moduleName" : "com.oracle.svm.shadowed.org.bytedeco.javacpp.linux.arm64"
                    },
                    "<others>": {
                        "optional": True,
                    },
                },
                "darwin": {
                    "amd64": {
                        "sha1": "c8f8264563fd6d5598884182341da03aebfc01d6",
                        "urls": ["{urlbase}/javacpp-shadowed-1.5.7_1-macosx-x86_64.jar"],
                        "moduleName" : "com.oracle.svm.shadowed.org.bytedeco.javacpp.macosx.x86_64"
                    },
                    "aarch64": {
                        # GR-34811
                        "optional": True,
                    },
                },
                "<others>": {
                    "<others>": {
                        "optional": True,
                    }
                }
            },
        }
    },

    "projects": {
        "com.oracle.svm.util": {
            "subDir": "src",
            "sourceDirs": ["src"],
            "dependencies": [
                "sdk:GRAAL_SDK",
                "compiler:GRAAL",
            ],
            "requires" : [
                "java.instrument",
            ],
            "requiresConcealed" : {
                "java.base" : ["jdk.internal.module"],
            },
            "javaCompliance": "11+",
            "annotationProcessors": [
                "compiler:GRAAL_PROCESSOR",
            ],
            "checkstyle": "com.oracle.svm.core",
            "workingSets": "SVM",
        },

        "com.oracle.svm.common": {
            "subDir": "src",
            "sourceDirs": ["src"],
            "dependencies": [
                "com.oracle.svm.util"
            ],
            "javaCompliance": "11+",
            "annotationProcessors": [
                "compiler:GRAAL_PROCESSOR",
            ],
            "checkstyle": "com.oracle.svm.core",
            "workingSets": "SVM",
        },

        "com.oracle.svm.processor" : {
            "subDir" : "src",
            "sourceDirs" : ["src"],
            "dependencies" : [
                "compiler:GRAAL_PROCESSOR"
            ],
            "requires" : [
                "java.compiler" # javax.annotation.processing.*
            ],
            "javaCompliance": "11+",
            "checkstyle" : "com.oracle.svm.core",
            "workingSets" : "SVM",
        },

        "com.oracle.svm.core": {
            "subDir": "src",
            "sourceDirs": [
                "src",
                "headers",
            ],
            "dependencies": [
                "com.oracle.svm.common",
            ],
            "requires" : [
                "java.compiler",
                "java.logging",
                "java.scripting",
                "jdk.httpserver",
                "jdk.jfr",
                "jdk.management",
                "jdk.unsupported",
            ],
            "requiresConcealed" : {
                "java.base" : [
                    "sun.invoke.util",
                    "sun.net",
                    "sun.nio.ch",
                    "sun.reflect.annotation",
                    "sun.reflect.generics.factory",
                    "sun.reflect.generics.reflectiveObjects",
                    "sun.reflect.generics.repository",
                    "sun.reflect.generics.tree",
                    "sun.security.jca",
                    "sun.security.ssl",
                    "sun.security.util",
                    "sun.text.spi",
                    "sun.util",
                    "sun.util.calendar",
                    "sun.util.locale.provider",
                    "sun.util.resources",
                    "jdk.internal.event",
                    "jdk.internal.loader",
                    "jdk.internal.logger",
                    "jdk.internal.misc",
                    "jdk.internal.module",
                    "jdk.internal.perf",
                    "jdk.internal.ref",
                    "jdk.internal.reflect",
                    "jdk.internal.util",
                ],
                "java.desktop": [
                    "sun.java2d",
                    "sun.java2d.pipe",
                ],
                "java.management": [
                    "com.sun.jmx.mbeanserver",
                    "sun.management",
                ],
                "jdk.management": [
                    "com.sun.management.internal"
                ],
                "jdk.httpserver@19+": [
                    "sun.net.httpserver.simpleserver",
                ],
                "jdk.jfr": [
                    "jdk.jfr.events",
                    "jdk.jfr.internal",
                    "jdk.jfr.internal.jfc",
                ],
                "jdk.jfr@11..18": [
                    "jdk.jfr.internal.handlers",
                ],
            },
            "javaCompliance": "11+",
            "checkstyleVersion" : "8.36.1",
            "annotationProcessors": [
                "compiler:GRAAL_PROCESSOR",
                "SVM_PROCESSOR",
            ],
            "workingSets": "SVM",
        },

        "com.oracle.svm.core.containers": {
            "subDir": "src",
            "sourceDirs": ["src"],
            "dependencies": ["com.oracle.svm.core"],
            "javaCompliance": "11+",
            "annotationProcessors": [
                "compiler:GRAAL_PROCESSOR",
                "SVM_PROCESSOR",
            ],
            "workingSets": "SVM",
            "spotbugs": "false",
        },


        "com.oracle.svm.core.jdk17": {
            "subDir": "src",
            "sourceDirs": ["src"],
            "dependencies": ["com.oracle.svm.core"],
            "requiresConcealed" : {
                "java.base" : [
                    "jdk.internal.access.foreign",
                    "jdk.internal.loader",
                    "jdk.internal.misc",
                    "jdk.internal.platform",
                    "sun.invoke.util",
                ],
            },
            "javaCompliance": "17+",
            "annotationProcessors": [
                "compiler:GRAAL_PROCESSOR",
                "SVM_PROCESSOR",
            ],
            "checkstyle": "com.oracle.svm.core",
            "workingSets": "SVM",
        },


        "com.oracle.svm.core.genscavenge": {
            "subDir": "src",
            "sourceDirs": [
                "src",
            ],
            "dependencies": [
                "com.oracle.svm.core",
            ],
            "requires" : [
                "jdk.management",
            ],
            "requiresConcealed" : {
                "java.base": [
                    "sun.nio.ch",
                ],
                "java.management": [
                    "sun.management",
                ],
                "jdk.internal.vm.ci" : [
                    "jdk.vm.ci.code",
                ],
            },
            "checkstyle": "com.oracle.svm.core",
            "javaCompliance": "11+",
            "annotationProcessors": [
                "compiler:GRAAL_PROCESSOR",
                "SVM_PROCESSOR",
            ],
            "workingSets": "SVM",
        },

        "com.oracle.svm.core.graal.amd64": {
            "subDir": "src",
            "sourceDirs": ["src"],
            "dependencies": [
                "com.oracle.svm.core",
            ],
            "requiresConcealed" : {
                "jdk.internal.vm.ci" : [
                    "jdk.vm.ci.code.site",
                ],
            },
            "checkstyle": "com.oracle.svm.core",
            "javaCompliance": "11+",
            "annotationProcessors": [
                "compiler:GRAAL_PROCESSOR",
                "SVM_PROCESSOR",
            ],
            "workingSets": "SVM",
        },
        "com.oracle.svm.core.graal.aarch64": {
            "subDir": "src",
            "sourceDirs": ["src"],
            "dependencies": [
                "com.oracle.svm.core",
            ],
            "requiresConcealed" : {
                "jdk.internal.vm.ci" : [
                    "jdk.vm.ci.code.site",
                ],
            },
            "checkstyle": "com.oracle.svm.core",
            "javaCompliance": "11+",
            "annotationProcessors": [
                "compiler:GRAAL_PROCESSOR",
                "SVM_PROCESSOR",
            ],
            "workingSets": "SVM",
        },
        "com.oracle.svm.core.graal.llvm": {
            "subDir": "src",
            "sourceDirs": ["src"],
            "dependencies": [
                "com.oracle.svm.hosted",
                "LLVM_WRAPPER_SHADOWED",
                "LLVM_PLATFORM_SPECIFIC_SHADOWED",
                "JAVACPP_PLATFORM_SPECIFIC_SHADOWED",
            ],
            "checkstyle": "com.oracle.svm.core",
            "javaCompliance": "11+",
            "annotationProcessors": [
                "compiler:GRAAL_PROCESSOR",
                "SVM_PROCESSOR",
            ],
            "workingSets": "SVM",
        },

        "com.oracle.svm.core.posix": {
            "subDir": "src",
            "sourceDirs": ["src"],
            "dependencies": [
                "com.oracle.svm.core.graal.amd64",
                "com.oracle.svm.core.graal.aarch64",
            ],
            "requiresConcealed" : {
                "jdk.internal.vm.ci" : [
                    "jdk.vm.ci.code",
                ],
            },
            "checkstyle": "com.oracle.svm.core",
            "javaCompliance": "11+",
            "annotationProcessors": [
                "compiler:GRAAL_PROCESSOR",
                "SVM_PROCESSOR",
            ],
            "workingSets": "SVM",
            "spotbugs": "false",
        },

        "com.oracle.svm.core.windows": {
            "subDir": "src",
            "sourceDirs": ["src"],
            "dependencies": [
                "com.oracle.svm.core.graal.amd64",
            ],
            "requiresConcealed" : {
                "jdk.internal.vm.ci" : [
                    "jdk.vm.ci.code",
                ],
            },
            "checkstyle": "com.oracle.svm.core",
            "javaCompliance": "11+",
            "annotationProcessors": [
                "compiler:GRAAL_PROCESSOR",
                "SVM_PROCESSOR",
            ],
            "workingSets": "SVM",
            "spotbugs": "false",
        },

        "com.oracle.graal.pointsto": {
            "subDir": "src",
            "sourceDirs": ["src"],
            "dependencies": [
                "com.oracle.svm.common",
            ],
            "requiresConcealed" : {
                "java.base" : [
                    "jdk.internal.misc"
                ]
            },
            "checkstyle": "com.oracle.svm.core",
            "javaCompliance": "11+",
            "annotationProcessors": [
                "compiler:GRAAL_PROCESSOR",
            ],
            "workingSets": "SVM",
        },

        "com.oracle.graal.pointsto.standalone": {
            "subDir": "src",
            "sourceDirs": ["src"],
            "dependencies": [
                "com.oracle.graal.pointsto",
            ],
            "requires" : [
                "jdk.unsupported" # sun.misc.Unsafe
            ],
            "requiresConcealed" : {
                "java.base" : [
                    "jdk.internal.misc"
                ],
                "jdk.internal.vm.ci" : [
                    "jdk.vm.ci.code",
                ]
            },
            "checkstyle": "com.oracle.svm.core",
            "javaCompliance": "11+",
            "annotationProcessors": [
                "compiler:GRAAL_PROCESSOR",
            ],
            "workingSets": "SVM",
        },

        "com.oracle.graal.reachability": {
            "subDir": "src",
            "sourceDirs": ["src"],
            "dependencies": [
                "com.oracle.graal.pointsto",
            ],
            "checkstyle": "com.oracle.svm.core",
            "javaCompliance": "11+",
            "annotationProcessors": [
                "compiler:GRAAL_PROCESSOR",
            ],
            "workingSets": "SVM",
        },

        "com.oracle.svm.hosted": {
            "subDir": "src",
            "sourceDirs": ["src"],
            "dependencies": [
                "com.oracle.objectfile",
                "com.oracle.svm.core",
                "com.oracle.graal.reachability"
            ],
            "requires" : [
                "java.desktop",
                "java.instrument",
                "java.security.sasl",
                "java.smartcardio",
                "java.xml.crypto",
                "jdk.jfr",
                "jdk.management",
            ],
            "requiresConcealed" : {
                "java.base" : [
                    "jdk.internal.misc",
                    "jdk.internal.vm.annotation",
                    "jdk.internal.org.objectweb.asm",
                    "sun.reflect.annotation",
                    "sun.security.jca",
                    "sun.security.provider",
                    "sun.security.x509",
                    "sun.util.locale.provider",
                    "sun.util.resources",
                    "jdk.internal.module",
                    "sun.text.spi",
                    "jdk.internal.reflect",
                    "sun.util.cldr",
                    "sun.util.locale",
                    "sun.invoke.util",
                ],
                "jdk.internal.vm.ci" : [
                    "jdk.vm.ci.meta",
                    "jdk.vm.ci.code",
                    "jdk.vm.ci.code.site",
                    "jdk.vm.ci.hotspot",
                    "jdk.vm.ci.runtime",
                ],
                "jdk.management": [
                    "com.sun.management.internal"
                ],
                "jdk.jfr": [
                    "jdk.jfr.internal",
                    "jdk.jfr.internal.jfc",
                ],
            },
            "javaCompliance": "11+",
            "checkstyleVersion": "8.36.1",
            "annotationProcessors": [
                "compiler:GRAAL_PROCESSOR",
                "SVM_PROCESSOR",
            ],
            "workingSets": "SVM",
        },

        "com.oracle.svm.hosted.jdk17": {
            "subDir": "src",
            "sourceDirs": ["src"],
            "dependencies": [
                "com.oracle.svm.hosted",
            ],
            "requiresConcealed" : {
                "java.base" :
                    ["jdk.internal.loader"],
                "jdk.internal.vm.ci" :
                    ["jdk.vm.ci.meta"],
            },
            "javaCompliance": "17+",
            "annotationProcessors": [
                "compiler:GRAAL_PROCESSOR",
                "SVM_PROCESSOR",
            ],
            "workingSets": "SVM",
        },
        # Native libraries below explicitly set _FORTIFY_SOURCE to 0. This constant controls how glibc handles some
        # functions that can cause a stack overflow like snprintf. If set to 1 or 2, it causes glibc to use internal
        # functions with extra checking that are not available in all libc implementations. Different distros use
        # different defaults for this constant (e.g., gcc on Ubuntu 18.04 sets it to 2), so we set it to 0 here.
        "com.oracle.svm.native.libchelper": {
            "subDir": "src",
            "native": "static_lib",
            "os_arch": {
                "solaris": {
                    "<others>": {
                        "ignore": "solaris is not supported",
                    },
                },
                "windows": {
                    "<others>": {
                        "cflags": ["-Zi", "-O2", "-D_LITTLE_ENDIAN", "-DJDK_VER=<jdk_ver>"],
                    },
                },
                "<others>": {
                    "<others>": {
                        "cflags": ["-g", "-gdwarf-4", "-fPIC", "-O2", "-D_LITTLE_ENDIAN", "-ffunction-sections", "-fdata-sections", "-fvisibility=hidden", "-D_FORTIFY_SOURCE=0"],
                    },
                },
            },
        },

        "com.oracle.svm.native.reporterchelper": {
            "subDir": "src",
            "native": "shared_lib",
            "deliverable": "reporterchelper",
            "platformDependent": True,
            "use_jdk_headers": True,
            "os_arch": {
                "windows": {
                    "<others>": {
                        "cflags": ["-Wall"]
                    }
                },
                "<others>": {
                    "<others>": {
                        "cflags": ["-Wall", "-Werror"],
                    },
                },
            },
        },

        "com.oracle.svm.native.darwin": {
            "subDir": "src",
            "native": "static_lib",
            "os_arch": {
                "darwin": {
                    "<others>": {
                        "cflags": ["-ObjC", "-fPIC", "-O1", "-D_LITTLE_ENDIAN", "-ffunction-sections", "-fdata-sections", "-fvisibility=hidden", "-D_FORTIFY_SOURCE=0"],
                    },
                },
                "<others>": {
                    "<others>": {
                        "ignore": "only needed on darwin",
                    },
                },
            },
        },

        "com.oracle.svm.native.jvm.posix": {
            "subDir": "src",
            "native": "static_lib",
            "deliverable" : "jvm",
            "use_jdk_headers" : True,
            "os_arch" : {
                "darwin": {
                    "<others>" : {
                        "cflags": ["-g", "-fPIC", "-O2", "-ffunction-sections", "-fdata-sections", "-fvisibility=hidden"],
                    },
                },
                "linux": {
                    "<others>" : {
                        "cflags": ["-g", "-gdwarf-4", "-fPIC", "-O2", "-ffunction-sections", "-fdata-sections", "-fvisibility=hidden", "-D_FORTIFY_SOURCE=0", "-D_GNU_SOURCE"],
                    },
                },
                "<others>": {
                    "<others>": {
                        "ignore": "only darwin and linux are supported",
                    },
                },
            },
            "dependencies": [
                "svm-jvmfuncs-fallback-builder",
            ],
        },

        "com.oracle.svm.native.jvm.windows": {
            "subDir": "src",
            "native": "static_lib",
            "deliverable" : "jvm",
            "use_jdk_headers" : True,
            "os_arch" : {
                "windows": {
                    "amd64" : {
                        "cflags": ["-MD", "-Zi", "-O2"],
                    },
                },
                "<others>": {
                    "<others>": {
                        "ignore": "only windows is supported",
                    },
                },
            },
            "dependencies": [
                "svm-jvmfuncs-fallback-builder",
            ],
        },

        "svm-jvmfuncs-fallback-builder": {
            "class" : "SubstrateJvmFuncsFallbacksBuilder",
        },

        "com.oracle.svm.driver": {
            "subDir": "src",
            "sourceDirs": [
                "src",
                "resources"
            ],
            "dependencies": [
                "com.oracle.svm.hosted",
            ],
            "requires" : [
                "jdk.management",
            ],
            "checkstyle": "com.oracle.svm.hosted",
            "workingSets": "SVM",
            "annotationProcessors": [
                "compiler:GRAAL_PROCESSOR",
                "SVM_PROCESSOR",
            ],
            "javaCompliance": "11+",
            "spotbugs": "false",
        },

        "com.oracle.svm.junit": {
            "subDir": "src",
            "sourceDirs": [
                "src",
                "resources",
            ],
            "dependencies": [
                "com.oracle.svm.core",
                "mx:JUNIT_TOOL",
            ],
            "checkstyle": "com.oracle.svm.core",
            "workingSets": "SVM",
            "annotationProcessors": [
                "compiler:GRAAL_PROCESSOR",
                "SVM_PROCESSOR",
            ],
            "javaCompliance": "11+",
            "spotbugs": "false",
        },

        "com.oracle.svm.test": {
            "subDir": "src",
            "sourceDirs": ["src"],
            "dependencies": [
                "mx:JUNIT_TOOL",
                "sdk:GRAAL_SDK",
                "SVM",
            ],
            "requires": [
                "java.compiler",
                "jdk.jfr",
            ],
            "requiresConcealed" : {
                "java.base" : [
                    "jdk.internal.misc",
                    "sun.security.jca",
                ],
            },
            "checkstyle": "com.oracle.svm.test",
            "checkstyleVersion" : "8.36.1",
            "workingSets": "SVM",
            "annotationProcessors": [
                "compiler:GRAAL_PROCESSOR",
                "SVM_PROCESSOR",
            ],
            "javaCompliance": "11+",
            "spotbugs": "false",
        },

        "com.oracle.svm.test.jdk17": {
            "subDir": "src",
            "sourceDirs": ["src"],
            "dependencies": [
                "mx:JUNIT_TOOL",
                "sdk:GRAAL_SDK",
                "SVM",
            ],
            "checkstyle": "com.oracle.svm.test",
            "workingSets": "SVM",
            "annotationProcessors": [
                "compiler:GRAAL_PROCESSOR",
                "SVM_PROCESSOR",
            ],
            "javaCompliance": "17+",
            "spotbugs": "false",
            "testProject": True,
        },

        "com.oracle.svm.configure.test": {
            "subDir": "src",
            "sourceDirs": ["src"],
            "dependencies": [
                "mx:JUNIT_TOOL",
                "sdk:GRAAL_SDK",
                "com.oracle.svm.configure",
            ],
            "checkstyle": "com.oracle.svm.test",
            "workingSets": "SVM",
            "annotationProcessors": [
                "compiler:GRAAL_PROCESSOR",
                "SVM_PROCESSOR",
            ],
            "javaCompliance": "11+",
            "spotbugs": "false",
            "testProject": True,
        },

        "com.oracle.svm.tutorial" : {
            "subDir": "src",
            "sourceDirs" : ["src"],
            "dependencies" : ["sdk:GRAAL_SDK"],
            "checkstyle" : "com.oracle.svm.hosted",
            "javaCompliance": "11+",
            "workingSets" : "SVM",
            "spotbugs" : "false",
        },

        "com.oracle.objectfile" : {
            "subDir": "src",
            "sourceDirs" : ["src"],
            "dependencies" : [
                "compiler:GRAAL"
            ],
            "requiresConcealed" : {
                "java.base" : [
                    "jdk.internal.misc",
                    "jdk.internal.ref",
                    "sun.nio.ch",
                ],
                "jdk.internal.vm.ci" : [
                    "jdk.vm.ci.code",
                ],
            },
            "checkstyle" : "com.oracle.svm.hosted",
            "javaCompliance": "11+",
            "annotationProcessors": [
                "compiler:GRAAL_PROCESSOR",
                "SVM_PROCESSOR",
            ],
            "workingSets" : "SVM",
            "spotbugs" : "false",
        },

        "com.oracle.svm.graal": {
            "subDir": "src",
            "sourceDirs": ["src"],
            "dependencies": [
                "com.oracle.svm.hosted",
            ],
            "requiresConcealed" : {
                "java.base" : [
                    "jdk.internal.misc",
                ],
                "jdk.internal.vm.ci" : [
                    "jdk.vm.ci.aarch64",
                    "jdk.vm.ci.code.site",
                    "jdk.vm.ci.runtime",
                ],
            },
            "checkstyle": "com.oracle.svm.hosted",
            "javaCompliance": "11+",
            "annotationProcessors": [
                "compiler:GRAAL_PROCESSOR",
                "SVM_PROCESSOR",
            ],
        },

        "com.oracle.svm.graal.test": {
            "subDir": "src",
            "sourceDirs": ["src"],
            "dependencies": [
                "mx:JUNIT_TOOL",
                "sdk:GRAAL_SDK",
                "com.oracle.svm.graal",
            ],
            "checkstyle": "com.oracle.svm.test",
            "workingSets": "SVM",
            "annotationProcessors": [
                "compiler:GRAAL_PROCESSOR",
                "SVM_PROCESSOR",
            ],
            "javaCompliance": "11+",
            "spotbugs": "false",
            "testProject": True,
        },

        "com.oracle.svm.thirdparty": {
            "subDir": "src",
            "sourceDirs": ["src"],
            "dependencies": [
                "com.oracle.svm.core",
            ],
            "requires" : [
                "jdk.unsupported",
            ],
            "checkstyle": "com.oracle.svm.core",
            "javaCompliance": "11+",
            "annotationProcessors": [
                "compiler:GRAAL_PROCESSOR",
                "SVM_PROCESSOR",
            ],
            "workingSets": "SVM",
        },

        "com.oracle.svm.bench": {
            "subDir": "src",
            "sourceDirs": ["src"],
            "dependencies": [
                "sdk:GRAAL_SDK",
            ],
            "checkstyle": "com.oracle.svm.core",
            "javaCompliance": "11+",
            "annotationProcessors": [
                "compiler:GRAAL_PROCESSOR",
                "SVM_PROCESSOR",
            ],
            "workingSets": "SVM",
        },

        "com.oracle.svm.truffle": {
            "subDir": "src",
            "sourceDirs": ["src"],
            "dependencies": [
                "com.oracle.svm.graal",
                "truffle:TRUFFLE_API",
            ],
            "requiresConcealed" : {
                "java.base" : [
                    "jdk.internal.misc",
                ],
                "jdk.internal.vm.ci": [
                    "jdk.vm.ci.meta",
                ]
            },
            "checkstyle": "com.oracle.svm.hosted",
            "javaCompliance": "11+",
            "annotationProcessors": [
                "compiler:GRAAL_PROCESSOR",
                "SVM_PROCESSOR",
            ],
            "workingSets": "SVM",
        },

        "com.oracle.svm.truffle.nfi": {
            "subDir": "src",
            "sourceDirs": ["src"],
            "dependencies": [
                "com.oracle.svm.truffle",
            ],
            "checkstyle": "com.oracle.svm.hosted",
            "javaCompliance": "11+",
            "annotationProcessors": [
                "compiler:GRAAL_PROCESSOR",
                "SVM_PROCESSOR",
                "truffle:TRUFFLE_DSL_PROCESSOR",
            ],
            "workingSets": "SVM",
        },

        "com.oracle.svm.truffle.nfi.posix": {
            "subDir": "src",
            "sourceDirs": ["src"],
            "dependencies": [
                "com.oracle.svm.truffle.nfi",
                "com.oracle.svm.core.posix",
            ],
            "checkstyle": "com.oracle.svm.hosted",
            "javaCompliance": "11+",
            "annotationProcessors": [
                "compiler:GRAAL_PROCESSOR",
                "SVM_PROCESSOR",
                "truffle:TRUFFLE_DSL_PROCESSOR",
            ],
            "workingSets": "SVM",
        },

        "com.oracle.svm.truffle.nfi.windows": {
            "subDir": "src",
            "sourceDirs": ["src"],
            "dependencies": [
                "com.oracle.svm.truffle.nfi",
                "com.oracle.svm.core.windows",
            ],
            "checkstyle": "com.oracle.svm.hosted",
            "javaCompliance": "11+",
            "annotationProcessors": [
                "compiler:GRAAL_PROCESSOR",
                "SVM_PROCESSOR",
                "truffle:TRUFFLE_DSL_PROCESSOR",
            ],
            "workingSets": "SVM",
        },

        "com.oracle.svm.polyglot": {
            "subDir": "src",
            "sourceDirs": ["src"],
            "generatedDependencies": [
                "com.oracle.svm.graal",
            ],
            "checkstyle": "com.oracle.svm.core",
            "javaCompliance": "11+",
            "annotationProcessors": [
                "compiler:GRAAL_PROCESSOR",
                "SVM_PROCESSOR",
            ],
            "workingSets": "SVM",
            "spotbugs": "false",
        },

        "org.graalvm.polyglot.nativeapi" : {
            "subDir": "src",
            "sourceDirs" : [
                "src",
                "resources",
            ],
            "dependencies" : [
                "sdk:GRAAL_SDK",
                "com.oracle.svm.hosted",
            ],
            "checkstyle": "com.oracle.svm.core",
            "javaCompliance": "11+",
            "annotationProcessors" : [
                "compiler:GRAAL_PROCESSOR",
                "SVM_PROCESSOR",
            ],
            "workingSets" : "SVM",
            "spotbugs": "false",
        },

        "com.oracle.svm.graal.hotspot.libgraal" : {
            "subDir": "src",
            "sourceDirs": ["src"],
            "dependencies": [
                "com.oracle.svm.graal",
                "compiler:GRAAL"
            ],
            "requiresConcealed" : {
                "java.base" : [
                    "jdk.internal.misc",
                ],
            },
            "checkstyle" : "com.oracle.svm.hosted",
            "javaCompliance": "11+",
            "annotationProcessors": [
                "compiler:GRAAL_PROCESSOR",
                "SVM_PROCESSOR",
            ],
            "defaultBuild": False,
        },

        "com.oracle.svm.configure": {
            "subDir": "src",
            "sourceDirs": [
                "src",
                "resources",
            ],
            "dependencies": [
                "com.oracle.svm.core",
            ],
            "checkstyle": "com.oracle.svm.hosted",
            "workingSets": "SVM",
            "annotationProcessors": [
            ],
            "javaCompliance": "11+",
            "spotbugs": "false",
        },

        "com.oracle.svm.jvmtiagentbase": {
            "subDir": "src",
            "sourceDirs": [
                "src",
            ],
            "dependencies": [
                "com.oracle.svm.core",
            ],
            "checkstyle": "com.oracle.svm.hosted",
            "workingSets": "SVM",
            "annotationProcessors": [
                "compiler:GRAAL_PROCESSOR",
                "SVM_PROCESSOR",
            ],
            "javaCompliance": "11+",
            "spotbugs": "false",
        },

        "com.oracle.svm.agent": {
            "subDir": "src",
            "sourceDirs": [
                "src",
                "resources"
            ],
            "dependencies": [
                "JVMTI_AGENT_BASE",
                "com.oracle.svm.configure",
                "com.oracle.svm.driver",
            ],
            "checkstyle": "com.oracle.svm.hosted",
            "workingSets": "SVM",
            "annotationProcessors": [
                "compiler:GRAAL_PROCESSOR",
                "SVM_PROCESSOR",
            ],
            "javaCompliance": "11+",
            "spotbugs": "false",
        },

        "com.oracle.svm.diagnosticsagent": {
            "subDir": "src",
            "sourceDirs": [
                "src",
                "resources"
            ],
            "dependencies": [
                "JVMTI_AGENT_BASE",
            ],
            "requiresConcealed" : {
                "java.base" : [
                    "jdk.internal.loader",
                    "jdk.internal.org.objectweb.asm",
                ],
            },
            "checkstyle": "com.oracle.svm.hosted",
            "workingSets": "SVM",
            "annotationProcessors": [
                "compiler:GRAAL_PROCESSOR",
                "SVM_PROCESSOR",
            ],
            "javaCompliance": "11+",
            "spotbugs": "false",
        },

        "com.oracle.svm.truffle.tck" : {
            "subDir": "src",
            "sourceDirs": ["src"],
            "dependencies": [
                "com.oracle.svm.hosted",
            ],
            "requires" : [
                "jdk.unsupported",
            ],
            "checkstyle" : "com.oracle.svm.hosted",
            "workingSets": "SVM",
            "annotationProcessors": [
                "compiler:GRAAL_PROCESSOR",
                "SVM_PROCESSOR",
            ],
            "javaCompliance": "11+",
        },
    },

    "distributions": {
        #
        # External Distributions
        #
        "SVM_PROCESSOR" : {
            "subDir": "src",
            "dependencies" : [
                "com.oracle.svm.processor",
             ],
            "distDependencies": [
                "compiler:GRAAL_PROCESSOR",
            ],
            "maven": False,
        },

        "SVM": {
            "subDir": "src",
            "description" : "SubstrateVM image builder components",
            "dependencies": [
                "com.oracle.svm.graal",
                "com.oracle.svm.truffle",
                "com.oracle.svm.hosted",
                "com.oracle.svm.hosted.jdk17",
                "com.oracle.svm.core",
                "com.oracle.svm.core.jdk17",
                "com.oracle.svm.core.graal.amd64",
                "com.oracle.svm.core.graal.aarch64",
                "com.oracle.svm.core.posix",
                "com.oracle.svm.core.windows",
                "com.oracle.svm.core.genscavenge",
                "com.oracle.svm.core.containers",
            ],
            "distDependencies": [
                "sdk:GRAAL_SDK",
                "OBJECTFILE",
                "POINTSTO",
                "compiler:GRAAL",
                "NATIVE_IMAGE_BASE",
            ],
            "moduleInfo" : {
                "name" : "org.graalvm.nativeimage.builder",
                "exports" : [
                    "com.oracle.svm.hosted                        to java.base",
                    "com.oracle.svm.truffle.api                   to org.graalvm.truffle",
                    "* to org.graalvm.nativeimage.base,jdk.internal.vm.compiler,org.graalvm.nativeimage.driver,org.graalvm.nativeimage.configure,org.graalvm.nativeimage.librarysupport,org.graalvm.nativeimage.junitsupport,org.graalvm.nativeimage.llvm,org.graalvm.nativeimage.agent.jvmtibase,org.graalvm.nativeimage.agent.tracing,org.graalvm.nativeimage.agent.diagnostics,com.oracle.svm.svm_enterprise,com.oracle.svm.svm_enterprise.llvm,com.oracle.svm_enterprise.ml_dataset",
                ],
                "opens" : [
                    "com.oracle.svm.core                          to jdk.internal.vm.compiler",
                    "com.oracle.svm.core.nodes                    to jdk.internal.vm.compiler",
                    "com.oracle.svm.core.graal.nodes              to jdk.internal.vm.compiler",
                    "com.oracle.svm.core.graal.snippets           to jdk.internal.vm.compiler",
                    "com.oracle.svm.hosted.fieldfolding           to jdk.internal.vm.compiler",
                    "com.oracle.svm.hosted.reflect                to jdk.internal.vm.compiler",
                ],
                "requires": [
                    "java.management",
                    "jdk.management",
                    "java.xml.crypto",
                    "java.desktop",
                    "java.security.sasl",
                    "java.smartcardio",
                    "java.net.http",
                    "jdk.sctp",
                    "jdk.scripting.nashorn@11..14",
                ],
                "uses" : [
                    "org.graalvm.nativeimage.Platform",
                    "org.graalvm.compiler.options.OptionDescriptors",
                    "com.oracle.truffle.api.TruffleLanguage.Provider",
                    "com.oracle.truffle.api.instrumentation.TruffleInstrument.Provider",
                    "com.oracle.svm.hosted.NativeImageClassLoaderPostProcessing",
                    "java.util.spi.ResourceBundleControlProvider",
                    "com.oracle.svm.core.feature.AutomaticallyRegisteredFeatureServiceRegistration",
                ],
                "requiresConcealed": {
                    "jdk.internal.vm.ci": [
                        "jdk.vm.ci.common",
                        "jdk.vm.ci.meta",
                        "jdk.vm.ci.code",
                        "jdk.vm.ci.services",
                        "jdk.vm.ci.runtime",
                        "jdk.vm.ci.amd64",
                        "jdk.vm.ci.aarch64",
                        "jdk.vm.ci.hotspot",
                    ],
                    "java.base": [
                        "sun.reflect.annotation",
                        "sun.reflect.generics.reflectiveObjects",
                        "sun.reflect.generics.repository",
                        "sun.reflect.generics.tree",
                        "sun.reflect.generics.scope",
                        "sun.util.calendar",
                        "sun.util.locale",
                        "sun.security.jca",
                        "sun.security.util",
                        "sun.security.provider",
                        "sun.security.ssl",
                        "com.sun.crypto.provider",
                        "sun.reflect.generics.repository",
                        "jdk.internal.org.objectweb.asm",
                        "sun.util.locale.provider",
                        "sun.util.cldr",
                        "sun.util.resources",
                        "sun.invoke.util",
                        "sun.net",
                    ],
                    "java.management": [
                        "sun.management",
                    ],
                    "java.xml.crypto": [
                        "org.jcp.xml.dsig.internal.dom",
                    ],
                },
            },
            "noMavenJavadoc": True,
        },

        "SVM_LIBFFI": {
            "subDir": "src",
            "description" : "SubstrateVM support for Truffle NFI LibFFI backend",
            "dependencies": [
                "com.oracle.svm.truffle.nfi",
                "com.oracle.svm.truffle.nfi.posix",
                "com.oracle.svm.truffle.nfi.windows",
            ],
            "distDependencies": [
                "SVM",
            ],
        },

        "JVMTI_AGENT_BASE": {
            "subDir": "src",
            "description": "Base framework for creating a JVMTI agent.",
            "dependencies": [
                "com.oracle.svm.jvmtiagentbase",
            ],
            "distDependencies": [
                "LIBRARY_SUPPORT",
                "SVM_DRIVER",
            ],
            "moduleInfo" : {
                "name" : "org.graalvm.nativeimage.agent.jvmtibase",
                "exports" : [
                    "com.oracle.svm.jvmtiagentbase",
                    "com.oracle.svm.jvmtiagentbase.jvmti",
                ],
            },
            "maven": False,
        },

        "LIBRARY_SUPPORT": {
            "subDir": "src",
            "description" : "SubstrateVM basic library-support components",
            "dependencies": [
                "com.oracle.svm.polyglot",
                "com.oracle.svm.thirdparty",
            ],
            "distDependencies": [
                "sdk:GRAAL_SDK",
                "SVM",
            ],
            "moduleInfo" : {
                "name" : "org.graalvm.nativeimage.librarysupport",
                "exports" : [
                    "* to org.graalvm.nativeimage.builder",
                ],
            },
        },

        "JUNIT_SUPPORT": {
            "subDir": "src",
            "description" : "SubstrateVM suppoprt for building JUnit test into image",
            "dependencies": [
                "com.oracle.svm.junit",
            ],
            "distDependencies": [
                "sdk:GRAAL_SDK",
                "SVM",
                "compiler:GRAAL",
                "mx:JUNIT_TOOL",
            ],
            "moduleInfo" : {
                "name" : "org.graalvm.nativeimage.junitsupport",
                "exports" : [
                    "* to org.graalvm.nativeimage.builder,org.graalvm.nativeimage.base,org.graalvm.nativeimage.pointsto"
                ],
                "requires" : [
                    "static com.oracle.mxtool.junit",
                    "static junit",
                    "static hamcrest",
                ]
            },
        },

        "OBJECTFILE": {
            "subDir": "src",
            "description" : "SubstrateVM object file writing library",
            "dependencies": [
                "com.oracle.objectfile"
            ],
            "distDependencies": [
                "compiler:GRAAL",
            ],
            "moduleInfo" : {
              "name" : "org.graalvm.nativeimage.objectfile",
              "exports" : [
                "com.oracle.objectfile",
                "com.oracle.objectfile.io",
                "com.oracle.objectfile.debuginfo",
                "com.oracle.objectfile.macho",
              ],

              "requiresConcealed" : {
                "java.base" : [
                  "sun.nio.ch",
                  "jdk.internal.ref",
                ],
              }
            },
        },

        "GRAAL_HOTSPOT_LIBRARY": {
            "subDir": "src",
            "description" : "SubstrateVM HotSpot Graal library support",
            "javaCompliance": "11+",
            "dependencies": [
                "com.oracle.svm.graal.hotspot.libgraal",
            ],
            "overlaps" : [
                "LIBRARY_SUPPORT"
            ],
            "distDependencies": [
                "SVM",
            ],
            "defaultBuild": False,
            "maven": False,
        },

        #
        # Native Projects
        #
        "SVM_HOSTED_NATIVE": {
            "native": True,
            "platformDependent" : True,
            "platforms" : [
                "linux-amd64",
                "darwin-amd64",
                "windows-amd64",
            ],
            "layout": {
                "<os>-<arch>/": [
                    "dependency:com.oracle.svm.native.libchelper/*",
                    "dependency:com.oracle.svm.native.darwin/*",
                    "dependency:com.oracle.svm.native.jvm.posix/*",
                    "dependency:com.oracle.svm.native.jvm.windows/*",
                ],
            },
            "description" : "SubstrateVM image builder native components",
            "maven": True
        },

        #
        # Internal Distributions
        #
        "SVM_DRIVER": {
            "subDir": "src",
            "description" : "SubstrateVM native-image building tool",
            "mainClass": "com.oracle.svm.driver.NativeImage",
            "dependencies": [
                "com.oracle.svm.driver",
                "svm-compiler-flags-builder",
            ],
            "distDependencies": [
                "LIBRARY_SUPPORT",
            ],
            "moduleInfo" : {
              "name" : "org.graalvm.nativeimage.driver",
              "exports" : [
                "com.oracle.svm.driver",
                "com.oracle.svm.driver.metainf",
              ],
              "uses" : [
                "org.graalvm.compiler.options.OptionDescriptors",
              ],
              "requires" : [
                "org.graalvm.nativeimage.builder",
                "java.management",
                "jdk.management",
              ],
            },
            "maven": False,
        },

        "SVM_AGENT": {
            "subDir": "src",
            "description" : "SubstrateVM native-image-agent library",
            "dependencies": [
                "com.oracle.svm.agent",
                "com.oracle.svm.configure",
            ],
            "distDependencies": [
                "JVMTI_AGENT_BASE",
                "LIBRARY_SUPPORT",
                "SVM_DRIVER",
                "SVM_CONFIGURE"
            ],
            "moduleInfo" : {
                "name" : "org.graalvm.nativeimage.agent.tracing",
                "exports" : [
                    "com.oracle.svm.agent",
                ],
                "requiresConcealed" : {
                    "jdk.internal.vm.ci" : [
                        "jdk.vm.ci.meta",
                    ],
                }
            },
            # vm: included as binary, tool descriptor intentionally not copied
            "maven": False,
        },

        "SVM_DIAGNOSTICS_AGENT": {
            "subDir": "src",
            "description" : "Native-image diagnostics agent",
            "dependencies": [
                "com.oracle.svm.diagnosticsagent",
            ],
            "distDependencies": [
                "JVMTI_AGENT_BASE",
                "LIBRARY_SUPPORT",
            ],
            "moduleInfo" : {
                "name" : "org.graalvm.nativeimage.agent.diagnostics",
                "exports" : [
                    "com.oracle.svm.diagnosticsagent",
                ],
            },
            "maven": False,
        },

        "SVM_CONFIGURE": {
            "subDir": "src",
            "description" : "SubstrateVM native-image configuration tool",
            "mainClass": "com.oracle.svm.configure.ConfigurationTool",
            "dependencies": [
                "com.oracle.svm.configure",
            ],
            "distDependencies": [
                "LIBRARY_SUPPORT",
            ],
            "moduleInfo" : {
                "name" : "org.graalvm.nativeimage.configure",
                "exports" : [
                    "* to org.graalvm.nativeimage.agent.tracing",
                    "com.oracle.svm.configure",
                ],
            },
            "maven": False,
        },

        "NATIVE_IMAGE_BASE": {
            "subDir": "src",
            "description" : "Native Image base that can be shared by native image building and pointsto.",
            "dependencies": [
                "com.oracle.svm.common",
                "com.oracle.svm.util",
            ],
            "distDependencies": [
                "compiler:GRAAL",
            ],
            "exclude": [
            ],
            "moduleInfo" : {
                "name" : "org.graalvm.nativeimage.base",
                "exports" : [
                    "com.oracle.svm.util                   to org.graalvm.nativeimage.pointsto,org.graalvm.nativeimage.builder,org.graalvm.nativeimage.librarysupport,org.graalvm.nativeimage.driver,org.graalvm.nativeimage.llvm,org.graalvm.nativeimage.agent.jvmtibase,org.graalvm.nativeimage.agent.tracing,org.graalvm.nativeimage.agent.diagnostics,org.graalvm.nativeimage.junitsupport,com.oracle.svm.svm_enterprise",
                    "com.oracle.svm.common.option          to org.graalvm.nativeimage.pointsto,org.graalvm.nativeimage.builder,org.graalvm.nativeimage.driver",
                ],
            }
        },

        "POINTSTO": {
            "subDir": "src",
            "description" : "SubstrateVM static analysis to find ahead-of-time the code",
            "dependencies": [
                "com.oracle.svm.util",
                "com.oracle.graal.pointsto",
            ],
            "distDependencies": [
                "compiler:GRAAL",
                "NATIVE_IMAGE_BASE",
            ],
            "exclude": [
            ],
            "moduleInfo" : {
              "name" : "org.graalvm.nativeimage.pointsto",
              "exports" : [
                "com.oracle.graal.pointsto",
                "com.oracle.graal.pointsto.api",
                "com.oracle.graal.pointsto.heap",
                "com.oracle.graal.pointsto.heap.value",
                "com.oracle.graal.pointsto.reports",
                "com.oracle.graal.pointsto.constraints",
                "com.oracle.graal.pointsto.util",
                "com.oracle.graal.pointsto.meta",
                "com.oracle.graal.pointsto.flow",
                "com.oracle.graal.pointsto.flow.builder",
                "com.oracle.graal.pointsto.nodes",
                "com.oracle.graal.pointsto.phases",
                "com.oracle.graal.pointsto.results",
                "com.oracle.graal.pointsto.typestate",
                "com.oracle.graal.pointsto.infrastructure",
                "com.oracle.graal.pointsto.flow.context.object",
                "com.oracle.graal.pointsto.flow.context.bytecode",
              ],
              "requires": [
                "java.management",
                "jdk.management",
              ],
              "requiresConcealed" : {
                "java.management": [
                  "sun.management",
                ],
                "jdk.internal.vm.ci" : [
                  "jdk.vm.ci.meta",
                  "jdk.vm.ci.common",
                  "jdk.vm.ci.code",
                  "jdk.vm.ci.runtime",
                ],
              }
            },
        },

        "STANDALONE_POINTSTO": {
            "subDir": "src",
            "description" : "A standalone version of SubstrateVM static analysis to use for general pointsto analysis",
            "dependencies": [
                "com.oracle.graal.pointsto.standalone",
            ],
            "distDependencies": [
                "compiler:GRAAL",
                "NATIVE_IMAGE_BASE",
                "POINTSTO"
            ],
            "exclude": [
            ],
            "moduleInfo" : {
                "name" : "org.graalvm.nativeimage.pointsto.standalone",
                "exports" : [
                    "com.oracle.graal.pointsto.standalone",
                ],
                "requires": [
                    "java.management",
                    "jdk.management",
                ],
                "requiresConcealed" : {
                    "java.management": [
                        "sun.management",
                    ],
                    "jdk.internal.vm.ci" : [
                        "jdk.vm.ci.meta",
                        "jdk.vm.ci.common",
                        "jdk.vm.ci.code",
                        "jdk.vm.ci.runtime",
                    ],
                    "jdk.internal.vm.compiler" : [
                        "org.graalvm.compiler.options"
                    ]
                }
            },
        },

        "SVM_TESTS" : {
          "subDir": "src",
          "relpath" : True,
          "dependencies" : [
            "com.oracle.svm.test",
            "com.oracle.svm.test.jdk17",
            "com.oracle.svm.configure.test",
            "com.oracle.svm.graal.test",
          ],
          "distDependencies": [
            "mx:JUNIT_TOOL",
            "sdk:GRAAL_SDK",
            "SVM",
            "SVM_CONFIGURE",
          ],
          "testDistribution" : True,
        },

        "POLYGLOT_NATIVE_API" : {
            "subDir": "src",
            "dependencies": [
                "org.graalvm.polyglot.nativeapi",
            ],
            "distDependencies": [
                "sdk:GRAAL_SDK",
                "SVM",
            ],
            "maven": False
        },

        "POLYGLOT_NATIVE_API_HEADERS" : {
            "native" : True,
            "platformDependent" : True,
            "description" : "polyglot.nativeapi header files for the GraalVM build process",
            "layout" : {
                "./" : [
                    "extracted-dependency:POLYGLOT_NATIVE_API/*.h",
                ],
            },
        },

        "SVM_GRAALVM_SUPPORT" : {
            "native" : True,
            "platformDependent" : True,
            "description" : "SubstrateVM support distribution for the GraalVM",
            "layout" : {
                "clibraries/" : ["extracted-dependency:substratevm:SVM_HOSTED_NATIVE"],
                "builder/clibraries/" : ["extracted-dependency:substratevm:SVM_HOSTED_NATIVE"],
                "builder/lib/" : ["dependency:com.oracle.svm.native.reporterchelper"],
            },
        },

        "SVM_NFI_GRAALVM_SUPPORT" : {
            "native" : True,
            "platformDependent" : True,
            "description" : "Native libraries and headers for SubstrateVM NFI support",
            "layout" : {
                "native-image.properties": "file:mx.substratevm/language-nfi.properties",
                "builder/clibraries-libffi/" : [
                    "extracted-dependency:truffle:LIBFFI_DIST"
                ],
                "builder/clibraries-libffi/include/" : [
                    "file:src/com.oracle.svm.libffi/include/svm_libffi.h",
                    "extracted-dependency:truffle:TRUFFLE_NFI_GRAALVM_SUPPORT/include/trufflenfi.h",
                ],
                # The following files are intentionally left empty. The "none" backend is actually nothing, but we still
                # need some files so native-image doesn't complain about missing files on the classpath.
                "truffle-nfi-none.jar" : "string:",
                "builder/svm-none.jar" : "string:",
                "builder/clibraries-none/.empty.h" : "file:src/com.oracle.svm.libffi/include/empty.h",
            },
        },

        "NATIVE_IMAGE_GRAALVM_SUPPORT" : {
            "native" : True,
            "platformDependent" : True,
            "description" : "Native Image support distribution for the GraalVM",
            "os_arch" : {
                "windows": {
                    "<others>" : {
                        "layout" : {
                            "bin/" : "file:mx.substratevm/rebuild-images.cmd",
                        },
                    },
                },
                "<others>": {
                    "<others>": {
                        "layout" : {
                            "bin/rebuild-images" : "file:mx.substratevm/rebuild-images.sh",
                        },
                    },
                },
            },
        },

        "NATIVE_IMAGE_LICENSE_GRAALVM_SUPPORT" : {
            "native" : True,
            "platformDependent" : False,
            "description" : "Native Image support distribution for the GraalVM",
            "layout" : {
                "LICENSE_NATIVEIMAGE.txt" : "file:LICENSE",
            },
        },

        "NATIVE_IMAGE_JUNIT_SUPPORT" : {
            "native" : True,
            "description" : "Native-image based junit testing support",
            "layout" : {
                "native-image.properties" : "file:mx.substratevm/macro-junit.properties",
                "svm-junit.packages" : "file:mx.substratevm/svm-junit.packages",
            },
        },

        "NATIVE_IMAGE_JUNITCP_SUPPORT" : {
            "native" : True,
            "description" : "Native-image based junit testing support but with running image-builder on classpath",
            "layout" : {
                "native-image.properties" : "file:mx.substratevm/macro-junitcp.properties",
                "svm-junit.packages" : "file:mx.substratevm/svm-junit.packages",
            },
        },

        "SVM_LLVM" : {
            "subDir" : "src",
            "description" : "LLVM backend for Native Image",
            "dependencies" : ["com.oracle.svm.core.graal.llvm"],
            "distDependencies" : [
                "SVM",
                "sdk:LLVM_TOOLCHAIN"
            ],
            "javaProperties": {
                "llvm.bin.dir": "<path:LLVM_TOOLCHAIN>/bin/",
            },
            "exclude": [
                "LLVM_WRAPPER_SHADOWED",
                "LLVM_PLATFORM_SPECIFIC_SHADOWED",
                "JAVACPP_PLATFORM_SPECIFIC_SHADOWED",
            ],
            "moduleInfo" : {
                "name" : "org.graalvm.nativeimage.llvm",
                "exports" : [
                    "* to org.graalvm.nativeimage.builder,org.graalvm.nativeimage.base",
                ],
            },
            "maven" : False,
        },

        "SVM_TRUFFLE_TCK" : {
            "subDir" : "src",
            "description" : "Truffle TCK",
            "dependencies" : ["com.oracle.svm.truffle.tck"],
            "distDependencies" : ["SVM"],
            "maven" : True,
        },
    },
}
