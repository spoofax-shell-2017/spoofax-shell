package org.metaborg.spoofax.shell.services;

import org.metaborg.spoofax.shell.functions.FunctionComposer;
import org.metaborg.spoofax.shell.output.FailOrSuccessResult;
import org.metaborg.spoofax.shell.output.IResult;
import org.metaborg.spoofax.shell.output.StyleResult;

/**
 * Interface for the state of an {@link IEditorServices} class.
 *
 * All API that the <code>IEditorServices</code> expose is defined here.
 */
public interface IEditorServicesStrategy {

    /**
     * Can be used by the client to check whether the services can be requested.
     *
     * <p>
     * Services can only meaningfully be requested when a language is loaded.
     * </p>
     *
     * @return true iff some language has been loaded using
     *         {@link IEditorServices#load(FunctionComposer)}.
     */
    boolean isLoaded();

    /**
     * Attempts to provide highlighting over the <code>source</code> code.
     *
     * @param source
     *            String - the code that must be highlighted.
     * @return {@link FailOrSuccessResult} - A {@link StyleResult} containing a valid highlighting,
     *         or a failed result
     */
    FailOrSuccessResult<StyleResult, IResult> highlight(String source);

}
