package org.metaborg.spoofax.shell.client;

import java.io.IOException;

import org.fusesource.jansi.Ansi;
import org.metaborg.spoofax.shell.commands.CommandNotFoundException;
import org.metaborg.spoofax.shell.commands.ICommandInvoker;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * Interactive REPL (Read-Eval-Print Loop) which reads an expression or command typed in by the user
 * via an {@link IEditor}, executes them with a {@link ICommandInvoker} and prints it to an
 * {@link IDisplay}.
 */
public final class Repl {
    private static Injector injector;

    private ICommandInvoker invoker;
    private IEditor editor;
    private IDisplay display;

    private boolean running;

    /**
     * @param editor
     *            The {@link IEditor} for receiving input.
     * @param display
     *            The {@link IDisplay} for displaying results.
     * @param invoker
     *            The {@link ICommandInvoker} for executing user input.
     * @throws IOException
     *             when an IO error occurs.
     */
    @Inject
    public Repl(IEditor editor, IDisplay display, ICommandInvoker invoker) {
        this.editor = editor;
        this.display = display;
        this.invoker = invoker;
    }

    /**
     * Run the Repl, asking for input and sending it for execution.
     *
     * @throws IOException
     *             when an IO error occurs.
     */
    public void run() throws IOException {
        display.displayResult(Ansi.ansi().a("Welcome to the ").bold().a("Spoofax").reset().a(" REPL").toString());

        String input;
        running = true;
        while (running) {
            input = editor.getInput().trim();
            if (input.length() == 0) {
                continue;
            }
            try {
                invoker.execute(input);
            } catch (CommandNotFoundException e) {
                display.displayError(e.getMessage());
            }
        }
    }

    /**
     * @param args
     *            Unused.
     * @throws IOException
     *             when an IO error occurs.
     */
    public static void main(String[] args) throws IOException {
        injector = Guice.createInjector(new ReplModule());
        injector.getInstance(Repl.class).run();
    }
}
