package org.metaborg.spoofax.shell.client;

import org.metaborg.spoofax.shell.ReplModule;
import org.metaborg.spoofax.shell.client.eclipse.ColorManager;
import org.metaborg.spoofax.shell.client.eclipse.commands.ExitCommand;
import org.metaborg.spoofax.shell.client.eclipse.impl.IWidgetFactory;
import org.metaborg.spoofax.shell.commands.IReplCommand;

import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.MapBinder;

/**
 * Bindings for the Eclipse REPL.
 */
public class EclipseReplModule extends ReplModule {

    @Override
    protected void bindCommands(MapBinder<String, IReplCommand> commandBinder) {
        super.bindCommands(commandBinder);
        commandBinder.addBinding("exit").to(ExitCommand.class);
    }

    @Override
    protected void configure() {
        super.configure();

        // Singleton so that all REPLs talk to the same ColorManager.
        bind(ColorManager.class).in(Singleton.class);
        bind(IInputHistory.class).to(InputHistory.class);
        install(new FactoryModuleBuilder().build(IWidgetFactory.class));
    }
}
