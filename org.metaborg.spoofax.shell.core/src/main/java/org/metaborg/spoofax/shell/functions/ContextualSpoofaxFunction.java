package org.metaborg.spoofax.shell.functions;

import java.util.Optional;

import org.metaborg.core.context.IContext;
import org.metaborg.core.context.IContextService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.shell.client.IResult;
import org.metaborg.spoofax.shell.output.FailOrSuccessResult;
import org.metaborg.spoofax.shell.output.IResultFactory;
import org.metaborg.spoofax.shell.output.ISpoofaxResult;

/**
 * Abstract base class for all {@link AbstractSpoofaxFunction}s that are applied within an
 * {@link IContext}. Upon applying this function, the context is retrieved from the input argument.
 * If the context is not present (i.e. {@link ISpoofaxResult#context()} is an
 * {@link Optional#empty() empty Optional}), it is created with the {@link IContextService} given
 * upon construction of this function.
 *
 * @param <In>
 *            The argument type of the {@link #apply(In)} method.
 * @param <Success>
 *            The type of a successful {@link ISpoofaxResult}.
 */
//@formatter:off
public abstract class ContextualSpoofaxFunction<In extends ISpoofaxResult<?>,
                                                Success extends ISpoofaxResult<?>>
        extends AbstractSpoofaxFunction<In, Success> {
//@formatter:on
    private final IContextService contextService;

    /**
     * Instantiate a {@link ContextualSpoofaxFunction}.
     *
     * @param contextService
     *            The {@link IContextService} with which to create a new {@link IContext} if
     *            necessary.
     * @param resultFactory
     *            The {@link ResulFactory}.
     * @param project
     *            The project in which this command should operate.
     * @param lang
     *            The language to which this command applies.
     */
    public ContextualSpoofaxFunction(IContextService contextService, IResultFactory resultFactory,
                                     IProject project, ILanguageImpl lang) {
        super(resultFactory, project, lang);
        this.contextService = contextService;
    }

    /**
     * Apply this function to the given argument, within the given {@link IContext}. The context is
     * that of the input parameter, or a new context created with the {@link IContextService} if the
     * input parameter does not have a context.
     *
     * @param context
     *            The {@link IContext} in which to apply this function. Either the context of the
     *            input argument if present, or a newly created one.
     * @param a
     *            The input argument.
     * @return A {@link FailOrSuccessResult failure or success}.
     * @throws Exception
     *             When applying this function resulted in an error.
     */
    protected abstract FailOrSuccessResult<Success, IResult> applyThrowing(IContext context, In a)
        throws Exception;

    @Override
    protected FailOrSuccessResult<Success, IResult> applyThrowing(In a) throws Exception {
        final IContext context;
        if (a.context().isPresent()) {
            context = a.context().get();
        } else {
            context = contextService.get(a.source(), project, lang);
        }
        return applyThrowing(context, a);
    }
}