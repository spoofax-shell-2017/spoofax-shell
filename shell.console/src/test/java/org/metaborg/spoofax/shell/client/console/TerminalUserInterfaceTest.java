package org.metaborg.spoofax.shell.client.console;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Test;
import org.metaborg.spoofax.shell.client.console.TerminalUserInterface;

import com.google.inject.Guice;
import com.google.inject.Injector;

import jline.console.ConsoleReader;

/**
 * Tests the {@link TerminalUserInterface} by simulating user input.
 */
public class TerminalUserInterfaceTest {
    private TerminalUserInterface ui;
    private static final String PROMPT = "<TEST>";
    private static final String CONT_PROMPT = ".TEST.";
    private static final char C_A = '\001';
    public static final char C_D = '\004';
    private static final char C_E = '\005';
    private static final char ESC = '\033';
    private static final String ARROW_UP = ESC + "[A";
    private static final String ARROW_DOWN = ESC + "[B";
    public static final char ENTER = '\r';
    private static final String ASDF_QWERTY = /* >>> */ "asdf" + ENTER
    /* ... */ + ENTER
    /* >>> */ + "qwerty" + ENTER
    /* ... */ + ENTER;
    private ByteArrayInputStream in;
    private ByteArrayOutputStream out;

    /**
     * Setup and inject the {@link InputStream}s and {@link OutputStream}s.
     *
     * @param inputString
     *            The simulated user input.
     * @throws IOException
     *             When an IO error occurs upon construction of the {@link ConsoleReader}.
     */
    public void setUp(String inputString) throws IOException {
        in = new ByteArrayInputStream(inputString.getBytes("UTF-8"));
        out = new ByteArrayOutputStream();

        Injector injector = Guice.createInjector(new UserInputSimulationModule(in, out));
        ui = injector.getInstance(TerminalUserInterface.class);
        ui.setPrompt(PROMPT);
        ui.setContinuationPrompt(CONT_PROMPT);
    }

    /**
     * Test whether entering CTRL + D returns null for input.
     */
    @Test
    public void testCtrlD() {
        try {
            setUp(String.valueOf(C_D));
            assertNull(ui.getInput());
        } catch (IOException e) {
            fail("Should not happen");
        }
    }

    /**
     * Test whether entering CTRL + D returns null for input when entered on the second line.
     */
    @Test
    public void testCtrlDEnteredOnSecondLine() {
        try {
            setUp(/* >>> */"asdf" + ENTER
            /* ... */ + C_D);
            assertNull(ui.getInput());
        } catch (IOException e) {
            fail("Should not happen");
        }
    }

    /**
     * Test keyboard shortcuts (CTRL + A, CTRL + E).
     */
    @Test
    public void testKeyboardShortcuts() {
        try {
            setUp(/* >>> */"asdf" + C_A + "qwerty" + C_E + "hjkl" + ENTER
            /* ... */ + "sdf" + C_A + 'a' + ENTER
            /* ... */ + ENTER);
            assertEquals("qwertyasdfhjkl\nasdf", ui.getInput());
        } catch (IOException e) {
            fail("Should not happen");
        }
    }

    /**
     * Test whether interleaving lines with "ENTER", and then accepting the lines with double
     * "ENTER", returns input that spans multiple lines.
     */
    @Test
    public void testMultilineInput() {
        try {
            setUp(/* >>> */ "asdf" + ENTER
            /* ... */ + "qwerty" + ENTER
            /* ... */ + ENTER);
            assertEquals("asdf\nqwerty", ui.getInput());
        } catch (IOException e) {
            fail("Should not happen");
        }
    }

    /**
     * Test whether the continuation prompt is shown when entering multiline input.
     */
    @Test
    public void testContinuationPromptShown() {
        try {
            setUp(/* >>> */ "asdf" + ENTER
            /* ... */ + "qwerty" + ENTER
            /* ... */ + ENTER);
            assertEquals("asdf\nqwerty", ui.getInput());
            assertEquals(PROMPT + "asdf\n" + CONT_PROMPT + "qwerty\n" + CONT_PROMPT + '\n',
                         out.toString("UTF-8"));
        } catch (IOException e) {
            fail("Should not happen");
        }
    }

    /**
     * Test whether an up-arrow causes the input to be the previously entered input.
     */
    @Test
    public void testHistory_single() {
        try {
            setUp(ASDF_QWERTY/* >>> */ + ARROW_UP /* qwerty */ + ENTER + ENTER);
            ui.getInput(); // asdf
            ui.getInput(); // qwerty
            assertEquals("qwerty", ui.getInput());
        } catch (IOException e) {
            fail("Should not happen");
        }
    }

    /**
     * Test whether two up-arrows causes the input to be the input entered two times before.
     */
    @Test
    public void testHistory_double() {
        try {
            setUp(ASDF_QWERTY/* >>> */ + ARROW_UP /* qwerty */ + ARROW_UP /* asdf */ + ENTER
                  + ENTER);
            ui.getInput(); // asdf
            ui.getInput(); // qwerty
            assertEquals("asdf", ui.getInput());
        } catch (IOException e) {
            fail("Should not happen");
        }
    }

    /**
     * Test whether an up-arrow followed by a down-arrow causes the input to be the previously
     * entered input.
     */
    @Test
    public void testHistory_up_up_down() {
        try {
            setUp(ASDF_QWERTY/* >>> */ + ARROW_UP /* qwerty */ + ARROW_UP /* asdf */
                  + ARROW_DOWN /* qwerty */ + ENTER + ENTER);
            ui.getInput(); // asdf
            ui.getInput(); // qwerty
            assertEquals("qwerty", ui.getInput());
        } catch (IOException e) {
            fail("Should not happen");
        }
    }

}
