package org.metaborg.spoofax.shell.client.eclipse;

import java.awt.Color;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.RGB;
import org.metaborg.core.style.IStyle;

/**
 * Utility methods for the Eclipse Client.
 */
public final class EclipseUtil {

	private EclipseUtil() {
		//Empty private constructor to prevent instantiation.
	}

	/**
	 * Create an SWT {@link StyleRange} based on the MetaBorg styling at the provided offset.
	 *
	 * @param colorManager
	 *            {@link ColorManager} to manage SWT colors.
	 * @param style
	 *            {@link IStyle} The MetaBorg styling.
	 * @param offset
	 *            int - The offset to start.
	 * @param length
	 *            int - The length of the styling.
	 * @return {@link StyleRange} - SWT Styling.
	 */
	public static StyleRange style(ColorManager colorManager, IStyle style, int offset,
			int length) {
		StyleRange styleRange = new StyleRange();

		styleRange.start = offset;
		styleRange.length = length;
		if (style.color() != null) {
			styleRange.foreground = colorManager.getColor(awtToRGB(style.color()));
		}
		if (style.backgroundColor() != null) {
			styleRange.background = colorManager.getColor(awtToRGB(style.backgroundColor()));
		}
		if (style.bold()) {
			styleRange.fontStyle |= SWT.BOLD;
		}
		if (style.italic()) {
			styleRange.fontStyle |= SWT.ITALIC;
		}

		return styleRange;
	}

	/**
	 * Convert an AWT {@link Color} to SWT {@link RGB}.
	 *
	 * @param awt
	 *            {@link Color} The AWT color instance.
	 * @return {@link RGB} The SWT color instance.
	 */
	public static RGB awtToRGB(Color awt) {
		return new RGB(awt.getRed(), awt.getGreen(), awt.getBlue());
	}

}
