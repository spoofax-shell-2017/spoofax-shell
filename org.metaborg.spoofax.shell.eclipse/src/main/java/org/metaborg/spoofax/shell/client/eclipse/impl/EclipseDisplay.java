package org.metaborg.spoofax.shell.client.eclipse.impl;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Composite;
import org.metaborg.core.style.IStyle;
import org.metaborg.spoofax.shell.client.IDisplay;
import org.metaborg.spoofax.shell.client.eclipse.ColorManager;
import org.metaborg.spoofax.shell.client.eclipse.EclipseUtil;
import org.metaborg.spoofax.shell.output.StyledText;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

/**
 * An Eclipse-based {@link IDisplay}, which uses a {@link TextViewer} to display results and
 * error messages.
 *
 * Note that this class should always be run in and accessed from the UI thread!
 */
public class EclipseDisplay implements IDisplay {
    // TODO: Use ReplDocument to provide custom partitioning? Perhaps more something for the output
    // as opposed to input. Should be relatively easy for output to at least partition different
    // input/output combinations.
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

    @Override
    public void displayStyledText(StyledText text) {
        IDocument doc = getDocument();

        text.getSource().forEach(e -> {
            int offset = doc.getLength();

            append(doc, offset, e.fragment());

            IStyle style = e.style();
            if (style != null) {
                StyleRange styleRange = EclipseUtil.style(
                        colorManager,
                        e.style(),
                        offset,
                        e.region().length());

                output.getTextWidget().setStyleRange(styleRange);
            }
        });

        if (doc != null) {
            append(doc, doc.getLength(), "\n");
            scrollText();
        }
    }
}
