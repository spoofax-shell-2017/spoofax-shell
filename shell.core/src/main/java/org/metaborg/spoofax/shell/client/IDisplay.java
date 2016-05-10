package org.metaborg.spoofax.shell.client;

/**
 * An {@link IDisplay} is for displaying the results of a command or expression that was executed,
 * whether it was an error or a successful result.
 */
public interface IDisplay {

    /**
     * Display a result string.
     * @param s The string to be displayed.
     */
    void displayResult(String s);

    /**
     * Display an error string.
     * @param s The string to be displayed.
     */
    void displayError(String s);
}
