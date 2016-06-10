package org.metaborg.spoofax.shell.functions;

import java.util.Collection;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.action.ITransformAction;
import org.metaborg.core.context.IContext;
import org.metaborg.core.context.IContextService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.core.transform.ISpoofaxTransformService;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxTransformUnit;
import org.metaborg.spoofax.shell.client.IResult;
import org.metaborg.spoofax.shell.output.FailOrSuccessResult;
import org.metaborg.spoofax.shell.output.IResultFactory;
import org.metaborg.spoofax.shell.output.ParseResult;
import org.metaborg.spoofax.shell.output.TransformResult;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * Creates a {@link TransformResult} from a given {@link ParseResult}.
 */
public class PTransformFunction extends AbstractSpoofaxFunction<ParseResult, TransformResult> {

    private final IContextService contextService;
    private final ISpoofaxTransformService transformService;
    private final ITransformAction action;

    /**
     * Instantiate a new {@link PTransformFunction}.
     *
     * @param contextService
     *            The {@link IContextService}.
     * @param transformService
     *            The {@link ISpoofaxTransformService}.
     * @param resultFactory
     *            The {@link ResultFactory}.
     * @param project
     *            The {@link IProject} in which this command should operate.
     * @param lang
     *            The {@link ILanguageImpl} to which this command applies.
     * @param action
     *            The {@link ITransformAction} that this command executes.
     */
    @Inject
    public PTransformFunction(IContextService contextService,
                              ISpoofaxTransformService transformService,
                              IResultFactory resultFactory, @Assisted IProject project,
                              @Assisted ILanguageImpl lang, @Assisted ITransformAction action) {
        super(resultFactory, project, lang);
        this.contextService = contextService;
        this.transformService = transformService;
        this.action = action;
    }

    @Override
    protected FailOrSuccessResult<TransformResult, IResult> applyThrowing(ParseResult a)
        throws MetaborgException {
        IContext context;
        if (a.context().isPresent()) {
            context = a.context().get();
        } else {
            context = contextService.get(a.source(), project, lang);
        }

        Collection<ISpoofaxTransformUnit<ISpoofaxParseUnit>> transform =
            transformService.transform(a.unit(), context, action.goal());
        return FailOrSuccessResult
            .ofSpoofaxResult(resultFactory.createPTransformResult(transform.iterator().next()));
    }
}
