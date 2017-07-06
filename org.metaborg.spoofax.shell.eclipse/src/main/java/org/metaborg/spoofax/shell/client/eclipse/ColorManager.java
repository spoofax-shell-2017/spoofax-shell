package org.metaborg.spoofax.shell.client.eclipse;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * In SWT, all resources (colors, images, et cetera) claimed from the "Operating System" need to be
 * disposed of manually. The ColorManager is a solution to an otherwise uncomfortable problem of
 * having to track colors everywhere: just ask the ColorManager for a color (see
 * {@link ColorManager#getColor(RGB)}) and it will manage it for you. The only thing you have to do
 * to release the resources, is to dispose the ColorManager (see {@link #dispose()}) when you don't
 * need it anymore.
 *
 * Note that the ColorManager is not thread-safe.
 */
public class ColorManager implements ISharedTextColors {
    private final Map<RGB, Color> colors = new HashMap<>();

    /**
     * Dispose all created colors.
     */
    @Override
    public void dispose() {
        colors.values().stream().forEach(Color::dispose);
        colors.clear();
    }

    /**
     * Get, or create if not available, the specified {@link Color}.
     *
     * @param rgb
     *            The color to retrieve.
     * @return The {@link Color}.
     */
    @Override
    public Color getColor(RGB rgb) {
        Color result = colors.get(rgb);

        if (result == null) {
            result = new Color(Display.getCurrent(), rgb);
            colors.put(rgb, result);
        }

        return result;
    }

}
