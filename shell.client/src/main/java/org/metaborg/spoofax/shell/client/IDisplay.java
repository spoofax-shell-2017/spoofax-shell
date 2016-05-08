package org.metaborg.spoofax.shell.client;

/**
 * An {@link IDisplay} is for displaying the results of a command or expressions that was executed,
 * whether it was an error or a successful result.
 */
public interface IDisplay {

    public void displayResult(String s);

    public void displayError(String s);
}
