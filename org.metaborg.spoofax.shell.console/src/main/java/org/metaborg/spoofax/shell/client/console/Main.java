package org.metaborg.spoofax.shell.client.console;

import java.awt.Color;
import java.io.IOException;

import org.metaborg.spoofax.shell.client.console.impl.ConsoleRepl;
import org.metaborg.spoofax.shell.client.console.impl.ConsoleReplModule;
import org.metaborg.spoofax.shell.output.StyledText;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * This class launches a {@link ConsoleRepl}, a console based REPL.
 */
public final class Main {

    private Main() {
    }

    /**
     * Instantiates and runs a new {@link ConsoleRepl}.
     *
     * @param args
     *            The path to a language implementation location, using any URI supported by Apache
     *            VFS.
     * @throws IOException
     *             When an IO error occurs during execution.
     */
    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new ConsoleReplModule());
        IDisplay display = injector.getInstance(IDisplay.class);

        StyledText message = new StyledText(Color.BLUE, "Welcome to the ")
            .append(Color.GREEN, "Spoofax").append(Color.BLUE, " REPL");
        display.displayResult(message);

        ConsoleRepl repl = injector.getInstance(ConsoleRepl.class);
        repl.run();
    }
}
