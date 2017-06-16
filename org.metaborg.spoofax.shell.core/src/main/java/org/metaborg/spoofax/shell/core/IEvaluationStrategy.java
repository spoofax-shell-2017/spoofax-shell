package org.metaborg.spoofax.shell.core;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.IContext;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * A Strategy pattern for evaluation.
 */
public interface IEvaluationStrategy {

	/**
	 * Provide the strategy identifier in terms of a name String.
	 *
	 * @return The name of this strategy.
	 */
	String name();

	/**
	 * Evaluate the given Stratego term using this strategy.
	 *
	 * @param term
	 *            The input term.
	 * @param context
	 *            The {@link IContext}.
	 * @return The output Stratego term.
	 * @throws MetaborgException
	 *             When evaluation fails for one reason or another.
	 */
	IStrategoTerm evaluate(IStrategoTerm term, IContext context) throws MetaborgException;

}
