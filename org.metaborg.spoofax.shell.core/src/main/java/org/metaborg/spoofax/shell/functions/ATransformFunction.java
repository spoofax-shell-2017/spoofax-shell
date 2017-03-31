package org.metaborg.spoofax.shell.functions;

import java.util.Collection;

import org.metaborg.core.action.ITransformAction;
import org.metaborg.core.context.IContext;
import org.metaborg.core.context.IContextService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.core.transform.ISpoofaxTransformService;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxTransformUnit;
import org.metaborg.spoofax.shell.output.AnalyzeResult;
import org.metaborg.spoofax.shell.output.FailOrSuccessResult;
import org.metaborg.spoofax.shell.output.IResult;
import org.metaborg.spoofax.shell.output.IResultFactory;
import org.metaborg.spoofax.shell.output.TransformResult;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * Creates an {@link TransformResult} from a given {@link AnalyzeResult}.
 */
public class ATransformFunction extends ContextualSpoofaxFunction<AnalyzeResult, TransformResult> {

    private final ISpoofaxTransformService transformService;
    private final ITransformAction action;

    /**
     * Instantiate a new {@link ATransformFunction}.
     *
     * @param contextService
     *            The {@link IContextService}.
     * @param transformService
     *            The {@link ISpoofaxTransformService}.
     * @param resultFactory
     *            The {@link IResultFactory}.
     * @param project
     *            The {@link IProject} in which this command should operate.
     * @param lang
     *            The {@link ILanguageImpl} to which this command applies.
     * @param action
     *            The {@link ITransformAction} that this command executes.
     */
    @Inject
    public ATransformFunction(IContextService contextService,
                              ISpoofaxTransformService transformService,
                              IResultFactory resultFactory, @Assisted IProject project,
                              @Assisted ILanguageImpl lang, @Assisted ITransformAction action) {
        super(contextService, resultFactory, project, lang);
        this.transformService = transformService;
        this.action = action;
    }

    @Override
    protected FailOrSuccessResult<TransformResult, IResult>
            applyThrowing(IContext context, AnalyzeResult a) throws Exception {
        Collection<ISpoofaxTransformUnit<ISpoofaxAnalyzeUnit>> transform =
            transformService.transform(a.unit(), context, action.goal());
        return FailOrSuccessResult
            .ofSpoofaxResult(resultFactory.createATransformResult(transform.iterator().next()));
    }
}
