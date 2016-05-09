package org.metaborg.spoofax.shell.commands;

import java.util.HashMap;

/**
 * Default implementation of an {@link ICommandInvoker}.
 */
public class SpoofaxCommandInvoker implements ICommandInvoker {
    private HashMap<String, IReplCommand> commands;
    private HashMap<String, String> descriptions;
    private IEvaluationCommand eval;

    /**
     * Initializes data structure for storing the commands and descriptions.
     */
    public SpoofaxCommandInvoker() {
        commands = new HashMap<>();
        descriptions = new HashMap<>();
    }

    @Override
    public void addCommand(String commandName, IReplCommand c) {
        commands.put(commandName, c);
    }

    @Override
    public void addCommand(String commandName, String description, IReplCommand c) {
        addCommand(commandName, c);
        descriptions.put(commandName, description);
    }

    @Override
    public void setEvaluationCommand(IEvaluationCommand eval) {
        this.eval = eval;
    }

    @Override
    public IEvaluationCommand evaluationCommand() {
        return eval;
    }

    private void ensureCommandExists(String commandName) throws CommandNotFoundException {
        if (!commands.containsKey(commandName)) {
            throw new CommandNotFoundException(commandName);
        }
    }

    @Override
    public String commandDescriptionFromName(String commandName) throws CommandNotFoundException {
        ensureCommandExists(commandName);
        return descriptions.getOrDefault(commandName, "");
    }

    @Override
    public IReplCommand commandFromName(String commandName) throws CommandNotFoundException {
        ensureCommandExists(commandName);
        return commands.get(commandName);
    }

    @Override
    public String commandPrefix() {
        return ":";
    }

}
