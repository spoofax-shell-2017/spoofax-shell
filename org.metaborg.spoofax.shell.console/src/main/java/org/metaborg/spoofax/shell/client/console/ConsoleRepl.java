package org.metaborg.spoofax.shell.client.console;

import java.awt.Color;
import java.io.IOException;

import org.metaborg.spoofax.shell.client.IDisplay;
import org.metaborg.spoofax.shell.client.Repl;
import org.metaborg.spoofax.shell.core.StyledText;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * This class launches a console based REPL.
 *
 * It uses a GNU Readline-like input buffer with multiline editing capabilities, keyboard shortcuts
 * and persistent history. ANSI color codes are used to display colors.
 */
public final class ConsoleRepl {
    private static Injector injector;

    static {
        injector = Guice.createInjector(new ConsoleReplModule());
    }

    private ConsoleRepl() {
    }

    /**
     * @param args
     *            Unused.
     * @throws IOException
     *             When an IO error occurs during execution.
     */
    public static void main(String[] args) throws IOException {
        StyledText message = new StyledText(Color.BLUE, "Welcome to the ")
            .append(Color.GREEN, "Spoofax").append(Color.BLUE, " REPL");
        injector.getInstance(IDisplay.class).displayResult(message);
        injector.getInstance(Repl.class).run();
    }
}
