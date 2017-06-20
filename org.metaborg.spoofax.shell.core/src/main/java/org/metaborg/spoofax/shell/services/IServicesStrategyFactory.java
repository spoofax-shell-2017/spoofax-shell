package org.metaborg.spoofax.shell.services;

import org.metaborg.spoofax.shell.functions.FunctionComposer;

/**
 * Factory that returns {@link IEditorServices}.
 */
public interface IServicesStrategyFactory {

    /**
     * Create a strategy for unloaded behaviour.
     *
     * @return {@link IEditorServices} The correct behaviour for unloaded services.
     */
    IEditorServicesStrategy createUnloadedStrategy();

    /**
     * Create a strategy for loaded behaviour.
     *
     * @param composer
     *            {@link FunctionComposer} The composer that is used to execute editor services.
     *
     * @return {@link IEditorServices} The correct behaviour for loaded services.
     */
    IEditorServicesStrategy createLoadedStrategy(FunctionComposer composer);

}
