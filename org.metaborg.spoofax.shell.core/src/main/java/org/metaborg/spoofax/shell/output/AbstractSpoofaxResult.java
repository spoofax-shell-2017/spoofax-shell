package org.metaborg.spoofax.shell.output;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.unit.IUnit;

/**
 * Represents an {@link AbstractSpoofaxResult} as returned by the {@link IReplCommand}.
 * Wraps Spoofax {@link IUnit} of various types.
 * @param <T> the wrapped subtype of {@link IUnit}
 */
public abstract class AbstractSpoofaxResult<T extends IUnit> implements ISpoofaxResult<T> {
    private T unit;

    /**
     * Constructor for an {@link AbstractResult}.
     * @param unit    the wrapped {@link IUnit}
     */
    public AbstractSpoofaxResult(T unit) {
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
}
