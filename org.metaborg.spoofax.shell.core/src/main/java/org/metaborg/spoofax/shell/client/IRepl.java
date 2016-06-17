package org.metaborg.spoofax.shell.client;

import org.metaborg.spoofax.shell.commands.IReplCommand;
import org.metaborg.spoofax.shell.invoker.CommandNotFoundException;
import org.metaborg.spoofax.shell.invoker.ICommandInvoker;

/**
 * This interface defines the evaluation part of a REPL (Read-Eval-Print-Loop). The reason for only
 * defining the evaluation phase is the fact that different clients have different needs and as
 * such, there is no way of defining {@code read} and {@code print} methods suitable for all
 * possible clients.
 *
 * The default {@link IRepl#eval(String)} implementation uses an {@link ICommandInvoker} to process
 * user input. This {@link ICommandInvoker} invokes {@link IReplCommand}s, which return
 * {@link IResult}s that can be visited by an {@link IResultVisitor}.
 */
public interface IRepl {
    /**
     * Evaluate the input. This default implementation strips whitespace, skips over empty strings
     * and uses an {@link ICommandInvoker}.
     *
     * @param input
     *            The input to send for evaluation.
     * @return An {@link IResult} to process the result of the evaluation. When the input is empty,
     *         it returns an {@link IResult} that does nothing upon accepting a
     *         {@link IResultVisitor visitor}.
     * @throws CommandNotFoundException
     *             When the command could not be found.
     */
    default IResult eval(String input) throws CommandNotFoundException {
        input = input.trim();
        if (input.length() == 0) {
            return (visitor) -> {
            };
        }
        return getInvoker().execute(input);
    }

    /**
     * Return the {@link ICommandInvoker} used to invoke commands on the user's behalf.
     *
     * @return The {@link ICommandInvoker}.
     */
    ICommandInvoker getInvoker();
}
