package org.metaborg.spoofax.shell.commands;

import java.util.function.Consumer;

/**
 * Default implementation of {@link ICommandFactory}.
 */
public class SpoofaxCommandFactory implements ICommandFactory {
    private ICommandInvoker invoker;

    /**
     * Construct a new {@link SpoofaxCommandFactory}.
     *
     * @param invoker
     *            The {@link ICommandInvoker} to add the created commands to.
     */
    public SpoofaxCommandFactory(ICommandInvoker invoker) {
        this.invoker = invoker;
    };

    @Override
    public void createEvaluationCommand(Consumer<String> onErrorHook,
                                        Consumer<String> onCompleteHook) {
        invoker.setEvaluationCommand(s -> {
            onCompleteHook.accept("Hello.");
        });
    }

}
