package org.metaborg.spoofax.shell.client.eclipse.impl.hooks;

import org.metaborg.spoofax.shell.client.IDisplay;
import org.metaborg.spoofax.shell.client.hooks.IResultHook;
import org.metaborg.spoofax.shell.output.ISpoofaxResult;

import com.google.inject.Inject;

/**
 * An Eclipse-based implementation of {@link IResultHook}.
 */
public class EclipseResultHook implements IResultHook {
    private final IDisplay display;

    /**
     * Instantiates a new EclipseResultHook.
     *
     * @param display
     *            The {@link IDisplay} to display the messages on.
     */
    @Inject
    public EclipseResultHook(IDisplay display) {
        this.display = display;
    }

    @Override
    public void accept(ISpoofaxResult<?> result) {
        this.display.displayResult(result.styled());
    }

}
