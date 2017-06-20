package org.metaborg.spoofax.shell.output;

import java.util.Optional;

import org.metaborg.core.unit.IUnit;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * Interface for {@link ISpoofaxResult}s that also have an optional AST.
 * @param <T> The type of the wrapped {@link IUnit}.
 */
public interface ISpoofaxTermResult<T extends IUnit> extends ISpoofaxResult<T> {

    /**
     * Returns the ast of this unit as a {@link IStrategoTerm} if present.
     * @return a {@link IStrategoTerm} or null
     */
    Optional<IStrategoTerm> ast();

    @Override
    default void accept(IResultVisitor visitor) {
        visitor.visitTermResult(this);
    }
}
