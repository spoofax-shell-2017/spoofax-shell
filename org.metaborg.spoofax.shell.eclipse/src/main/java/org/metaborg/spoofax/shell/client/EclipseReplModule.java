package org.metaborg.spoofax.shell.client;

import org.metaborg.spoofax.shell.ReplModule;
import org.metaborg.spoofax.shell.client.eclipse.ColorManager;
import org.metaborg.spoofax.shell.client.eclipse.impl.IWidgetFactory;

import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 * Bindings for the Eclipse REPL.
 */
public class EclipseReplModule extends ReplModule {

    @Override
    protected void configure() {
        super.configure();

        // Singleton so that all REPLs talk to the same ColorManager.
        bind(ColorManager.class).in(Singleton.class);
        bind(IInputHistory.class).to(InputHistory.class);
        install(new FactoryModuleBuilder().build(IWidgetFactory.class));
    }
}
