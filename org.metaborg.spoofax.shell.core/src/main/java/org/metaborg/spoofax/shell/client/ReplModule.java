package org.metaborg.spoofax.shell.client;

import java.util.function.Consumer;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.IProjectService;
import org.metaborg.core.project.ISimpleProjectService;
import org.metaborg.core.project.SimpleProjectService;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.core.resource.ResourceService;
import org.metaborg.spoofax.core.SpoofaxModule;
import org.metaborg.spoofax.shell.commands.EvaluateCommand;
import org.metaborg.spoofax.shell.commands.ExitCommand;
import org.metaborg.spoofax.shell.commands.HelpCommand;
import org.metaborg.spoofax.shell.commands.IReplCommand;
import org.metaborg.spoofax.shell.commands.LanguageCommand;
import org.metaborg.spoofax.shell.core.StyledText;
import org.metaborg.spoofax.shell.hooks.OnEvalErrorHook;
import org.metaborg.spoofax.shell.hooks.OnEvalSuccessHook;
import org.metaborg.spoofax.shell.invoker.ICommandFactory;
import org.metaborg.spoofax.shell.invoker.ICommandInvoker;
import org.metaborg.spoofax.shell.invoker.SpoofaxCommandInvoker;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;

/**
 * Client library bindings.
 */
public class ReplModule extends SpoofaxModule {

    protected MapBinder<String, IReplCommand> commandBinder;

    /**
     * Binds the default commands.
     */
    protected void configureCommands() {
        commandBinder = MapBinder.newMapBinder(binder(), String.class, IReplCommand.class);
        commandBinder.addBinding("exit").to(ExitCommand.class).in(Singleton.class);
        commandBinder.addBinding("help").to(HelpCommand.class).in(Singleton.class);
        commandBinder.addBinding("load").to(LanguageCommand.class).in(Singleton.class);
        // FIXME: partially rewrite commandinvoker so eval becomes part of the hashmap
        // commandBinder.addBinding("eval").to(EvaluateCommand.class).in(Singleton.class);
        bind(IReplCommand.class).annotatedWith(Names.named("EvalCommand"))
            .to(EvaluateCommand.class).in(Singleton.class);

        bind(ICommandInvoker.class).to(SpoofaxCommandInvoker.class);
    }

    @Override
    protected void bindProject() {
        bind(SimpleProjectService.class).in(Singleton.class);
        bind(ISimpleProjectService.class).to(SimpleProjectService.class);
        bind(IProjectService.class).to(SimpleProjectService.class);
    }

    @Override
    protected void configure() {
        super.configure();

        configureCommands();

        // @formatter:off
        bind(new TypeLiteral<Consumer<StyledText>>() { }).annotatedWith(Names.named("onSuccess"))
                .to(OnEvalSuccessHook.class).in(Singleton.class);
        bind(new TypeLiteral<Consumer<StyledText>>() { }).annotatedWith(Names.named("onError"))
                .to(OnEvalErrorHook.class).in(Singleton.class);
        // @formatter:on

        install(new FactoryModuleBuilder().build(ICommandFactory.class));
    }

    /**
     * FIXME: hardcoded project returned here.
     * @param resourceService the Spoofax {@link ResourceService}
     * @param projectService the Spoofax {@link ISimpleProjectService}
     * @return an {@link IProject}
     * @throws MetaborgException when creating a project failed
     */
    @Provides
    protected IProject project(IResourceService resourceService,
                               ISimpleProjectService projectService)
            throws MetaborgException {
        return projectService.create(resourceService.resolve("tmp:"));
    }
}
