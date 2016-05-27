package org.metaborg.spoofax.shell.output;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.awt.Color;

import org.junit.Test;
import org.metaborg.core.style.RegionStyle;
import org.metaborg.core.style.Style;
import org.metaborg.spoofax.shell.output.StyledText;

/**
 * Test cases for the {@link StyledText} class, which wraps Spoofax's {@link RegionStyle}.
 */
public class StyledTextTest {

    /**
     * Test a string with no style.
     */
    @Test
    public void testString() {
        StyledText styledText = new StyledText("Hello world");

        assertEquals("Hello world", styledText.toString());
        assertEquals(1, styledText.getSource().size());
        assertNull(styledText.getSource().get(0).style());
    }

    /**
     * Test a string styled with a color.
     */
    @Test
    public void testColoredString() {
        StyledText styledText = new StyledText(Color.RED, "Hello ").append("world");

        assertEquals("Hello world", styledText.toString());
        assertEquals(2, styledText.getSource().size());
        assertNotNull(styledText.getSource().get(0).style());
        assertNull(styledText.getSource().get(1).style());
    }

    /**
     * Test a string styled with an arbitrary style.
     */
    @Test
    public void testStyledString() {
        Style style1 = new Style(Color.BLUE, Color.CYAN, false, false, false);
        Style style2 = new Style(Color.RED, Color.YELLOW, false, false, false);
        StyledText styledText = new StyledText(style1, "Hello ").append(style2, "world");

        assertEquals("Hello world", styledText.toString());
        assertEquals(2, styledText.getSource().size());
        assertEquals(style1, styledText.getSource().get(0).style());
        assertEquals(style2, styledText.getSource().get(1).style());
    }
}
