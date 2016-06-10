package org.metaborg.spoofax.shell.invoker;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.metaborg.core.MetaborgException;

import com.google.common.collect.Maps;

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
        invoker = new SpoofaxCommandInvoker(Maps.newHashMap());
    }

    /**
     * Test correct throwing of {@link CommandNotFoundException}.
     */
    @Test
    public void testCommandNotFound() {
        try {
            invoker.commandFromName("does-not-exist");
            invoker.execute(":does-not-exist");
            fail("No exceptions thrown, but command does not exist");
        } catch (CommandNotFoundException e) {
            assertCommandNameInMessage("does-not-exist", e);
        } catch (MetaborgException e) {
            fail("Should not happen");
        }
    }

    private static void assertCommandNameInMessage(String commandName, CommandNotFoundException e) {
        assertTrue("Command name should be present in CommandNotFoundException message",
                   e.getMessage().contains('\"' + commandName + '\"'));
    }
}
