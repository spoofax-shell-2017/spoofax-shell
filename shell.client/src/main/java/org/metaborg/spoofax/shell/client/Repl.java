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
            invoker.execute(input);
        }
    }
}
