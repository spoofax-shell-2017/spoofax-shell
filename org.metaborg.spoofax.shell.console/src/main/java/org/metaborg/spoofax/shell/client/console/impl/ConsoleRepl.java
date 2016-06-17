package org.metaborg.spoofax.shell.client.console.impl;

import java.awt.Color;
import java.io.IOException;

import org.metaborg.spoofax.shell.client.IDisplay;
import org.metaborg.spoofax.shell.client.IRepl;
import org.metaborg.spoofax.shell.invoker.CommandNotFoundException;
import org.metaborg.spoofax.shell.invoker.ICommandInvoker;
import org.metaborg.spoofax.shell.output.StyledText;

import com.google.inject.Inject;

/**
 * A console based REPL.
 *
 * It uses a GNU Readline-like input buffer with multiline editing capabilities, keyboard shortcuts
 * and persistent history. ANSI color codes are used to display colors.
 */
public class ConsoleRepl implements IRepl {
    private final ICommandInvoker invoker;
    private final TerminalUserInterface iface;
    private final IDisplay display;
    private boolean running;

    /**
     * Instantiates a new ConsoleRepl.
     *
     * @param iface
     *            The {@link TerminalUserInterface} to retrieve user input from.
     * @param display
     *            The {@link IDisplay} for displaying the results.
     * @param invoker
     *            The {@link ICommandInvoker} for executing user input.
     */
    @Inject
    public ConsoleRepl(TerminalUserInterface iface, IDisplay display, ICommandInvoker invoker) {
        this.invoker = invoker;
        this.iface = iface;
        this.display = display;
    }

    /**
     * Whether or not to keep running the loop.
     *
     * @param running
     *            {@code true} to keep the loop running, {@code false} to stop it.
     *
     * @see ConsoleRepl#run()
     */
    public void setRunning(boolean running) {
        this.running = running;
    }

    /**
     * Evaluate {@code input}, without having to enter the main loop in {@link #run()}. This is
     * useful to initialize some state before control is handed to the user.
     *
     * @param input
     *            The input to evaluate.
     */
    public void runOnce(String input) {
        try {
            eval(input).accept(display);
        } catch (CommandNotFoundException e) {
            this.display.displayStyledText(new StyledText(Color.RED, e.getMessage()));
        }
    }

    /**
     * Run {@link IRepl#eval(String)} in a loop, for as long as {@code running} is {@code true}.
     *
     * @see IRepl#eval(String)
     * @see ConsoleRepl#setRunning(boolean)
     */
    public void run() {
        try {
            this.iface.history().loadFromDisk();

            String input;
            setRunning(true);
            while (running && (input = this.iface.getInput()) != null) {
                runOnce(input);
            }

            this.iface.history().persistToDisk();
        } catch (IOException e) {
            this.display.displayStyledText(new StyledText(Color.RED, e.getMessage()));
        }
    }

    @Override
    public ICommandInvoker getInvoker() {
        return this.invoker;
    }

}
