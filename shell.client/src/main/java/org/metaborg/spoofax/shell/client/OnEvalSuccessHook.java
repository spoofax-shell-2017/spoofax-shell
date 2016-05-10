package org.metaborg.spoofax.shell.client;

import java.util.function.Consumer;

import com.google.inject.Inject;

/**
 * Called upon success of an evaluation command.
 */
class OnEvalSuccessHook implements Consumer<String> {
    private IDisplay display;

    /**
     * @param display The {@link IDisplay} to show the result on.
     */
    @Inject
    OnEvalSuccessHook(IDisplay display) {
        this.display = display;
    }

    @Override
    public void accept(String s) {
        display.displayResult(s);
    }
}