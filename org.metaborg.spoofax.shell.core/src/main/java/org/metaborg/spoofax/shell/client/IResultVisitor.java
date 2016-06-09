package org.metaborg.spoofax.shell.client;

import org.metaborg.spoofax.shell.output.FailResult;
import org.metaborg.spoofax.shell.output.ISpoofaxResult;
import org.metaborg.spoofax.shell.output.StyledText;

/**
 * An {@link IResultVisitor} is for visiting the results of a command or expression that was
 * executed, whether it was an error or a successful result.
 */
public interface IResultVisitor {

    /**
     * Visit an {@link ISpoofaxResult}.
     *
     * @param result
     *            The result to be displayed.
     */
    void visitResult(ISpoofaxResult<?> result);

    /**
     * Visit a {@link StyledText} message.
     *
     * @param message
     *            The message to be displayed.
     */
    void visitMessage(StyledText message);

    /**
     * Visit an error.
     *
     * @param errorResult
     *            The {@link IResult} representing an error.
     */
    void visitFailure(FailResult errorResult);

    /**
     * Visits an exception that was caught at some point in producing a result.
     *
     * @param thrown The {@link Throwable} that was caught.
     */
    void visitException(Throwable thrown);
}
