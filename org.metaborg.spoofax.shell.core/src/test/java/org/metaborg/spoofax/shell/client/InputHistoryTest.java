package org.metaborg.spoofax.shell.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@link InputHistory}.
 */
public class InputHistoryTest {
    private static final int SIZE = 3;
    private static final String LAST = "qwerty";
    private static final String MIDDLE = "asdf";
    private static final String FIRST = "wasd";
    private InputHistory history;

    /**
     * Set up the test environment.
     */
    @Before
    public void setUp() {
        history = new InputHistory();
        history.append(LAST);
        history.append(MIDDLE);
        history.append(FIRST);
        // Reset is necessary after appending to move the index to the end of the list.
        history.reset();
    }

    /**
     * Test the {@link IInputHistory#size()}, {@link IInputHistory#append(String)} and
     * {@link IInputHistory#getMostRecent()} methods.
     */
    @Test
    public void testAppend() {
        String entry = "test";
        int size = history.size();
        assertEquals(SIZE, size);
        history.append(entry);
        assertEquals(size + 1, history.size());
        assertEquals(entry, history.getMostRecent());
    }

    /**
     * Test {@link IInputHistory#get(int)}.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGet() {
        assertEquals(MIDDLE, history.get(1));
        assertEquals(LAST, history.get(0));
        history.get(SIZE + 1);
    }

    /**
     * Test the stateful {@link IInputHistory#getPrevious()}, {@link IInputHistory#getNext()} and
     * {@link IInputHistory#reset()} methods.
     */
    @Test
    public void testGetStateful() {
        // Getting the next item when there is none should return the empty string.
        assertEquals("", history.getNext());

        assertEquals(FIRST, history.getPrevious());
        assertEquals(MIDDLE, history.getPrevious());
        assertEquals(LAST, history.getPrevious());
        // Getting the last item again should simply return the last item.
        assertEquals(LAST, history.getPrevious());

        // After a reset, the first item should be returned again.
        history.reset();
        assertEquals(FIRST, history.getPrevious());

        // Continue once more so that we can test getNext();
        assertEquals(MIDDLE, history.getPrevious());
        assertEquals(FIRST, history.getNext());
        // Again, the empty string should be returned now.
        assertEquals("", history.getNext());
    }

    /**
     * Test {@link InputHistory#allEntries()}, {@link InputHistory#entries(int)} and
     * {@link InputHistory#entries(int, int)}.
     */
    @Test
    public void testEntries() {
        assertThat(history.allEntries(), CoreMatchers.hasItems(FIRST, MIDDLE, LAST));
        // From index is inclusive.
        assertThat(history.entries(1), CoreMatchers.hasItems(MIDDLE, FIRST));
        // To index is exclusive.
        assertThat(history.entries(1, SIZE), CoreMatchers.hasItems(MIDDLE));

        // Add a new entry from our own interface.
        history.append("hjkl");
        assertThat(history.allEntries(),
                   CoreMatchers.hasItems(FIRST, MIDDLE, LAST, "hjkl"));
        assertThat(history.entries(1), CoreMatchers.hasItems(MIDDLE, FIRST, "hjkl"));
        // This one should not have changed.
        assertThat(history.entries(1, SIZE), CoreMatchers.hasItems(MIDDLE));
    }
}
