package org.metaborg.spoofax.shell.client.console;

import org.metaborg.spoofax.shell.output.StyledText;

/**
 * An {@link IDisplay} is for displaying the results of a command or expression that was executed,
 * whether it was an error or a successful result.
 */
public interface IDisplay {

    /**
     * Display a result string with a style.
     * @param s The string to be displayed.
     */
    void displayResult(StyledText s);

    /**
     * Display an error string with a style.
     * @param s The string to be displayed.
     */
    void displayError(StyledText s);

}
