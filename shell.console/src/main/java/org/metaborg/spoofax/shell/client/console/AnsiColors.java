package org.metaborg.spoofax.shell.client.console;

import java.awt.Color;
import java.util.Arrays;

/**
 * Represents a list of possible ansi colors and maps them to Java colors.
 */
public enum AnsiColors {
    BLACK(0, Color.BLACK),
    RED(1, Color.RED),
    GREEN(2, Color.GREEN),
    YELLOW(3, Color.YELLOW),
    BLUE(4, Color.BLUE),
    MAGENTA(5, Color.MAGENTA),
    CYAN(6, Color.CYAN),
    WHITE(7, Color.WHITE);

    private int idx;
    private Color color;

    /**
     * Construct all colors.
     * @param idx the ansi index
     * @param color the color
     */
    AnsiColors(int idx, Color color) {
        this.idx = idx;
        this.color = color;
    }

    /**
     * Find the ansi color idx corresponding to a java color.
     * @param color the java color
     * @return an ansi color idx
     */
    public static int findClosest(Color color) {
        return Arrays.stream(AnsiColors.values())
            .sorted((a, b) -> Integer.compare(colorDiff(a.color, color), colorDiff(b.color, color)))
            .findFirst()
            .get()
            .idx;
    }

    private static int colorDiff(Color a, Color b) {
        return Math.abs(a.getBlue() - b.getBlue())
             + Math.abs(a.getGreen() - b.getGreen())
             + Math.abs(a.getRed() - b.getRed());
    }
}
