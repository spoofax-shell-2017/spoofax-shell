package org.metaborg.spoofax.shell.core;

import org.metaborg.core.MetaborgException;
import org.metaborg.spoofax.shell.invoker.CommandNotFoundException;
import org.metaborg.spoofax.shell.invoker.ICommandInvoker;

import com.google.inject.Inject;

/**
 * An interactive REPL (Read-Eval-Print Loop) which reads an expression or command typed in by the
 * user, executes it with an {@link ICommandInvoker} and sends the result to
 * {@link org.metaborg.spoofax.shell.hooks.Hook} for processing.
 */
public abstract class Repl {
    protected final ICommandInvoker invoker;
    protected boolean running;

    /**
     * Instantiates a new REPL.
     *
     * @param invoker
     *            The {@link ICommandInvoker} for executing user input.
     */
    @Inject
    public Repl(ICommandInvoker invoker) {
        this.invoker = invoker;
    }

    /**
     * Whether or not to keep running the loop.
     *
     * @param running
     *            {@code true} to keep the loop running, {@code} false to stop it.
     *
     * @see #run()
     */
    public void setRunning(boolean running) {
        this.running = running;
    }

    /**
     * Run {@code eval(read())} in a loop, for as long as {@code running} is {@code true}.
     *
     * @throws MetaborgException
     *             When something goes wrong during execution.
     * @throws CommandNotFoundException
     *             When the command could not be found.
     *
     * @see #read()
     * @see #eval(String)
     * @see #setRunning(boolean)
     */
    public void run() throws MetaborgException, CommandNotFoundException {
        setRunning(true);
        while (this.running) {
            eval(read());
        }
    }

    /**
     * Read user input. Depending on the implementation, this can either be blocking or
     * non-blocking. Note that the combination of a non-blocking read and the default {@link #run()}
     * implementation result in an infinite loop.
     *
     * @return The entered input.
     */
    protected abstract String read();

    /**
     * Evaluate the input. This default implementation strips whitespace, skips over empty strings
     * and uses an {@link ICommandInvoker}.
     *
     * @param input
     *            The input to send for evaluation.
     * @throws MetaborgException
     *             When something goes wrong during execution.
     * @throws CommandNotFoundException
     *             When the command could not be found.
     */
    protected void eval(String input) throws MetaborgException, CommandNotFoundException {
        input = input.trim();
        if (input.length() == 0) {
            return;
        }
        invoker.execute(input);
    }

}
