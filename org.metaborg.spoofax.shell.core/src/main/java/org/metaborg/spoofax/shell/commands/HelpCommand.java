package org.metaborg.spoofax.shell.commands;

import java.awt.Color;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.metaborg.spoofax.shell.core.StyledText;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Shows descriptions of all commands, or one command if given.
 */
public class HelpCommand implements IReplCommand {
    private ICommandInvoker invoker;
    private final Consumer<StyledText> successHook;
    private final Consumer<StyledText> errorHook;

    /**
     * Instantiates a new HelpCommand.
     *
     * @param invoker
     *            The {@link ICommandInvoker}.
     * @param successHook
     *            Called when a given command and its description was found.
     * @param errorHook
     *            Called when a given command was not found.
     */
    @Inject
    public HelpCommand(ICommandInvoker invoker,
                       @Named("onSuccess") Consumer<StyledText> successHook,
                       @Named("onError") Consumer<StyledText> errorHook) {
        this.invoker = invoker;
        this.successHook = successHook;
        this.errorHook = errorHook;
    }

    @Override
    public String description() {
        return "You just used it.";
    }

    @Override
    public void execute(String... args) {
        try {
            StringBuilder output = new StringBuilder();
            Map<String, IReplCommand> commands = invoker.getCommands();

            Set<String> commandNames;
            if (args.length > 0) {
                commandNames = Collections.singleton(args[0]);
                invoker.commandFromName(args[0]);
            } else {
                commandNames = commands.keySet();
            }

            int longestCommand = commandNames.stream()
                .max((a, b) -> Integer.compare(a.length(), b.length())).orElse("").length();

            // @formatter:off
            commandNames.forEach(name ->
                output.append(invoker.commandPrefix()).append(name)
                      .append(StringUtils.repeat(' ', longestCommand - name.length() + 1))
                      .append(commands.get(name).description()).append('\n'));
            // @formatter:on

            successHook.accept(new StyledText(output.toString()));
        } catch (CommandNotFoundException e) {
            errorHook.accept(new StyledText(Color.RED, e.getMessage()));
        }
    }

}
