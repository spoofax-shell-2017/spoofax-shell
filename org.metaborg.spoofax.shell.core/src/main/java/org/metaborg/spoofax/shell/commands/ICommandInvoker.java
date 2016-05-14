package org.metaborg.spoofax.shell.commands;

import java.util.Arrays;

/**
 * An interface for binding and executing {@link IReplCommand}s.
 */
public interface ICommandInvoker {

    /**
     * @return The command executed for evaluation.
     */
    IReplCommand evaluationCommand();

    /**
     * Returns the command with name {@code commandName}.
     *
     * @param commandName
     *            The name of an {@link IReplCommand}.
     * @return The {@link IReplCommand} bound to {@code commandName}.
     * @throws CommandNotFoundException
     *             When the command could not be found.
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
     *            An optionally prefixed command name.
     * @return The command name without prefix if found. Otherwise just the same String as the
     *         argument.
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
     * @throws CommandNotFoundException
     *             When the command could not be found.
     */
    default void execute(String optionallyPrefixedCommandName) throws CommandNotFoundException {
        if (optionallyPrefixedCommandName.startsWith(commandPrefix())) {
            String[] split = optionallyPrefixedCommandName.split("\\s+", 2);
            String commandName = split[0].substring(commandPrefix().length());
            String[] argument =
                split.length > 1 ? Arrays.copyOfRange(split, 1, split.length) : new String[0];
            commandFromName(commandName).execute(argument);
        } else {
            evaluationCommand().execute(optionallyPrefixedCommandName);
        }
    }
}
