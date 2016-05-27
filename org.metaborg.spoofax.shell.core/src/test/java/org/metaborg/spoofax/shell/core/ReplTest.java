package org.metaborg.spoofax.shell.core;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.metaborg.core.MetaborgException;
import org.metaborg.spoofax.shell.commands.ExitCommand;
import org.metaborg.spoofax.shell.invoker.CommandNotFoundException;
import org.metaborg.spoofax.shell.invoker.ICommandFactory;
import org.metaborg.spoofax.shell.invoker.SpoofaxCommandInvoker;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Maps;
import com.google.inject.Provider;

/**
 * Tests the REPL by simulating user input.
 */
@RunWith(MockitoJUnitRunner.class)
public class ReplTest {
    @Mock private ICommandFactory factory;
    @Mock private Provider<Repl> provider;

    private SpoofaxCommandInvoker invoker;
    private ExitCommand exitCommand;
    private Repl repl;

    /**
     * Setup the mock invoker.
     * @throws CommandNotFoundException when the exit command was not found
     */
    @Before
    public void setup() throws CommandNotFoundException {
        exitCommand = spy(new ExitCommand(provider));
        invoker = spy(new SpoofaxCommandInvoker(factory, Maps.newHashMap()));
        repl = spy(new Repl(invoker) {
            @Override
            protected String read() {
                return ":exit";
            }
        });

        doReturn(exitCommand).when(invoker).commandFromName("exit");
        when(provider.get()).thenReturn(repl);
    }

    /**
     * Tests the {@link ExitCommand}.
     */
     @Test
     public void testExitCommand() {
         try {
             repl.run();

             // Ensure that the command was given to the invoker just once.
             verify(invoker, times(1)).execute(":exit");

             // Ensure that exitCommand was executed once.
             verify(exitCommand, times(1)).execute();

             // Verify that the Editor was not asked for input after the exit command was executed.
             verify(repl, times(1)).read();
         } catch (MetaborgException | CommandNotFoundException e) {
             fail("Should not happen");
         }
     }
}
