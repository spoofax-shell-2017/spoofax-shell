package org.metaborg.spoofax.shell.client.eclipse.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.console.TextConsoleViewer;
import org.metaborg.spoofax.shell.client.IDisplay;
import org.metaborg.spoofax.shell.output.StyledText;

import com.google.inject.Inject;

/**
 * An Eclipse-based {@link IDisplay}, which uses a {@link MessageConsole} to display results and
 * error messages.
 */
public class EclipseDisplay implements IDisplay {
    private final TextConsoleViewer viewer;
    private final MessageConsoleStream out;
    private final MessageConsoleStream err;

    /**
     * Instantiates a new EclipseDisplay.
     *
     * @param parent
     *            A {@link Composite} control which will be the parent of this EclipseEditor.
     *            (cannot be {@code null}).
     */
    @Inject
    public EclipseDisplay(Composite parent) {
        MessageConsole console = new MessageConsole("Spoofax REPL Console", null);
        this.viewer = new TextConsoleViewer(parent, console);
        this.out = console.newMessageStream();
        this.err = console.newMessageStream();
        // TODO: when StyledText can be translated to color understood by Eclipse, this should go.
        this.err.setColor(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
    }

    /**
     * Returns the {@link Control} backing this EclipseDisplay.
     *
     * @return The {@link Control} backing this EclipseDisplay.
     */
    public Control getControl() {
        return this.viewer.getControl();
    }

    @Override
    public void displayResult(StyledText message) {
        this.out.println(message.toString());
    }

    // TODO: Since all markup happens in the message itself, why have a separate displayError
    // method?
    @Override
    public void displayError(StyledText message) {
        this.err.println(message.toString());
    }

}
