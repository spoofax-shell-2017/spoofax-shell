package org.metaborg.spoofax.shell.core;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.IContext;
import org.metaborg.spoofax.shell.output.AnalyzeResult;
import org.metaborg.spoofax.shell.output.ParseResult;
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
     * @param parsed The parsed input.
     * @param context The {@link IContext}.
     * @return The output Stratego term.
     * @throws MetaborgException When evaluation fails for one reason or another.
     */
    IStrategoTerm evaluate(ParseResult parsed, IContext context) throws MetaborgException;

    /**
     * Evaluate the given Stratego term using this strategy.
     * @param analyzed The analyzed input.
     * @param context The {@link IContext}.
     * @return The output Stratego term.
     * @throws MetaborgException When evaluation fails for one reason or another.
     */
    IStrategoTerm evaluate(AnalyzeResult analyzed, IContext context) throws MetaborgException;
}
