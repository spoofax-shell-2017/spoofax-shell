package org.metaborg.spoofax.shell.client.console.impl.hooks;

import org.metaborg.spoofax.shell.client.console.IDisplay;
import org.metaborg.spoofax.shell.hooks.IMessageHook;
import org.metaborg.spoofax.shell.output.StyledText;

import com.google.inject.Inject;

/**
 * A console-based implementation of {@link IMessageHook}.
 */
public class ConsoleMessageHook implements IMessageHook {
    private final IDisplay display;

    /**
     * Instantiates a new ConsoleMessageHook.
     *
     * @param display
     *            The {@link IDisplay} to display the messages on.
     */
    @Inject
    public ConsoleMessageHook(IDisplay display) {
        this.display = display;
    }

    @Override
    public void accept(StyledText message) {
        this.display.displayResult(message);
    }

}
