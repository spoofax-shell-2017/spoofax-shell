package org.metaborg.spoofax.shell.client;

import org.metaborg.core.completion.ICompletionService;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;

/**
 * An {@link IEditor} is where expressions in some language can be typed. It takes care of
 * keybindings, offering completions, the input history and multiline editing capabilities.
 */
public interface IEditor {

    /**
     * Get the input from the user, optionally spanning multiple lines.
     *
     * @return The input typed in by the user.
     */
    String getInput();

    /**
     * Set the completion service to be used when hitting TAB.
     *
     * @param completionService
     *            The {@link ICompletionService} for providing completion.
     */
    void setSpoofaxCompletion(ICompletionService<ISpoofaxParseUnit> completionService);

    /**
     * @return The history of evaluated expressions, oldest entries first.
     */
    IInputHistory history();
}