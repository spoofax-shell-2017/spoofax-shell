package org.metaborg.spoofax.shell.output;

import org.metaborg.core.unit.IUnit;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * Represents an {@link AbstractResult} as returned by the {@link SpoofaxCommand}.
 * Wraps Spoofax {@link IUnit} of various types. Additionally, stores an AST.
 * @param <T> the wrapped subtype of {@link IUnit}
 */
//@formatter:off
public abstract class AbstractSpoofaxTermResult<T extends IUnit>
        extends AbstractSpoofaxResult<T> implements ISpoofaxTermResult<T> {
//@formatter:on
    private final IStrategoCommon common;

    /**
     * Constructor for an {@link AbstractResult}.
     * @param common  the {@link IStrategoCommon} service.
     * @param unit    the wrapped {@link IUnit}.
     */
    public AbstractSpoofaxTermResult(IStrategoCommon common, T unit) {
        super(unit);
        this.common = common;
    }

    /**
     * Returns a textual representation of a term.
     * @param term  the term
     * @return a string
     */
    public StyledText toString(IStrategoTerm term) {
        return new StyledText(common.toString(term));
    }

    @Override
    public StyledText styled() {
        if (!valid() || !ast().isPresent()) {
            return new StyledText(messages().toString());
        }
        return toString(ast().get());
    }

}
