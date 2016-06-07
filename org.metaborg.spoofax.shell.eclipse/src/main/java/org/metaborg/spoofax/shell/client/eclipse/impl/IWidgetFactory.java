package org.metaborg.spoofax.shell.client.eclipse.impl;

import org.eclipse.swt.widgets.Composite;
import org.metaborg.spoofax.shell.client.IDisplay;

/**
 * An interface for Guice's {@link com.google.inject.assistedinject.FactoryModuleBuilder}.
 */
public interface IWidgetFactory {

    /**
     * Instantiate a new {@link EclipseDisplay}.
     *
     * @param parent
     *            A {@link Composite} control which will be the parent of this EclipseDisplay.
     *            (cannot be {@code null}).
     * @return The created {@link EclipseDisplay}.
     */
    EclipseDisplay createDisplay(Composite parent);

    /**
     * Instantiate a new {@link EclipseEditor}.
     *
     * @param parent
     *            A {@link Composite} control which will be the parent of this EclipseEditor.
     *            (cannot be {@code null}).
     * @return The created {@link EclipseEditor}.
     */
    EclipseEditor createEditor(Composite parent);

    /**
     * Instantiate a new {@link EclipseRepl}.
     *
     * @param display
     *            The {@link IDisplay} to send results to.
     * @return The created {@link EclipseRepl}.
     */
    EclipseRepl createRepl(IDisplay display);

}
