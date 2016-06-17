package org.metaborg.spoofax.shell.invoker;

import java.util.Arrays;
import java.util.Map;

import org.metaborg.spoofax.shell.commands.IReplCommand;
import org.metaborg.spoofax.shell.output.IResult;
import org.metaborg.spoofax.shell.output.IResultVisitor;

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
     * @return the command that will be executed by default.
     */
    IReplCommand getDefault();

    /**
     * Set the default command to the provided {@link IReplCommand}.
     * @param defaultCommand the new default {@link IReplCommand}.
     */
    void setDefault(IReplCommand defaultCommand);

    /**
     * Execute the {@link IReplCommand} which is bound to the given command name, minus the prefix.
     *
     * @param optionallyPrefixedCommandName
     *            The name of the {@link IReplCommand} to be executed.
     * @return A visitable {@link IResult} representing the result of the command. Use an
     *         {@link IResultVisitor} to visit it.
     * @throws CommandNotFoundException
     *             When the command could not be found.
     */
    default IResult execute(String optionallyPrefixedCommandName)
        throws CommandNotFoundException {
        if (optionallyPrefixedCommandName.startsWith(commandPrefix())) {
            String[] split = optionallyPrefixedCommandName.split("\\s+", 2);
            String commandName = split[0].substring(commandPrefix().length());
            String[] argument =
                split.length > 1 ? Arrays.copyOfRange(split, 1, split.length) : new String[0];
            return commandFromName(commandName).execute(argument);
        } else {
            // TODO: create sensible way to set default
            return getDefault().execute(optionallyPrefixedCommandName);
        }
    }

    /**
     * Add a command to the list of available commands.
     *
     * @param name
     *            The name of the {@link IReplCommand}
     * @param command
     *            The {@link IReplCommand}
     */
    void addCommand(String name, IReplCommand command);

    /**
     * Get a list of all available commands.
     *
     * @return a {@link Map} from command name to {@link IReplCommand}
     */
    Map<String, IReplCommand> getCommands();

    /**
     * Reset the list of available commands to its initial default list.
     */
    void resetCommands();
}
