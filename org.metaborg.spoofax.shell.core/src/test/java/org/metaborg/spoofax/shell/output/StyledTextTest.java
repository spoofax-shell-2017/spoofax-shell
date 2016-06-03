package org.metaborg.spoofax.shell.output;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.awt.Color;
import java.util.List;

import org.junit.Test;
import org.metaborg.core.style.IRegionStyle;
import org.metaborg.core.style.RegionStyle;
import org.metaborg.core.style.Style;

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
        List<IRegionStyle<String>> source = styledText.getSource();

        assertEquals("Hello world", styledText.toString());
        assertEquals(1, source.size());
        assertEquals("Hello world", source.get(0).fragment());
        assertNull(source.get(0).style());
    }

    /**
     * Test a string styled with a color.
     */
    @Test
    public void testColoredString() {
        final int part1Start = 0;
        final int part1End = 5;
        String part1 = "Hello ";

        final int part2Start = 6;
        final int part2End = 10;
        String part2 = "world";

        StyledText styledText = new StyledText(Color.RED, part1).append(part2);
        List<IRegionStyle<String>> source = styledText.getSource();

        assertEquals(part1, styledText.getSource().get(0).fragment());
        assertEquals(part2, styledText.getSource().get(1).fragment());
        assertEquals(part1 + part2, styledText.toString());
        assertEquals(2, source.size());
        assertEquals(part1Start, source.get(0).region().startOffset());
        assertEquals(part1End, source.get(0).region().endOffset());
        assertEquals(part2Start, source.get(1).region().startOffset());
        assertEquals(part2End, source.get(1).region().endOffset());

        assertNotNull(source.get(0).style());
        assertNull(source.get(1).style());
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
