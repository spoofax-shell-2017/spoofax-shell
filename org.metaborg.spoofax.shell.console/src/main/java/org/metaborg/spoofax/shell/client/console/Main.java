package org.metaborg.spoofax.shell.client.console;

import java.awt.Color;

import org.metaborg.core.style.Style;
import org.metaborg.spoofax.shell.client.ConsoleReplModule;
import org.metaborg.spoofax.shell.client.IDisplay;
import org.metaborg.spoofax.shell.client.console.impl.ConsoleRepl;
import org.metaborg.spoofax.shell.output.StyledText;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * This class launches a {@link ConsoleRepl}, a console based REPL.
 */
public final class Main {
    private static final String ERROR = "Invalid commandline parameters: %s%nThe only argument "
                                        + "accepted is the path to a language implementation "
                                        + "location, using any filesystem supported by Apache VFS";

    private Main() {
    }

    private static StyledText error(String[] args) {
        StringBuilder invalidArgs = new StringBuilder();
        for (String arg : args) {
            invalidArgs.append(arg).append(", ");
        }
        // Remove the appended ", " from the string.
        invalidArgs.delete(invalidArgs.length() - 2, invalidArgs.length());
        return new StyledText(Color.RED, String.format(ERROR, invalidArgs.toString()));
    }

    /**
     * Instantiates and runs a new {@link ConsoleRepl}.
     *
     * @param args
     *            The path to a language implementation location, using any filesystem supported by
     *            Apache VFS.
     */
    public static void main(String[] args) {
        System.setProperty("org.apache.commons.logging.Log",
                "org.apache.commons.logging.impl.NoOpLog");

        Injector injector = Guice.createInjector(new ConsoleReplModule());
        IDisplay display = injector.getInstance(IDisplay.class);

        StyledText message = new StyledText(Color.BLUE, "Welcome to the ")
            .append(new Style(Color.GREEN, Color.BLUE, true, true, true), "Spoofax")
            .append(Color.BLUE, " REPL");
        display.displayStyledText(message);

        ConsoleRepl repl = injector.getInstance(ConsoleRepl.class);
        if (args.length == 1) {
            repl.runOnce(":load " + args[0]);
        } else if (args.length > 1) {
            display.displayStyledText(error(args));
        }

        repl.run();
    }
}
