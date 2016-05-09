package org.metaborg.spoofax.shell.commands;

import java.util.function.Consumer;

/**
 * Factory for creating commands. Implementations of this factory encapsulate the creation of
 * commands, and also their names and description. The commands are added to an
 * {@link CommandInvoker}.
 */
public interface ICommandFactory {

    /**
     * Create an {@link IEvaluationCommand}.
     * 
     * TODO: Create more specific interfaces for the hooks, which can accept more parameters.
     * The parameters that are needed are currently unknown.
     * 
     * @param onErrorHook Called upon an error by the created {@link IEvaluationCommand}.
     * @param onCompleteHook Called upon successful by the created {@link IEvaluationCommand}.
     */
    void createEvaluationCommand(Consumer<String> onErrorHook, Consumer<String> onCompleteHook);
}
