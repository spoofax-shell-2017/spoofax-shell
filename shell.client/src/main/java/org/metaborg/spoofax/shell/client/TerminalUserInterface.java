package org.metaborg.spoofax.shell.client;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Color;
import org.metaborg.core.completion.ICompletionService;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import jline.console.ConsoleReader;

/**
 * A terminal UI which is both an {@link IEditor} and an {@link IDisplay}.
 */
public class TerminalUserInterface implements IEditor, IDisplay {
    private ConsoleReader reader;
    private String prompt;
    private String continuationPrompt;
    private ArrayList<String> lines;
    private PrintWriter out;
    private PrintWriter err;

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
    @Inject
    public TerminalUserInterface(@Named("in") InputStream in, @Named("out") OutputStream out,
        @Named("err") OutputStream err) throws IOException {
        reader = new ConsoleReader(in, out);
        reader.setExpandEvents(false);
        reader.setHandleUserInterrupt(true);
        reader.setBellEnabled(true);
        this.out =
            new PrintWriter(new BufferedWriter(new OutputStreamWriter(out,
                                                                      Charset.forName("UTF-8"))));
        this.err =
            new PrintWriter(new BufferedWriter(new OutputStreamWriter(err,
                                                                      Charset.forName("UTF-8"))));

        setPrompt(coloredFg(Color.RED, "[In ]: "));
        setContinuationPrompt("[...]: ");

        lines = new ArrayList<>();
    }

    private String coloredFg(Color c, String s) {
        return Ansi.ansi().fg(c).a(s).reset().toString();
    }

    /**
     * Save this line as the end of the multi-line input.
     *
     * @param lastLine
     *            the line to save.
     */
    protected void saveLine(String lastLine) {
        lines.add(lastLine);
    }

    // -------------- IEditor --------------
    @Override
    public void setPrompt(String promptString) {
        prompt = promptString;
    }

    @Override
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
        input = lines.stream().collect(Collectors.joining("\n"));
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

    // -------------- IDisplay --------------
    @Override
    public void displayResult(String s) {
        out.println(s);
        out.flush();
    }

    @Override
    public void displayError(String s) {
        err.println(s);
        out.flush();
    }
}
