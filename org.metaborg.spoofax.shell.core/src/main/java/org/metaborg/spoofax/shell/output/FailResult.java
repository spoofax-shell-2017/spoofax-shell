package org.metaborg.spoofax.shell.output;

/**
 * An {@link IResult} that represents a failure in execution, along with the cause of the failure.
 */
public class FailResult implements IResult {
    private final ISpoofaxResult<?> cause;

    /**
     * Create a new {@link FailResult} due to the given cause.
     * @param cause The cause of the error.
     */
    public FailResult(ISpoofaxResult<?> cause) {
        this.cause = cause;
    }

    @Override
    public void accept(IResultVisitor visitor) {
        visitor.visitFailure(this);
    }

    /**
     * @return The cause of the error.
     */
    public ISpoofaxResult<?> getCause() {
        return cause;
    }

}
