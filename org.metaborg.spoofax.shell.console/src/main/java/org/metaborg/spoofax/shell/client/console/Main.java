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

    private Main() {
    }

    /**
     * Instantiates and runs a new {@link ConsoleRepl}.
     *
     * @param args
     *            The path to a language implementation location, using any URI supported by Apache
     *            VFS.
     */
    // TODO: make the argument work again.
    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new ConsoleReplModule());
        IDisplay display = injector.getInstance(IDisplay.class);

        StyledText message = new StyledText(Color.BLUE, "Welcome to the ")
            .append(new Style(Color.GREEN, Color.BLUE, true, true, true), "Spoofax")
            .append(Color.BLUE, " REPL");
        display.displayStyledText(message);

        ConsoleRepl repl = injector.getInstance(ConsoleRepl.class);
        repl.run();
    }
}
