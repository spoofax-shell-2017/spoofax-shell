package org.metaborg.spoofax.shell.commands;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

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
     *
     * @throws CommandNotFoundException
     *             Cannot occur.
     */
    @Test
    public void testExecutesCommandWithoutPrefix() throws CommandNotFoundException {
        execute(commandPrefix() + "test");
    }

    /**
     * Test whether the command gets evaluated, because it does not begin with
     * {@link #commandPrefix()}.
     *
     * @throws CommandNotFoundException
     *             Cannot occur.
     */
    @Test
    public void testExecutesEvaluationCommand() throws CommandNotFoundException {
        execute("test");
    }

    /**
     * Called from {@link #testExecutesCommandWithoutPrefix()}.
     *
     * @param commandName
     *            If correctly, without the prefix.
     * @return An empty command.
     */
    @Override
    public IReplCommand commandFromName(String commandName) {
        assertEquals("test", commandName);
        // Does nothing on execution.
        return new IReplCommand() {

            @Override
            public void execute(String... args) {
                // dummy
            }

            @Override
            public String description() {
                return "dummy";
            }
        };
    }

    @Override
    public String commandPrefix() {
        return "PREFIX/";
    }

    /**
     * Called from {@link #testExecutesEvaluationCommand()}.
     */
    @Override
    public IReplCommand evaluationCommand() {
        return new IReplCommand() {

            @Override
            public void execute(String... args) {
                assertEquals("test", args[0]);
            }

            @Override
            public String description() {
                return "dummy";
            }
        };
    }

}