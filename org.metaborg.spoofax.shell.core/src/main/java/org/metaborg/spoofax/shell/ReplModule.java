package org.metaborg.spoofax.shell;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.ISimpleProjectService;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.core.resource.ResourceService;
import org.metaborg.spoofax.shell.client.IDisplay;
import org.metaborg.spoofax.shell.client.IRepl;
import org.metaborg.spoofax.shell.commands.DefaultCommand;
import org.metaborg.spoofax.shell.commands.HelpCommand;
import org.metaborg.spoofax.shell.commands.IReplCommand;
import org.metaborg.spoofax.shell.commands.LanguageCommand;
import org.metaborg.spoofax.shell.core.IEvaluationStrategy;
import org.metaborg.spoofax.shell.functions.ATransformFunction;
import org.metaborg.spoofax.shell.functions.AnalyzeFunction;
import org.metaborg.spoofax.shell.functions.EvaluateFunction;
import org.metaborg.spoofax.shell.functions.FailableFunction;
import org.metaborg.spoofax.shell.functions.IFunctionFactory;
import org.metaborg.spoofax.shell.functions.InputFunction;
import org.metaborg.spoofax.shell.functions.OpenInputFunction;
import org.metaborg.spoofax.shell.functions.PTransformFunction;
import org.metaborg.spoofax.shell.functions.ParseFunction;
import org.metaborg.spoofax.shell.invoker.ICommandInvoker;
import org.metaborg.spoofax.shell.invoker.SpoofaxCommandInvoker;
import org.metaborg.spoofax.shell.output.AnalyzeResult;
import org.metaborg.spoofax.shell.output.EvaluateResult;
import org.metaborg.spoofax.shell.output.IResult;
import org.metaborg.spoofax.shell.output.IResultFactory;
import org.metaborg.spoofax.shell.output.IResultVisitor;
import org.metaborg.spoofax.shell.output.ISpoofaxTermResult;
import org.metaborg.spoofax.shell.output.InputResult;
import org.metaborg.spoofax.shell.output.ParseResult;
import org.metaborg.spoofax.shell.output.TransformResult;

import com.google.common.io.Files;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;

/**
 * This class binds the core classes. It is intended to be subclassed by client implementations. These subclasses should
 * bind their implementations of {@link IRepl} and either {@link IResultVisitor} or {@link IDisplay} (which is also an
 * {@link IResultVisitor}).
 */
public abstract class ReplModule extends AbstractModule {
    @Override protected void configure() {
        MapBinder<String, IReplCommand> commandBinder =
            MapBinder.newMapBinder(binder(), String.class, IReplCommand.class);
        MapBinder<String, IEvaluationStrategy> evalStrategyBinder =
            MapBinder.newMapBinder(binder(), String.class, IEvaluationStrategy.class);
        bindCommands(commandBinder);
        bindEvalStrategies(evalStrategyBinder);
        bindFactories();
    }


    /**
     * Binds the default commands.
     *
     * @param commandBinder
     *            The {@link MapBinder} for binding the commands to their names.
     */
    protected void bindCommands(MapBinder<String, IReplCommand> commandBinder) {
        commandBinder.addBinding("help").to(HelpCommand.class);
        commandBinder.addBinding("load").to(LanguageCommand.class);
        bind(IReplCommand.class).annotatedWith(Names.named("default_command")).to(DefaultCommand.class);
        bind(ICommandInvoker.class).to(SpoofaxCommandInvoker.class);
    }

    /**
     * Binds the evaluation strategies.
     *
     * @param evalStrategyBinder
     *            The {@link MapBinder} for binding the strategies to their names.
     */
    protected void bindEvalStrategies(MapBinder<String, IEvaluationStrategy> evalStrategyBinder) {
    }

    /**
     * Binds implementations for the {@link IResultFactory} and the {@link IFunctionFactory}.
     */
    protected void bindFactories() {
        install(new FactoryModuleBuilder()
            .implement(TransformResult.class, Names.named("parsed"), TransformResult.Parsed.class)
            .implement(TransformResult.class, Names.named("analyzed"), TransformResult.Analyzed.class)
            .build(IResultFactory.class));

        install(new FactoryModuleBuilder()
            .implement(new TypeLiteral<FailableFunction<String, InputResult, IResult>>() {}, Names.named("Source"),
                InputFunction.class)
            .implement(new TypeLiteral<FailableFunction<String, InputResult, IResult>>() {}, Names.named("Open"),
                OpenInputFunction.class)
            .implement(new TypeLiteral<FailableFunction<InputResult, ParseResult, IResult>>() {}, ParseFunction.class)
            .implement(new TypeLiteral<FailableFunction<ParseResult, AnalyzeResult, IResult>>() {},
                AnalyzeFunction.class)
            .implement(new TypeLiteral<FailableFunction<ParseResult, TransformResult, IResult>>() {},
                PTransformFunction.class)
            .implement(new TypeLiteral<FailableFunction<AnalyzeResult, TransformResult, IResult>>() {},
                ATransformFunction.class)
            .implement(new TypeLiteral<FailableFunction<ISpoofaxTermResult<?>, EvaluateResult, IResult>>() {},
                EvaluateFunction.class)
            .build(IFunctionFactory.class));
    }


    /**
     * FIXME: hardcoded project returned here.
     *
     * @param resourceService
     *            the Spoofax {@link ResourceService}
     * @param projectService
     *            the Spoofax {@link ISimpleProjectService}
     * @return an {@link IProject}
     * @throws MetaborgException
     *             when creating a project failed
     */
    @Provides protected IProject project(IResourceService resourceService, ISimpleProjectService projectService)
        throws MetaborgException {
        FileObject resolve = resourceService.resolve(Files.createTempDir());
        return projectService.create(resolve);
    }
}
