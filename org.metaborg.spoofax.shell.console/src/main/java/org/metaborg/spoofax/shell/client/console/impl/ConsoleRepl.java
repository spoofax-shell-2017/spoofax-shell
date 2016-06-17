package org.metaborg.spoofax.shell.client.console.impl;

import java.awt.Color;
import java.io.IOException;

import org.metaborg.core.MetaborgException;
import org.metaborg.spoofax.shell.client.IRepl;
import org.metaborg.spoofax.shell.client.IResultVisitor;
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
    private final IResultVisitor visitor;
    private boolean running;

    /**
     * Instantiates a new ConsoleRepl.
     *
     * @param iface
     *            The {@link TerminalUserInterface} to retrieve user input from.
     * @param visitor
     *            The {@link IResultVisitor} for visiting the results.
     * @param invoker
     *            The {@link ICommandInvoker} for executing user input.
     */
    @Inject
    public ConsoleRepl(TerminalUserInterface iface, IResultVisitor visitor,
                       ICommandInvoker invoker) {
        this.invoker = invoker;
        this.iface = iface;
        this.visitor = visitor;
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
     * Run {@link IRepl#eval(String)} in a loop, for as long as {@code running} is {@code true}.
     *
     * @throws MetaborgException
     *             When something goes wrong during execution.
     * @throws CommandNotFoundException
     *             When the command could not be found.
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
                try {
                    eval(input).accept(visitor);
                } catch (CommandNotFoundException | MetaborgException e) {
                    this.visitor.visitMessage(new StyledText(Color.RED, e.getMessage()));
                }
            }

            this.iface.history().persistToDisk();
        } catch (IOException e) {
            this.visitor.visitMessage(new StyledText(Color.RED, e.getMessage()));
        }
    }

    @Override
    public ICommandInvoker getInvoker() {
        return this.invoker;
    }

}
