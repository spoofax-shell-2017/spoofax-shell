package org.metaborg.spoofax.shell.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.metaborg.core.completion.ICompletionService;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;

import static org.fusesource.jansi.Ansi.ansi;

import jline.console.ConsoleReader;

/**
 * An {@link IEditor} to be used in a terminal.
 */
public class TerminalEditor implements IEditor {
    ConsoleReader reader;
    String prompt;
    String continuationPrompt;
    ArrayList<String> lines;

    public TerminalEditor() throws IOException {
        this(System.in, System.out);
    }

    public TerminalEditor(InputStream in, OutputStream out) throws IOException {
        reader = new ConsoleReader(in, out);
        reader.setExpandEvents(false);
        reader.setHandleUserInterrupt(true);
        reader.setBellEnabled(true);
        setPrompt(">>> ");
        setContinuationPrompt("... ");
        lines = new ArrayList<>();
    }

    protected void saveLine(String lastLine) {
        lines.add(lastLine);
    }

    public void setPrompt(String promptString) {
        prompt = promptString;
    }

    public void setContinuationPrompt(String promptString) {
        continuationPrompt = promptString;
    }

    @Override
    public void setSpoofaxCompletion(ICompletionService<ISpoofaxParseUnit> completionService) {
        reader.addCompleter((buffer, cursor, candidates) -> {
            return cursor;
        });
    }

    @Override
    public String getInput() throws IOException {
        String input;
        String lastLine;
        reader.setPrompt(prompt);
        // While the input is not empty, keep asking.
        while ((lastLine = reader.readLine()) != null && lastLine.trim().length() > 0) {
            reader.flush();
            reader.setPrompt(continuationPrompt);
            saveLine(lastLine);
        }
        // Concat the strings with newlines inbetween
        input = lines.stream().reduce((left, right) -> left + "\n" + right).orElse("");
        // Clear the lines for next input.
        lines.clear();
        return input;
    }

    @Override
    public List<String> history() {
        // @formatter:off
        return StreamSupport.stream(reader.getHistory().spliterator(), false)
                            .map(entry -> entry.value().toString())
                            .collect(Collectors.toList());
        // @formatter:on
    }

    public static void main(String[] args) throws IOException {
        System.out.println(ansi().a("Welcome to the ").bold().a("Spoofax").reset().a(" REPL"));
        String input = "";
        IEditor ed = new TerminalEditor();
        while (!(input = ed.getInput()).trim().equals("exit")) {
            System.out.println("User typed in \"" + input + '"');
        }
    }
}
