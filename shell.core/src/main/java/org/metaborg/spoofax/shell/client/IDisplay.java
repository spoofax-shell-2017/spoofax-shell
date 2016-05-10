package org.metaborg.spoofax.shell.client;

import org.metaborg.spoofax.shell.commands.StyledText;

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

    /**
     * Display a result string with no style.
     * @param s The string to be displayed.
     */
    default void displayResult(String s) {
        displayResult(new StyledText(s));
    }

    /**
     * Display an error string with no style.
     * @param s The string to be displayed.
     */
    default void displayError(String s) {
        displayError(new StyledText(s));
    }
}
