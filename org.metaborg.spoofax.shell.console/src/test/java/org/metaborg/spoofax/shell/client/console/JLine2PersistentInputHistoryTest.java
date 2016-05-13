package org.metaborg.spoofax.shell.client.console;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.metaborg.spoofax.shell.client.console.TerminalUserInterfaceTest.ENTER;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.inject.Module;
import com.google.inject.name.Names;

import jline.console.history.FileHistory;
import jline.console.history.MemoryHistory;

/**
 * Tests the loading and saving of the history from and to the disk.
 */
public class JLine2PersistentInputHistoryTest extends JLine2InputHistoryTest {
    private File tempHistory;

    /**
     * Create a temporary file.
     *
     * @throws IOException
     *             When an IO error occurs while creating the file.
     */
    @Before
    public void setUp() throws IOException {
        tempHistory = File.createTempFile("test_history", ".tmp");
        setUp("asdf" + ENTER + "fdsa" + ENTER + ENTER);
    }

    @Override
    protected Module moduleOverride() {
        return b -> {
            // Bind the persistent history adapter.
            b.bind(JLine2InputHistory.class).to(JLine2PersistentInputHistory.class);
            // Bind jline's MemoryHistory so that we can test the adapter.
            b.bind(jline.console.history.MemoryHistory.class).toInstance(theDelegate);
            // Bind the temp file path.
            b.bindConstant().annotatedWith(Names.named("historyPath")).to(tempHistory.getPath());
        };
    }

    private JLine2PersistentInputHistory systemUnderTest() {
        // This cast is safe as the JLine2InputHistory class was bound to the persistent version.
        return (JLine2PersistentInputHistory) hist;
    }

    /**
     * Tests the switch of the delegate before and after loading.
     */
    @Test
    public void testDelegateSwitch() {
        try {
            assertEquals(theDelegate, systemUnderTest().delegate());
            assertTrue(systemUnderTest().delegate() instanceof MemoryHistory);
            systemUnderTest().loadFromDisk();
            assertTrue(systemUnderTest().delegate() instanceof FileHistory);
        } catch (IOException e) {
            fail("Should not happen");
        }
    }

    /**
     * Tests persistence through creating a temporary file.
     */
    @Test
    public void testPersistentHistory() {
        try {
            assertThat(hist.allEntries(), hasItems("asdf", "fdsa"));

            // Now load entries from disk. The file is empty, so allEntries should still have all
            // in-memory contents as before.
            systemUnderTest().loadFromDisk();
            assertThat(hist.allEntries(), hasItems("asdf", "fdsa"));

            hist.append("qwerty");
            hist.append("hjkl");

            // Persist to disk, check the file contents.
            systemUnderTest().persistToDisk();
            assertEquals("asdf\nfdsa\nqwerty\nhjkl\n", Files.toString(tempHistory, Charsets.UTF_8));

            // Load it back and check that the entries are there.
            systemUnderTest().loadFromDisk();
            assertThat(hist.allEntries(), hasItems("qwerty", "hjkl"));
        } catch (IOException e) {
            fail("Should not happen");
        }
    }

}
