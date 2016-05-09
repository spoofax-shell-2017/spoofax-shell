package org.metaborg.spoofax.shell.commands;

import java.util.Map;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Default implementation of an {@link ICommandInvoker}.
 */
public class SpoofaxCommandInvoker implements ICommandInvoker {
    private Map<String, IReplCommand> commands;
    private IReplCommand eval;

    /**
     * @param commands
     *            The commands, with their command names as key (without prefix).
     * @param eval
     *            The {@link SpoofaxEvaluationCommand} used for evaluation when no command prefix
     *            was given.
     */
    @Inject
    SpoofaxCommandInvoker(Map<String, IReplCommand> commands,
                          @Named("EvalCommand") IReplCommand eval) {
        this.commands = commands;
        this.eval = eval;
    }

    @Override
    public IReplCommand evaluationCommand() {
        return eval;
    }

    private void ensureCommandExists(String commandName) throws CommandNotFoundException {
        if (!commands.containsKey(commandName)) {
            throw new CommandNotFoundException(commandName);
        }
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
