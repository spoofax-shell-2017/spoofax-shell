package org.metaborg.spoofax.shell.client.console;

import java.io.IOException;

import org.metaborg.spoofax.shell.core.Repl;
import org.metaborg.spoofax.shell.invoker.ICommandInvoker;

import com.google.inject.Inject;

/**
 * A console based REPL.
 *
 * It uses a GNU Readline-like input buffer with multiline editing capabilities, keyboard shortcuts
 * and persistent history. ANSI color codes are used to display colors.
 */
public class ConsoleRepl extends Repl {
    private final IEditor editor;
    private final IDisplay display;

    /**
     * Instantiates a new ConsoleRepl.
     *
     * @param editor
     *            The {@link IEditor} for receiving input.
     * @param display
     *            The {@link IDisplay} for displaying results.
     * @param invoker
     *            The {@link ICommandInvoker} for executing user input.
     */
    @Inject
    public ConsoleRepl(IEditor editor, IDisplay display, ICommandInvoker invoker) {
        super(invoker);
        this.editor = editor;
        this.display = display;
    }

    @Override
    public void run() {
        try {
            this.editor.history().loadFromDisk();

            String input;
            setRunning(true);
            while (running && (input = read()) != null) {
                print(eval(input));
            }

            this.editor.history().persistToDisk();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String read() {
        String input = null;
        try {
            input = editor.getInput();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return input;
    }

    @Override
    protected void print(String result) {
        display.displayResult(result);
    }

}
