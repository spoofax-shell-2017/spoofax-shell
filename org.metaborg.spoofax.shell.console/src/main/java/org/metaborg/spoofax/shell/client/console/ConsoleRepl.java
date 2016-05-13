package org.metaborg.spoofax.shell.client.console;

import java.awt.Color;
import java.io.IOException;

import org.metaborg.spoofax.shell.client.IDisplay;
import org.metaborg.spoofax.shell.client.Repl;
import org.metaborg.spoofax.shell.commands.StyledText;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class ConsoleRepl {
    private static Injector injector;

    static {
        injector = Guice.createInjector(new ConsoleReplModule());
    }

    /**
     * @param args
     *            Unused.
     * @throws IOException
     *             when an IO error occurs.
     */
    public static void main(String[] args) throws IOException {
        StyledText message = new StyledText(Color.BLUE, "Welcome to the ").append(Color.GREEN, "Spoofax")
                .append(Color.BLUE, " REPL");
        injector.getInstance(IDisplay.class).displayResult(message);
        injector.getInstance(Repl.class).run();
    }
}
