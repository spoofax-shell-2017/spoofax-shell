package org.metaborg.spoofax.shell.core;

import org.metaborg.spoofax.core.SpoofaxModule;
import org.metaborg.spoofax.shell.commands.EvaluateCommand;
import org.metaborg.spoofax.shell.commands.ICommandInvoker;
import org.metaborg.spoofax.shell.commands.IReplCommand;
import org.metaborg.spoofax.shell.commands.SpoofaxCommandInvoker;

import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;

/**
 * Core library bindings.
 */
public class CoreModule extends SpoofaxModule {
    @Override
    protected void configure() {
        super.configure();

        MapBinder.newMapBinder(binder(), String.class, IReplCommand.class);
        bind(IReplCommand.class).annotatedWith(Names.named("EvalCommand"))
            .to(EvaluateCommand.class).in(Singleton.class);

        bind(ICommandInvoker.class).to(SpoofaxCommandInvoker.class);
    }
}
