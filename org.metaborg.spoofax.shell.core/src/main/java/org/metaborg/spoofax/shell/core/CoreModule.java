package org.metaborg.spoofax.shell.core;

import org.metaborg.spoofax.shell.commands.ICommandInvoker;
import org.metaborg.spoofax.shell.commands.IReplCommand;
import org.metaborg.spoofax.shell.commands.SpoofaxCommandInvoker;
import org.metaborg.spoofax.shell.commands.SpoofaxEvaluationCommand;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

/**
 * Core library bindings.
 */
public class CoreModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(IReplCommand.class).annotatedWith(Names.named("EvalCommand"))
            .to(SpoofaxEvaluationCommand.class).in(Singleton.class);
        bind(ICommandInvoker.class).to(SpoofaxCommandInvoker.class);
    }
}
