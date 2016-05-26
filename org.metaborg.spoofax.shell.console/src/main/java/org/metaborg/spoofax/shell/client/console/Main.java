package org.metaborg.spoofax.shell.client.console;

import java.awt.Color;
import java.io.IOException;

import org.metaborg.core.MetaborgException;
import org.metaborg.spoofax.core.Spoofax;
import org.metaborg.spoofax.shell.client.IDisplay;
import org.metaborg.spoofax.shell.core.StyledText;

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
    public static void main(String[] args) throws IOException {
        try (Spoofax spoofax = new Spoofax()) {
            Injector injector = Guice.createInjector(new ConsoleReplModule());
            IDisplay display = injector.getInstance(IDisplay.class);

            StyledText message = new StyledText(Color.BLUE, "Welcome to the ")
                .append(Color.GREEN, "Spoofax").append(Color.BLUE, " REPL");
            display.displayResult(message);

            ConsoleRepl repl = injector.getInstance(ConsoleRepl.class);
            repl.run();
        } catch (MetaborgException e) {
            e.printStackTrace();
        }
    }
}
