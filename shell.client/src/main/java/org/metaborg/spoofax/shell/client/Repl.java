package org.metaborg.spoofax.shell.client;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Color;
import org.metaborg.core.MetaborgException;
import org.metaborg.spoofax.core.Spoofax;
import org.metaborg.spoofax.shell.commands.ICommandInvoker;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Interactive REPL (Read-Eval-Print Loop) which reads an expression or command typed in by the user
 * via an {@link IEditor}, executes them with a {@link ICommandInvoker} and prints it to an
 * {@link IDisplay}.
 */
public final class Repl {
    private ICommandInvoker invoker;
    private IEditor editor;
    private IDisplay display;

    private Repl(ICommandInvoker invoker, InputStream in, OutputStream out, OutputStream err)
        throws IOException {
        this.invoker = invoker;
        TerminalEditor editor = new TerminalEditor(in, out);
        editor.setPrompt(coloredFg(Color.RED, "[In ]: "));
        editor.setContinuationPrompt("[...]: ");
        this.editor = editor;
    }

    private String coloredFg(Color c, String s) {
        return Ansi.ansi().fg(c).a(s).reset().toString();
    }

    private void run() throws IOException {
        System.out.println(Ansi.ansi().a("Welcome to the ").bold().a("Spoofax").reset().a(" REPL"));
        String input;
        while (!(input = editor.getInput()).trim().equals("exit")) {
            if (input.length() == 0) {
                continue;
            }
            try {
                display.displayResult(invoker.execute(input));
            } catch (IOException | MetaborgException e) {
                display.displayError(e.getMessage());
            }
        }
    }

    /**
     * A simple test REPL.
     * 
     * @param args
     *            <languagepath> <projectpath>.
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: <languagepath> <projectpath>\n");
            return;
        }

        try (Spoofax spoofax = new Spoofax()) {
            SpoofaxTest test = new SpoofaxTest(spoofax, args[0], args[1]);
            new Repl(test, System.in, System.out, System.err).run();
        } catch (MetaborgException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
