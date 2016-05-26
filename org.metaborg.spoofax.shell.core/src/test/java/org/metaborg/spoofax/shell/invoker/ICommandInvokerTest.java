package org.metaborg.spoofax.shell.invoker;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;
import org.metaborg.spoofax.shell.commands.IReplCommand;
import org.metaborg.spoofax.shell.invoker.CommandNotFoundException;
import org.metaborg.spoofax.shell.invoker.ICommandInvoker;

/**
 * Tests default method implementations of {@link ICommandInvoker}.
 */
public class ICommandInvokerTest implements ICommandInvoker {

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

    @Override
    public void addCommand(String name, IReplCommand command) {
        // TODO Auto-generated method stub
    }

    @Override
    public Map<String, IReplCommand> getCommands() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void resetCommands() {
        // TODO Auto-generated method stub
    }

}