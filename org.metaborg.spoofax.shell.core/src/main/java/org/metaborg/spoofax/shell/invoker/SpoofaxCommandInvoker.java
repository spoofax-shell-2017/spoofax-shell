package org.metaborg.spoofax.shell.invoker;

import java.util.Map;

import org.metaborg.spoofax.shell.commands.IReplCommand;

import com.google.common.collect.Maps;
import com.google.inject.Inject;

/**
 * Default implementation of an {@link ICommandInvoker}.
 */
public class SpoofaxCommandInvoker implements ICommandInvoker {
    private final Map<String, IReplCommand> defaults;
    private final Map<String, IReplCommand> commands;

    /**
     * Instantiates a new SpoofaxCommandInvoker.
     *
     * @param defaults
     *            The commands, with their command names as key (without prefix).
     */
    @Inject
    public SpoofaxCommandInvoker(Map<String, IReplCommand> defaults) {
        this.defaults = defaults;
        this.commands = Maps.newConcurrentMap();
        this.resetCommands();
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

    @Override
    public void addCommand(String name, IReplCommand command) {
        commands.put(name, command);
    }

    @Override
    public Map<String, IReplCommand> getCommands() {
        return commands;
    }

    @Override
    public void resetCommands() {
        commands.clear();
        commands.putAll(defaults);
    }
}
