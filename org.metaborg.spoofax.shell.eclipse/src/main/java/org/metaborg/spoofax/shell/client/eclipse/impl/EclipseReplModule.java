package org.metaborg.spoofax.shell.client.eclipse.impl;

import org.eclipse.swt.widgets.Composite;
import org.metaborg.spoofax.shell.client.IDisplay;
import org.metaborg.spoofax.shell.client.IEditor;
import org.metaborg.spoofax.shell.client.eclipse.impl.hooks.EclipseMessageHook;
import org.metaborg.spoofax.shell.client.eclipse.impl.hooks.EclipseResultHook;
import org.metaborg.spoofax.shell.client.hooks.IMessageHook;
import org.metaborg.spoofax.shell.client.hooks.IResultHook;
import org.metaborg.spoofax.shell.core.IRepl;
import org.metaborg.spoofax.shell.core.ReplModule;

import com.google.inject.Singleton;

/**
 * Bindings for the Eclipse REPL.
 */
public class EclipseReplModule extends ReplModule {
    private final Composite parent;

    /**
     * Instantiates a new EclipseReplModule.
     *
     * @param parent
     *            The {@link Composite} parent of the created widgets.
     */
    public EclipseReplModule(Composite parent) {
        this.parent = parent;
    }

    private void configureUserInterface() {
        bind(EclipseDisplay.class).in(Singleton.class);
        bind(IDisplay.class).to(EclipseDisplay.class);
        bind(Composite.class).toInstance(parent);
        bind(IRepl.class).to(EclipseRepl.class);
        bind(EclipseRepl.class).in(Singleton.class);

        // bind(UI.class).in(Singleton.class);
        bind(IEditor.class).to(EclipseEditor.class);
        // bind(IInputHistory.class).to(EclipseInputHistory.class);
        bind(IMessageHook.class).to(EclipseMessageHook.class);
        bind(IResultHook.class).to(EclipseResultHook.class);
    }

    @Override
    protected void configure() {
        super.configure();
        configureUserInterface();
    }
}
