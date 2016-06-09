package org.metaborg.spoofax.shell.output;

import org.metaborg.spoofax.shell.client.IResult;
import org.metaborg.spoofax.shell.client.IResultVisitor;

public class ExceptionResult implements IResult {
    private Throwable thrown;

    public ExceptionResult(Throwable thrown) {
        this.thrown = thrown;
    }

    @Override
    public void accept(IResultVisitor visitor) {
        visitor.visitException(thrown);
    }

}
