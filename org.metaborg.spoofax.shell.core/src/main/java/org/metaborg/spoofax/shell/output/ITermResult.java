package org.metaborg.spoofax.shell.output;

import java.util.Optional;

import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * Interface for results that may have an AST.
 */
public interface ITermResult {

    /**
     * Returns the ast of this unit as a {@link IStrategoTerm} if present.
     * @return a {@link IStrategoTerm} or null
     */
    Optional<IStrategoTerm> ast();
}
