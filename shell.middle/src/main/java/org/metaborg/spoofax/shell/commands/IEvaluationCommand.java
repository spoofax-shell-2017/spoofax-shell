package org.metaborg.spoofax.shell.commands;

/**
 * Command for evaluating the String as an expression in some language.
 */
public interface IEvaluationCommand {

    /**
     * Evaluate the given String as an expression in some language.
     * @param s The String to be parsed and executed.
     */
    void evaluate(String s);
}
