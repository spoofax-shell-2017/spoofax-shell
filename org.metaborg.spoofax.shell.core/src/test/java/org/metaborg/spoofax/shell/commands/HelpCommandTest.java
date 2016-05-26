package org.metaborg.spoofax.shell.commands;

import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.shell.invoker.CommandNotFoundException;
import org.metaborg.spoofax.shell.invoker.ICommandFactory;
import org.metaborg.spoofax.shell.invoker.ICommandInvoker;
import org.metaborg.spoofax.shell.output.StyledText;
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
    @Mock private Consumer<StyledText> onSuccess;
    @Mock private Consumer<StyledText> onError;
    @Mock private IProject project;

    @Mock private ICommandFactory commandFactory;

    private HelpCommand helpCommand;
    private Map<String, IReplCommand> commands;

    private static IReplCommand single, multi;
    static {
        single = new SingleLineComment();
        multi = new MultiLineComment();
    }

    /**
     * Represents a test command with a single line description.
     */
    private static class SingleLineComment implements IReplCommand {
        @Override
        public void execute(String... args) { }

        @Override
        public String description() {
            return "test-1";
        }
    }

    /**
     * Represents a test command with a multi line description.
     */
    private static class MultiLineComment implements IReplCommand {
        @Override
        public void execute(String... args) { }

        @Override
        public String description() {
            return "test-2\ntest-2";
        }
    }

    /**
     * Set up mocks used in the test case.
     * @throws CommandNotFoundException when command could not be found
     */
    @Before
    public void setup() throws CommandNotFoundException {
        commands = Maps.newHashMap();
        commands.put("name-1", single);
        commands.put("name-2", multi);

        when(invoker.getCommands()).thenReturn(commands);
        when(invoker.commandFromName("name-1")).thenReturn(single);
        when(invoker.commandFromName("name-2")).thenReturn(multi);
        helpCommand = new HelpCommand(invoker, onSuccess, onError);
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
     * @throws CommandNotFoundException when the command could not be found
     */
    @Test
    public void testCommandNotFound() throws CommandNotFoundException {
        when(invoker.commandFromName(any())).thenThrow(new CommandNotFoundException("error"));

        helpCommand.execute("invalid-command");
        verify(onSuccess, never()).accept(any());
        verify(onError, times(1)).accept(any());
    }

    /**
     * Test getting help for an existing command with a single line description.
     */
    @Test
    public void testCommandSingleLine() {
        String expected = "name-1 test-1";

        Consumer<StyledText> onSuccess = (s) -> assertEquals(expected, s.toString());
        helpCommand = new HelpCommand(invoker, onSuccess, onError);

        helpCommand.execute("name-1");
        verify(onError, never()).accept(any());
    }

    /**
     * Test getting help for an existing command with a multi line description.
     */
    @Test
    public void testCommandMultiLine() {
        String expected = "name-2 test-2\n"
                        + "       test-2";

        Consumer<StyledText> onSuccess = (s) -> assertEquals(expected, s.toString());
        helpCommand = new HelpCommand(invoker, onSuccess, onError);

        helpCommand.execute("name-2");
        verify(onError, never()).accept(any());
    }

    /**
     * Test getting help for an existing command with a multi line description.
     */
    @Test
    public void testCommands() {
        String expected = "name-1 test-1\n"
                        + "name-2 test-2\n"
                        + "       test-2";

        Consumer<StyledText> onSuccess = (s) -> assertEquals(expected, s.toString());
        helpCommand = new HelpCommand(invoker, onSuccess, onError);

        helpCommand.execute();
        verify(onError, never()).accept(any());
    }

}
