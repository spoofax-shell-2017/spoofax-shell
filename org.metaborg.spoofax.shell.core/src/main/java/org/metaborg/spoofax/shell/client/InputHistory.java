package org.metaborg.spoofax.shell.client;

import java.util.LinkedList;
import java.util.List;

/**
 * A volatile implementation of {@link IInputHistory}, to be used by clients that place no special
 * demands on their history.
 * <p>
 * This implementation is backed by a {@link LinkedList}.
 */
public class InputHistory implements IInputHistory {
    private final List<String> history;
    private int index;

    /**
     * Instantiate a new InputHistory.
     */
    public InputHistory() {
        this.history = new LinkedList<>();
        reset();
    }

    @Override
    public void append(String newEntry) {
        this.history.add(newEntry);
    }

    private String current() {
        if (this.index >= size()) {
            return "";
        }
        return this.history.get(this.index);
    }

    @Override
    public String getPrevious() {
        if (this.index <= 0) {
            return current();
        }
        this.index -= 1;
        return current();
    }

    @Override
    public String getNext() {
        if (this.index >= size()) {
            return current();
        }
        this.index += 1;
        return current();
    }

    @Override
    public void reset() {
        this.index = size();
    }

    @Override
    public int size() {
        return this.history.size();
    }

    @Override
    public List<String> entries(int from, int to) {
        return this.history.subList(from, to);
    }
}
