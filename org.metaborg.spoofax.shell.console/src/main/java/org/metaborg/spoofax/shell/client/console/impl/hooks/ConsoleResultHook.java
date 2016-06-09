package org.metaborg.spoofax.shell.client.console.impl.hooks;

import org.metaborg.spoofax.shell.client.IDisplay;
import org.metaborg.spoofax.shell.client.hooks.IResultHook;
import org.metaborg.spoofax.shell.output.ISpoofaxResult;

import com.google.inject.Inject;

/**
 * A console-based implementation of {@link IResultHook}.
 */
public class ConsoleResultHook implements IResultHook {
    private final IDisplay display;

    /**
     * Instantiates a new ConsoleResultHook.
     *
     * @param display
     *            The {@link IDisplay} to display the results on.
     */
    @Inject
    public ConsoleResultHook(IDisplay display) {
        this.display = display;
    }

    @Override
    public void accept(ISpoofaxResult<?> result) {
        this.display.displayResult(result);
    }

}
