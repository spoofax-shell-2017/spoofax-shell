package org.metaborg.spoofax.shell.client.console;

import java.io.IOException;
import java.util.List;

/**
 * This interface represents the input history of a REPL session.
 */
public interface IInputHistory {

    /**
     * Append a new entry to the end of the input history.
     *
     * @param newEntry
     *            The new entry to append.
     */
    void append(String newEntry);

    /**
     * Get the most recent input history entry. Default implementation is equal to {@link #get(int)
     * get({@link #size()} - 1)}.
     *
     * @return The most recent history entry.
     */
    default String getMostRecent() {
        return get(size() - 1);
    }

    /**
     * Get a single history entry. Default implementation is equal to {@link #entries(int, int)
     * entries(index, index + 1)}.{@link List#get(int) get(0)}.
     *
     * @param index
     *            The index of the history entry.
     * @return The history entry at the given index.
     */
    default String get(int index) {
        return entries(index, index + 1).get(0);
    }

    /**
     * @return The amount of history entries currently stored.
     */
    int size();

    /**
     * Same as {@link #entries(int) entries(0)}.
     *
     * @return All entries, oldest first.
     */
    default List<String> allEntries() {
        return entries(0);
    }

    /**
     * Same as {@link #entries(int, int) entries(from, #size())}.
     *
     * @param from
     *            The index from which to return the history, inclusive.
     * @return The history entries from the specified index, oldest first.
     */
    default List<String> entries(int from) {
        return entries(from, size());
    }

    /**
     * Return the entries in the specified range.
     *
     * @param from
     *            The index from which to return the history, inclusive.
     * @param to
     *            The index to which to return the history, exclusive.
     * @return The entries in the specified range, oldest first.
     */
    List<String> entries(int from, int to);

    /**
     * Optionally supported operation. When implemented, the input history should be read from a
     * file.
     *
     * @throws IOException
     *             When an IO error occurs while reading the input history from the file system.
     */
    default void loadFromDisk() throws IOException {
    }

    /**
     * Optionally supported operation. When implemented, the input history should be written to a
     * file.
     *
     * @throws IOException
     *             When an IO error occurs while writing the input history to disk.
     */
    default void persistToDisk() throws IOException {
    }
}
