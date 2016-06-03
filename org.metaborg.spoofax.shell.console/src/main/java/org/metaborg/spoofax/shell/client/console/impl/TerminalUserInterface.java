package org.metaborg.spoofax.shell.client.console.impl;

import static org.metaborg.spoofax.shell.client.console.AnsiColors.findClosest;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.fusesource.jansi.Ansi;
import org.metaborg.core.completion.ICompletionService;
import org.metaborg.core.style.IStyle;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.shell.client.IDisplay;
import org.metaborg.spoofax.shell.client.IEditor;
import org.metaborg.spoofax.shell.client.IInputHistory;
import org.metaborg.spoofax.shell.output.ISpoofaxResult;
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

        this.setPrompt(new StyledText(Color.GREEN, "[In ]: "));
        this.setContinuationPrompt(new StyledText("[...]: "));
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

    /**
     * Set the prompt to display.
     *
     * @param promptString
     *            The prompt string.
     */
    public void setPrompt(StyledText promptString) {
        prompt = promptString;
    }

    /**
     * Set the prompt to display when in multiline mode.
     *
     * @param promptString
     *            The prompt string.
     */
    public void setContinuationPrompt(StyledText promptString) {
        continuationPrompt = promptString;
    }

    // -------------- IEditor --------------
    @Override
    public String getInput() {
        String input = null;
        String lastLine;
        reader.setPrompt(ansi(prompt));
        try {
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
        } catch (IOException e) {
            err.println("An error occured: " + e.getMessage() + "\nExiting...");
            err.flush();
        }
        return input;
    }

    @Override
    public void setSpoofaxCompletion(ICompletionService<ISpoofaxParseUnit> completionService) {
        reader.addCompleter((buffer, cursor, candidates) -> {
            return cursor;
        });
    }

    @Override
    public IInputHistory history() {
        return hist;
    }

    // -------------- IDisplay --------------
    @Override
    public void displayResult(ISpoofaxResult<?> result) {
        out.println(ansi(result.styled()));
        out.flush();
    }

    @Override
    public void displayMessage(StyledText message) {
        err.println(ansi(message));
        err.flush();
    }

    private <T> void optional(T t, Function<T, Boolean> check, Consumer<T> accept) {
        if (check.apply(t)) {
            accept.accept(t);
        }
    }

    private String ansi(StyledText styled) {
        String text = styled.toString();

        Ansi ansi = Ansi.ansi();
        styled.getSource().forEach(e -> {
            String fragment = text.substring(e.region().startOffset(), e.region().endOffset() + 1);

            if (e.style() != null) {
                IStyle style = e.style();
                optional(style.color(),           (c) -> c != null, (c) -> ansi.fg(findClosest(c)));
                optional(style.backgroundColor(), (c) -> c != null, (c) -> ansi.bg(findClosest(c)));
                optional(style.bold(),           (c) -> c, (c) -> ansi.bold());
                optional(style.italic(),         (c) -> c, (c) -> ansi.a(Ansi.Attribute.ITALIC));
                optional(style.underscore(),     (c) -> c, (c) -> ansi.a(Ansi.Attribute.UNDERLINE));
                ansi.a(fragment).reset();
            } else {
                ansi.a(fragment);
            }
        });
        return ansi.toString();
    }
}
