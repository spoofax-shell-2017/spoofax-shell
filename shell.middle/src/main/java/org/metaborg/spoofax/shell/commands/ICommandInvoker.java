package org.metaborg.spoofax.shell.commands;

/**
 * An interface for binding and executing {@link IReplCommand}s.
 */
public interface ICommandInvoker {

    /**
     * Add a {@link IReplCommand} to be executed when {@code commandName} is given, under the given
     * description.
     * 
     * @param commandName
     *            The name to which the command is bound.
     * @param description
     *            Description of what the command does.
     * @param c
     *            The {@link IReplCommand} to be bound to {@code commandName}.
     */
    public void addCommand(String commandName, String description, IReplCommand c);

    /**
     * Same as {@link #addCommand(String, String, IReplCommand)}, with the empty {@link String}
     * {@code ""} as description.
     * 
     * @param commandName
     * @param c
     */
    public default void addCommand(String commandName, IReplCommand c) {
        addCommand(commandName, "", c);
    }

    /**
     * @param commandName
     *            The name of an {@link IReplCommand}.
     * @return The description of what the {@link IReplCommand} bound to {@code commandName} does.
     */
    public String commandDescriptionFromName(String commandName);

    /**
     * @param commandName
     *            The name of an {@link IReplCommand}.
     * @return The {@link IReplCommand} bound to {@code commandName}
     */
    public IReplCommand commandFromName(String commandName);

    /**
     * @return The prefix of the {@link IReplCommand}s. The {@link IReplCommand}s are stored without
     *         this prefix.
     */
    public String commandPrefix();

    /**
     * Ensure that the given parameter is returned without the {@link #commandPrefix()}.
     * 
     * @param optionallyPrefixedCommandName
     *            And optionally prefixed command name.
     * @return The command name without prefix if found. Otherwise just the same String as the
     *         argument
     */
    public default String ensureNoPrefix(String optionallyPrefixedCommandName) {
        if (optionallyPrefixedCommandName.startsWith(commandPrefix()))
            return optionallyPrefixedCommandName.substring(commandPrefix().length());
        // No prefix found, so just return the argument.
        return optionallyPrefixedCommandName;
    }

    /**
     * Execute the {@link IReplCommand} which is bound to the given command name, minus the prefix.
     * 
     * @param prefixedCommandName
     *            The name of the {@link IReplCommand} to be executed.
     */
    public default void execute(String prefixedCommandName) {
        if (prefixedCommandName.startsWith(commandPrefix()))
            commandFromName(prefixedCommandName.substring(commandPrefix().length())).execute();
    }
}
