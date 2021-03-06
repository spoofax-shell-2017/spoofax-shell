package org.metaborg.spoofax.shell.core;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.IContext;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import com.google.inject.Inject;


/**
 * An {@link IEvaluationStrategy} for Stratego-based languages.
 */
public class StrategoEvaluationStrategy implements IEvaluationStrategy {

	// TODO: hardcoded init and eval terms
	private static final String INIT_TERM = "shell_init";
	private static final String EVAL_TERM = "shell_eval";

	private final IStrategoCommon strategoCommon;
	private final ITermFactory termFactory;

	/**
	 * We keep track of a global environment.
	 * It is expected to be passed along all eval iterations.
	 */
	private IStrategoTerm env;

	/**
	 * Construct a new {@link StrategoEvaluationStrategy}.
	 * On the first {@link #evaluate(IStrategoTerm, IContext) call an
	 * environment is initialised and subsequent calls will use this
	 * envirnonment.
	 *
	 * @param strategoCommon
	 *            The interface for all Statego related functionality.
	 * @param termFactoryService
	 *            The {@link ITermFactoryService} for retrieving an {@link ITermFactory}.
	 */
	@Inject
	public StrategoEvaluationStrategy(IStrategoCommon strategoCommon,
			ITermFactoryService termFactoryService) {
		this.strategoCommon = strategoCommon;
		this.termFactory = termFactoryService.getGeneric();
	}

	@Override
	public String name() {
		return "stratego";
	}

	@Override
	public IStrategoTerm evaluate(IStrategoTerm term, IContext context) throws MetaborgException {
		// initialize the environment
		if (env == null) {
			env = strategoCommon.invoke(context.language(), context, term, INIT_TERM);
		}

		IStrategoTerm result = strategoCommon.invoke(context.language(), context,
				termFactory.makeTuple(term, env), EVAL_TERM);

		if (Tools.isTermTuple(result)) {
			int subterms = result.getSubtermCount();
			if (subterms == 2) {
				IStrategoTerm newEnv = result.getSubterm(1);
				IStrategoTerm value = result.getSubterm(0);
				env = newEnv;
				return value;
			} else {
				throw new MetaborgException(String.format(
						"Evaluation result expected: Tuple of 2. Found: Tuple of %d", subterms));
			}
		} else {
			// TODO give useful 'found' if possible
			throw new MetaborgException(
					String.format("Evaluation result expected: Tuple. Found: %s",
							"<result.getTermType()=" + result.getTermType() + ">"));
		}

	}

}
