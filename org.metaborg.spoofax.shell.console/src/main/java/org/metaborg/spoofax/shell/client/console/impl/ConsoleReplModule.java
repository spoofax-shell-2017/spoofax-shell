package org.metaborg.spoofax.shell.client.console.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.metaborg.spoofax.shell.client.console.IDisplay;
import org.metaborg.spoofax.shell.client.console.IEditor;
import org.metaborg.spoofax.shell.client.console.IInputHistory;
import org.metaborg.spoofax.shell.client.console.commands.ExitCommand;
import org.metaborg.spoofax.shell.client.console.impl.history.JLine2InputHistory;
import org.metaborg.spoofax.shell.client.console.impl.history.JLine2PersistentInputHistory;
import org.metaborg.spoofax.shell.client.console.impl.hooks.ConsoleMessageHook;
import org.metaborg.spoofax.shell.client.console.impl.hooks.ConsoleResultHook;
import org.metaborg.spoofax.shell.commands.IReplCommand;
import org.metaborg.spoofax.shell.core.IRepl;
import org.metaborg.spoofax.shell.core.ReplModule;
import org.metaborg.spoofax.shell.hooks.IMessageHook;
import org.metaborg.spoofax.shell.hooks.IResultHook;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

/**
 * Bindings for the console REPL.
 */
public class ConsoleReplModule extends ReplModule {

    @Override
    protected void bindCommands(MapBinder<String, IReplCommand> commandBinder) {
        super.bindCommands(commandBinder);
        commandBinder.addBinding("exit").to(ExitCommand.class).in(Singleton.class);
    }

    /**
     * Binds the user interface implementations.
     */
    protected void bindUserInterface() {
        bind(IRepl.class).to(ConsoleRepl.class);
        bind(ConsoleRepl.class).in(Singleton.class);
        bind(IInputHistory.class).to(JLine2InputHistory.class);
        bind(JLine2InputHistory.class).to(JLine2PersistentInputHistory.class);

        bind(TerminalUserInterface.class).in(Singleton.class);
        bind(IEditor.class).to(TerminalUserInterface.class);
        bind(IDisplay.class).to(TerminalUserInterface.class);
        bind(IMessageHook.class).to(ConsoleMessageHook.class);
        bind(IResultHook.class).to(ConsoleResultHook.class);

        bind(InputStream.class).annotatedWith(Names.named("in")).toInstance(System.in);
        bind(OutputStream.class).annotatedWith(Names.named("out")).toInstance(System.out);
        bind(OutputStream.class).annotatedWith(Names.named("err")).toInstance(System.err);

        bindConstant().annotatedWith(Names.named("historyPath"))
            .to(System.getProperty("user.home") + "/.spoofax_history");
    }

    @Override
    protected void configure() {
        super.configure();
        bindUserInterface();
    }

    /**
     * TODO: Replace with "CheckedProvides" because IO in Provider method is bad practice, see:
     * https://github.com/google/guice/wiki/ThrowingProviders.
     *
     * @param in
     *            The {@link InputStream}.
     * @param out
     *            The {@link OutputStream}.
     * @return A {@link jline.console.ConsoleReader} with the given streams.
     * @throws IOException
     *             When an IO error occurs upon construction.
     */
    @Provides
    @Singleton
    protected jline.console.ConsoleReader provideConsoleReader(@Named("in") InputStream in,
                                                               @Named("out") OutputStream out)
        throws IOException {
        return new jline.console.ConsoleReader(in, out);
    }
}
