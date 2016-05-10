package org.metaborg.spoofax.shell.client.console;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;

import org.metaborg.spoofax.shell.client.IDisplay;
import org.metaborg.spoofax.shell.client.IEditor;
import org.metaborg.spoofax.shell.commands.ICommandInvoker;
import org.metaborg.spoofax.shell.commands.IReplCommand;
import org.metaborg.spoofax.shell.commands.SpoofaxCommandInvoker;
import org.metaborg.spoofax.shell.commands.SpoofaxEvaluationCommand;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;

/**
 * Bindings for Repl.
 */
public class ReplModule extends AbstractModule {

    @Override
    protected void configure() {
        MapBinder<String, IReplCommand> commandBinder =
            MapBinder.newMapBinder(binder(), String.class, IReplCommand.class);
        commandBinder.addBinding("exit").to(Repl.ExitCommand.class).in(Singleton.class);

        bind(TerminalUserInterface.class).in(Singleton.class);
        bind(IEditor.class).to(TerminalUserInterface.class);
        bind(IDisplay.class).to(TerminalUserInterface.class);

        bind(InputStream.class).annotatedWith(Names.named("in")).toInstance(System.in);
        bind(OutputStream.class).annotatedWith(Names.named("out")).toInstance(System.out);
        bind(OutputStream.class).annotatedWith(Names.named("err")).toInstance(System.err);

        bind(new TypeLiteral<Consumer<String>>() { }).annotatedWith(Names.named("onSuccess"))
            .to(OnEvalSuccessHook.class).in(Singleton.class);
        bind(new TypeLiteral<Consumer<String>>() { }).annotatedWith(Names.named("onError"))
            .to(OnEvalErrorHook.class).in(Singleton.class);
        bind(IReplCommand.class).annotatedWith(Names.named("EvalCommand"))
            .to(SpoofaxEvaluationCommand.class).in(Singleton.class);
        bind(ICommandInvoker.class).to(SpoofaxCommandInvoker.class);
        bind(Repl.class).in(Singleton.class);
    }
}
