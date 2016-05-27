package org.metaborg.spoofax.shell.client.console.impl.history;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.metaborg.spoofax.shell.client.console.TerminalUserInterfaceTest.ENTER;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Supplier;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.metaborg.spoofax.shell.client.console.TerminalUserInterfaceTest;
import org.metaborg.spoofax.shell.client.console.UserInputSimulationModule;
import org.metaborg.spoofax.shell.client.console.impl.TerminalUserInterface;
import org.metaborg.spoofax.shell.output.StyledText;
import org.mockito.Mockito;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

/**
 * Tests the JLine2 history adapter. It tests the interplay between our interface and theirs: first
 * history entries are added by user-simulation in {@link #setUp(String)}, and then the test
 * operates on it via our interface, to see if it correctly implements an adapter towards the
 * outside.
 */
public class JLine2InputHistoryTest {
    protected JLine2InputHistory hist;
    protected jline.console.history.MemoryHistory theDelegate;
    protected ByteArrayInputStream in;
    protected ByteArrayOutputStream out;

    protected static final int HIST_GET_OUT_OF_BOUND_LEFT = -1;
    protected static final int HIST_GET_OUT_OF_BOUND_RIGHT = 2;

    protected static final int HIST_SIZE_BEFORE_APPEND = 2;
    protected static final int HIST_SIZE_AFTER_APPEND = 3;

    protected static final int HIST_ENTRIES_FROM_INDEX = 1;
    protected static final int HIST_ENTRIES_TO_INDEX = 3;

    /**
     * Setup and inject the {@link InputStream}s and {@link OutputStream}s.
     *
     * @param inputString
     *            The simulated user input.
     * @throws IOException
     *             When an IO error occurs upon construction of the
     *             {@link jline.console.ConsoleReader ConsoleReader}.
     */
    public void setUp(String inputString) throws IOException {
        in = new ByteArrayInputStream(inputString.getBytes("UTF-8"));
        out = new ByteArrayOutputStream();
        theDelegate = Mockito.spy(new jline.console.history.MemoryHistory());
        Injector injector = Guice.createInjector(Modules
            .override(new UserInputSimulationModule(in, out)).with(moduleOverride()));
        hist = injector.getInstance(JLine2InputHistory.class);
        // We use this just to process the input and put stuff in the history.
        TerminalUserInterface ui = injector.getInstance(TerminalUserInterface.class);
        ui.setPrompt(new StyledText(TerminalUserInterfaceTest.PROMPT));
        ui.setContinuationPrompt(new StyledText(TerminalUserInterfaceTest.CONT_PROMPT));
        ui.getInput();
    }

    /**
     * @return The bindings that override or extend those of the {@link UserInputSimulationModule}.
     */
    protected Module moduleOverride() {
        return b -> {
            // Override ConsoleReplModule to use JLine2InputHistory, instead of the persistent
            // version.
            b.bind(JLine2InputHistory.class);
            // Bind JLine's MemoryHistory so that we can test the adapter.
            b.bind(jline.console.history.MemoryHistory.class).toInstance(theDelegate);
        };
    }

    private static void assertOutOfBoundsException(Supplier<?> fun) {
        try {
            fun.get();
            fail("IndexOutOfBoundsException expected, but did not get thrown");
        } catch (IndexOutOfBoundsException e) {
        }
    }

    /**
     * Tests whether the {@link JLine2InputHistory#get(int)} method correctly returns entries
     * entered through simulated input.
     */
    @Test
    public void testGet() {
        try {
            setUp("asdf" + ENTER + "fdsa" + ENTER);
            Mockito.verify(theDelegate, Mockito.times(2)).add(Mockito.anyString());
            assertEquals("asdf", hist.get(0));
            assertEquals("fdsa", hist.get(1));
            assertOutOfBoundsException(() -> hist.get(HIST_GET_OUT_OF_BOUND_LEFT));
            assertOutOfBoundsException(() -> hist.get(HIST_GET_OUT_OF_BOUND_RIGHT));
        } catch (IOException e) {
            fail("Should not happen");
        }
    }

