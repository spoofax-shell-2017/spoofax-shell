package org.metaborg.spoofax.shell.client;

import java.util.function.Consumer;

import org.metaborg.spoofax.shell.commands.IReplCommand;

/**
 * Typedef interface. All {@link IReplCommand}s return a hook, which can be a lambda. The reason
 * that hooks are returned is to allow the client to process it wherever and whenever it needs.
 */
@FunctionalInterface
public interface IResult extends Consumer<IResultVisitor> {

    @Override
    void accept(IResultVisitor visitor);

}
