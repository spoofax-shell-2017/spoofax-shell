package org.metaborg.spoofax.shell.commands;

import java.util.function.Function;

/**
 * Factory for creating commands. Implementations of this factory encapsulate the creation of
 * commands, and also their names and description. The commands are added to an
 * {@link CommandInvoker}.
 */
public interface ICommandFactory {

    /**
     * Create an evaluation command.
     * 
     * TODO: Create more specific interfaces for the hooks, which can accept more parameters.
     * The parameters that are needed are currently unknown.
     * 
     * @param onErrorHook
     * @param onCompleteHook
     */
    public void createEvaluationCommand(Function onErrorHook, Function onCompleteHook);
}
