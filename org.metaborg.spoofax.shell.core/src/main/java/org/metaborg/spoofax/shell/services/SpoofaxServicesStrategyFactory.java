package org.metaborg.spoofax.shell.services;

import org.metaborg.spoofax.shell.functions.FunctionComposer;

/**
 * Default implementation of {@link IServicesStrategyFactory}.
 */
public class SpoofaxServicesStrategyFactory implements IServicesStrategyFactory {

    @Override
    public IEditorServicesStrategy createUnloadedStrategy() {
        return new UnloadedServices();
    }

    @Override
    public IEditorServicesStrategy createLoadedStrategy(FunctionComposer composer) {
        return new LoadedServices(composer);
    }

}
