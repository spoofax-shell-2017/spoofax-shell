package org.metaborg.spoofax.shell.client.console.impl.history;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.metaborg.spoofax.shell.client.console.IInputHistory;

import com.google.inject.Inject;

/**
 * An adapter for JLine2's {@link jline.console.history.History} implementation.
 */
public class JLine2InputHistory implements IInputHistory {
    protected final jline.console.ConsoleReader reader;
    private final jline.console.history.MemoryHistory delegateHist;

    /**
     * @param reader
     *            The {@link jline.console.ConsoleReader ConsoleReader} for setting the history
     *            being used.
     * @param delegateHist
     *            The jline2 history implementation that will be delegated to.
     */
    @Inject
    public JLine2InputHistory(jline.console.ConsoleReader reader,
                              jline.console.history.MemoryHistory delegateHist) {
        this.reader = reader;
        this.delegateHist = delegateHist;
        reader.setHistory(delegateHist);
    }

    /**
     * @return The delegate that is currently active. When the
     *         {@link jline.console.history.FileHistory FileHistory} delegate has not been loaded
     *         from disk, the {@link jline.console.history.MemoryHistory MemoryHistory} delegate is
     *         used instead.
     */
    protected jline.console.history.History delegate() {
        return delegateHist;
    }

    @Override
    public String get(int index) {
        return delegate().get(index).toString();
    }

    @Override
    public void append(String newEntry) {
        delegate().add(newEntry);
    }

    @Override
    public int size() {
        return delegate().size();
    }

    @Override
    public List<String> entries(int from, int to) {
        // @formatter:off
        return StreamSupport.stream(delegate().spliterator(), false)
            .skip(from)
            .limit(to - from)
            .map(entry -> entry.value().toString())
            .collect(Collectors.toList());
        // @formatter:on
    }
}
