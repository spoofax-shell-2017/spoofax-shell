package org.metaborg.spoofax.shell.commands;

import java.util.Map;

import com.google.inject.Inject;

/**
 * Default implementation of an {@link ICommandInvoker}.
 */
public class SpoofaxCommandInvoker implements ICommandInvoker {
    private final Map<String, IReplCommand> commands;

    /**
     * Instantiates a new SpoofaxCommandInvoker.
     *
     * @param commands
     *            The commands, with their command names as key (without prefix).
     */
    @Inject
    SpoofaxCommandInvoker(Map<String, IReplCommand> commands) {
        this.commands = commands;
    }

    @Override
    public IReplCommand commandFromName(String commandName) throws CommandNotFoundException {
        if (!commands.containsKey(commandName)) {
            throw new CommandNotFoundException(commandName);
        }
        return commands.get(commandName);
    }

    @Override
    public String commandPrefix() {
        return ":";
    }

}
