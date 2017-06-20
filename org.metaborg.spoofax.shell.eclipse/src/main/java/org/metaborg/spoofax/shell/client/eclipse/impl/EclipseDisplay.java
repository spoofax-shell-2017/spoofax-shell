package org.metaborg.spoofax.shell.client.eclipse.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.OverviewRuler;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;
import org.eclipse.ui.texteditor.DefaultRangeIndicator;
import org.metaborg.core.source.ISourceRegion;
import org.metaborg.core.style.IStyle;
import org.metaborg.spoofax.shell.client.IDisplay;
import org.metaborg.spoofax.shell.client.eclipse.ColorManager;
import org.metaborg.spoofax.shell.client.eclipse.EclipseUtil;
import org.metaborg.spoofax.shell.output.ExceptionResult;
import org.metaborg.spoofax.shell.output.FailOrSuccessVisitor;
import org.metaborg.spoofax.shell.output.IResult;
import org.metaborg.spoofax.shell.output.ISpoofaxTermResult;
import org.metaborg.spoofax.shell.output.PrintResult;
import org.metaborg.spoofax.shell.output.StyledText;
import org.metaborg.spoofax.shell.services.IEditorServices;

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
    private final ProjectionViewer viewer;
    private final ColorManager colorManager;
    private final Document document = new Document();
    private final ProjectionAnnotationModel projectionAnnotationModel;
    private final IEditorServices editorServices;
    // private final ProjectionAnnotationModel annotationModel;

    /**
     * Instantiates a new EclipseDisplay.
     *
     * @param colorManager
     *            The {@link ColorManager} to retrieve colors from.
     * @param parent
     *            A {@link Composite} control which will be the parent of this EclipseDisplay.
     *            (cannot be {@code null}).
     * @param editorServices the {@link IEditorServices} used to request editor services.
     */
    @AssistedInject
    public EclipseDisplay(
            ColorManager colorManager,
            @Assisted Composite parent,
            IEditorServices editorServices) {
        // see AbstractDecoratedTextEditor.VERTICAL_RULER_WIDTH
        IAnnotationAccess annotationAccess = new DefaultMarkerAnnotationAccess();
        IOverviewRuler overviewRuler = new OverviewRuler(annotationAccess, 0, colorManager);
        CompositeRuler ruler = new CompositeRuler();

        this.editorServices = editorServices;

        this.viewer = new ProjectionViewer(parent, ruler, overviewRuler, true,
                SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        this.viewer.getTextWidget().setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));
        this.viewer.getTextWidget().setAlwaysShowScrollBars(false);
        this.viewer.setEditable(true);
        this.viewer.setDocument(document, new ProjectionAnnotationModel());

        ProjectionSupport projectionSupport = new ProjectionSupport(viewer, annotationAccess,
                colorManager);
        projectionSupport.install();
        viewer.setRangeIndicator(new DefaultRangeIndicator());
        this.viewer.enableProjection();
        this.projectionAnnotationModel = viewer.getProjectionAnnotationModel();
        this.colorManager = colorManager;
    }

    private void scrollText() {
        viewer.revealRange(viewer.getDocument().getLength(), 0);
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
    public void displayStyledText(StyledText styledText) {
        displayStyledText(styledText, Collections.emptyList());
    }

    private void displayStyledText(StyledText text, List<ISourceRegion> foldingRegions) {

        int offsetPreAppendNewText = document.getLength();

        text.getSource().forEach(e -> {
            int offset = document.getLength();

            append(document, offset, e.fragment());

            IStyle style = e.style();
            if (style != null) {
                StyleRange styleRange = EclipseUtil.style(
                        colorManager,
                        e.style(),
                        offset,
                        e.region().length());

                viewer.getTextWidget().setStyleRange(styleRange);
            }
        });

        append(document, document.getLength(), "\n");
        scrollText();
        List<Position> positions = foldingRegions.stream().map(region -> {
            return new Position(region.startOffset() + offsetPreAppendNewText, region.length());
        }).collect(Collectors.toList());

        HashMap<Annotation, Position> newAnnotations = new HashMap<>();
        for (Position position : positions) {
            ProjectionAnnotation annotation = new ProjectionAnnotation();
            newAnnotations.put(annotation, position);
        }
        projectionAnnotationModel.modifyAnnotations(null, newAnnotations, null);
    }

    @Override
    public void visitTermResult(ISpoofaxTermResult<?> result) {
        editorServices
            .foldAndPrint(result)
            .accept(new FailOrSuccessVisitor<PrintResult, IResult>() {

                    @Override
                    public void visitSuccess(PrintResult result) {
                        displayStyledText(result.getText(), result.getRegions());
                    }

                    @Override
                    public void visitFailure(IResult result) {
                        result.accept(EclipseDisplay.this);
                    }

                    @Override
                    public void visitException(ExceptionResult result) {
                        result.accept(EclipseDisplay.this);
                    }
                });
    }

}
