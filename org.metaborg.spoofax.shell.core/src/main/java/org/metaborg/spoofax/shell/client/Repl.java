package org.metaborg.spoofax.shell.client;

import java.awt.Color;
import java.io.IOException;

import org.metaborg.spoofax.shell.invoker.CommandNotFoundException;
import org.metaborg.spoofax.shell.invoker.ICommandInvoker;
import org.metaborg.spoofax.shell.output.StyledText;

import com.google.inject.Inject;

/**
 * Interactive REPL (Read-Eval-Print Loop) which reads an expression or command typed in by the user
 * via an {@link IEditor}, executes them with a {@link ICommandInvoker} and prints it to an
 * {@link IDisplay}.
 */
public class Repl {
    private final ICommandInvoker invoker;
    private final IEditor editor;
    private final IDisplay display;

    private boolean running;

    /**
     * Instantiates a new REPL.
     *
     * @param editor
     *            The {@link IEditor} for receiving input.
     * @param display
     *            The {@link IDisplay} for displaying results.
     * @param invoker
     *            The {@link ICommandInvoker} for executing user input.
     */
    @Inject
    public Repl(IEditor editor, IDisplay display, ICommandInvoker invoker) {
        this.editor = editor;
        this.display = display;
        this.invoker = invoker;

        // TODO: this does not really belong here.
        this.editor.setPrompt(new StyledText(Color.GREEN, "[In ]: "));
        this.editor.setContinuationPrompt(new StyledText("[...]: "));
    }

    /**
     * Run the REPL, asking for input and sending it for execution.
     *
     * @throws IOException
     *             When an IO error occurs.
     */
    public void run() throws IOException {
        String input;
        setRunning(true);
        while (running && (input = editor.getInput()) != null) {
            input = input.trim();
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
     * @param running the running to set
     */
    public void setRunning(boolean running) {
        this.running = running;
    }
}
