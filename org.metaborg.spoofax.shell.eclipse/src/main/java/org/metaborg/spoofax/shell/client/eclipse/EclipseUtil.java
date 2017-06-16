package org.metaborg.spoofax.shell.client.eclipse;

import java.awt.Color;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.RGB;
import org.metaborg.core.style.IStyle;

public class EclipseUtil {

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

	public static RGB awtToRGB(Color awt) {
		return new RGB(awt.getRed(), awt.getGreen(), awt.getBlue());
	}

}
