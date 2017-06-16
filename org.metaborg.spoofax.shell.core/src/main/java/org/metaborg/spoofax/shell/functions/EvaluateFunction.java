package org.metaborg.spoofax.shell.functions;

import java.util.Map;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.context.IContextService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.core.shell.ShellFacet;
import org.metaborg.spoofax.shell.core.IEvaluationStrategy;
import org.metaborg.spoofax.shell.output.EvaluateResult;
import org.metaborg.spoofax.shell.output.FailOrSuccessResult;
import org.metaborg.spoofax.shell.output.FailResult;
import org.metaborg.spoofax.shell.output.IResult;
import org.metaborg.spoofax.shell.output.IResultFactory;
import org.metaborg.spoofax.shell.output.ISpoofaxTermResult;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * Creates an {@link EvaluateResult} from a given {@link AnalyzeResult}.
 */
public class EvaluateFunction extends ContextualSpoofaxFunction<ISpoofaxTermResult<?>,
                                                                EvaluateResult> {
    private final Map<String, IEvaluationStrategy> evaluationStrategies;

    /**
     * Instantiate an {@link AEvalFunction}.
     *
     * @param evaluationStrategies
     *            The {@link IEvaluationStrategy} implementations, grouped by their names as keys.
     * @param contextService
     *            The {@link IContextService}.
     * @param resultFactory
     *            The {@link IResultFactory} for creating delegate commands.
     * @param project
     *            The {@link IProject} in which this command should operate.
     * @param lang
     *            The {@link ILanguageImpl} to which this command applies.
     */
    @Inject
    public EvaluateFunction(Map<String, IEvaluationStrategy> evaluationStrategies,
                            IContextService contextService, IResultFactory resultFactory,
                            @Assisted IProject project, @Assisted ILanguageImpl lang) {
        super(contextService, resultFactory, project, lang);
        this.evaluationStrategies = evaluationStrategies;
    }

    @Override
    protected FailOrSuccessResult<EvaluateResult, IResult>
            applyThrowing(IContext context, ISpoofaxTermResult<?> a) throws Exception {
        if (!a.ast().isPresent()) {
            return FailOrSuccessResult.failed(new FailResult(a));
        }
        ShellFacet facet = shellFacet(context.language());

        IEvaluationStrategy evalStrategy = evaluationStrategy(facet.getEvaluationMethod());

        IStrategoTerm result = evalStrategy.evaluate(a.ast().get(), context);

        return FailOrSuccessResult.ofSpoofaxResult(resultFactory.createEvaluateResult(a, result));
    }

    private ShellFacet shellFacet(ILanguageImpl langImpl) throws MetaborgException {
        ShellFacet facet = langImpl.facet(ShellFacet.class);
        if (facet == null) {
            throw new MetaborgException("No ESV configuration found for the REPL.");
        }
        return facet;
    }

    private IEvaluationStrategy evaluationStrategy(String evaluationMethod)
        throws MetaborgException {
        IEvaluationStrategy evalStrategy = evaluationStrategies.get(evaluationMethod);
        if (evalStrategy == null) {
            Iterable<String> quoted = evaluationStrategies.keySet().stream()
                .map(s -> '\"' + s + '\"')::iterator;
            throw new MetaborgException(
                    "Evaluation method \"%s\" not supported.\nSupported evaluation method(s): %s",
                    evaluationMethod,
                    String.join(", ", quoted));
        }
        return evalStrategy;
    }
}
