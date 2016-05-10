package org.metaborg.spoofax.shell.commands;

/**
 * Interface for REPL commands. Used together with {@link ICommandInvoker}, instances of
 * implementors of this interface can be bound to names and descriptions.
 */
public interface IReplCommand {

    /**
     * @return the description of this command.
     */
    String description();

    /**
     * Execute this command.
     *
     * @param args The arguments for this command.
     */
    void execute(String... args);
}
