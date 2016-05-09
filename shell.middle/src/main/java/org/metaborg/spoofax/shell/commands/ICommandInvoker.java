package org.metaborg.spoofax.shell.commands;

/**
 * An interface for binding and executing {@link IReplCommand}s.
 */
public interface ICommandInvoker {

    /**
     * Add an {@link IReplCommand} to be executed when {@code commandName} is given, under the given
     * description.
     *
     * @param commandName
     *            The name to which the command is bound.
     * @param description
     *            Description of what the command does.
     * @param c
     *            The {@link IReplCommand} to be bound to {@code commandName}.
     */
    void addCommand(String commandName, String description, IReplCommand c);

    /**
     * Same as {@link #addCommand(String, String, IReplCommand)}, with the empty {@link String}
     * {@code ""} as description.
     *
     * @param commandName
     *            The name to which the command is bound.
     * @param c
     *            The {@link IReplCommand} to be bound to {@code commandName}.
     */
    default void addCommand(String commandName, IReplCommand c) {
        addCommand(commandName, "", c);
    }

    /**
     * Set the command to be executed for evaluation. This command is executed when the input string
     * does not start with {@link #commandPrefix()}.
     *
     * @param eval
     *            The {@link IEvaluationCommand} to be executed for evaluation.
     */
    void setEvaluationCommand(IEvaluationCommand eval);

    /**
     * @return The command executed for evaluation.
     */
    IEvaluationCommand evaluationCommand();

    /**
     * @param commandName
     *            The name of an {@link IReplCommand}.
     * @return The description of what the {@link IReplCommand} bound to {@code commandName} does.
     */
    String commandDescriptionFromName(String commandName);

    /**
     * @param commandName
     *            The name of an {@link IReplCommand}.
     * @return The {@link IReplCommand} bound to {@code commandName}
     */
    IReplCommand commandFromName(String commandName);

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
     */
    default void execute(String optionallyPrefixedCommandName) {
        if (optionallyPrefixedCommandName.startsWith(commandPrefix())) {
            commandFromName(optionallyPrefixedCommandName.substring(commandPrefix().length()))
                .execute();
            return;
        }
        evaluationCommand().evaluate(optionallyPrefixedCommandName);
    }
}
