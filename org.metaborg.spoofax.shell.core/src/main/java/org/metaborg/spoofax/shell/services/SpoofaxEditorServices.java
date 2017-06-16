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

    protected IEditorServicesStrategy strategy;

    /**
     * Instantiates a new SpoofaxEditorServices.
     *
     * The Services will not return any meaningful results until a language has
     * been loaded with {@link #load(FunctionComposer)}.
     * @param {@link IEditorServicesStrategy} The injected 'unloaded' strategy that is used.
     */
    @Inject
    public SpoofaxEditorServices(IEditorServicesStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public FailOrSuccessResult<StyleResult, IResult> highlight(String source) {
        return strategy.highlight(source);
    }

    @Override
    public void load(FunctionComposer composer) {
        strategy = new LoadedServices(composer);
    }

    @Override
    public boolean isLoaded() {
        return strategy.isLoaded();
    }
}
