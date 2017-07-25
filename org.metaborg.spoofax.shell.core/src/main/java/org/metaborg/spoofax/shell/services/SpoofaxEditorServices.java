package org.metaborg.spoofax.shell.services;

import org.metaborg.spoofax.shell.functions.FunctionComposer;
import org.metaborg.spoofax.shell.output.FailOrSuccessResult;
import org.metaborg.spoofax.shell.output.IResult;
import org.metaborg.spoofax.shell.output.StyleResult;

import com.google.inject.Inject;

/**
 * Default implementation of {@link IEditorServices}.
 */
public class SpoofaxEditorServices implements IEditorServices {

    private final IServicesStrategyFactory strategyfactory;
    private IEditorServicesStrategy strategy;

    /**
     * Instantiates a new SpoofaxEditorServices.
     *
     * The services will not return any meaningful results until a language has
     * been loaded with {@link #load(FunctionComposer)}.
     *
     * @param strategyfactory
     *            {@link IServicesStrategyFactory} The factory to instantiate strategies.
     */
    @Inject
    public SpoofaxEditorServices(IServicesStrategyFactory strategyfactory) {
        this.strategyfactory = strategyfactory;
        this.strategy = strategyfactory.createUnloadedStrategy();
    }

    @Override
    public void load(FunctionComposer composer) {
        strategy = strategyfactory.createLoadedStrategy(composer);
    }

    @Override
    public FailOrSuccessResult<StyleResult, IResult> highlight(String source) {
        return strategy.highlight(source);
    }

    @Override
    public boolean isLoaded() {
        return strategy.isLoaded();
    }
}
