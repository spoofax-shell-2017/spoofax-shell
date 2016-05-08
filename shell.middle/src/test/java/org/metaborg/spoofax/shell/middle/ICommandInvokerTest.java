package org.metaborg.spoofax.shell.middle;

import static org.junit.Assert.*;

import org.junit.Test;
import org.metaborg.spoofax.shell.commands.ICommandInvoker;
import org.metaborg.spoofax.shell.commands.IReplCommand;

/**
 * Tests default method implementations of {@link ICommandInvoker}.
 */
public class ICommandInvokerTest implements ICommandInvoker {

    @Test
    public void testEnsureNoPrefix() {
        assertEquals("Returns argument without prefix.", "expected",
                     ensureNoPrefix(commandPrefix() + "expected"));
    }

    @Test
    public void testExecutesCommandWithoutPrefix() {
        execute("PREFIX/test");
    }

    @Override
    public void addCommand(String commandName, String description, IReplCommand c) {
    }

    @Override
    public String commandDescriptionFromName(String commandName) {
        return null;
    }

    /**
     * Called from {@link #testExecutesCommandWithoutPrefix()}
     * @param commandName if correctly, without the prefix.
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

}