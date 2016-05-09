package org.metaborg.spoofax.shell.commands;

public class SpoofaxCommandInvoker implements ICommandInvoker {
    public IEvaluationCommand eval;

    @Override
    public void addCommand(String commandName, String description, IReplCommand c) {
    }

    @Override
    public void setEvaluationCommand(IEvaluationCommand eval) {
        this.eval = eval;
    }

    @Override
    public IEvaluationCommand evaluationCommand() {
        return eval;
    }

    @Override
    public String commandDescriptionFromName(String commandName) {
        return null;
    }

    @Override
    public IReplCommand commandFromName(String commandName) {
        return null;
    }

    @Override
    public String commandPrefix() {
        return ":";
    }

}
