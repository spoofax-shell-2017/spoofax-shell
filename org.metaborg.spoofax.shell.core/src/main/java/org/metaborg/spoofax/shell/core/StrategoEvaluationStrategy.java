package org.metaborg.spoofax.shell.core;

import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * An {@link IEvaluationStrategy} using Stratego for evaluation.
 */
public class StrategoEvaluationStrategy implements IEvaluationStrategy {

    @Override
    public String name() {
        return "stratego";
    }

    @Override
    public IStrategoTerm evaluate(IStrategoTerm input) {
        return null;
    }
}
