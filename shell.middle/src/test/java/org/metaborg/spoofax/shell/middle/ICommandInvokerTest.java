package org.metaborg.spoofax.shell.middle;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.metaborg.spoofax.shell.commands.CommandNotFoundException;
import org.metaborg.spoofax.shell.commands.ICommandInvoker;
import org.metaborg.spoofax.shell.commands.IEvaluationCommand;
import org.metaborg.spoofax.shell.commands.IReplCommand;

/**
 * Tests default method implementations of {@link ICommandInvoker}.
 */
public class ICommandInvokerTest implements ICommandInvoker {

    /**
     * Test whether {@link #ensureNoPrefix(String)} correctly strips off the prefix, and keeps it
     * otherwise when there is none.
     */
    @Test
    public void testEnsureNoPrefix() {
        assertEquals("Returns argument without prefix.", "expected",
                     ensureNoPrefix(commandPrefix() + "expected"));
        assertEquals("Returns argument as given.", "expected", ensureNoPrefix("expected"));
    }

    /**
     * Test whether the command gets executed without a prefix. See {@link #commandFromName(String)}
     * for the assertion.
     * @throws CommandNotFoundException cannot occur.
     */
    @Test
    public void testExecutesCommandWithoutPrefix() throws CommandNotFoundException {
        execute(commandPrefix() + "test");
    }

    /**
     * Test whether the command gets evaluated, because it does not begin with
     * {@link #commandPrefix()}.
     * @throws CommandNotFoundException cannot occur.
     */
    @Test
    public void testExecutesEvaluationCommand() throws CommandNotFoundException {
        execute("test");
    }

    @Override
    public void addCommand(String commandName, String description, IReplCommand c) {
    }

    @Override
    public String commandDescriptionFromName(String commandName) {
        return null;
    }

    /**
     * Called from {@link #testExecutesCommandWithoutPrefix()}.
     *
     * @param commandName
     *            if correctly, without the prefix.
     * @return an empty command.
     */
    @Override
    public IReplCommand commandFromName(String commandName) {
        assertEquals("test", commandName);
        // Does nothing on execution.
        return () -> {
        };
    }

    @Override
    public String commandPrefix() {
        return "PREFIX/";
    }

    @Override
    public void setEvaluationCommand(IEvaluationCommand eval) {
    }

    /**
     * Called from {@link #testExecutesEvaluationCommand()}.
     */
    @Override
    public IEvaluationCommand evaluationCommand() {
        return (s) -> assertEquals("test", s);
    }

}