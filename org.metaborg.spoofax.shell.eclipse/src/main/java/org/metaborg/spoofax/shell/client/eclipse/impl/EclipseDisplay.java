package org.metaborg.spoofax.shell.client.eclipse.impl;

import java.awt.Color;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.metaborg.core.source.ISourceRegion;
import org.metaborg.core.style.IStyle;
import org.metaborg.spoofax.shell.client.IDisplay;
import org.metaborg.spoofax.shell.client.eclipse.ColorManager;
import org.metaborg.spoofax.shell.output.StyledText;

import com.google.inject.Inject;

/**
 * An Eclipse-based {@link IDisplay}, which uses a {@link TextViewer} to display results and error
 * messages.
 *
 * Note that this class should always be run in and accessed from the UI thread!
 */
public class EclipseDisplay implements IDisplay {
    private final ITextViewer output;
    private final ColorManager colorManager;

    /**
     * Instantiates a new EclipseDisplay.
     *
     * @param parent
     *            A {@link Composite} control which will be the parent of this EclipseDisplay.
     *            (cannot be {@code null}).
     * @param colorManager
     *            The {@link ColorManager} to retrieve colors from.
     */
    @Inject
    public EclipseDisplay(Composite parent, ColorManager colorManager) {
        this.output = new TextViewer(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        this.output.getTextWidget().setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));
        this.output.getTextWidget().setAlwaysShowScrollBars(false);
        this.output.setEditable(false);
        this.output.setDocument(new Document());

        this.colorManager = colorManager;
    }

    private IDocument getDocument() {
        return this.output.getDocument();
    }

    private void scrollText() {
        output.revealRange(getDocument().getLength(), 0);
    }

    private void append(IDocument doc, int offset, String fragment) {
        if (offset > 0) {
            try {
                doc.replace(offset, 0, fragment);
            } catch (BadLocationException ex) {
                ex.printStackTrace();
                // TODO: this could mess with the styleranges, perhaps style() shouldn't be called
                // when this exception occurs.
            }
        } else {
            doc.set(fragment);
        }
    }

    private void style(IStyle style, int offset, int length) {
        if (style != null) {
            StyleRange styleRange = new StyleRange();

            styleRange.start = offset;
            styleRange.length = length;
            styleRange.foreground = this.colorManager.getColor(awtToRGB(style.color()));
            styleRange.background = this.colorManager.getColor(awtToRGB(style.backgroundColor()));

            if (style.bold()) {
                styleRange.fontStyle |= SWT.BOLD;
            }
            if (style.italic()) {
                styleRange.fontStyle |= SWT.ITALIC;
            }

            output.getTextWidget().setStyleRange(styleRange);
        }
    }

    @Override
    public void displayResult(StyledText message) {
        IDocument doc = getDocument();
        int offset = doc.getLength();
        String text = message.toString();

        // TODO: restore StyledText so that substrings aren't necessary anymore?
        message.getSource().forEach(e -> {
            ISourceRegion region = e.region();
            append(doc, offset,
                   text.substring(region.startOffset(), region.endOffset() + 1) + '\n');
            style(e.style(), offset, region.length());
        });

        scrollText();
    }

    // TODO: Since all markup happens in the message itself, why have a separate displayError
    // method? However, if IResultHook passes on the ISpoofaxResult to IDisplay, then this separate
    // method is indeed needed.
    @Override
    public void displayError(StyledText message) {
        displayResult(message);
    }

    private RGB awtToRGB(Color awt) {
        if (awt == null) {
            return ColorManager.getDefault();
        }
        return new RGB(awt.getRed(), awt.getGreen(), awt.getBlue());
    }
}
