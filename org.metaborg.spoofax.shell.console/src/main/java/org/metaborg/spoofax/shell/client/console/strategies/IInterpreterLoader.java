package org.metaborg.spoofax.shell.client.console.strategies;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.meta.lang.dynsem.interpreter.DynSemVM;

/**
 * Interface for loading a DynSem interpreter.
 */
public interface IInterpreterLoader {

    /**
     * Loads the DynSem interpreter for the given language implementation.
     *
     * @param langImpl
     *            A language implementation with a DynSem specification.
     * @return The DynSem entrypoint for the interpreter.
     * @throws MetaborgException
     *             When loading results in an error.
     */
    DynSemVM createInterpreterForLanguage(ILanguageImpl langImpl) throws MetaborgException;

}