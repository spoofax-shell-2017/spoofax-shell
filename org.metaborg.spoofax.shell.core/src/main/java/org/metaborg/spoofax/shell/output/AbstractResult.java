package org.metaborg.spoofax.shell.output;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.unit.IUnit;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.shell.commands.SpoofaxCommand;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * Represents an {@link AbstractResult} as returned by the {@link SpoofaxCommand}.
 * Wraps Spoofax {@link IUnit} of various types.
 * @param <T> the wrapped subtype of {@link IUnit}
 */
public abstract class AbstractResult <T extends IUnit> implements ISpoofaxResult<IUnit> {
    private IStrategoCommon common;
    private T unit;

    /**
     * Constructor for an {@link AbstractResult}.
     * @param common  the {@link IStrategoCommon} service
     * @param unit    the wrapped unit
     */
    public AbstractResult(IStrategoCommon common, T unit) {
        this.common = common;
        this.unit = unit;
    }

    @Override
    public FileObject source() {
        return unit.source();
    }

    @Override
    public T unit() {
        return unit;
    }

    /**
     * Returns a textual representation of a term.
     * @param term  the term
     * @return a string
     */
    public StyledText toString(IStrategoTerm term) {
        return new StyledText(common.toString(term));
    }
}
