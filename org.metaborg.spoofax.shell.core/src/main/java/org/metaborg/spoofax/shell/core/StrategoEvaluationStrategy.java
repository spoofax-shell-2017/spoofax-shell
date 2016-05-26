package org.metaborg.spoofax.shell.core;

import org.metaborg.core.context.IContext;
import org.metaborg.spoofax.shell.output.AnalyzeResult;
import org.metaborg.spoofax.shell.output.ParseResult;
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
    public IStrategoTerm evaluate(ParseResult parsed, IContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IStrategoTerm evaluate(AnalyzeResult analyzed, IContext context) {
        // TODO Auto-generated method stub
        return null;
    }
}
