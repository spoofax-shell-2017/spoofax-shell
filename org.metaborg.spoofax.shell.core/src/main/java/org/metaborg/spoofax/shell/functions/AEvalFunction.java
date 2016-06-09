package org.metaborg.spoofax.shell.functions;

import java.util.Map;

import org.metaborg.core.context.IContext;
import org.metaborg.core.context.IContextService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.core.shell.ShellFacet;
import org.metaborg.spoofax.shell.client.IResult;
import org.metaborg.spoofax.shell.core.IEvaluationStrategy;
import org.metaborg.spoofax.shell.output.AnalyzeResult;
import org.metaborg.spoofax.shell.output.FailOrSuccessResult;
import org.metaborg.spoofax.shell.output.EvaluateResult;
import org.metaborg.spoofax.shell.output.IResultFactory;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * Represents an evaluate command sent to Spoofax.
 */
public class AEvalFunction extends AbstractSpoofaxFunction<AnalyzeResult, EvaluateResult> {
    private IContextService contextService;

    @Inject
    private Map<String, IEvaluationStrategy> evaluationStrategies;

    /**
     * Instantiate an {@link PEvalFunction}.
     *
     * @param contextService
     *            The {@link IContextService}.
     * @param resultFactory
     *            The {@link IResultFactory} for creating delegate commands.
     * @param project
     *            The project in which this command should operate.
     * @param lang
     *            The language to which this command applies.
     */
    @Inject
    public AEvalFunction(IContextService contextService, IResultFactory resultFactory,
                         @Assisted IProject project, @Assisted ILanguageImpl lang) {
        super(resultFactory, project, lang);
        this.contextService = contextService;
    }

    @Override
    protected FailOrSuccessResult<EvaluateResult, IResult> applyThrowing(AnalyzeResult a)
        throws Exception {
        IContext context = a.context().orElse(contextService.get(a.source(), project, lang));
        ShellFacet facet = context.language().facet(ShellFacet.class);
        IEvaluationStrategy evalStrategy = evaluationStrategies.get(facet.getEvaluationMethod());
        IStrategoTerm result = evalStrategy.evaluate(a, context);

        return FailOrSuccessResult.ofSpoofaxResult(resultFactory.createEvaluateResult(a, result));
    }
}
