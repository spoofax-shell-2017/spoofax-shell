package org.metaborg.spoofax.shell.output;

/**
 * An {@link IResultVisitor} is for visiting the results of a command or expression that was
 * executed, whether it was an error or a successful result.
 */
public interface IResultVisitor {

    /**
     * Visit an {@link ISpoofaxResult}.
     *
     * @param result
     *            The result to be visited.
     */
    void visitResult(ISpoofaxResult<?> result);

    /**
     * Visit a {@link StyledText} message.
     *
     * @param message
     *            The message to be visited.
     */
    void visitMessage(StyledText message);

    /**
     * Visit a failure.
     *
     * @param failResult
     *            The {@link FailResult} representing a failure.
     */
    void visitFailure(FailResult failResult);

    /**
     * Visits an exception that was caught at some point in producing a result.
     *
     * @param thrown The {@link Throwable} that was caught.
     */
    void visitException(Throwable thrown);
}
