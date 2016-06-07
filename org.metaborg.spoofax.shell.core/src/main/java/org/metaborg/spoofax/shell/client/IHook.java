package org.metaborg.spoofax.shell.client;

import java.util.function.Consumer;

import org.metaborg.spoofax.shell.commands.IReplCommand;

/**
 * Typedef interface. All {@link IReplCommand}s return a hook, which can be a lambda. The reason
 * that hooks are returned is to allow the client to process it wherever and whenever it needs.
 */
@FunctionalInterface
public interface IHook extends Consumer<IDisplay> {

    @Override
    void accept(IDisplay display);

}
