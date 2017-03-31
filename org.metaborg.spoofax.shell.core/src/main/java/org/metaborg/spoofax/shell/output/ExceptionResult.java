package org.metaborg.spoofax.shell.output;

/**
 * Represents a command that has resulted in a {@link Throwable}.
 */
public class ExceptionResult implements IResult {
    private final Throwable thrown;

    /**
     * Create an {@link ExceptionResult} from a {@link Throwable}.
     * @param thrown  the {@link Throwable}
     */
    public ExceptionResult(Throwable thrown) {
        this.thrown = thrown;
    }

    @Override
    public void accept(IResultVisitor visitor) {
        visitor.visitException(thrown);
    }

}
