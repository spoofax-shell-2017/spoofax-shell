package org.metaborg.spoofax.shell.commands;

/**
 * An interface for binding and executing {@link IReplCommand}s.
 */
public interface ICommandInvoker {

    /**
     * @return The command executed for evaluation.
     */
    IReplCommand evaluationCommand();

    /**
     * @param commandName
     *            The name of an {@link IReplCommand}.
     * @return The {@link IReplCommand} bound to {@code commandName}.
     * @throws CommandNotFoundException when the command could not be found.
     */
    IReplCommand commandFromName(String commandName) throws CommandNotFoundException;

    /**
     * @return The prefix of the {@link IReplCommand}s. The {@link IReplCommand}s are stored without
     *         this prefix.
     */
    String commandPrefix();

    /**
     * Ensure that the given parameter is returned without the {@link #commandPrefix()}.
     *
     * @param optionallyPrefixedCommandName
     *            And optionally prefixed command name.
     * @return The command name without prefix if found. Otherwise just the same String as the
     *         argument
     */
    default String ensureNoPrefix(String optionallyPrefixedCommandName) {
        if (optionallyPrefixedCommandName.startsWith(commandPrefix())) {
            return optionallyPrefixedCommandName.substring(commandPrefix().length());
        }
        // No prefix found, so just return the argument.
        return optionallyPrefixedCommandName;
    }

    /**
     * Execute the {@link IReplCommand} which is bound to the given command name, minus the prefix.
     *
     * @param optionallyPrefixedCommandName
     *            The name of the {@link IReplCommand} to be executed.
     * @throws CommandNotFoundException when the command could not be found.
     */
    default void execute(String optionallyPrefixedCommandName) throws CommandNotFoundException {
        if (optionallyPrefixedCommandName.startsWith(commandPrefix())) {
            commandFromName(optionallyPrefixedCommandName.substring(commandPrefix().length()))
                .execute();
            return;
        }
        evaluationCommand().execute(optionallyPrefixedCommandName);
    }
}
