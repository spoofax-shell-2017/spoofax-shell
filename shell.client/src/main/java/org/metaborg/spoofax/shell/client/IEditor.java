package org.metaborg.spoofax.shell.client;

import java.io.IOException;
import java.util.List;

import org.metaborg.core.completion.ICompletionService;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;

/**
 * An {@link IEditor} is where expressions in some language can be typed. It takes care of the
 * prompt, keybindings, the history and multiline editing capabilities.
 */
public interface IEditor {

    /**
     * Get the input from the user, optionally spanning multiple lines.
     * 
     * @return The input typed in by the user.
     * @throws IOException
     */
    public String getInput() throws IOException;

    /**
     * Set the completion service to be used when hitting TAB.
     * 
     * @param completionService
     */
    public void setSpoofaxCompletion(ICompletionService<ISpoofaxParseUnit> completionService);

    /**
     * TODO: Consider replacing List<String> with a History type?
     * 
     * @return the history of evaluated expressions, from recent to old.
     */
    public List<String> history();

}