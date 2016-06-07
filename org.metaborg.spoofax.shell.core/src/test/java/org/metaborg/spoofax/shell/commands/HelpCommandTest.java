package org.metaborg.spoofax.shell.commands;

import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.metaborg.core.MetaborgException;
import org.metaborg.spoofax.shell.client.IDisplay;
import org.metaborg.spoofax.shell.invoker.CommandNotFoundException;
import org.metaborg.spoofax.shell.invoker.ICommandInvoker;
import org.metaborg.spoofax.shell.output.StyledText;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Maps;

/**
 * Test creating and using the {@link HelpCommand}.
 */
@RunWith(MockitoJUnitRunner.class)
public class HelpCommandTest {
    // Constructor mocks
    @Mock private ICommandInvoker invoker;

    @Mock private IDisplay display;
    @Captor private ArgumentCaptor<StyledText> captor;

    // Command mocks
    @Mock private IReplCommand singleLineComment;
    @Mock private IReplCommand multiLineComment;

    private HelpCommand helpCommand;
    private Map<String, IReplCommand> commands;

    /**
     * Set up mocks used in the test case.
     * @throws CommandNotFoundException when command could not be found
     */
    @Before
    public void setup() throws CommandNotFoundException {
        commands = Maps.newHashMap();
        commands.put("name-1", singleLineComment);
        commands.put("name-2", multiLineComment);

        when(singleLineComment.description()).thenReturn("test-1");
        when(multiLineComment.description()).thenReturn("test-2\ntest-2");
        when(invoker.getCommands()).thenReturn(commands);

        when(invoker.commandFromName("name-1")).thenReturn(singleLineComment);
        when(invoker.commandFromName("name-2")).thenReturn(multiLineComment);

        helpCommand = new HelpCommand(invoker);
    }

    /**
     * Verify that the description of a command is never null.
     */
    @Test
    public void testDescription() {
        assertThat(helpCommand.description(), isA(String.class));
    }

    /**
     * Test getting help for a command that does not exist.
     *
     * @throws MetaborgException
     *             expected.
     * @throws CommandNotFoundException
     *             Should not happen.
     */
    @Test(expected = MetaborgException.class)
    public void testCommandNotFound() throws MetaborgException, CommandNotFoundException {
        when(invoker.commandFromName(any())).thenThrow(new CommandNotFoundException("error"));
        helpCommand.execute("invalid-command").accept(display);
    }

    /**
     * Test getting help for an existing command with a single line description.
     *
     * @throws MetaborgException
     *             Not expected
     */
    @Test
    public void testCommandSingleLine() throws MetaborgException {
        String expected = "name-1 test-1";
        helpCommand.execute("name-1").accept(display);
        verify(display, times(1)).displayMessage(captor.capture());
        assertEquals(expected, captor.getValue().toString());
    }

    /**
     * Test getting help for an existing command with a multi-line description.
     *
     * @throws MetaborgException
     *             Not expected.
     */
    @Test
    public void testCommandMultiLine() throws MetaborgException {
        String expected = "name-2 test-2\n"
                        + "       test-2";
        helpCommand.execute("name-2").accept(display);
        verify(display, times(1)).displayMessage(captor.capture());
        assertEquals(expected, captor.getValue().toString());
    }

    /**
     * Test getting help for an existing command with a multi-line description.
     *
     * @throws MetaborgException
     *             Not expected.
     */
    @Test
    public void testCommands() throws MetaborgException {
        String expected = "name-1 test-1\n"
                        + "name-2 test-2\n"
                        + "       test-2";
        helpCommand.execute(new String[0]).accept(display);
        verify(display, times(1)).displayMessage(captor.capture());
        assertEquals(expected, captor.getValue().toString());
    }

}
