package org.metaborg.spoofax.shell.output;

import org.metaborg.core.unit.IUnit;

/**
 * Typedef interface for {@link ISpoofaxResult}s that also have an AST.
 * @param <T> The type of the wrapped {@link IUnit}.
 */
public interface ISpoofaxTermResult<T extends IUnit> extends ITermResult, ISpoofaxResult<T> {

}
