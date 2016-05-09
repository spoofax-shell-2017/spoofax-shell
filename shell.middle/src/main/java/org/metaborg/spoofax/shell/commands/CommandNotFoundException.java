package org.metaborg.spoofax.shell.commands;

/**
 * Thrown when a {@link IReplCommand} has not been found.
 */
public class CommandNotFoundException extends Exception {
    private static final long serialVersionUID = 1L;

    private String commandName;

    /**
     * @param commandName
     *            The command name for which no {@link IReplCommand} could be found.
     */
    public CommandNotFoundException(String commandName) {
        this.commandName = commandName;
    }

    /**
     * @return the command name for which no {@link IReplCommand} could be found.
     */
    public String commandName() {
        return commandName;
    }

    @Override
    public String getMessage() {
        return "Command named \"" + commandName() + "\" was not found.";
    }
}
