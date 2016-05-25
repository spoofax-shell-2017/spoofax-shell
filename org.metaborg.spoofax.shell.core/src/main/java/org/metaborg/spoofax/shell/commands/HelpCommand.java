package org.metaborg.spoofax.shell.commands;

import java.awt.Color;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.metaborg.spoofax.shell.core.StyledText;
import org.metaborg.spoofax.shell.invoker.CommandNotFoundException;
import org.metaborg.spoofax.shell.invoker.ICommandInvoker;

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

    private String formathelp(Map<String, IReplCommand> commands) {
        int longestCommand = commands.keySet().stream().mapToInt(a -> a.length()).max().orElse(0);
        String format = "%-" + longestCommand + "s %s";

        return commands.keySet().stream().flatMap(name -> {
            String[] sname = name.split("\\R");
            String[] sdesc = commands.get(name).description().split("\\R");

            return IntStream.range(0, Math.max(sname.length, sdesc.length))
                    .mapToObj(idx -> String.format(format,
                                                   idx < sname.length ? sname[idx] : "",
                                                   idx < sdesc.length ? sdesc[idx] : ""));
        }).collect(Collectors.joining("\n"));
    }

    @Override
    public void execute(String... args) {
        try {
            Map<String, IReplCommand> commands;
            if (args.length > 0) {
                IReplCommand command = invoker.commandFromName(args[0]);
                commands = Collections.singletonMap(args[0], command);
            } else {
                commands = invoker.getCommands();
            }

            successHook.accept(new StyledText(formathelp(commands)));
        } catch (CommandNotFoundException e) {
            errorHook.accept(new StyledText(Color.RED, e.getMessage()));
        }
    }

}
