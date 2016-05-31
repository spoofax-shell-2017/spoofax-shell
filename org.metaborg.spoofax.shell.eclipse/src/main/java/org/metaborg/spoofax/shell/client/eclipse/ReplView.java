package org.metaborg.spoofax.shell.client.eclipse;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.part.ViewPart;
import org.metaborg.spoofax.shell.client.eclipse.impl.EclipseDisplay;
import org.metaborg.spoofax.shell.client.eclipse.impl.EclipseEditor;
import org.metaborg.spoofax.shell.client.eclipse.impl.EclipseRepl;
import org.metaborg.spoofax.shell.client.eclipse.impl.EclipseReplModule;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * A {@link ViewPart} showing two widgets: a {@link MessageConsole} (see {@link EclipseDisplay}) and
 * a {@link Text} (see {@link EclipseIEditor}). Together these widgets form a user interface to
 * {@link EclipseRepl}.
 */
public class ReplView extends ViewPart {
    private EclipseEditor editor;

    @Override
    public void createPartControl(Composite parent) {
        Injector injector = Guice.createInjector(new EclipseReplModule(parent));
        setupLayout(parent);
        setupDisplay(injector);
        setupEditor(injector);
        setupRepl(injector);
    }

    private void setupLayout(Composite parent) {
        GridLayout layout = new GridLayout(1, true);
        parent.setLayout(layout);
    }

    private void setupDisplay(Injector injector) {
        EclipseDisplay display = injector.getInstance(EclipseDisplay.class);
        display.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    }

    private void setupEditor(Injector injector) {
        this.editor = injector.getInstance(EclipseEditor.class);
        this.editor.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    }

    private void setupRepl(Injector injector) {
        EclipseRepl repl = injector.getInstance(EclipseRepl.class);
        this.editor.asObservable().subscribe(repl);
    }

    @Override
    public void setFocus() {
        this.editor.getControl().setFocus();
    }

}
