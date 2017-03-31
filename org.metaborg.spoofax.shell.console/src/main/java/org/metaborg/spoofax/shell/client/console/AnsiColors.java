package org.metaborg.spoofax.shell.client.console;

import java.awt.Color;
import java.util.Arrays;

import org.fusesource.jansi.Ansi;

/**
 * Represents a list of possible ANSI colors and maps them to Java colors.
 */
public enum AnsiColors {
    BLACK(Ansi.Color.BLACK, Color.BLACK),
    RED(Ansi.Color.RED, Color.RED),
    GREEN(Ansi.Color.GREEN, Color.GREEN),
    YELLOW(Ansi.Color.YELLOW, Color.YELLOW),
    BLUE(Ansi.Color.BLUE, Color.BLUE),
    MAGENTA(Ansi.Color.MAGENTA, Color.MAGENTA),
    CYAN(Ansi.Color.CYAN, Color.CYAN),
    WHITE(Ansi.Color.WHITE, Color.WHITE);

    private final Ansi.Color ansiColor;
    private final Color color;

    /**
     * Construct all ANSI color to color mappings.
     *
     * @param ansiColor
     *            The ANSI color.
     * @param color
     *            The Java color.
     */
    AnsiColors(Ansi.Color ansiColor, Color color) {
        this.ansiColor = ansiColor;
        this.color = color;
    }

    /**
     * Find the ANSI color most corresponding to a Java color.
     *
     * @param color
     *            The Java color.
     * @return The ANSI color.
     */
    public static Ansi.Color findClosest(Color color) {
        return Arrays.stream(AnsiColors.values())
            .min((a, b) -> Integer.compare(colorDiff(a.color, color), colorDiff(b.color, color)))
            .get()
            .ansiColor;
    }

    private static int colorDiff(Color a, Color b) {
        return Math.abs(a.getBlue() - b.getBlue())
             + Math.abs(a.getGreen() - b.getGreen())
             + Math.abs(a.getRed() - b.getRed());
    }
}
