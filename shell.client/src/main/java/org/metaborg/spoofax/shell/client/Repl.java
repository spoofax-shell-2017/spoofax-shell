package org.metaborg.spoofax.shell.client;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Color;
import org.metaborg.spoofax.shell.commands.CommandNotFoundException;
import org.metaborg.spoofax.shell.commands.ICommandInvoker;
import org.metaborg.spoofax.shell.commands.SpoofaxCommandFactory;
import org.metaborg.spoofax.shell.commands.SpoofaxCommandInvoker;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Interactive REPL (Read-Eval-Print Loop) which reads an expression or command typed in by the user
 * via an {@link IEditor}, executes them with a {@link ICommandInvoker} and prints it to an
 * {@link IDisplay}.
 */
public final class Repl {
    private ICommandInvoker invoker;
    private IEditor editor;
    private IDisplay display;
    private boolean running;

    /**
     * @param in
     *            The {@link InputStream} from which to read user input.
     * @param out
     *            The {@link PrintStream} to write results to.
     * @param err
     *            The {@link PrintStream} to write errors to.
     * @throws IOException
     *             when an IO error occurs.
     */
    public Repl(InputStream in, PrintStream out, PrintStream err) throws IOException {
        this(new SpoofaxCommandInvoker(), in, out, err);
    }

    /**
     * @param invoker
     *            The {@link ICommandInvoker} for executing user input.
     * @param in
     *            The {@link InputStream} from which to read user input
     * @param out
     *            The {@link PrintStream} to write results to.
     * @param err
     *            The {@link PrintStream} to write errors to.
     * @throws IOException
     *             when an IO error occurs.
     */
    public Repl(ICommandInvoker invoker, InputStream in, OutputStream out, OutputStream err)
        throws IOException {
        this.invoker = invoker;
        TerminalUserInterface ui = new TerminalUserInterface(in, out, err);
        ui.setPrompt(coloredFg(Color.RED, "[In ]: "));
        ui.setContinuationPrompt("[...]: ");
        this.editor = ui;
        this.display = ui;
        setCommands();
    }

    private void setCommands() {
        SpoofaxCommandFactory fact = new SpoofaxCommandFactory(invoker);
        fact.createEvaluationCommand(display::displayError, display::displayResult);
        invoker.addCommand("exit", "Exit the REPL session.", () -> running = false);
    }

    private String coloredFg(Color c, String s) {
        return Ansi.ansi().fg(c).a(s).reset().toString();
    }

    /**
     * Run the Repl, asking for input and sending it for execution.
     *
     * @throws IOException
     *             when an IO error occurs.
     */
    public void run() throws IOException {
        System.out.println(Ansi.ansi().a("Welcome to the ").bold().a("Spoofax").reset().a(" REPL"));
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
        new Repl(System.in, System.out, System.err).run();
    }
}
