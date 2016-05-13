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
import org.metaborg.spoofax.shell.client.console.AnsiColors;

/**
 * Test cases for the java color to ansi color mapping.
 */
@RunWith(Parameterized.class)
public class AnsiColorsTest {
    public static final int OFFSET = 5;

    private Ansi.Color expected;
    private Color input;
    private Color inputoffset;

    /**
     * Creates input for parameterized ansi color test cases.
     * @return an array of parameterized input
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
     * Instantiates a parameterized instance for the ansi color test.
     * @param expected the expected ansi color
     * @param input the java color
     * @param r test offset for red
     * @param g test offset for green
     * @param b test offset for blue
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
