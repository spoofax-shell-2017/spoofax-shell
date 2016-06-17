package org.metaborg.spoofax.shell.output;

import java.util.function.Consumer;

import org.metaborg.spoofax.shell.client.IResultVisitor;
import org.metaborg.spoofax.shell.commands.IReplCommand;

/**
 * Typedef interface. All {@link IReplCommand}s return an {@link IResult} (which of course can be a
 * lambda if one desires). The {@link IResult} can accept an {@link IResultVisitor}, and subclasses
 * decide which visit method of the visitor to call using double dispatch.
 */
@FunctionalInterface
public interface IResult extends Consumer<IResultVisitor> {

    @Override
    void accept(IResultVisitor visitor);

}
