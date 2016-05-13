package org.metaborg.spoofax.shell.client;

import java.io.IOException;
import java.util.List;

import org.metaborg.core.completion.ICompletionService;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.shell.core.StyledText;

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
     *             when an IO error occurs.
     */
    String getInput() throws IOException;

    /**
     * Set the completion service to be used when hitting TAB.
     *
     * @param completionService
     *            The {@link ICompletionService} for providing completion.
     */
    void setSpoofaxCompletion(ICompletionService<ISpoofaxParseUnit> completionService);

    /**
     * TODO: Consider replacing a {@link List} of {@link String}s with a History type?
     *
     * @return the history of evaluated expressions, from recent to old.
     */
    List<String> history();

    /**
     * Set the prompt to display.
     *
     * @param promptString
     *            The prompt string.
     */
	void setPrompt(StyledText promptString);

    /**
     * Set the prompt to display when in multi-line mode.
     *
     * @param styledText
     *            The prompt string.
     */
	void setContinuationPrompt(StyledText styledText);

}