package org.metaborg.spoofax.shell.client.console;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.metaborg.spoofax.shell.client.IInputHistory;
import org.metaborg.spoofax.shell.client.console.impl.history.JLine2InputHistory;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import jline.TerminalSupport;
import jline.console.ConsoleReader;

/**
 * Binds a {@link ConsoleReader} so that it can be used to simulate user input.
 */
public class UserInputSimulationModule extends AbstractModule {
    private final InputStream in;
    private final OutputStream out;
    private final ConsoleReader reader;

    /**
     * @param in
     *            The {@link InputStream}, which can contain simulated user input such as
     *            arrow-keys, return key, Ctrl+A ... Ctrl+Z, and other control codes.
     * @param out
     *            The {@link OutputStream}, which includes the prompts and verified user input.
     * @throws IOException
     *             When an IO error occurs upon construction of the {@link ConsoleReader}.
     */
    public UserInputSimulationModule(InputStream in, OutputStream out) throws IOException {
        // Set custom terminal to be "supported", so that control codes are captured.
        reader = new ConsoleReader(in, out, new TerminalSupport(true) {
        });

        this.in = in;
        this.out = out;
    }

    @Override
    protected void configure() {
        bind(IInputHistory.class).to(JLine2InputHistory.class);
        bind(ConsoleReader.class).toInstance(reader);
        bind(InputStream.class).annotatedWith(Names.named("in")).toInstance(in);
        bind(OutputStream.class).annotatedWith(Names.named("out")).toInstance(out);
        bind(OutputStream.class).annotatedWith(Names.named("err")).toInstance(out);
    }

}
