package org.metaborg.spoofax.shell.commands;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.metaborg.core.MetaborgException;
import org.metaborg.spoofax.shell.client.IResult;
import org.metaborg.spoofax.shell.invoker.CommandNotFoundException;
import org.metaborg.spoofax.shell.invoker.ICommandInvoker;
import org.metaborg.spoofax.shell.output.StyledText;

import com.google.inject.Inject;

/**
 * Shows descriptions of all commands, or one command if given.
 */
public class HelpCommand implements IReplCommand {
    private final ICommandInvoker invoker;

    /**
     * Instantiates a new HelpCommand.
     *
     * @param invoker
     *            The {@link ICommandInvoker}.
     */
    @Inject
    public HelpCommand(ICommandInvoker invoker) {
        this.invoker = invoker;
    }

    @Override
    public String description() {
        return "You just used it.";
    }

    /**
     * Formats the description of a {@link IReplCommand} map.
     * @param commands  the {@link IReplCommand} map
     * @return a formatted string
     */
    public String formathelp(Map<String, IReplCommand> commands) {
        int longestCommand = commands.keySet().stream().mapToInt(a -> a.length()).max().orElse(0);
        String format = "%-" + longestCommand + "s %s";

        return commands.keySet().stream().flatMap(name -> {
            String[] sname = name.split("\\R");
            String[] sdesc = commands.get(name).description().split("\\R");

            return IntStream.range(0, Math.max(sname.length, sdesc.length))
                    .<String>mapToObj(idx -> String.format(format,
                                                   idx < sname.length ? sname[idx] : "",
                                                   idx < sdesc.length ? sdesc[idx] : ""));
        }).collect(Collectors.joining("\n"));
    }

    @Override
    public IResult execute(String... args) throws MetaborgException {
        try {
            Map<String, IReplCommand> commands;
            if (args.length > 0) {
                IReplCommand command = invoker.commandFromName(args[0]);
                commands = Collections.singletonMap(args[0], command);
            } else {
                commands = invoker.getCommands();
            }

            return (display) -> display
                .visitMessage(new StyledText(formathelp(commands)));
        } catch (CommandNotFoundException e) {
            throw new MetaborgException("Command not found: " + e.commandName());
        }
    }

}
