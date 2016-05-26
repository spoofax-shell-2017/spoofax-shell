package org.metaborg.spoofax.shell.hooks;

import java.util.function.Consumer;

import org.metaborg.spoofax.shell.commands.SpoofaxCommand;
import org.metaborg.spoofax.shell.output.ISpoofaxResult;

/**
 * Called with the result of executing a {@link SpoofaxCommand}.
 *
 * @see {@link ISpoofaxResult}.
 */
public interface ResultHook extends Consumer<ISpoofaxResult<?>> {
}