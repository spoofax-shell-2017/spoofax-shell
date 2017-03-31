package org.metaborg.spoofax.shell.functions;

import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.shell.output.ExceptionResult;
import org.metaborg.spoofax.shell.output.FailOrSuccessResult;
import org.metaborg.spoofax.shell.output.IResult;
import org.metaborg.spoofax.shell.output.IResultFactory;
import org.metaborg.spoofax.shell.output.ISpoofaxResult;

import com.google.inject.Inject;

/**
 * Command for processing a String as an expression in some language.
 *
 * @param <In>
 *            The argument type of the {@link #apply(Object)} method.
 * @param <Success>
 *            The type of a successful {@link ISpoofaxResult}.
 */
//@formatter:off
public abstract class AbstractSpoofaxFunction<In, Success extends ISpoofaxResult<?>>
        implements FailableFunction<In, Success, IResult> {
//@formatter:on
    protected final IResultFactory resultFactory;
    protected final IProject project;
    protected final ILanguageImpl lang;

    /**
     * Instantiate a {@link AbstractSpoofaxFunction}.
     *
     * @param resultFactory
     *            The {@link IResultFactory}.
     * @param project
     *            The {@link IProject} in which this command should operate.
     * @param lang
     *            The {@link ILanguageImpl} to which this command applies.
     */
    @Inject
    public AbstractSpoofaxFunction(IResultFactory resultFactory, IProject project,
                                   ILanguageImpl lang) {
        this.resultFactory = resultFactory;
        this.project = project;
        this.lang = lang;
    }

    /**
     * Apply the argument, optionally throwing an exception. The exception is caught in the
     * {@link #apply(Object)} function, to be returned as an {@link ExceptionResult}.
     *
     * @param a
     *            The input argument.
     * @return A {@link FailOrSuccessResult failure or success}.
     * @throws Exception
     *             When applying this function resulted in an error.
     */
    protected abstract FailOrSuccessResult<Success, IResult> applyThrowing(In a) throws Exception;

    @Override
    public FailOrSuccessResult<Success, IResult> apply(In a) {
        try {
            return this.applyThrowing(a);
        } catch (Exception e) {
            return FailOrSuccessResult.failed(new ExceptionResult(e));
        }
    }
}