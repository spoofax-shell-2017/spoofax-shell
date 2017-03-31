package org.metaborg.spoofax.shell.client;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.ISimpleProjectService;
import org.metaborg.core.project.SimpleProjectService;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.core.resource.ResourceService;
import org.metaborg.spoofax.shell.ReplModule;
import org.metaborg.spoofax.shell.client.eclipse.ColorManager;
import org.metaborg.spoofax.shell.client.eclipse.commands.ExitCommand;
import org.metaborg.spoofax.shell.client.eclipse.impl.IWidgetFactory;
import org.metaborg.spoofax.shell.commands.IReplCommand;

import com.google.common.io.Files;
import com.google.inject.Provides;
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
