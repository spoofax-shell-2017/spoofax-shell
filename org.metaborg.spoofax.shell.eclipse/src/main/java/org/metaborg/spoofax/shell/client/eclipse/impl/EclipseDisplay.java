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
import org.metaborg.core.style.IStyle;
import org.metaborg.spoofax.shell.client.IResultVisitor;
import org.metaborg.spoofax.shell.client.eclipse.ColorManager;
import org.metaborg.spoofax.shell.output.FailResult;
import org.metaborg.spoofax.shell.output.ISpoofaxResult;
import org.metaborg.spoofax.shell.output.StyledText;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

/**
 * An Eclipse-based {@link IResultVisitor}, which uses a {@link TextViewer} to display results and
 * error messages.
 *
 * Note that this class should always be run in and accessed from the UI thread!
 */
public class EclipseDisplay implements IResultVisitor {
    private final ITextViewer output;
    private final ColorManager colorManager;

    /**
     * Instantiates a new EclipseDisplay.
     *
     * @param colorManager
     *            The {@link ColorManager} to retrieve colors from.
     * @param parent
     *            A {@link Composite} control which will be the parent of this EclipseDisplay.
     *            (cannot be {@code null}).
     */
    @AssistedInject
    public EclipseDisplay(ColorManager colorManager, @Assisted Composite parent) {
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
            if (style.color() != null) {
                styleRange.foreground = this.colorManager.getColor(awtToRGB(style.color()));
            }
            if (style.backgroundColor() != null) {
                styleRange.background =
                    this.colorManager.getColor(awtToRGB(style.backgroundColor()));
            }
            if (style.bold()) {
                styleRange.fontStyle |= SWT.BOLD;
            }
            if (style.italic()) {
                styleRange.fontStyle |= SWT.ITALIC;
            }

            output.getTextWidget().setStyleRange(styleRange);
        }
    }

    private RGB awtToRGB(Color awt) {
        return new RGB(awt.getRed(), awt.getGreen(), awt.getBlue());
    }

    @Override
    public void visitResult(ISpoofaxResult<?> result) {
        // TODO: use the information in the result to print better error messages.
        IDocument doc = getDocument();

        result.styled().getSource().forEach(e -> {
            int offset = doc.getLength();

            append(doc, offset, e.fragment());
            style(e.style(), offset, e.region().length());
        });

        append(doc, doc.getLength(), "\n");
        scrollText();
    }

    @Override
    public void visitMessage(StyledText message) {
        IDocument doc = getDocument();

        message.getSource().forEach(e -> {
            int offset = doc.getLength();

            append(doc, offset, e.fragment());
            style(e.style(), offset, e.region().length());
        });

        // TODO: this always append a newline, which means there will be an empty line between input
        // (which has its newline appended) and output.
        append(doc, doc.getLength(), "\n");
        scrollText();
    }

    @Override
    public void visitFailure(FailResult errorResult) {
        visitMessage(errorResult.getCause().styled());
    }

    @Override
    public void visitException(Throwable thrown) {
        visitMessage(new StyledText(Color.RED, thrown.getMessage()));
    }
}
