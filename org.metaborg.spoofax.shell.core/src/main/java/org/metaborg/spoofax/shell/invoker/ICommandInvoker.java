package org.metaborg.spoofax.shell.invoker;

import java.util.Arrays;
import java.util.Map;

import org.metaborg.spoofax.shell.commands.IReplCommand;

/**
 * An interface for binding and executing {@link IReplCommand}s.
 */
public interface ICommandInvoker {

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
            // FIXME: create sensible way to set default
            commandFromName("eval").execute(optionallyPrefixedCommandName);
        }
    }

    /**
     * Add a command to the list of available commands.
     * @param name    The name of the {@link IReplCommand}
     * @param command The {@link IReplCommand}
     */
    void addCommand(String name, IReplCommand command);

    /**
     * Get a list of all available commands.
     * @return a {@link Map} from command name to {@link IReplCommand}
     */
    Map<String, IReplCommand> getCommands();

    /**
     * Reset the list of available commands to its initial default list.
     */
    void resetCommands();
}
