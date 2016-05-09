package org.metaborg.spoofax.shell.middle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.metaborg.spoofax.shell.commands.CommandNotFoundException;
import org.metaborg.spoofax.shell.commands.ICommandInvoker;
import org.metaborg.spoofax.shell.commands.SpoofaxCommandInvoker;

/**
 * Test the default implementation of {@link ICommandInvoker}.
 */
public class SpoofaxCommandInvokerTest {
    private SpoofaxCommandInvoker invoker;

    /**
     * Setup the commands, one with description and one without.
     */
    @Before
    public void setUp() {
        invoker = new SpoofaxCommandInvoker();
        invoker.addCommand("has-description", "description", () -> {
        });
        invoker.addCommand("no-description", () -> {
        });
    }

    /**
     * Test whether no-description has an empty description, and whether has-description does have a
     * description.
     */
    @Test
    public void testCommandDescriptionFromName() {
        try {
            assertEquals("", invoker.commandDescriptionFromName("no-description"));
            assertEquals("description", invoker.commandDescriptionFromName("has-description"));
        } catch (CommandNotFoundException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test correct throwing of {@link CommandNotFoundException}.
     */
    @Test
    public void testCommandNotFound() {
        try {
            invoker.commandDescriptionFromName("does-not-exist");
            invoker.commandFromName("does-not-exist");
            invoker.execute(":does-not-exist");
            fail("No exceptions thrown, but command does not exist");
        } catch (CommandNotFoundException e) {
            assertCommandNameInMessage("does-not-exist", e);
        }
    }

    private static void assertCommandNameInMessage(String commandName, CommandNotFoundException e) {
        assertTrue("Command name should be present in CommandNotFoundException message",
                   e.getMessage().contains('\"' + commandName + '\"'));
    }
}
