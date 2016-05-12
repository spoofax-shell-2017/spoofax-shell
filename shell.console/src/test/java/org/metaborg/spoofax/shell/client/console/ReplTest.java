package org.metaborg.spoofax.shell.client.console;

import static org.junit.Assert.fail;
import static org.metaborg.spoofax.shell.client.console.TerminalUserInterfaceTest.C_D;
import static org.metaborg.spoofax.shell.client.console.TerminalUserInterfaceTest.ENTER;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.junit.Test;
import org.metaborg.spoofax.shell.client.IEditor;
import org.metaborg.spoofax.shell.client.Repl;
import org.metaborg.spoofax.shell.client.ReplModule;
import org.metaborg.spoofax.shell.commands.CommandNotFoundException;
import org.metaborg.spoofax.shell.commands.ICommandInvoker;
import org.mockito.Mockito;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

/**
 * Tests the Repl by simulating user input.
 */
public class ReplTest {
    private ByteArrayInputStream in;
    private ByteArrayOutputStream out;
    private Repl repl;
    private Injector injector;

    private void createInjector(Module... overrides) {
        Module overridden = Modules.override(new ReplModule()).with(overrides);
        injector = Guice.createInjector(overridden);
    }

    /**
     * Create initial injector, can be overridden later on by {@link #createInjector(Module...)}.
     *
     * @param inputString
     *            The simulated user input.
     * @throws UnsupportedEncodingException
     *             When UTF-8 is not supported.
     */
    public void setUp(String inputString) throws UnsupportedEncodingException {
        in = new ByteArrayInputStream(inputString.getBytes("UTF-8"));
        out = new ByteArrayOutputStream();

        injector = Guice.createInjector(new ConsoleReplModule());
    }

    /**
     * Setup and inject the {@link InputStream}s and {@link OutputStream}s.
     *
     * @param overrides
     *            Module overrides w.r.t. ReplModule.
     */
    public void createRepl(Module... overrides) {
        createInjector(overrides);
        repl = injector.getInstance(Repl.class);
    }

    /**
     * Test whether the Repl exits when Ctrl + D is pressed.
     */
    @Test
    public void testCtrlDDoesExit() {
        try {
            setUp(String.valueOf(C_D));
            ICommandInvoker mock = mock(ICommandInvoker.class, RETURNS_MOCKS);
            createRepl(new UserInputSimulationModule(in, out), new MockModule(mock));
            repl.run();
            // Ensure that the command invoker is never called with any command.
            verify(mock, never()).execute(anyString());
        } catch (IOException | CommandNotFoundException e) {
            fail("Should not happen");
        }
    }

    /**
     * Tests the {@link ExitCommand}.
     */
    @Test
    public void testExitCommand() {
        try {
            setUp(":exit" + ENTER + ENTER + ":exit" + ENTER + ENTER);
            createInjector(new UserInputSimulationModule(in, out));
            ICommandInvoker invokerMock = spy(injector.getInstance(ICommandInvoker.class));
            IEditor editorMock = spy(injector.getInstance(IEditor.class));

            // Create a Repl with the mock invoker and editor.
            createRepl(new UserInputSimulationModule(in, out),
                       new MockModule(invokerMock, editorMock));

            // Hack: Create an ExitCommand manually, by giving a provider to the repl instance.
            // Then stub the invoker so that it returns this exitCommand, not its own.
            Repl.ExitCommand exitCommandMock = spy(new Repl.ExitCommand(() -> repl));
            Mockito.when(invokerMock.commandFromName("exit")).thenReturn(exitCommandMock);

            repl.run();

            // Ensure that the command was given to the invoker just once.
            verify(invokerMock, times(1)).execute(":exit");

            // Ensure that exitCommand was executed once.
            verify(exitCommandMock, times(1)).execute();

            // Verify that the Editor was not asked for input after the exit command was executed.
            verify(editorMock, times(1)).getInput();
        } catch (IOException | CommandNotFoundException e) {
            fail("Should not happen");
        }
    }
}
