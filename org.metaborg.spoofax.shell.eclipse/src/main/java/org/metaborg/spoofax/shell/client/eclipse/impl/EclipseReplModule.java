package org.metaborg.spoofax.shell.client.eclipse.impl;

import org.eclipse.swt.widgets.Composite;
import org.metaborg.spoofax.shell.client.IDisplay;
import org.metaborg.spoofax.shell.client.eclipse.ColorManager;
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
        // TODO: use @AssistedInject for this. When done, create Injector in Activator.
        bind(Composite.class).toInstance(parent);

        // Singleton so that all hooks talk to the same IDisplay.
        bind(IDisplay.class).to(EclipseDisplay.class);
        bind(EclipseDisplay.class).in(Singleton.class);

        // bind(IEditor).to(EclipseEditor.class);

        // Singleton so that all commands talk to the same hooks.
        bind(IMessageHook.class).to(EclipseMessageHook.class);
        bind(EclipseMessageHook.class).in(Singleton.class);
        bind(IResultHook.class).to(EclipseResultHook.class);
        bind(EclipseResultHook.class).in(Singleton.class);
    }

    @Override
    protected void configure() {
        super.configure();
        configureUserInterface();
        bind(IRepl.class).to(EclipseRepl.class);

        // Singleton so that all REPLs talk to the same ColorManager.
        bind(ColorManager.class).in(Singleton.class);
    }
}
