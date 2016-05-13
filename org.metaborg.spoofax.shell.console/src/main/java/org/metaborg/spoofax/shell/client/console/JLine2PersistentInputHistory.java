package org.metaborg.spoofax.shell.client.console;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * An extension to the adapter for JLine2's History implementation. This adapter implementation
 * maintains two versions of JLine2's History classes: one that is initially used in-memory, and
 * another persistent History implementation which is used when {@link #loadFromDisk()} has been
 * called.
 *
 * Thus initially the in-memory version is used. When the input history has been loaded from disk,
 * the adapter switches to using a persistent history version which can be used to save back to disk
 * again using {@link #persistToDisk()}.
 */
public class JLine2PersistentInputHistory extends JLine2InputHistory {
    private final String filePath;
    private Optional<jline.console.history.FileHistory> delegateFileHist;

    /**
     * @param filePath
     *            The file path from which the history is loaded and to which the history is
     *            persisted.
     * @param reader
     *            The {@link jline.console.ConsoleReader ConsoleReader} for setting the correct
     *            delegate.
     * @param delegateHist
     *            The jline2 history implementation that will be delegated to as long as history has
     *            not been loaded from disk.
     */
    @Inject
    JLine2PersistentInputHistory(@Named("historyPath") String filePath,
                                 jline.console.ConsoleReader reader,
                                 jline.console.history.MemoryHistory delegateHist) {
        super(reader, delegateHist);
        this.filePath = filePath;
        reader.setHistory(delegateHist);
        delegateFileHist = Optional.empty();
    }

    /**
     * @return The delegate that is currently active. When the
     *         {@link jline.console.history.FileHistory FileHistory} delegate has not been loaded
     *         from disk, the {@link jline.console.history.MemoryHistory MemoryHistory} delegate is
     *         used instead.
     */
    protected jline.console.history.History delegate() {
        // orElse does not work on a subtype, but it does if you put flatMap in between.
        return delegateFileHist
            .flatMap((jline.console.history.History fileHist) -> Optional.of(fileHist))
            .orElse(super.delegate());
    }

    @Override
    public void loadFromDisk() throws IOException {
        final File histFile = new File(filePath);
        jline.console.history.FileHistory newDelegate =
            new jline.console.history.FileHistory(histFile);

        // Have to clear so that I can add all the old entries in front again...
        // newDelegate.clear();
        // delegate().forEach(e -> newDelegate.add(e.value().toString()));
        // newDelegate.load(histFile);

        reader.setHistory(newDelegate);
        delegateFileHist = Optional.of(newDelegate);
    }

    @Override
    public void persistToDisk() throws IOException {
        // Cannot use ifPresent, because I have to catch an IOException inside the lambda.
        if (delegateFileHist.isPresent()) {
            delegateFileHist.get().flush();
        }
    }
}
