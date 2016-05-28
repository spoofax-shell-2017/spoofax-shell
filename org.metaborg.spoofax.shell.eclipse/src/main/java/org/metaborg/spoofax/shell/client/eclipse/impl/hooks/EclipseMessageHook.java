package org.metaborg.spoofax.shell.client.eclipse.impl.hooks;

import org.metaborg.spoofax.shell.client.IDisplay;
import org.metaborg.spoofax.shell.client.hooks.IMessageHook;
import org.metaborg.spoofax.shell.output.StyledText;

import com.google.inject.Inject;

/**
 * An Eclipse-based implementation of {@link IMessageHook}.
 */
public class EclipseMessageHook implements IMessageHook {
    private final IDisplay display;

    /**
     * Instantiates a new EclipseMessageHook.
     *
     * @param display
     *            The {@link IDisplay} to display the messages on.
     */
    @Inject
    public EclipseMessageHook(IDisplay display) {
        this.display = display;
    }

    @Override
    public void accept(StyledText message) {
        this.display.displayResult(message);
    }

}
