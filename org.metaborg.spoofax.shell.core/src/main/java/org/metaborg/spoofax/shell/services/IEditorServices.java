package org.metaborg.spoofax.shell.services;

import org.metaborg.spoofax.shell.client.IRepl;
import org.metaborg.spoofax.shell.functions.FunctionComposer;
import org.metaborg.spoofax.shell.output.FailOrSuccessResult;
import org.metaborg.spoofax.shell.output.FailOrSuccessVisitor;

/**
 * Provides an interface to the {@link IRepl} client for requesting editor features.
 * <p>
 * The calls in this interface return a {@link FailOrSuccessResult} that can be visited using a
 * {@link FailOrSuccessVisitor}.
 * </p>
 * <p>
 * The interface implements a Strategy pattern to change its behaviour when a new language is loaded
 * via {@link #load(FunctionComposer)}.
 * </p>
 * <p>
 * The actual exposed API of <code>IEditorServices</code> is maintained in its strategy
 * interface {@link IEditorServicesStrategy}.
 * This interface only provides the additional ability to change strategies.
 * </p>
 */
public interface IEditorServices extends IEditorServicesStrategy {

    /**
     * Loads a language definition that the Editor Services are based on.
     *
     * <p>
     * The services will only return a meaningful result after calling this method for the
     * appropriate language.
     * </p>
     *
     * @param composer
     *            {@link FunctionComposer} - The Function Composer that was
     *            created with the language definition.
     */
    void load(FunctionComposer composer);

}
