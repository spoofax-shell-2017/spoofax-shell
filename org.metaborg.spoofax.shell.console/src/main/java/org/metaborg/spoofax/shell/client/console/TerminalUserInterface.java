package org.metaborg.spoofax.shell.client.console;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.fusesource.jansi.Ansi;
import org.metaborg.core.completion.ICompletionService;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.shell.client.IDisplay;
import org.metaborg.spoofax.shell.client.IEditor;
import org.metaborg.spoofax.shell.client.IInputHistory;
import org.metaborg.spoofax.shell.output.StyledText;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import jline.console.ConsoleReader;

/**
 * A terminal UI which is both an {@link IEditor} and an {@link IDisplay}.
 */
public class TerminalUserInterface implements IEditor, IDisplay {
    private final ConsoleReader reader;
    private final ArrayList<String> lines;
    private final PrintWriter out;
    private final PrintWriter err;
    private final IInputHistory hist;
    private StyledText prompt;
    private StyledText continuationPrompt;

    /**
     * @param reader
     *            The JLine2 {@link ConsoleReader} used to get input.
     * @param out
     *            The {@link PrintStream} to write results to.
     * @param err
     *            The {@link PrintStream} to write errors to.
     * @param hist
     *            The input history adapter for JLine2.
     * @throws IOException
     *             When an IO error occurs.
     */
    @Inject
    public TerminalUserInterface(ConsoleReader reader, @Named("out") OutputStream out,
                                 @Named("err") OutputStream err, IInputHistory hist)
                                     throws IOException {
        this.reader = reader;
        this.hist = hist;
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
     * Save this line as the end of the multiline input.
     *
     * @param lastLine
     *            The line to save.
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
        // Concatenate the strings with newlines in between.
        input = lastLine == null ? null : lines.stream().collect(Collectors.joining("\n"));
        // Clear the lines for next input.
        lines.clear();
        return input;
    }

    @Override
    public IInputHistory history() {
        return hist;
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
        err.flush();
    }

    private String ansi(StyledText text) {
        Ansi ansi = Ansi.ansi();
        text.getSource().stream()
        .forEach(e -> {
            if (e.style() != null && e.style().color() != null) {
                    ansi.fg(AnsiColors.findClosest(e.style().color())).a(e.fragment()).reset();
            } else {
                ansi.a(e.fragment());
            }
        });
        return ansi.toString();
    }
}
