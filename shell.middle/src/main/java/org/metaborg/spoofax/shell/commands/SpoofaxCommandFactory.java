package org.metaborg.spoofax.shell.commands;

import java.util.function.Consumer;

public class SpoofaxCommandFactory implements ICommandFactory {
    private ICommandInvoker invoker;

    public SpoofaxCommandFactory(ICommandInvoker invoker) {
        this.invoker = invoker;
    };

    @Override
    public void createEvaluationCommand(Consumer<String> onErrorHook, Consumer<String> onCompleteHook) {
        invoker.setEvaluationCommand(s -> {
          onCompleteHook.accept("Hello.");
        });
    }

}
