package org.metaborg.spoofax.shell.client;

import org.metaborg.core.project.ISimpleProjectService;
import org.metaborg.core.project.SimpleProjectService;
import org.metaborg.spoofax.shell.ReplModule;
import org.metaborg.spoofax.shell.client.eclipse.ColorManager;
import org.metaborg.spoofax.shell.client.eclipse.commands.ExitCommand;
import org.metaborg.spoofax.shell.client.eclipse.impl.IWidgetFactory;
import org.metaborg.spoofax.shell.commands.IReplCommand;
import org.metaborg.spoofax.shell.services.IEditorServices;
import org.metaborg.spoofax.shell.services.IServicesStrategyFactory;
import org.metaborg.spoofax.shell.services.SpoofaxEditorServices;
import org.metaborg.spoofax.shell.services.SpoofaxServicesStrategyFactory;

import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.MapBinder;

/**
 * Bindings for the Eclipse REPL.
 */
public class EclipseReplModule extends ReplModule {
    @Override protected void configure() {
        super.configure();

        // Bind simple project service for creating a fake project
        bind(ISimpleProjectService.class).to(SimpleProjectService.class).in(Singleton.class);

        // Singleton so that all REPLs talk to the same ColorManager.
        bind(ColorManager.class).in(Singleton.class);
        bind(IInputHistory.class).to(InputHistory.class);
        install(new FactoryModuleBuilder().build(IWidgetFactory.class));
    }

    @Override protected void bindCommands(MapBinder<String, IReplCommand> commandBinder) {
        super.bindCommands(commandBinder);
        commandBinder.addBinding("exit").to(ExitCommand.class);
    }

}
