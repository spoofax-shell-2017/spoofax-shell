package org.metaborg.spoofax.shell.commands;

import java.awt.Color;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.metaborg.spoofax.shell.core.StyledText;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

/**
 * Shows descriptions of all commands, or one command if given.
 */
public class HelpCommand implements IReplCommand {
    private final Map<String, IReplCommand> commands;
    private final Provider<ICommandInvoker> provider;
    private final Consumer<StyledText> successHook;
    private final Consumer<StyledText> errorHook;

    /**
     * Instantiates a new HelpCommand.
     *
     * @param commands
     *            The commands map, which contains all names too.
     * @param commandInvokerProvider
     *            A {@link Provider} for an {@link ICommandInvoker}.
     * @param successHook
     *            Called when a given command and its description was found.
     * @param errorHook
     *            Called when a given command was not found.
     */
    @Inject
    public HelpCommand(Map<String, IReplCommand> commands,
                       Provider<ICommandInvoker> commandInvokerProvider,
                       @Named("onSuccess") Consumer<StyledText> successHook,
                       @Named("onError") Consumer<StyledText> errorHook) {
        this.commands = commands;
        this.provider = commandInvokerProvider;
        this.successHook = successHook;
        this.errorHook = errorHook;
    }

    @Override
    public String description() {
        return "You just used it.";
    }

    @Override
    public void execute(String... args) {
        StringBuilder output = new StringBuilder();
        ICommandInvoker invoker = provider.get();
        Set<String> commandNames = commands.keySet();
        if (args.length > 0) {
            String commandName = args[0];
            commandNames = Collections.singleton(commandName);
            try {
                invoker.commandFromName(commandName);
            } catch (CommandNotFoundException e) {
                errorHook.accept(new StyledText(Color.RED, e.getMessage()));
            }
        }
        int longestCommand = commandNames.stream()
            .max((a, b) -> Integer.compare(a.length(), b.length())).orElse("").length();

        // @formatter:off
        // commandPrefix()commandfoobar commandDescription
        // commandPrefix()commandfoo    commandDescription
        commandNames.forEach(name ->
            output.append(invoker.commandPrefix()).append(name)
                  .append(StringUtils.repeat(' ', longestCommand - name.length() + 1))
                  .append(commands.get(name).description()).append('\n'));
        // @formatter:on
        successHook.accept(new StyledText(output.toString()));
    }

}
