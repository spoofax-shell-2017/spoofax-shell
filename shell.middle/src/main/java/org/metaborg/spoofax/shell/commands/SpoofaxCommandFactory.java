package org.metaborg.spoofax.shell.commands;

import java.util.function.Function;

public class SpoofaxCommandFactory implements ICommandFactory {
    private ICommandInvoker invoker;

    public SpoofaxCommandFactory(ICommandInvoker invoker) {
        this.invoker = invoker;
    };

    @Override
    public void createEvaluationCommand(Function onErrorHook, Function onCompleteHook) {
        invoker.setEvaluationCommand(s -> { });
    }

}
