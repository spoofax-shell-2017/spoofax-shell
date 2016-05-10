package org.metaborg.spoofax.shell.client.console;

import java.util.function.Consumer;

import org.metaborg.spoofax.shell.client.IDisplay;

import com.google.inject.Inject;

/**
 * Called upon success of an evaluation command.
 */
public class OnEvalErrorHook implements Consumer<String> {

    private IDisplay display;

    /**
     * @param display The {@link IDisplay} to show the result on.
     */
    @Inject
    OnEvalErrorHook(IDisplay display) {
        this.display = display;
    }

    @Override
    public void accept(String s) {
        display.displayError(s);
    }
}