    /**
     * Tests whether the {@link JLine2InputHistory#append(String)} method correctly adds a new entry
     * via our own interface. First tests on user-simulated input for two entries, and tests whether
     * the entry at index 2 is out of bounds. Then a new entry is appended via our adapter, and the
     * entry that was previously out of bound should now return the newly added entry.
     */
    @Test
    public void testAppend() {
        try {
            setUp("asdf" + ENTER + "fdsa" + ENTER);
            Mockito.verify(theDelegate, Mockito.times(2)).add(Mockito.anyString());
            assertEquals("asdf", hist.get(0));
            assertEquals("fdsa", hist.get(1));
            assertOutOfBoundsException(() -> hist.get(HIST_GET_OUT_OF_BOUND_LEFT));
            assertOutOfBoundsException(() -> hist.get(HIST_GET_OUT_OF_BOUND_RIGHT));
            hist.append("qwerty");
            Mockito.verify(theDelegate).add("qwerty");

            // This is the same as hist.get(HIST_GET_OUT_OF_BOUND_RIGHT)
            assertEquals("qwerty", hist.get(2));
            assertOutOfBoundsException(() -> hist.get(HIST_GET_OUT_OF_BOUND_RIGHT + 1));
        } catch (IOException e) {
            fail("Should not happen");
        }
    }

    /**
     * Tests whether the {@link JLine2InputHistory#getMostRecent()} method correctly returns the
     * most recent entry entered through simulated input, and then an entry appended via our
     * interface.
     */
    @Test
    public void testGetMostRecent() {
        try {
            setUp("asdf" + ENTER + "fdsa" + ENTER);
            Mockito.verify(theDelegate, Mockito.times(2)).add(Mockito.anyString());
            assertEquals("fdsa", hist.getMostRecent());
            hist.append("qwerty");
            assertEquals(HIST_SIZE_AFTER_APPEND, hist.size());
        } catch (IOException e) {
            fail("Should not happen");
        }
    }

    /**
     * Tests whether the size is reported correctly after input-simulation, and whether it is
     * reported correctly after appending a new entry via our own adapter interface.
     */
    @Test
    public void testSize() {
        try {
            setUp("asdf" + ENTER + "fdsa" + ENTER);
            Mockito.verify(theDelegate, Mockito.times(2)).add(Mockito.anyString());
            assertEquals(HIST_SIZE_BEFORE_APPEND, hist.size());
            hist.append("qwerty");
            assertEquals(HIST_SIZE_AFTER_APPEND, hist.size());
        } catch (IOException e) {
            fail("Should not happen");
        }
    }

    /**
     * Tests the following methods: {@link JLine2InputHistory#allEntries()} ,
     * {@link JLine2InputHistory#entries(int)}, {@link JLine2InputHistory#entries(int, int)}.
     */
    @Test
    public void testEntries() {
        try {
            setUp("asdf" + ENTER + "fdsa" + ENTER + "qwerty" + ENTER + "uiop" + ENTER);
            assertThat(hist.allEntries(), CoreMatchers.hasItems("asdf", "fdsa", "qwerty", "uiop"));
            // From index is inclusive.
            assertThat(hist.entries(HIST_ENTRIES_FROM_INDEX),
                       CoreMatchers.hasItems("fdsa", "qwerty", "uiop"));
            // To index is exclusive.
            assertThat(hist.entries(HIST_ENTRIES_FROM_INDEX, HIST_ENTRIES_TO_INDEX),
                       CoreMatchers.hasItems("fdsa", "qwerty"));
            assertThat(hist.entries(HIST_ENTRIES_FROM_INDEX, HIST_ENTRIES_TO_INDEX - 1),
                       CoreMatchers.hasItems("fdsa"));

            // Add a new entry from our own interface.
            hist.append("hjkl");
            assertThat(hist.allEntries(),
                       CoreMatchers.hasItems("asdf", "fdsa", "qwerty", "uiop", "hjkl"));
            assertThat(hist.entries(HIST_ENTRIES_FROM_INDEX),
                       CoreMatchers.hasItems("fdsa", "qwerty", "uiop", "hjkl"));
            // This one should not have changed.
            assertThat(hist.entries(HIST_ENTRIES_FROM_INDEX, HIST_ENTRIES_TO_INDEX),
                       CoreMatchers.hasItems("fdsa", "qwerty"));
        } catch (IOException e) {
            fail("Should not happen");
        }
    }
}
