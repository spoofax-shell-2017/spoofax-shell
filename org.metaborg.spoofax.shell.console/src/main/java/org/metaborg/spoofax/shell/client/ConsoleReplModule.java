package org.metaborg.spoofax.shell.client;

import java.io.InputStream;
import java.io.OutputStream;

import org.metaborg.spoofax.shell.ReplModule;
import org.metaborg.spoofax.shell.client.console.commands.ExitCommand;
import org.metaborg.spoofax.shell.client.console.impl.ConsoleRepl;
import org.metaborg.spoofax.shell.client.console.impl.TerminalUserInterface;
import org.metaborg.spoofax.shell.client.console.impl.history.JLine2InputHistory;
import org.metaborg.spoofax.shell.client.console.impl.history.JLine2PersistentInputHistory;
import org.metaborg.spoofax.shell.commands.IReplCommand;

import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;
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
        bind(IDisplay.class).to(TerminalUserInterface.class);

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
}
