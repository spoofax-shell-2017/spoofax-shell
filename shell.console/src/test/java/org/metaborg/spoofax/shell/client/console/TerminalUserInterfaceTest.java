package org.metaborg.spoofax.shell.client.console;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Test;
import org.metaborg.spoofax.shell.client.console.TerminalUserInterface;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;

import jline.TerminalSupport;
import jline.console.ConsoleReader;

/**
 * Tests the {@link TerminalUserInterface} by simulating user input.
 */
public class TerminalUserInterfaceTest {
    private TerminalUserInterface ui;
    private static final String PROMPT = "<TEST>";
    private static final String CONT_PROMPT = ".TEST.";
    private static final char ESC = '\033';
    private static final String ARROW_UP = ESC + "[A";
    private static final String ARROW_DOWN = ESC + "[B";
    private static final char ENTER = '\r';
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
        in = new ByteArrayInputStream(inputString.getBytes());
        out = new ByteArrayOutputStream();

        ConsoleReader reader = new ConsoleReader(in, out, new TerminalSupport(true) {
        });
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(ConsoleReader.class).toInstance(reader);
                bind(InputStream.class).annotatedWith(Names.named("in")).toInstance(in);
                bind(OutputStream.class).annotatedWith(Names.named("out")).toInstance(out);
                bind(OutputStream.class).annotatedWith(Names.named("err")).toInstance(out);
            }
        });
        ui = injector.getInstance(TerminalUserInterface.class);
        ui.setPrompt(PROMPT);
        ui.setContinuationPrompt(CONT_PROMPT);
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
