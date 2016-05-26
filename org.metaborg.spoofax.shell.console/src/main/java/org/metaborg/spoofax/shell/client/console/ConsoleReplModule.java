package org.metaborg.spoofax.shell.client.console;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.metaborg.spoofax.shell.client.IDisplay;
import org.metaborg.spoofax.shell.client.IEditor;
import org.metaborg.spoofax.shell.client.IInputHistory;
import org.metaborg.spoofax.shell.client.console.history.JLine2InputHistory;
import org.metaborg.spoofax.shell.client.console.history.JLine2PersistentInputHistory;
import org.metaborg.spoofax.shell.core.Repl;
import org.metaborg.spoofax.shell.core.ReplModule;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

/**
 * Bindings for the console REPL.
 */
public class ConsoleReplModule extends ReplModule {

    private void configureUserInterface() {
        bind(Repl.class).to(ConsoleRepl.class);
        bind(IInputHistory.class).to(JLine2InputHistory.class);
        bind(JLine2InputHistory.class).to(JLine2PersistentInputHistory.class);

        bind(IEditor.class).to(TerminalUserInterface.class);
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

        configureUserInterface();
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
