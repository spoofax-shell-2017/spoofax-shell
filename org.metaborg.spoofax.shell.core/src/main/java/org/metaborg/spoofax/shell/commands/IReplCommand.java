package org.metaborg.spoofax.shell.commands;

import org.metaborg.core.MetaborgException;
import org.metaborg.spoofax.shell.client.IResult;
import org.metaborg.spoofax.shell.client.IResultVisitor;
import org.metaborg.spoofax.shell.invoker.ICommandInvoker;

/**
 * Interface for REPL commands. Used together with {@link ICommandInvoker}, instances of
 * implementors of this interface can be bound to names and descriptions.
 */
public interface IReplCommand {

    /**
     * @return The description of this command.
     */
    String description();

    /**
     * Execute this command.
     *
     * @param args
     *            The arguments for this command.
     * @return A visitable {@link IResult result} of this command. Use {@link IResultVisitor} and
     *         call {@link IResult#accept(IResultVisitor)} with it.
     * @throws MetaborgException
     *             When something goes wrong during execution.
     */
    IResult execute(String... args) throws MetaborgException;
}
