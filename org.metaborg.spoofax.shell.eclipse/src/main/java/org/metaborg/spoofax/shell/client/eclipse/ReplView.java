package org.metaborg.spoofax.shell.client.eclipse;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.metaborg.spoofax.shell.client.eclipse.impl.EclipseDisplay;
import org.metaborg.spoofax.shell.client.eclipse.impl.EclipseEditor;
import org.metaborg.spoofax.shell.client.eclipse.impl.EclipseRepl;
import org.metaborg.spoofax.shell.client.eclipse.impl.EclipseReplModule;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * The workbench view containing all the widgets (see {@link EclipseDisplay} and
 * {@link EclipseEditor}) that together form the REPL. Note that because the plugin is registered to
 * Eclipse as a singleton, at most one ReplView will be active at any given time.
 */
public class ReplView extends ViewPart {
    private static final int DISPLAYWEIGHT = 5;
    private static final int EDITORWEIGHT = 1;
    private Composite page;
    private EclipseEditor editor;
    private ColorManager colorManager;

    @Override
    public void createPartControl(Composite parent) {
        this.page = new SashForm(parent, SWT.VERTICAL | SWT.LEFT_TO_RIGHT);

        // TODO: set Injector in Activator.class such that it can be accessed from elsewhere?
        Injector injector = Guice.createInjector(new EclipseReplModule(page));

        // Create the display first so it appears on top in the sash.
        injector.getInstance(EclipseDisplay.class);
        this.editor = injector.getInstance(EclipseEditor.class);
        // Must be after the instantiation of the two widgets.
        ((SashForm) this.page).setWeights(new int[] { DISPLAYWEIGHT, EDITORWEIGHT });

        // Instantiate the REPL and add it as observer of the editor.
        EclipseRepl repl = injector.getInstance(EclipseRepl.class);
        this.editor.asObservable().subscribe(repl);

        // Retrieve the color manager so that it can be disposed of when the view is closed.
        this.colorManager = injector.getInstance(ColorManager.class);
    }

    @Override
    public void setFocus() {
        this.editor.setFocus();
    }

    @Override
    public void dispose() {
        this.colorManager.dispose();
        super.dispose();
    }

}
