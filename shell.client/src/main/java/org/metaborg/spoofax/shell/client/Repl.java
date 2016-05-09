package org.metaborg.spoofax.shell.client;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Color;
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
    public ICommandInvoker invoker;
    public IEditor editor;
    public IDisplay display;

    public Repl(InputStream in, PrintStream out, PrintStream err) throws IOException {
        this(new SpoofaxCommandInvoker(), in, out, err);
    }

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

    public void setCommands() {
        SpoofaxCommandFactory fact = new SpoofaxCommandFactory(invoker);
        fact.createEvaluationCommand(display::displayError, display::displayResult);
    }

    public String coloredFg(Color c, String s) {
        return Ansi.ansi().fg(c).a(s).reset().toString();
    }

    public void run() throws IOException {
        System.out.println(Ansi.ansi().a("Welcome to the ").bold().a("Spoofax").reset().a(" REPL"));
        String input;
        while (!(input = editor.getInput()).trim().equals("exit")) {
            if (input.length() == 0) {
                continue;
            }
            invoker.execute(input);
        }
    }

    /**
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        new Repl(System.in, System.out, System.err).run();
    }
}
