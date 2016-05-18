package org.metaborg.spoofax.shell.core;

import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * A Strategy pattern for evaluation.
 */
public interface IEvaluationStrategy {

    /**
     * @return The name of this strategy.
     */
    String name();

    /**
     * Evaluate the given Stratego term using this strategy.
     * @param input The input Stratego term.
     * @return The output Stratego term.
     */
    IStrategoTerm evaluate(IStrategoTerm input);
}
