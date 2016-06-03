package org.metaborg.spoofax.shell.client;

import org.metaborg.spoofax.shell.output.ISpoofaxResult;
import org.metaborg.spoofax.shell.output.StyledText;

/**
 * An {@link IDisplay} is for displaying the results of a command or expression that was executed,
 * whether it was an error or a successful result.
 */
public interface IDisplay {

    /**
     * Display an {@link ISpoofaxResult}.
     *
     * @param result
     *            The result to be displayed.
     */
    void displayResult(ISpoofaxResult<?> result);

    /**
     * Display a {@link StyledText} message.
     *
     * @param message
     *            The message to be displayed.
     */
    void displayMessage(StyledText message);

}
