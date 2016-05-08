package org.metaborg.spoofax.shell.commands;

/**
 * Interface for REPL commands. Used together with {@link ICommandInvoker}, instances of
 * implementors of this interface can be bound to names and descriptions.
 */
public interface IReplCommand {

    /**
     * Execute this command.
     */
    public void execute();
}
