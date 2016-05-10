package org.metaborg.spoofax.shell.client.console;

import static org.metaborg.spoofax.shell.client.console.AnsiColors.findClosest;

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
import org.metaborg.core.completion.ICompletionService;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.shell.client.IDisplay;
import org.metaborg.spoofax.shell.client.IEditor;
import org.metaborg.spoofax.shell.commands.StyledText;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import jline.console.ConsoleReader;

/**
 * A terminal UI which is both an {@link IEditor} and an {@link IDisplay}.
 */
public class TerminalUserInterface implements IEditor, IDisplay {
    private ConsoleReader reader;
    private StyledText prompt;
    private StyledText continuationPrompt;
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

        lines = new ArrayList<>();
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
    public void setPrompt(StyledText promptString) {
        prompt = promptString;
    }

    @Override
    public void setContinuationPrompt(StyledText promptString) {
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
        reader.setPrompt(ansi(prompt));
        // While the input is not empty, keep asking.
        while ((lastLine = reader.readLine()) != null && lastLine.trim().length() > 0) {
            reader.flush();
            reader.setPrompt(ansi(continuationPrompt));
            saveLine(lastLine);
        }
        // Concat the strings with newlines inbetween
        input = lastLine == null ? null : lines.stream().collect(Collectors.joining("\n"));
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
    public void displayResult(StyledText s) {
        out.println(ansi(s));
        out.flush();
    }

    @Override
    public void displayError(StyledText s) {
        err.println(ansi(s));
        out.flush();
    }

    private String ansi(StyledText text) {
        Ansi ansi = Ansi.ansi();
        text.getSource().stream()
        .forEach(e -> {
            if (e.style() != null && e.style().color() != null) {
                ansi.fg(findClosest(e.style().color())).a(e.fragment()).reset();
            } else {
                ansi.a(e.fragment());
            }
        });
        return ansi.toString();
    }
}
