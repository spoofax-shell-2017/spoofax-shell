package org.metaborg.spoofax.shell.output;

import java.util.List;
import java.util.Optional;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.context.IContext;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.unit.IUnit;
import org.metaborg.spoofax.shell.client.IResultVisitor;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * Represents an {@link ISpoofaxResult} as returned by the {@link SpoofaxCommand} (TODO).
 * Wraps Spoofax {@link IUnit} of various types.
 * @param <T> the wrapped subtype of {@link IUnit}
 */
public interface ISpoofaxResult<T extends IUnit> extends IResult {
    /**
     * Returns the ast of this unit as a {@link IStrategoTerm} if present.
     * @return a {@link IStrategoTerm} or null
     */
    Optional<IStrategoTerm> ast();

    /**
     * Returns the context of this unit if present.
     * @return a {@link IContext}
     */
    Optional<IContext> context();

    /**
     * Returns a list of {@link IMessage} for all contained units.
     * @return a list of {@link IMessage}
     */
    List<IMessage> messages();

    /**
     * Returns the source {@link FileObject} for this unit.
     * @return the source {@link FileObject} for this unit
     */
    FileObject source();

    /**
     * Returns the result of this unit.
     * @return the result
     */
    StyledText styled();

    /**
     * @return the source text from which this ISpoofaxResult came about.
     */
    String sourceText();

    /**
     * Returns the wrapped unit.
     * @return the unit
     */
    T unit();

    /**
     * Returns a boolean indicating whether this unit is valid.
     * @return a boolean
     */
    boolean valid();

    @Override
    default void accept(IResultVisitor visitor) {
        visitor.visitResult(this);
    }
}
