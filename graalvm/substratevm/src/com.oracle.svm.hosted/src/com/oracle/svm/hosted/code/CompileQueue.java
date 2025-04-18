/*
 * Copyright (c) 2012, 2022, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.svm.hosted.code;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ForkJoinPool;

import org.graalvm.collections.EconomicMap;
import org.graalvm.compiler.api.replacements.Fold;
import org.graalvm.compiler.api.replacements.SnippetReflectionProvider;
import org.graalvm.compiler.asm.Assembler;
import org.graalvm.compiler.bytecode.Bytecode;
import org.graalvm.compiler.bytecode.BytecodeProvider;
import org.graalvm.compiler.bytecode.ResolvedJavaMethodBytecode;
import org.graalvm.compiler.code.CompilationResult;
import org.graalvm.compiler.code.DataSection;
import org.graalvm.compiler.core.GraalCompiler;
import org.graalvm.compiler.core.common.CompilationIdentifier;
import org.graalvm.compiler.core.common.CompilationIdentifier.Verbosity;
import org.graalvm.compiler.core.common.Fields;
import org.graalvm.compiler.core.common.GraalOptions;
import org.graalvm.compiler.core.common.spi.CodeGenProviders;
import org.graalvm.compiler.core.common.type.AbstractObjectStamp;
import org.graalvm.compiler.core.common.type.ObjectStamp;
import org.graalvm.compiler.core.common.type.Stamp;
import org.graalvm.compiler.core.common.type.StampPair;
import org.graalvm.compiler.debug.DebugContext;
import org.graalvm.compiler.debug.DebugContext.Description;
import org.graalvm.compiler.debug.DebugHandlersFactory;
import org.graalvm.compiler.debug.GlobalMetrics;
import org.graalvm.compiler.debug.GraalError;
import org.graalvm.compiler.debug.Indent;
import org.graalvm.compiler.graph.Node;
import org.graalvm.compiler.graph.Node.NodeIntrinsic;
import org.graalvm.compiler.graph.NodeClass;
import org.graalvm.compiler.graph.NodeSourcePosition;
import org.graalvm.compiler.lir.RedundantMoveElimination;
import org.graalvm.compiler.lir.alloc.RegisterAllocationPhase;
import org.graalvm.compiler.lir.asm.CompilationResultBuilder;
import org.graalvm.compiler.lir.asm.CompilationResultBuilderFactory;
import org.graalvm.compiler.lir.asm.DataBuilder;
import org.graalvm.compiler.lir.asm.FrameContext;
import org.graalvm.compiler.lir.framemap.FrameMap;
import org.graalvm.compiler.lir.phases.LIRPhase;
import org.graalvm.compiler.lir.phases.LIRSuites;
import org.graalvm.compiler.lir.phases.PostAllocationOptimizationPhase.PostAllocationOptimizationContext;
import org.graalvm.compiler.nodes.CallTargetNode;
import org.graalvm.compiler.nodes.ConstantNode;
import org.graalvm.compiler.nodes.EncodedGraph;
import org.graalvm.compiler.nodes.FieldLocationIdentity;
import org.graalvm.compiler.nodes.FixedNode;
import org.graalvm.compiler.nodes.FixedWithNextNode;
import org.graalvm.compiler.nodes.FrameState;
import org.graalvm.compiler.nodes.GraphState.GuardsStage;
import org.graalvm.compiler.nodes.IndirectCallTargetNode;
import org.graalvm.compiler.nodes.Invoke;
import org.graalvm.compiler.nodes.InvokeNode;
import org.graalvm.compiler.nodes.ParameterNode;
import org.graalvm.compiler.nodes.PiNode;
import org.graalvm.compiler.nodes.StartNode;
import org.graalvm.compiler.nodes.StateSplit;
import org.graalvm.compiler.nodes.StructuredGraph;
import org.graalvm.compiler.nodes.ValueNode;
import org.graalvm.compiler.nodes.extended.ForeignCallNode;
import org.graalvm.compiler.nodes.graphbuilderconf.GeneratedFoldInvocationPlugin;
import org.graalvm.compiler.nodes.graphbuilderconf.GraphBuilderConfiguration;
import org.graalvm.compiler.nodes.graphbuilderconf.GraphBuilderConfiguration.BytecodeExceptionMode;
import org.graalvm.compiler.nodes.graphbuilderconf.GraphBuilderContext;
import org.graalvm.compiler.nodes.graphbuilderconf.InlineInvokePlugin;
import org.graalvm.compiler.nodes.graphbuilderconf.InvocationPlugin;
import org.graalvm.compiler.nodes.java.MethodCallTargetNode;
import org.graalvm.compiler.nodes.util.GraphUtil;
import org.graalvm.compiler.nodes.virtual.CommitAllocationNode;
import org.graalvm.compiler.nodes.virtual.VirtualInstanceNode;
import org.graalvm.compiler.nodes.virtual.VirtualObjectNode;
import org.graalvm.compiler.nodes.virtual.VirtualObjectState;
import org.graalvm.compiler.options.OptionValues;
import org.graalvm.compiler.phases.BasePhase;
import org.graalvm.compiler.phases.OptimisticOptimizations;
import org.graalvm.compiler.phases.PhaseSuite;
import org.graalvm.compiler.phases.common.BoxNodeOptimizationPhase;
import org.graalvm.compiler.phases.common.CanonicalizerPhase;
import org.graalvm.compiler.phases.common.FixReadsPhase;
import org.graalvm.compiler.phases.common.FloatingReadPhase;
import org.graalvm.compiler.phases.tiers.HighTierContext;
import org.graalvm.compiler.phases.tiers.LowTierContext;
import org.graalvm.compiler.phases.tiers.MidTierContext;
import org.graalvm.compiler.phases.tiers.Suites;
import org.graalvm.compiler.phases.util.GraphOrder;
import org.graalvm.compiler.phases.util.Providers;
import org.graalvm.compiler.replacements.PEGraphDecoder;
import org.graalvm.compiler.replacements.nodes.MacroInvokable;
import org.graalvm.compiler.virtual.phases.ea.PartialEscapePhase;
import org.graalvm.compiler.virtual.phases.ea.ReadEliminationPhase;
import org.graalvm.nativeimage.ImageSingletons;

import com.oracle.graal.pointsto.api.PointstoOptions;
import com.oracle.graal.pointsto.flow.AnalysisParsedGraph;
import com.oracle.graal.pointsto.infrastructure.GraphProvider.Purpose;
import com.oracle.graal.pointsto.meta.AnalysisField;
import com.oracle.graal.pointsto.meta.AnalysisMethod;
import com.oracle.graal.pointsto.meta.AnalysisType;
import com.oracle.graal.pointsto.meta.HostedProviders;
import com.oracle.graal.pointsto.phases.SubstrateIntrinsicGraphBuilder;
import com.oracle.graal.pointsto.util.CompletionExecutor;
import com.oracle.graal.pointsto.util.CompletionExecutor.DebugContextRunnable;
import com.oracle.svm.core.SubstrateOptions;
import com.oracle.svm.core.SubstrateOptions.OptimizationLevel;
import com.oracle.svm.core.Uninterruptible;
import com.oracle.svm.core.code.FrameInfoEncoder;
import com.oracle.svm.core.deopt.DeoptEntryInfopoint;
import com.oracle.svm.core.deopt.DeoptTest;
import com.oracle.svm.core.deopt.Specialize;
import com.oracle.svm.core.graal.GraalConfiguration;
import com.oracle.svm.core.graal.code.StubCallingConvention;
import com.oracle.svm.core.graal.code.SubstrateBackend;
import com.oracle.svm.core.graal.meta.RuntimeConfiguration;
import com.oracle.svm.core.graal.meta.SubstrateForeignCallLinkage;
import com.oracle.svm.core.graal.meta.SubstrateForeignCallsProvider;
import com.oracle.svm.core.graal.nodes.ComputedIndirectCallTargetNode;
import com.oracle.svm.core.graal.nodes.DeoptEntryNode;
import com.oracle.svm.core.graal.nodes.DeoptTestNode;
import com.oracle.svm.core.graal.nodes.SubstrateFieldLocationIdentity;
import com.oracle.svm.core.graal.nodes.SubstrateNarrowOopStamp;
import com.oracle.svm.core.graal.phases.DeadStoreRemovalPhase;
import com.oracle.svm.core.graal.phases.OptimizeExceptionPathsPhase;
import com.oracle.svm.core.graal.snippets.DeoptTester;
import com.oracle.svm.core.graal.stackvalue.StackValueNode;
import com.oracle.svm.core.heap.RestrictHeapAccess;
import com.oracle.svm.core.heap.RestrictHeapAccessCallees;
import com.oracle.svm.core.meta.MethodPointer;
import com.oracle.svm.core.meta.SubstrateMethodPointerConstant;
import com.oracle.svm.core.util.InterruptImageBuilding;
import com.oracle.svm.core.util.VMError;
import com.oracle.svm.hosted.FeatureHandler;
import com.oracle.svm.hosted.NativeImageGenerator;
import com.oracle.svm.hosted.NativeImageOptions;
import com.oracle.svm.hosted.ProgressReporter;
import com.oracle.svm.hosted.diagnostic.HostedHeapDumpFeature;
import com.oracle.svm.hosted.meta.HostedField;
import com.oracle.svm.hosted.meta.HostedMethod;
import com.oracle.svm.hosted.meta.HostedType;
import com.oracle.svm.hosted.meta.HostedUniverse;
import com.oracle.svm.hosted.phases.DevirtualizeCallsPhase;
import com.oracle.svm.hosted.phases.HostedGraphBuilderPhase;
import com.oracle.svm.hosted.phases.ImageBuildStatisticsCounterPhase;
import com.oracle.svm.hosted.phases.ImplicitAssertionsPhase;
import com.oracle.svm.hosted.phases.StrengthenStampsPhase;
import com.oracle.svm.hosted.substitute.DeletedMethod;
import com.oracle.svm.util.ImageBuildStatistics;

import jdk.vm.ci.code.BytecodeFrame;
import jdk.vm.ci.code.BytecodePosition;
import jdk.vm.ci.code.DebugInfo;
import jdk.vm.ci.code.Register;
import jdk.vm.ci.code.site.Call;
import jdk.vm.ci.code.site.ConstantReference;
import jdk.vm.ci.code.site.DataPatch;
import jdk.vm.ci.code.site.Infopoint;
import jdk.vm.ci.code.site.Reference;
import jdk.vm.ci.meta.Constant;
import jdk.vm.ci.meta.MetaAccessProvider;
import jdk.vm.ci.meta.ResolvedJavaField;
import jdk.vm.ci.meta.ResolvedJavaMethod;
import jdk.vm.ci.meta.ResolvedJavaType;
import jdk.vm.ci.meta.VMConstant;

public class CompileQueue {

    public interface ParseFunction {
        void parse(DebugContext debug, HostedMethod method, CompileReason reason, RuntimeConfiguration config);
    }

    public interface CompileFunction {
        CompilationResult compile(DebugContext debug, HostedMethod method, CompilationIdentifier identifier, CompileReason reason, RuntimeConfiguration config);
    }

    protected final HostedUniverse universe;
    private final Boolean deoptimizeAll;
    protected CompletionExecutor executor;
    protected final ConcurrentMap<HostedMethod, CompileTask> compilations;
    protected final RuntimeConfiguration runtimeConfig;
    protected final MetaAccessProvider metaAccess;
    private Suites regularSuites = null;
    private Suites deoptTargetSuites = null;
    private LIRSuites regularLIRSuites = null;
    private LIRSuites deoptTargetLIRSuites = null;
    private final ConcurrentMap<Constant, DataSection.Data> dataCache;

    private SnippetReflectionProvider snippetReflection;
    private final FeatureHandler featureHandler;
    protected final OptionValues compileOptions;
    protected final GlobalMetrics metricValues = new GlobalMetrics();

    private volatile boolean inliningProgress;

    private final boolean printMethodHistogram = NativeImageOptions.PrintMethodHistogram.getValue();
    private final boolean optionAOTTrivialInline = SubstrateOptions.AOTTrivialInline.getValue();

    public abstract static class CompileReason {
        /**
         * For debugging only: chaining of the compile reason, so that you can track the compilation
         * of a method back to an entry point.
         */
        @SuppressWarnings("unused") private final CompileReason prevReason;

        public CompileReason(CompileReason prevReason) {
            this.prevReason = prevReason;
        }
    }

    public static class EntryPointReason extends CompileReason {

        public EntryPointReason() {
            super(null);
        }

        @Override
        public String toString() {
            return "entry point";
        }
    }

    public static class DirectCallReason extends CompileReason {

        private final HostedMethod caller;

        public DirectCallReason(HostedMethod caller, CompileReason prevReason) {
            super(prevReason);
            this.caller = caller;
        }

        @Override
        public String toString() {
            return "Direct call from " + caller.format("%r %h.%n(%p)");
        }
    }

    public static class VirtualCallReason extends CompileReason {

        private final HostedMethod caller;
        private final HostedMethod callTarget;

        public VirtualCallReason(HostedMethod caller, HostedMethod callTarget, CompileReason prevReason) {
            super(prevReason);
            this.caller = caller;
            this.callTarget = callTarget;
        }

        @Override
        public String toString() {
            return "Virtual call from " + caller.format("%r %h.%n(%p)") + ", callTarget " + callTarget.format("%r %h.%n(%p)");
        }
    }

    public static class MethodPointerConstantReason extends CompileReason {

        private final HostedMethod owner;
        private final HostedMethod callTarget;

        public MethodPointerConstantReason(HostedMethod owner, HostedMethod callTarget, CompileReason prevReason) {
            super(prevReason);
            this.owner = owner;
            this.callTarget = callTarget;
        }

        @Override
        public String toString() {
            return "Method " + callTarget.format("%r %h.%n(%p)") + " is reachable through a method pointer from " + owner.format("%r %h.%n(%p)");
        }
    }

    private interface Task extends DebugContextRunnable {
        @Override
        default DebugContext getDebug(OptionValues options, List<DebugHandlersFactory> factories) {
            return new DebugContext.Builder(options, factories).description(getDescription()).build();
        }
    }

    public class CompileTask implements Task {

        public final HostedMethod method;
        protected final CompileReason reason;
        public CompilationResult result;
        public final CompilationIdentifier compilationIdentifier;

        public CompileTask(HostedMethod method, CompileReason reason) {
            this.method = method;
            this.reason = reason;
            compilationIdentifier = new SubstrateHostedCompilationIdentifier(method);
        }

        @Override
        public DebugContext getDebug(OptionValues options, List<DebugHandlersFactory> factories) {
            return new DebugContext.Builder(options, factories).description(getDescription()).globalMetrics(metricValues).build();
        }

        @Override
        public void run(DebugContext debug) {
            result = doCompile(debug, method, compilationIdentifier, reason);
        }

        @Override
        public Description getDescription() {
            return new Description(method, compilationIdentifier.toString(Verbosity.ID));
        }

        public CompileReason getReason() {
            return reason;
        }
    }

    protected class TrivialInlineTask implements Task {

        private final HostedMethod method;
        private final Description description;

        TrivialInlineTask(HostedMethod method) {
            this.method = method;
            this.description = new Description(method, method.getName());
        }

        @Override
        public void run(DebugContext debug) {
            doInlineTrivial(debug, method);
        }

        @Override
        public Description getDescription() {
            return description;
        }
    }

    public class ParseTask implements Task {

        protected final CompileReason reason;
        private final HostedMethod method;
        private final Description description;

        public ParseTask(HostedMethod method, CompileReason reason) {
            this.method = method;
            this.reason = reason;
            this.description = new Description(method, method.getName());
        }

        @Override
        public void run(DebugContext debug) {
            doParse(debug, this);
        }

        @Override
        public Description getDescription() {
            return description;
        }
    }

    public CompileQueue(DebugContext debug, FeatureHandler featureHandler, HostedUniverse universe, SharedRuntimeConfigurationBuilder runtimeConfigBuilder, Boolean deoptimizeAll,
                    SnippetReflectionProvider snippetReflection, ForkJoinPool executorService) {
        this.universe = universe;
        this.compilations = new ConcurrentHashMap<>();
        this.runtimeConfig = runtimeConfigBuilder.getRuntimeConfig();
        this.metaAccess = runtimeConfigBuilder.metaAccess;
        this.deoptimizeAll = deoptimizeAll;
        this.dataCache = new ConcurrentHashMap<>();
        this.executor = new CompletionExecutor(universe.getBigBang(), executorService, universe.getBigBang().getHeartbeatCallback());
        this.featureHandler = featureHandler;
        this.snippetReflection = snippetReflection;
        this.compileOptions = getCustomizedOptions(debug);

        callForReplacements(debug, runtimeConfig);
    }

    public static OptimisticOptimizations getOptimisticOpts() {
        return OptimisticOptimizations.ALL.remove(OptimisticOptimizations.Optimization.UseLoopLimitChecks);
    }

    protected void callForReplacements(DebugContext debug, @SuppressWarnings("hiding") RuntimeConfiguration runtimeConfig) {
        NativeImageGenerator.registerReplacements(debug, featureHandler, runtimeConfig, runtimeConfig.getProviders(), true, true);
    }

    @SuppressWarnings("try")
    public void finish(DebugContext debug) {
        ProgressReporter reporter = ProgressReporter.singleton();
        try {
            try (ProgressReporter.ReporterClosable ac = reporter.printParsing()) {
                parseAll();
            }

            if (!PointstoOptions.UseExperimentalReachabilityAnalysis.getValue(universe.hostVM().options())) {
                /*
                 * Reachability Analysis creates call graphs with more edges compared to the
                 * Points-to Analysis, therefore the annotations would have to be added to a lot
                 * more methods if these checks are supposed to pass, see GR-39002
                 */
                UninterruptibleAnnotationChecker.checkBeforeCompilation(universe.getMethods());
                RestrictHeapAccessAnnotationChecker.check(debug, universe, universe.getMethods());
            }

            /*
             * The graph in the analysis universe is no longer necessary. This clears the graph for
             * methods that were not "parsed", i.e., method that were reached by the static analysis
             * but are no longer reachable now.
             */
            for (HostedMethod method : universe.getMethods()) {
                method.wrapped.setAnalyzedGraph(null);
            }

            if (ImageSingletons.contains(HostedHeapDumpFeature.class)) {
                ImageSingletons.lookup(HostedHeapDumpFeature.class).beforeInlining();
            }
            try (ProgressReporter.ReporterClosable ac = reporter.printInlining()) {
                inlineTrivialMethods(debug);
            }
            if (ImageSingletons.contains(HostedHeapDumpFeature.class)) {
                ImageSingletons.lookup(HostedHeapDumpFeature.class).afterInlining();
            }

            assert suitesNotCreated();
            createSuites();
            try (ProgressReporter.ReporterClosable ac = reporter.printCompiling()) {
                compileAll();
            }
        } catch (InterruptedException ie) {
            throw new InterruptImageBuilding();
        }
        if (printMethodHistogram) {
            printMethodHistogram();
        }
        if (ImageSingletons.contains(HostedHeapDumpFeature.class)) {
            ImageSingletons.lookup(HostedHeapDumpFeature.class).compileQueueAfterCompilation();
        }
        metricValues.print(compileOptions);
    }

    private boolean suitesNotCreated() {
        return regularSuites == null && deoptTargetLIRSuites == null && regularLIRSuites == null && deoptTargetSuites == null;
    }

    private void createSuites() {
        regularSuites = NativeImageGenerator.createSuites(featureHandler, runtimeConfig, snippetReflection, true);
        modifyRegularSuites(regularSuites);
        deoptTargetSuites = NativeImageGenerator.createSuites(featureHandler, runtimeConfig, snippetReflection, true);
        removeDeoptTargetOptimizations(deoptTargetSuites);
        regularLIRSuites = NativeImageGenerator.createLIRSuites(featureHandler, runtimeConfig.getProviders(), true);
        deoptTargetLIRSuites = NativeImageGenerator.createLIRSuites(featureHandler, runtimeConfig.getProviders(), true);
        removeDeoptTargetOptimizations(deoptTargetLIRSuites);
    }

    protected void modifyRegularSuites(@SuppressWarnings("unused") Suites suites) {
    }

    protected PhaseSuite<HighTierContext> afterParseCanonicalization() {
        PhaseSuite<HighTierContext> phaseSuite = new PhaseSuite<>();
        phaseSuite.appendPhase(new ImplicitAssertionsPhase());
        phaseSuite.appendPhase(new DeadStoreRemovalPhase());
        phaseSuite.appendPhase(new DevirtualizeCallsPhase());
        phaseSuite.appendPhase(CanonicalizerPhase.create());
        if (!PointstoOptions.UseExperimentalReachabilityAnalysis.getValue(universe.hostVM().options())) {
            phaseSuite.appendPhase(new StrengthenStampsPhase());
        }
        phaseSuite.appendPhase(CanonicalizerPhase.create());
        phaseSuite.appendPhase(new OptimizeExceptionPathsPhase());
        if (ImageBuildStatistics.Options.CollectImageBuildStatistics.getValue(universe.hostVM().options())) {
            phaseSuite.appendPhase(CanonicalizerPhase.create());
            phaseSuite.appendPhase(new ImageBuildStatisticsCounterPhase(ImageBuildStatistics.CheckCountLocation.AFTER_PARSE_CANONICALIZATION));
        }
        phaseSuite.appendPhase(CanonicalizerPhase.create());
        return phaseSuite;
    }

    public Map<HostedMethod, CompileTask> getCompilations() {
        return compilations;
    }

    public void purge() {
        compilations.clear();
    }

    public Collection<CompileTask> getCompilationTasks() {
        return compilations.values();
    }

    private void printMethodHistogram() {
        long sizeAllMethods = 0;
        long sizeDeoptMethods = 0;
        long sizeDeoptMethodsInNonDeopt = 0;
        long sizeNonDeoptMethods = 0;
        int numberOfMethods = 0;
        int numberOfNonDeopt = 0;
        int numberOfDeopt = 0;
        long totalNumDeoptEntryPoints = 0;
        long totalNumDuringCallEntryPoints = 0;

        System.out.format("Code Size; Nodes Parsing; Nodes Before; Nodes After; Is Trivial;" +
                        " Deopt Target; Code Size; Nodes Parsing; Nodes Before; Nodes After; Deopt Entries; Deopt During Call;" +
                        " Entry Points; Direct Calls; Virtual Calls; Method\n");

        List<CompileTask> tasks = new ArrayList<>(compilations.values());
        tasks.sort(Comparator.comparing(t2 -> t2.method.format("%H.%n(%p) %r")));

        for (CompileTask task : tasks) {
            HostedMethod method = task.method;
            CompilationResult result = task.result;

            CompilationInfo ci = method.compilationInfo;
            if (!ci.isDeoptTarget()) {
                numberOfMethods += 1;
                sizeAllMethods += result.getTargetCodeSize();
                System.out.format("%8d; %5d; %5d; %5d; %s;", result.getTargetCodeSize(), ci.numNodesAfterParsing, ci.numNodesBeforeCompilation, ci.numNodesAfterCompilation,
                                ci.isTrivialMethod ? "T" : " ");

                int deoptMethodSize = 0;
                if (ci.deoptTarget != null) {
                    CompilationInfo dci = ci.deoptTarget.compilationInfo;

                    numberOfDeopt += 1;
                    deoptMethodSize = compilations.get(ci.deoptTarget).result.getTargetCodeSize();
                    sizeDeoptMethods += deoptMethodSize;
                    sizeDeoptMethodsInNonDeopt += result.getTargetCodeSize();
                    totalNumDeoptEntryPoints += dci.numDeoptEntryPoints;
                    totalNumDuringCallEntryPoints += dci.numDuringCallEntryPoints;

                    System.out.format(" D; %6d; %5d; %5d; %5d; %4d; %4d;", deoptMethodSize, dci.numNodesAfterParsing, dci.numNodesBeforeCompilation, dci.numNodesAfterCompilation,
                                    dci.numDeoptEntryPoints,
                                    dci.numDuringCallEntryPoints);

                } else {
                    sizeNonDeoptMethods += result.getTargetCodeSize();
                    numberOfNonDeopt += 1;
                    System.out.format("  ; %6d; %5d; %5d; %5d; %4d; %4d;", 0, 0, 0, 0, 0, 0);
                }

                System.out.format(" %4d; %4d; %4d; %s%n", ci.numEntryPointCalls.get(), ci.numDirectCalls.get(), ci.numVirtualCalls.get(), method.format("%H.%n(%p) %r"));
            }
        }
        System.out.println();
        System.out.println("Size all methods                           ; " + sizeAllMethods);
        System.out.println("Size deopt methods                         ; " + sizeDeoptMethods);
        System.out.println("Size deopt methods in non-deopt mode       ; " + sizeDeoptMethodsInNonDeopt);
        System.out.println("Size non-deopt method                      ; " + sizeNonDeoptMethods);
        System.out.println("Number of methods                          ; " + numberOfMethods);
        System.out.println("Number of non-deopt methods                ; " + numberOfNonDeopt);
        System.out.println("Number of deopt methods                    ; " + numberOfDeopt);
        System.out.println("Number of deopt entry points               ; " + totalNumDeoptEntryPoints);
        System.out.println("Number of deopt during calls entries       ; " + totalNumDuringCallEntryPoints);
    }

    protected void parseAll() throws InterruptedException {
        executor.init();

        parseDeoptimizationTargetMethods();
        parseAheadOfTimeCompiledMethods();

        // calling start before marking methods for parsing summons evil daemons
        executor.start();
        executor.complete();
        executor.shutdown();
    }

    /**
     * Regular compiled methods. Only entry points and manually marked methods are compiled, all
     * transitively reachable methods are then identified by looking at the callees of already
     * parsed methods.
     */
    private void parseAheadOfTimeCompiledMethods() {

        for (HostedMethod method : universe.getMethods()) {
            if (method.isEntryPoint() || CompilationInfoSupport.singleton().isForcedCompilation(method) ||
                            method.wrapped.isDirectRootMethod() && method.wrapped.isImplementationInvoked()) {
                ensureParsed(method, null, new EntryPointReason());
            }
            if (method.wrapped.isVirtualRootMethod()) {
                for (HostedMethod impl : method.getImplementations()) {
                    VMError.guarantee(impl.wrapped.isImplementationInvoked());
                    ensureParsed(impl, null, new EntryPointReason());
                }
            }
        }

        SubstrateForeignCallsProvider foreignCallsProvider = (SubstrateForeignCallsProvider) runtimeConfig.getProviders().getForeignCalls();
        for (SubstrateForeignCallLinkage linkage : foreignCallsProvider.getForeignCalls().values()) {
            HostedMethod method = (HostedMethod) linkage.getDescriptor().findMethod(runtimeConfig.getProviders().getMetaAccess());
            if (method.wrapped.isDirectRootMethod() && method.wrapped.isImplementationInvoked()) {
                ensureParsed(method, null, new EntryPointReason());
            }
            if (method.wrapped.isVirtualRootMethod()) {
                for (HostedMethod impl : method.getImplementations()) {
                    VMError.guarantee(impl.wrapped.isImplementationInvoked());
                    ensureParsed(impl, null, new EntryPointReason());
                }
            }
        }
    }

    private void parseDeoptimizationTargetMethods() {
        /*
         * Deoptimization target code for all methods that were manually marked as deoptimization
         * targets.
         */
        universe.getMethods().stream()
                        .filter(method -> CompilationInfoSupport.singleton().isDeoptTarget(method))
                        .forEach(method -> ensureParsed(universe.createDeoptTarget(method), null, new EntryPointReason()));

        /*
         * Deoptimization target code for deoptimization testing: all methods that are not
         * blacklisted are possible deoptimization targets. The methods are also flagged so that all
         * possible deoptimization entry points are emitted.
         */
        universe.getMethods().stream()
                        .filter(method -> method.getWrapped().isImplementationInvoked() && canDeoptForTesting(method))
                        .forEach(this::ensureParsedForDeoptTesting);
    }

    private void ensureParsedForDeoptTesting(HostedMethod method) {
        method.compilationInfo.canDeoptForTesting = true;
        ensureParsed(universe.createDeoptTarget(method), null, new EntryPointReason());
    }

    private static boolean checkTrivial(HostedMethod method, StructuredGraph graph) {
        if (!method.compilationInfo.isTrivialMethod() && method.canBeInlined() && InliningUtilities.isTrivialMethod(graph)) {
            method.compilationInfo.setTrivialMethod(true);
            return true;
        } else {
            return false;
        }
    }

    @SuppressWarnings("try")
    protected void inlineTrivialMethods(DebugContext debug) throws InterruptedException {
        int round = 0;
        do {
            ProgressReporter.singleton().reportStageProgress();
            inliningProgress = false;
            round++;
            try (Indent ignored = debug.logAndIndent("==== Trivial Inlining  round %d\n", round)) {

                executor.init();
                universe.getMethods().stream()
                                .filter(method -> method.compilationInfo.getCompilationGraph() != null)
                                .forEach(method -> executor.execute(new TrivialInlineTask(method)));
                universe.getMethods().stream()
                                .map(method -> method.compilationInfo.getDeoptTargetMethod()).filter(Objects::nonNull)
                                .forEach(deoptTargetMethod -> executor.execute(new TrivialInlineTask(deoptTargetMethod)));
                executor.start();
                executor.complete();
                executor.shutdown();
            }
        } while (inliningProgress);
    }

    class TrivialInliningPlugin implements InlineInvokePlugin {

        boolean inlinedDuringDecoding;

        @Override
        public InlineInfo shouldInlineInvoke(GraphBuilderContext b, ResolvedJavaMethod method, ValueNode[] args) {
            if (makeInlineDecision((HostedMethod) b.getMethod(), (HostedMethod) method) && b.recursiveInliningDepth(method) == 0) {
                inlinedDuringDecoding = true;
                return InlineInfo.createStandardInlineInfo(method);
            } else {
                return InlineInfo.DO_NOT_INLINE_WITH_EXCEPTION;
            }
        }
    }

    class InliningGraphDecoder extends PEGraphDecoder {

        InliningGraphDecoder(StructuredGraph graph, Providers providers, TrivialInliningPlugin inliningPlugin) {
            super(AnalysisParsedGraph.HOST_ARCHITECTURE, graph, providers, null,
                            null,
                            new InlineInvokePlugin[]{inliningPlugin},
                            null, null, null, null,
                            new ConcurrentHashMap<>(), new ConcurrentHashMap<>(), true);
        }

        @Override
        protected EncodedGraph lookupEncodedGraph(ResolvedJavaMethod method, BytecodeProvider intrinsicBytecodeProvider) {
            return ((HostedMethod) method).compilationInfo.getCompilationGraph().getEncodedGraph();
        }
    }

    @SuppressWarnings("try")
    private void doInlineTrivial(DebugContext debug, HostedMethod method) {
        /*
         * Before doing any work, check if there is any potential for inlining.
         *
         * Note that we do not have information about the recursive inlining depth, but that is OK
         * because in that case we just over-estimate the inlining potential, i.e., we do the
         * decoding just to find out that nothing could be inlined.
         */
        boolean inliningPotential = false;
        for (var invokeInfo : method.compilationInfo.getCompilationGraph().getInvokeInfos()) {
            if (invokeInfo.getInvokeKind().isDirect() && makeInlineDecision(method, invokeInfo.getTargetMethod())) {
                inliningPotential = true;
                break;
            }
        }
        if (!inliningPotential) {
            return;
        }
        var providers = runtimeConfig.lookupBackend(method).getProviders();
        var graph = method.compilationInfo.createGraph(debug, CompilationIdentifier.INVALID_COMPILATION_ID, false);
        try (var s = debug.scope("InlineTrivial", graph, method, this)) {
            var inliningPlugin = new TrivialInliningPlugin();
            var decoder = new InliningGraphDecoder(graph, providers, inliningPlugin);
            decoder.decode(method);

            if (inliningPlugin.inlinedDuringDecoding) {
                CanonicalizerPhase.create().apply(graph, providers);
                /*
                 * Publish the new graph, it can be picked up immediately by other threads trying to
                 * inline this method. This can be a minor source of non-determinism in inlining
                 * decisions.
                 */
                method.compilationInfo.encodeGraph(graph);
                if (checkTrivial(method, graph)) {
                    inliningProgress = true;
                }
            }
        } catch (Throwable ex) {
            throw debug.handle(ex);
        }
    }

    private boolean makeInlineDecision(HostedMethod method, HostedMethod callee) {
        if (universe.hostVM().neverInlineTrivial(method.getWrapped(), callee.getWrapped())) {
            return false;
        }
        if (callee.shouldBeInlined()) {
            return true;
        }
        if (optionAOTTrivialInline && callee.compilationInfo.isTrivialMethod()) {
            return true;
        }
        return false;
    }

    private static boolean mustNotAllocateCallee(HostedMethod method) {
        return ImageSingletons.lookup(RestrictHeapAccessCallees.class).mustNotAllocate(method);
    }

    private static boolean mustNotAllocate(HostedMethod method) {
        /*
         * GR-15580: This check is suspicious. The no-allocation restriction is propagated through
         * the call graph, so checking explicitly for annotated methods means that either not enough
         * methods are excluded from inlining, or the inlining restriction is not necessary at all.
         * We should elevate all methods that really need an inlining restriction
         * to @Uninterruptible or mark them as @NeverInline, so that no-allocation does not need any
         * more inlining restrictions and this code can be removed.
         */
        RestrictHeapAccess annotation = method.getAnnotation(RestrictHeapAccess.class);
        return annotation != null && annotation.access() == RestrictHeapAccess.Access.NO_ALLOCATION;
    }

    public static boolean callerAnnotatedWith(Invoke invoke, Class<? extends Annotation> annotationClass) {
        return getCallerAnnotation(invoke, annotationClass) != null;
    }

    private static <T extends Annotation> T getCallerAnnotation(Invoke invoke, Class<T> annotationClass) {
        for (FrameState state = invoke.stateAfter(); state != null; state = state.outerFrameState()) {
            assert state.getMethod() != null : state;
            T annotation = state.getMethod().getAnnotation(annotationClass);
            if (annotation != null) {
                return annotation;
            }
        }
        return null;
    }

    protected void compileAll() throws InterruptedException {
        executor.init();
        scheduleEntryPoints();
        executor.start();
        executor.complete();
        executor.shutdown();
    }

    public void scheduleEntryPoints() {
        for (HostedMethod method : universe.getMethods()) {
            if (!ignoreEntryPoint(method) && (method.isEntryPoint() || CompilationInfoSupport.singleton().isForcedCompilation(method)) ||
                            method.wrapped.isDirectRootMethod() && method.wrapped.isImplementationInvoked()) {
                ensureCompiled(method, new EntryPointReason());
            }
            if (method.wrapped.isVirtualRootMethod()) {
                for (HostedMethod impl : method.getImplementations()) {
                    VMError.guarantee(impl.wrapped.isImplementationInvoked());
                    ensureCompiled(impl, new EntryPointReason());
                }
            }
            HostedMethod deoptTargetMethod = method.compilationInfo.getDeoptTargetMethod();
            if (deoptTargetMethod != null) {
                ensureCompiled(deoptTargetMethod, new EntryPointReason());
            }
        }
    }

    @SuppressWarnings("unused")
    protected boolean ignoreEntryPoint(HostedMethod method) {
        return false;
    }

    protected void ensureParsed(HostedMethod method, HostedMethod callerMethod, CompileReason reason) {
        if (!(NativeImageOptions.AllowFoldMethods.getValue() || method.getAnnotation(Fold.class) == null ||
                        metaAccess.lookupJavaType(GeneratedFoldInvocationPlugin.class).isAssignableFrom(callerMethod.getDeclaringClass()))) {
            throw VMError.shouldNotReachHere("Parsing method annotated with @" + Fold.class.getSimpleName() + ": " +
                            method.format("%H.%n(%p)") +
                            ". Make sure you have used Graal annotation processors on the parent-project of the method's declaring class.");
        }
        if (!method.compilationInfo.inParseQueue.getAndSet(true)) {
            executor.execute(new ParseTask(method, reason));
        }
    }

    protected void doParse(DebugContext debug, ParseTask task) {
        ParseFunction fun = task.method.compilationInfo.getCustomParseFunction();
        if (fun == null) {
            fun = this::defaultParseFunction;
        }
        fun.parse(debug, task.method, task.reason, runtimeConfig);
    }

    private StructuredGraph transplantGraph(DebugContext debug, HostedMethod hMethod, CompileReason reason) {
        AnalysisMethod aMethod = hMethod.getWrapped();
        StructuredGraph aGraph = aMethod.decodeAnalyzedGraph(debug, null);
        if (aGraph == null) {
            throw VMError.shouldNotReachHere("Method not parsed during static analysis: " + aMethod.format("%r %H.%n(%p)") + ". Reachable from: " + reason);
        }

        /*
         * The graph in the analysis universe is no longer necessary once it is transplanted into
         * the hosted universe.
         */
        aMethod.setAnalyzedGraph(null);

        /*
         * The static analysis always needs NodeSourcePosition. But for AOT compilation, we only
         * need to preserve them when explicitly enabled, to reduce memory pressure.
         */
        boolean trackNodeSourcePosition = GraalOptions.TrackNodeSourcePosition.getValue(compileOptions);
        StructuredGraph graph = aGraph.copy(universe.lookup(aGraph.method()), compileOptions, debug, trackNodeSourcePosition);

        transplantEscapeAnalysisState(graph);

        IdentityHashMap<Object, Object> replacements = new IdentityHashMap<>();
        for (Node node : graph.getNodes()) {
            NodeClass<?> nodeClass = node.getNodeClass();

            for (int i = 0; i < nodeClass.getData().getCount(); i++) {
                Object oldValue = nodeClass.getData().get(node, i);
                Object newValue = replaceAnalysisObjects(oldValue, node, replacements, universe);
                if (oldValue != newValue) {
                    nodeClass.getData().putObjectChecked(node, i, newValue);
                }
            }
            /*
             * The NodeSourcePosition is not part of the regular "data" fields, so we need to
             * process it manually.
             */
            if (trackNodeSourcePosition) {
                node.setNodeSourcePosition((NodeSourcePosition) replaceAnalysisObjects(node.getNodeSourcePosition(), node, replacements, universe));
            } else {
                node.clearNodeSourcePosition();
            }
        }

        return graph;
    }

    public static Object replaceAnalysisObjects(Object obj, Node node, IdentityHashMap<Object, Object> replacements, HostedUniverse hUniverse) {
        if (obj == null) {
            return obj;
        }
        Object existingReplacement = replacements.get(obj);
        if (existingReplacement != null) {
            return existingReplacement;
        }

        Object newReplacement;

        if (obj instanceof Node) {
            throw VMError.shouldNotReachHere("Must not replace a Graal graph nodes, only data objects referenced from a node");

        } else if (obj instanceof AnalysisType) {
            newReplacement = hUniverse.lookup((AnalysisType) obj);
        } else if (obj instanceof AnalysisMethod) {
            newReplacement = hUniverse.lookup((AnalysisMethod) obj);
        } else if (obj instanceof AnalysisField) {
            newReplacement = hUniverse.lookup((AnalysisField) obj);
        } else if (obj instanceof FieldLocationIdentity) {
            ResolvedJavaField inner = ((FieldLocationIdentity) obj).getField();
            assert inner instanceof AnalysisField;
            newReplacement = new SubstrateFieldLocationIdentity((ResolvedJavaField) replaceAnalysisObjects(inner, node, replacements, hUniverse));
        } else if (obj.getClass() == ObjectStamp.class) {
            ObjectStamp stamp = (ObjectStamp) obj;
            if (stamp.type() == null) {
                /* No actual type referenced, so we can keep the original object. */
                newReplacement = obj;
            } else {
                /*
                 * ObjectStamp references a type indirectly, so we need to provide a new stamp with
                 * a modified type.
                 */
                newReplacement = new ObjectStamp((ResolvedJavaType) replaceAnalysisObjects(stamp.type(), node, replacements, hUniverse), stamp.isExactType(), stamp.nonNull(), stamp.alwaysNull(),
                                stamp.isAlwaysArray());
            }
        } else if (obj.getClass() == SubstrateNarrowOopStamp.class) {
            SubstrateNarrowOopStamp stamp = (SubstrateNarrowOopStamp) obj;
            if (stamp.type() == null) {
                newReplacement = obj;
            } else {
                newReplacement = new SubstrateNarrowOopStamp((ResolvedJavaType) replaceAnalysisObjects(stamp.type(), node, replacements, hUniverse), stamp.isExactType(), stamp.nonNull(),
                                stamp.alwaysNull(),
                                stamp.isAlwaysArray(), stamp.getEncoding());
            }
        } else if (obj.getClass() == PiNode.PlaceholderStamp.class) {
            assert ((PiNode.PlaceholderStamp) obj).type() == null : "PlaceholderStamp never references a type";
            newReplacement = obj;
        } else if (obj instanceof AbstractObjectStamp) {
            throw VMError.shouldNotReachHere("missing replacement of a subclass of AbstractObjectStamp: " + obj.getClass().getTypeName());

        } else if (obj.getClass() == StampPair.class) {
            StampPair pair = (StampPair) obj;
            Stamp trustedStamp = (Stamp) replaceAnalysisObjects(pair.getTrustedStamp(), node, replacements, hUniverse);
            Stamp uncheckedStamp = (Stamp) replaceAnalysisObjects(pair.getUncheckedStamp(), node, replacements, hUniverse);
            if (trustedStamp != pair.getTrustedStamp() || uncheckedStamp != pair.getUncheckedStamp()) {
                newReplacement = StampPair.create(trustedStamp, uncheckedStamp);
            } else {
                newReplacement = pair;
            }

        } else if (obj.getClass() == ResolvedJavaMethodBytecode.class) {
            ResolvedJavaMethodBytecode bc = (ResolvedJavaMethodBytecode) obj;
            newReplacement = new ResolvedJavaMethodBytecode(hUniverse.lookup(bc.getMethod()), bc.getOrigin());

        } else if (obj instanceof Object[]) {
            Object[] originalArray = (Object[]) obj;
            Object[] copyArray = null;
            for (int i = 0; i < originalArray.length; i++) {
                Object original = originalArray[i];
                Object replaced = replaceAnalysisObjects(original, node, replacements, hUniverse);
                if (replaced != original) {
                    if (copyArray == null) {
                        copyArray = Arrays.copyOf(originalArray, originalArray.length);
                    }
                    copyArray[i] = replaced;
                }
            }
            newReplacement = copyArray != null ? copyArray : originalArray;

        } else if (obj.getClass() == NodeSourcePosition.class) {
            NodeSourcePosition nsp = (NodeSourcePosition) obj;

            NodeSourcePosition replacedCaller = (NodeSourcePosition) replaceAnalysisObjects(nsp.getCaller(), node, replacements, hUniverse);
            ResolvedJavaMethod replacedMethod = (ResolvedJavaMethod) replaceAnalysisObjects(nsp.getMethod(), node, replacements, hUniverse);
            newReplacement = new NodeSourcePosition(nsp.getSourceLanguage(), replacedCaller, replacedMethod, nsp.getBCI(), nsp.getMarker());

        } else if (obj.getClass() == BytecodePosition.class) {
            BytecodePosition nsp = (BytecodePosition) obj;

            BytecodePosition replacedCaller = (BytecodePosition) replaceAnalysisObjects(nsp.getCaller(), node, replacements, hUniverse);
            ResolvedJavaMethod replacedMethod = (ResolvedJavaMethod) replaceAnalysisObjects(nsp.getMethod(), node, replacements, hUniverse);
            newReplacement = new BytecodePosition(replacedCaller, replacedMethod, nsp.getBCI());

        } else if (obj.getClass() == SubstrateMethodPointerConstant.class) {
            SubstrateMethodPointerConstant methodPointerConstant = (SubstrateMethodPointerConstant) obj;

            MethodPointer methodPointer = methodPointerConstant.pointer();
            ResolvedJavaMethod method = methodPointer.getMethod();
            ResolvedJavaMethod replacedMethod = (ResolvedJavaMethod) replaceAnalysisObjects(method, node, replacements, hUniverse);
            newReplacement = new SubstrateMethodPointerConstant(new MethodPointer(replacedMethod));

        } else if (obj.getClass() == ComputedIndirectCallTargetNode.FieldLoad.class) {
            ComputedIndirectCallTargetNode.FieldLoad fieldLoad = (ComputedIndirectCallTargetNode.FieldLoad) obj;
            newReplacement = new ComputedIndirectCallTargetNode.FieldLoad(hUniverse.lookup(fieldLoad.getField()));
        } else if (obj.getClass() == ComputedIndirectCallTargetNode.FieldLoadIfZero.class) {
            ComputedIndirectCallTargetNode.FieldLoadIfZero fieldLoadIfZero = (ComputedIndirectCallTargetNode.FieldLoadIfZero) obj;
            newReplacement = new ComputedIndirectCallTargetNode.FieldLoadIfZero(fieldLoadIfZero.getObject(), hUniverse.lookup(fieldLoadIfZero.getField()));

        } else {
            /* Check that we do not have a class or package name that relates to the analysis. */
            assert !obj.getClass().getName().toLowerCase().contains("analysis") : "Object " + obj + " of " + obj.getClass() + " in node " + node;
            assert !obj.getClass().getName().toLowerCase().contains("pointsto") : "Object " + obj + " of " + obj.getClass() + " in node " + node;
            newReplacement = obj;
        }

        replacements.put(obj, newReplacement);
        return newReplacement;
    }

    /**
     * The nodes produced by escape analysis need some manual patching: escape analysis requires
     * that {@link ResolvedJavaType#getInstanceFields} is stable and uses the index of a field in
     * that array also to index its own data structures. But {@link AnalysisType} and
     * {@link HostedType} cannot return fields in the same order: Fields that are not seen as
     * reachable by the static analysis are removed from the hosted type; and the layout of objects,
     * i.e., the field order, is only decided after static analysis. Therefore, we need to fix up
     * all the nodes that implicitly use the field index.
     */
    protected void transplantEscapeAnalysisState(StructuredGraph graph) {
        for (CommitAllocationNode node : graph.getNodes().filter(CommitAllocationNode.class)) {
            List<ValueNode> values = node.getValues();
            List<ValueNode> aValues = new ArrayList<>(values);
            values.clear();

            int aObjectStartIndex = 0;
            for (VirtualObjectNode virtualObject : node.getVirtualObjects()) {
                transplantVirtualObjectState(virtualObject, aValues, values, aObjectStartIndex);
                aObjectStartIndex += virtualObject.entryCount();
            }
            assert aValues.size() == aObjectStartIndex;
        }

        for (VirtualObjectState node : graph.getNodes().filter(VirtualObjectState.class)) {
            List<ValueNode> values = node.values();
            List<ValueNode> aValues = new ArrayList<>(values);
            values.clear();

            transplantVirtualObjectState(node.object(), aValues, values, 0);
        }

        for (VirtualInstanceNode node : graph.getNodes(VirtualInstanceNode.TYPE)) {
            AnalysisType aType = (AnalysisType) node.type();
            ResolvedJavaField[] aFields = node.getFields();
            assert Arrays.equals(aFields, aType.getInstanceFields(true));
            HostedField[] hFields = universe.lookup(aType).getInstanceFields(true);
            /*
             * We cannot directly write the final field `VirtualInstanceNode.fields`. So we rely on
             * the NodeClass mechanism, which is also used to transplant all other fields.
             */
            Fields nodeClassDataFields = node.getNodeClass().getData();
            for (int i = 0; i < nodeClassDataFields.getCount(); i++) {
                if (nodeClassDataFields.get(node, i) == aFields) {
                    nodeClassDataFields.putObjectChecked(node, i, hFields);
                }
            }
        }
    }

    private void transplantVirtualObjectState(VirtualObjectNode virtualObject, List<ValueNode> aValues, List<ValueNode> hValues, int aObjectStartIndex) {
        AnalysisType aType = (AnalysisType) virtualObject.type();
        if (aType.isArray()) {
            /* For arrays, there is no change between analysis and hosted elements. */
            for (int i = 0; i < virtualObject.entryCount(); i++) {
                hValues.add(aValues.get(aObjectStartIndex + i));
            }
        } else {
            /*
             * For instance fields, we need to add fields in the order of the hosted fields.
             * `AnalysisField.getPosition` gives us the index of the field in the analysis-level
             * list of field values.
             */
            assert virtualObject.entryCount() == aType.getInstanceFields(true).length;
            HostedField[] hFields = universe.lookup(aType).getInstanceFields(true);
            for (HostedField hField : hFields) {
                int aPosition = hField.wrapped.getPosition();
                assert hField.wrapped.equals(aType.getInstanceFields(true)[aPosition]);
                hValues.add(aValues.get(aObjectStartIndex + aPosition));
            }
        }
    }

    private final boolean parseOnce = SubstrateOptions.parseOnce();

    @SuppressWarnings("try")
    private void defaultParseFunction(DebugContext debug, HostedMethod method, CompileReason reason, RuntimeConfiguration config) {
        if (method.getAnnotation(NodeIntrinsic.class) != null) {
            throw VMError.shouldNotReachHere("Parsing method annotated with @" + NodeIntrinsic.class.getSimpleName() + ": " +
                            method.format("%H.%n(%p)") +
                            ". Make sure you have used Graal annotation processors on the parent-project of the method's declaring class.");
        }

        HostedProviders providers = (HostedProviders) config.lookupBackend(method).getProviders();
        boolean needParsing = false;

        StructuredGraph graph;
        if (parseOnce) {
            graph = transplantGraph(debug, method, reason);
        } else {
            graph = method.buildGraph(debug, method, providers, Purpose.AOT_COMPILATION);
            if (graph == null) {
                InvocationPlugin plugin = providers.getGraphBuilderPlugins().getInvocationPlugins().lookupInvocation(method, debug.getOptions());
                if (plugin != null && !plugin.inlineOnly()) {
                    Bytecode code = new ResolvedJavaMethodBytecode(method);
                    // DebugContext debug = new DebugContext(options,
                    // providers.getSnippetReflection());
                    graph = new SubstrateIntrinsicGraphBuilder(compileOptions, debug, providers,
                                    code).buildGraph(plugin);
                }
            }
            if (graph == null && method.isNative() &&
                            NativeImageOptions.ReportUnsupportedElementsAtRuntime.getValue()) {
                graph = DeletedMethod.buildGraph(debug, method, providers, DeletedMethod.NATIVE_MESSAGE);
            }
            if (graph == null) {
                needParsing = true;
                graph = new StructuredGraph.Builder(compileOptions, debug)
                                .method(method)
                                .recordInlinedMethods(false)
                                .build();
            }
        }
        try (DebugContext.Scope s = debug.scope("Parsing", graph, method, this)) {

            try {
                if (needParsing) {
                    GraphBuilderConfiguration gbConf = createHostedGraphBuilderConfiguration(providers, method);
                    new HostedGraphBuilderPhase(providers, gbConf, getOptimisticOpts(), null, providers.getWordTypes()).apply(graph);

                } else {
                    graph.getGraphState().setGuardsStage(GuardsStage.FIXED_DEOPTS);
                }

                PhaseSuite<HighTierContext> afterParseSuite = afterParseCanonicalization();
                afterParseSuite.apply(graph, new HighTierContext(providers, afterParseSuite, getOptimisticOpts()));

                method.compilationInfo.numNodesAfterParsing = graph.getNodeCount();
                if (!parseOnce) {
                    UninterruptibleAnnotationChecker.checkAfterParsing(method, graph);
                }

                for (Invoke invoke : graph.getInvokes()) {
                    if (!canBeUsedForInlining(invoke)) {
                        invoke.setUseForInlining(false);
                    }
                    CallTargetNode targetNode = invoke.callTarget();
                    ensureParsed(method, reason, targetNode, (HostedMethod) targetNode.targetMethod(), targetNode.invokeKind().isIndirect() || targetNode instanceof IndirectCallTargetNode);
                }
                for (Node n : graph.getNodes()) {
                    if (n instanceof MacroInvokable) {
                        /*
                         * A MacroInvokable might be lowered back to a regular invoke. At this point
                         * we do not know if that happens, but we need to prepared and have the
                         * graph of the potential callee parsed as if the MacroNode was an Invoke.
                         */
                        MacroInvokable macroNode = (MacroInvokable) n;
                        ensureParsed(method, reason, null, (HostedMethod) macroNode.getTargetMethod(), macroNode.getInvokeKind().isIndirect());
                    }
                }

                beforeEncode(method, graph);
                assert GraphOrder.assertSchedulableGraph(graph);
                method.compilationInfo.encodeGraph(graph);
                method.compilationInfo.setCompileOptions(compileOptions);
                checkTrivial(method, graph);

            } catch (Throwable ex) {
                GraalError error = ex instanceof GraalError ? (GraalError) ex : new GraalError(ex);
                error.addContext("method: " + method.format("%r %H.%n(%p)"));
                throw error;
            }

        } catch (Throwable e) {
            throw debug.handle(e);
        }
    }

    private void ensureParsed(HostedMethod method, CompileReason reason, CallTargetNode targetNode, HostedMethod invokeTarget, boolean isIndirect) {
        if (isIndirect) {
            for (HostedMethod invokeImplementation : invokeTarget.getImplementations()) {
                handleSpecialization(method, targetNode, invokeTarget, invokeImplementation);
                ensureParsed(invokeImplementation, method, new VirtualCallReason(method, invokeImplementation, reason));
            }
        } else {
            /*
             * Direct calls to instance methods (invokespecial bytecode or devirtualized calls) can
             * go to methods that are unreachable if the receiver is always null. At this time, we
             * do not know the receiver types, so we filter such invokes by looking at the
             * reachability status from the point of view of the static analysis. Note that we
             * cannot use "isImplementationInvoked" because (for historic reasons) it also returns
             * true if a method has a graph builder plugin registered. All graph builder plugins are
             * already applied during parsing before we reach this point, so we look at the "simple"
             * implementation invoked status.
             */
            if (invokeTarget.wrapped.isSimplyImplementationInvoked()) {
                handleSpecialization(method, targetNode, invokeTarget, invokeTarget);
                ensureParsed(invokeTarget, method, new DirectCallReason(method, reason));
            }
        }
    }

    @SuppressWarnings("unused")
    protected void beforeEncode(HostedMethod method, StructuredGraph graph) {
    }

    protected OptionValues getCustomizedOptions(DebugContext debug) {
        return debug.getOptions();
    }

    protected GraphBuilderConfiguration createHostedGraphBuilderConfiguration(HostedProviders providers, HostedMethod method) {
        GraphBuilderConfiguration gbConf = GraphBuilderConfiguration.getDefault(providers.getGraphBuilderPlugins()).withBytecodeExceptionMode(BytecodeExceptionMode.CheckAll);

        if (SubstrateOptions.optimizationLevel() == OptimizationLevel.O0 && !method.isDeoptTarget()) {
            /*
             * Disabling liveness analysis preserves the values of local variables beyond the
             * bytecode-liveness. This greatly helps debugging. When local variable numbers are
             * reused by javac, local variables can still get illegal values. Since we cannot
             * "restore" such illegal values during deoptimization, we cannot disable liveness
             * analysis for deoptimization target methods.
             */
            gbConf = gbConf.withRetainLocalVariables(true);
        }

        return gbConf;
    }

    protected boolean containsStackValueNode(HostedMethod method) {
        return universe.getBigBang().getHostVM().containsStackValueNode(method.wrapped);
    }

    protected boolean canBeUsedForInlining(Invoke invoke) {
        HostedMethod caller = (HostedMethod) invoke.asNode().graph().method();
        HostedMethod callee = (HostedMethod) invoke.callTarget().targetMethod();

        if (canDeoptForTesting(caller) && Modifier.isNative(callee.getModifiers())) {
            /*
             * We must not deoptimize in the stubs for native functions, since they don't have a
             * valid bytecode state.
             */
            return false;
        }
        if (canDeoptForTesting(caller) && containsStackValueNode(callee)) {
            /*
             * We must not inline a method that has stack values and can be deoptimized.
             */
            return false;
        }

        if (caller.compilationInfo.isDeoptTarget()) {
            if (caller.compilationInfo.isDeoptEntry(invoke.bci(), true, false)) {
                /*
                 * The call can be on the stack for a deoptimization, so we need an actual
                 * non-inlined invoke to deoptimize too.
                 *
                 * We could lift this restriction by providing an explicit deopt entry point (with
                 * the correct exception handling edges) in addition to the inlined method.
                 */
                return false;
            }
            if (CompilationInfoSupport.singleton().isDeoptInliningExclude(callee)) {
                /*
                 * The graphs for runtime compilation have an intrinisic for the callee, which might
                 * alter the behavior. Be safe and do not inline, otherwise we might optimize too
                 * aggressively.
                 *
                 * For example, the Truffle method CompilerDirectives.inCompiledCode is
                 * intrinisified to return a constant with the opposite value than returned by the
                 * method we would inline here, i.e., we would constant-fold away the compiled-code
                 * only code (which is the code we need deoptimization entry points for).
                 */
                return false;
            }
        }

        if (callee.getAnnotation(Specialize.class) != null) {
            return false;
        }
        if (callerAnnotatedWith(invoke, Specialize.class) && callee.getAnnotation(DeoptTest.class) != null) {
            return false;
        }

        if (!Uninterruptible.Utils.inliningAllowed(caller, callee)) {
            return false;
        }
        if (!mustNotAllocateCallee(caller) && mustNotAllocate(callee)) {
            return false;
        }
        /*
         * Note that we do not check callee.canBeInlined() yet. Otherwise a @NeverInline annotation
         * on a virtual target method would also prevent inlining of a concrete implementation
         * method (after a later de-virtualization of the invoke) that is not annotated
         * with @NeverInline. It is the responsibility of every inlining phase to check
         * canBeInlined().
         */
        return invoke.useForInlining();
    }

    private static void handleSpecialization(final HostedMethod method, CallTargetNode targetNode, HostedMethod invokeTarget, HostedMethod invokeImplementation) {
        if (method.getAnnotation(Specialize.class) != null && !method.compilationInfo.isDeoptTarget() && invokeTarget.getAnnotation(DeoptTest.class) != null) {
            /*
             * Collect the constant arguments to a method which should be specialized.
             */
            if (invokeImplementation.compilationInfo.specializedArguments != null) {
                VMError.shouldNotReachHere("Specialized method " + invokeImplementation.format("%H.%n(%p)") + " can only be called from one place");
            }
            invokeImplementation.compilationInfo.specializedArguments = new ConstantNode[targetNode.arguments().size()];
            int idx = 0;
            for (ValueNode argument : targetNode.arguments()) {
                if (!(argument instanceof ConstantNode)) {
                    VMError.shouldNotReachHere("Argument " + idx + " of specialized method " + invokeImplementation.format("%H.%n(%p)") + " is not constant");
                }
                invokeImplementation.compilationInfo.specializedArguments[idx++] = (ConstantNode) argument;
            }
        }
    }

    protected void ensureCompiled(HostedMethod method, CompileReason reason) {
        CompilationInfo compilationInfo = method.compilationInfo;

        if (printMethodHistogram) {
            if (reason instanceof DirectCallReason) {
                compilationInfo.numDirectCalls.incrementAndGet();
            } else if (reason instanceof VirtualCallReason) {
                compilationInfo.numVirtualCalls.incrementAndGet();
            } else if (reason instanceof EntryPointReason) {
                compilationInfo.numEntryPointCalls.incrementAndGet();
            }
        }

        /*
         * Fast non-atomic check if method is already scheduled for compilation, to avoid frequent
         * access of the ConcurrentHashMap.
         */
        if (compilationInfo.inCompileQueue) {
            return;
        }

        CompileTask task = new CompileTask(method, reason);
        CompileTask oldTask = compilations.putIfAbsent(method, task);
        if (oldTask != null) {
            return;
        }
        compilationInfo.inCompileQueue = true;

        executor.execute(task);
        method.setCompiled();
    }

    class HostedCompilationResultBuilderFactory implements CompilationResultBuilderFactory {
        @Override
        public CompilationResultBuilder createBuilder(CodeGenProviders providers,
                        FrameMap frameMap,
                        Assembler<?> asm,
                        DataBuilder dataBuilder,
                        FrameContext frameContext,
                        OptionValues options,
                        DebugContext debug,
                        CompilationResult compilationResult,
                        Register uncompressedNullRegister) {
            return new CompilationResultBuilder(providers,
                            frameMap,
                            asm,
                            dataBuilder,
                            frameContext,
                            options,
                            debug,
                            compilationResult,
                            uncompressedNullRegister,
                            EconomicMap.wrapMap(dataCache),
                            CompilationResultBuilder.NO_VERIFIERS);
        }
    }

    protected CompilationResult doCompile(DebugContext debug, final HostedMethod method, CompilationIdentifier compilationIdentifier, CompileReason reason) {
        CompileFunction fun = method.compilationInfo.getCustomCompileFunction();
        if (fun == null) {
            fun = this::defaultCompileFunction;
        }
        return fun.compile(debug, method, compilationIdentifier, reason, runtimeConfig);
    }

    @SuppressWarnings("try")
    private CompilationResult defaultCompileFunction(DebugContext debug, HostedMethod method, CompilationIdentifier compilationIdentifier, CompileReason reason, RuntimeConfiguration config) {
        if (NativeImageOptions.PrintAOTCompilation.getValue()) {
            System.out.println("Compiling " + method.format("%r %H.%n(%p)") + "  [" + reason + "]");
        }

        try {
            SubstrateBackend backend = config.lookupBackend(method);

            VMError.guarantee(method.compilationInfo.getCompilationGraph() != null, "The following method is reachable during compilation, but was not seen during Bytecode parsing: " + method);
            StructuredGraph graph = method.compilationInfo.createGraph(debug, compilationIdentifier, true);

            if (method.compilationInfo.specializedArguments != null) {
                // Do the specialization: replace the argument locals with the constant arguments.
                int idx = 0;
                for (ConstantNode argument : method.compilationInfo.specializedArguments) {
                    ParameterNode local = graph.getParameter(idx++);
                    if (local != null) {
                        local.replaceAndDelete(ConstantNode.forConstant(argument.asJavaConstant(), runtimeConfig.getProviders().getMetaAccess(), graph));
                    }
                }
            }

            /* Check that graph is in good shape before compilation. */
            assert GraphOrder.assertSchedulableGraph(graph);

            try (DebugContext.Scope s = debug.scope("Compiling", graph, method, this)) {

                if (deoptimizeAll && method.compilationInfo.canDeoptForTesting) {
                    insertDeoptTests(method, graph);
                }
                method.compilationInfo.numNodesBeforeCompilation = graph.getNodeCount();
                method.compilationInfo.numDeoptEntryPoints = graph.getNodes().filter(DeoptEntryNode.class).count();
                method.compilationInfo.numDuringCallEntryPoints = graph.getNodes(MethodCallTargetNode.TYPE).snapshot().stream()
                                .map(MethodCallTargetNode::invoke)
                                .filter(invoke -> method.compilationInfo.isDeoptEntry(invoke.bci(), true, false))
                                .count();

                Suites suites = method.compilationInfo.isDeoptTarget() ? deoptTargetSuites : regularSuites;
                LIRSuites lirSuites = method.compilationInfo.isDeoptTarget() ? deoptTargetLIRSuites : regularLIRSuites;

                CompilationResult result = backend.newCompilationResult(compilationIdentifier, method.format("%H.%n(%p)"));

                try (Indent indent = debug.logAndIndent("compile %s", method)) {
                    GraalCompiler.compileGraph(graph, method, backend.getProviders(), backend, null, getOptimisticOpts(), method.getProfilingInfo(), suites, lirSuites, result,
                                    new HostedCompilationResultBuilderFactory(), false);
                }
                method.compilationInfo.numNodesAfterCompilation = graph.getNodeCount();

                if (method.compilationInfo.isDeoptTarget()) {
                    assert verifyDeoptTarget(method, graph, result);
                }
                ensureCalleesCompiled(method, reason, result);

                /* Shrink resulting code array to minimum size, to reduze memory footprint. */
                if (result.getTargetCode().length > result.getTargetCodeSize()) {
                    result.setTargetCode(Arrays.copyOf(result.getTargetCode(), result.getTargetCodeSize()), result.getTargetCodeSize());
                }

                return result;
            }
        } catch (Throwable ex) {
            GraalError error = ex instanceof GraalError ? (GraalError) ex : new GraalError(ex);
            error.addContext("method: " + method.format("%r %H.%n(%p)") + "  [" + reason + "]");
            throw error;
        }
    }

    protected void ensureCalleesCompiled(HostedMethod method, CompileReason reason, CompilationResult result) {
        for (Infopoint infopoint : result.getInfopoints()) {
            if (infopoint instanceof Call) {
                Call call = (Call) infopoint;
                HostedMethod callTarget = (HostedMethod) call.target;
                if (call.direct) {
                    ensureCompiled(callTarget, new DirectCallReason(method, reason));
                } else if (callTarget != null && callTarget.getImplementations() != null) {
                    for (HostedMethod impl : callTarget.getImplementations()) {
                        ensureCompiled(impl, new VirtualCallReason(method, callTarget, reason));
                    }
                }
            }
        }
        ensureCompiledForMethodPointerConstants(method, reason, result);
    }

    protected final void ensureCompiledForMethodPointerConstants(HostedMethod method, CompileReason reason, CompilationResult result) {
        for (DataPatch dataPatch : result.getDataPatches()) {
            Reference reference = dataPatch.reference;
            if (reference instanceof ConstantReference) {
                VMConstant constant = ((ConstantReference) reference).getConstant();
                if (constant instanceof SubstrateMethodPointerConstant) {
                    MethodPointer pointer = ((SubstrateMethodPointerConstant) constant).pointer();
                    final ResolvedJavaMethod method1 = pointer.getMethod();
                    HostedMethod hMethod = (HostedMethod) method1;
                    ensureCompiled(hMethod, new MethodPointerConstantReason(method, hMethod, reason));
                }
            }
        }
    }

    protected void removeDeoptTargetOptimizations(Suites suites) {
        GraalConfiguration.hostedInstance().removeDeoptTargetOptimizations(suites);

        PhaseSuite<HighTierContext> highTier = suites.getHighTier();
        highTier.removePhase(PartialEscapePhase.class);
        highTier.removePhase(ReadEliminationPhase.class);
        highTier.removePhase(BoxNodeOptimizationPhase.class);
        PhaseSuite<MidTierContext> midTier = suites.getMidTier();
        midTier.removePhase(FloatingReadPhase.class);
        PhaseSuite<LowTierContext> lowTier = suites.getLowTier();
        ListIterator<BasePhase<? super LowTierContext>> it = lowTier.findPhase(FixReadsPhase.class);
        if (it != null) {
            FixReadsPhase fixReads = (FixReadsPhase) it.previous();
            it.remove();
            boolean replaceInputsWithConstants = false;
            it.add(new FixReadsPhase(replaceInputsWithConstants, fixReads.getSchedulePhase()));
        }
    }

    private static void removeDeoptTargetOptimizations(LIRSuites lirSuites) {
        ListIterator<LIRPhase<PostAllocationOptimizationContext>> it = lirSuites.getPostAllocationOptimizationStage().findPhase(RedundantMoveElimination.class);
        if (it != null) {
            it.remove();
        }
        lirSuites.getAllocationStage().findPhaseInstance(RegisterAllocationPhase.class).setNeverSpillConstants(true);
    }

    public static boolean isDeoptEntry(HostedMethod method, CompilationResult compilation, Infopoint infopoint) {
        BytecodeFrame topFrame = infopoint.debugInfo.frame();
        BytecodeFrame rootFrame = topFrame;
        while (rootFrame.caller() != null) {
            rootFrame = rootFrame.caller();
        }
        assert rootFrame.getMethod().equals(method);

        boolean isBciDeoptEntry = method.compilationInfo.isDeoptEntry(rootFrame.getBCI(), rootFrame.duringCall, rootFrame.rethrowException);
        if (isBciDeoptEntry) {
            /*
             * When an infopoint's bci corresponds to a deoptimization entrypoint, it does not
             * necessarily mean that the infopoint itself is for a deoptimization entrypoint. This
             * is because the infopoint can also be for present debugging purposes and happen to
             * have the same bci. Further checks are needed to determine actual deoptimization
             * entrypoints.
             */
            assert topFrame == rootFrame : "Deoptimization target has inlined frame: " + topFrame;
            if (topFrame.duringCall) {
                /*
                 * During call entrypoints must always be linked to a call.
                 */
                VMError.guarantee(infopoint instanceof Call, String.format("Unexpected infopoint type: %s%nFrame: %s", infopoint, topFrame));
                return compilation.isValidCallDeoptimizationState((Call) infopoint);
            } else {
                /*
                 * Other deoptimization entrypoints correspond to an DeoptEntryOp.
                 */
                return infopoint instanceof DeoptEntryInfopoint;
            }
        }

        return false;
    }

    private static boolean verifyDeoptTarget(HostedMethod method, StructuredGraph graph, CompilationResult result) {
        Map<Long, BytecodeFrame> encodedBciMap = new HashMap<>();

        /*
         * All deopt targets must have a graph.
         */
        assert graph != null : "Deopt target must have a graph.";

        /*
         * No deopt targets can have a StackValueNode in the graph.
         */
        assert graph.getNodes(StackValueNode.TYPE).isEmpty() : "No stack value nodes must be present in deopt target.";

        for (Infopoint infopoint : result.getInfopoints()) {
            if (infopoint.debugInfo != null) {
                DebugInfo debugInfo = infopoint.debugInfo;
                if (!debugInfo.hasFrame()) {
                    continue;
                }

                if (isDeoptEntry(method, result, infopoint)) {
                    BytecodeFrame frame = debugInfo.frame();
                    long encodedBci = FrameInfoEncoder.encodeBci(frame.getBCI(), frame.duringCall, frame.rethrowException);

                    BytecodeFrame previous = encodedBciMap.put(encodedBci, frame);
                    assert previous == null : "duplicate encoded bci " + encodedBci + " in deopt target " + method + " found.\n\n" + frame +
                                    "\n\n" + previous;
                }

            }
        }

        return true;
    }

    /**
     * Returns true if a method should be considered as deoptimization source. This is only a
     * feature for testing. Note that usually all image compiled methods cannot deoptimize.
     */
    protected boolean canDeoptForTesting(HostedMethod method) {
        if (method.getName().equals("<clinit>")) {
            /* Cannot deoptimize into static initializers. */
            return false;
        }

        if (method.getAnnotation(DeoptTest.class) != null) {
            return true;
        }

        if (method.isEntryPoint()) {
            /*
             * Entry points from C have special entry/exit nodes added, so they cannot be
             * deoptimized.
             */
            return false;
        }
        if (method.isNative()) {
            /*
             * Native methods (i.e., the stubs that actually perform the native calls) cannot be
             * deoptimized.
             */
            return false;
        }
        if (method.wrapped.isIntrinsicMethod()) {
            return false;
        }
        if (Uninterruptible.Utils.isUninterruptible(method)) {
            return false;
        }
        if (method.getAnnotation(RestrictHeapAccess.class) != null) {
            return false;
        }
        if (StubCallingConvention.Utils.hasStubCallingConvention(method)) {
            /* Deoptimization runtime cannot fill the callee saved registers. */
            return false;
        }
        if (containsStackValueNode(method)) {
            return false;
        }

        if (deoptimizeAll) {
            /*
             * The DeoptimizeAll option is set. So we use all methods for deoptimization testing.
             * Exclude some "runtime" methods, like the heap code, via this blacklist. Issue GR-1706
             * tracks the bug in DebugValueMap.
             */
            String className = method.getDeclaringClass().getName();
            if (className.contains("/svm/core/code/CodeInfoEncoder") ||
                            className.contains("com/oracle/svm/core/thread/JavaThreads") ||
                            className.contains("com/oracle/svm/core/thread/PlatformThreads") ||
                            className.contains("com/oracle/svm/core/heap/") ||
                            className.contains("com/oracle/svm/core/genscavenge/") ||
                            className.contains("com/oracle/svm/core/thread/VMOperationControl") ||
                            className.contains("debug/internal/DebugValueMap") && method.getName().equals("registerTopLevel")) {
                return false;
            }
            /*
             * Method without bytecodes, e.g., methods that have a manually constructed graph, are
             * usually not deoptimizable. This needs to change as soon as we want to runtime compile
             * our synthetic annotation methods.
             */
            if (method.getCode() == null) {
                return false;
            }

            return true;
        } else {
            return false;
        }
    }

    /**
     * Inserts a call to {@link DeoptTester#deoptTest} right after FixedWithNextNode StateSplits.
     *
     * @param method method that is being augmented with deopt test calls
     * @param graph The graph of a deoptimizable method or the corresponding deopt target method.
     */
    private static void insertDeoptTests(HostedMethod method, StructuredGraph graph) {
        for (Node node : graph.getNodes()) {
            if (node instanceof FixedWithNextNode && node instanceof StateSplit && !(node instanceof InvokeNode) && !(node instanceof ForeignCallNode) && !(node instanceof DeoptTestNode) &&
                            !(method.isSynchronized() && node instanceof StartNode)) {
                FixedWithNextNode fixedWithNext = (FixedWithNextNode) node;
                FixedNode next = fixedWithNext.next();
                DeoptTestNode testNode = graph.add(new DeoptTestNode());
                fixedWithNext.setNext(null);
                testNode.setNext(next);
                fixedWithNext.setNext(testNode);
                if (((StateSplit) node).hasSideEffect() && ((StateSplit) node).stateAfter() != null) {
                    testNode.setStateAfter(((StateSplit) node).stateAfter().duplicateWithVirtualState());
                } else {
                    testNode.setStateAfter(GraphUtil.findLastFrameState((FixedNode) node).duplicateWithVirtualState());
                }
            }
        }
    }

    public Map<HostedMethod, CompilationResult> getCompilationResults() {
        Map<HostedMethod, CompilationResult> result = new TreeMap<>(HostedUniverse.METHOD_COMPARATOR);
        for (Entry<HostedMethod, CompileTask> entry : compilations.entrySet()) {
            result.put(entry.getKey(), entry.getValue().result);
        }
        return result;
    }

    public Suites getRegularSuites() {
        return regularSuites;
    }
}
