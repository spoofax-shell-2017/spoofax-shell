package org.metaborg.spoofax.shell.client.console;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.metaborg.spoofax.shell.client.IDisplay;
import org.metaborg.spoofax.shell.client.IEditor;
import org.metaborg.spoofax.shell.client.Repl;
import org.metaborg.spoofax.shell.client.ReplModule;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

/**
 * Bindings for the console repl.
 */
public class ConsoleReplModule extends ReplModule {

    private void configureUserInterface() {
        bind(TerminalUserInterface.class).in(Singleton.class);
        bind(IEditor.class).to(TerminalUserInterface.class);
        bind(IDisplay.class).to(TerminalUserInterface.class);

        bind(InputStream.class).annotatedWith(Names.named("in")).toInstance(System.in);
        bind(OutputStream.class).annotatedWith(Names.named("out")).toInstance(System.out);
        bind(OutputStream.class).annotatedWith(Names.named("err")).toInstance(System.err);

        bindConstant().annotatedWith(Names.named("historyPath"))
            .to(System.getProperty("user.dir") + "/.spoofax-shell_history");
    }

    @Override
    protected void configure() {
        super.configure();

        configureUserInterface();

        bind(Repl.class).in(Singleton.class);
    }

    /**
     * TODO: Replace with "CheckedProvides" because IO in Provider method is bad practice, see:
     * https://github.com/google/guice/wiki/ThrowingProviders.
     *
     * @param in
     *            The {@link InputStream}.
     * @param out
     *            The {@link OutputStream}.
     * @return a {@link ConsoleReader} with the given streams.
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
