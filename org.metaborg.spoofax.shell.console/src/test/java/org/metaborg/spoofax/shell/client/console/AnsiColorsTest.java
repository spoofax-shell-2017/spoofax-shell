package org.metaborg.spoofax.shell.client.console;


import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;

import org.fusesource.jansi.Ansi;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test cases for the Java color to ANSI color mapping.
 */
@RunWith(Parameterized.class)
public class AnsiColorsTest {
    public static final int OFFSET = 5;

    private final Ansi.Color expected;
    private final Color input;
    private final Color inputoffset;

    /**
     * Creates input for parameterized ANSI color test cases.
     *
     * @return An array of parameterized input
     */
    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { Ansi.Color.BLACK,  Color.BLACK,     OFFSET, 0, 0 },
            { Ansi.Color.RED,     Color.RED,     -OFFSET, 0, 0 },
            { Ansi.Color.GREEN,   Color.GREEN,    OFFSET, 0, 0 },
            { Ansi.Color.YELLOW,  Color.YELLOW,  -OFFSET, 0, 0 },
            { Ansi.Color.BLUE,    Color.BLUE,     OFFSET, 0, 0 },
            { Ansi.Color.MAGENTA, Color.MAGENTA, -OFFSET, 0, 0 },
            { Ansi.Color.CYAN,    Color.CYAN,     OFFSET, 0, 0 },
            { Ansi.Color.WHITE,   Color.WHITE,   -OFFSET, 0, 0 }
        });
    }

    /**
     * Instantiates a parameterized instance for the ANSI color test.
     *
     * @param expected
     *            The expected ANSI color
     * @param input
     *            The Java color
     * @param r
     *            Test offset for red
     * @param g
     *            Test offset for green
     * @param b
     *            Test offset for blue
     */
    public AnsiColorsTest(Ansi.Color expected, Color input, int r, int g, int b) {
        this.expected = expected;
        this.input = input;
        this.inputoffset = new Color(input.getRed() + r, input.getGreen() + g, input.getBlue() + b);

    }

    /**
     * Run the actual tests.
     */
    @Test
    public void test() {
        assertEquals(expected, AnsiColors.findClosest(input));
        assertEquals(expected, AnsiColors.findClosest(inputoffset));
    }

}
