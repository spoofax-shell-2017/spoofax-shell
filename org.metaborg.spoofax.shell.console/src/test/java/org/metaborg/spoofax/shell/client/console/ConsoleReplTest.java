package org.metaborg.spoofax.shell.client.console;

import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.mock;
import static org.metaborg.spoofax.shell.client.console.TerminalUserInterfaceTest.C_D;
import static org.metaborg.spoofax.shell.client.console.TerminalUserInterfaceTest.ENTER;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.metaborg.spoofax.shell.client.IEditor;
import org.metaborg.spoofax.shell.client.MockModule;
import org.metaborg.spoofax.shell.client.Repl;
import org.metaborg.spoofax.shell.client.ReplModule;
import org.metaborg.spoofax.shell.client.ReplTest;
import org.metaborg.spoofax.shell.commands.ICommandInvoker;

import com.google.inject.Guice;

/**
 * Tests integration of {@link Repl} with {@link TerminalUserInterface}.
 */
public class ConsoleReplTest extends ReplTest {
    private ByteArrayInputStream in;
    private ByteArrayOutputStream out;

    @Override
    protected ReplModule replModule() {
        return new ConsoleReplModule();
    }

    /**
     * Setup the input streams.
     *
     * @param inputString
     *            The user input that will be simulated.
     * @throws UnsupportedEncodingException
     *             When UTF-8 is not supported.
     */
    private void setUp(String inputString) throws UnsupportedEncodingException {
        in = new ByteArrayInputStream(inputString.getBytes("UTF-8"));
        out = new ByteArrayOutputStream();

        injector = Guice.createInjector(replModule());
    }

    @Override
    protected void setUpCtrlD() throws IOException {
        setUp(String.valueOf(C_D));
        invokerMock = mock(ICommandInvoker.class, RETURNS_MOCKS);

        // Create a user input simulated Repl with the mock invoker.
        createRepl(new UserInputSimulationModule(in, out), new MockModule(invokerMock));
    }

    @Override
    protected void setUpExit() throws IOException {
        setUp(":exit" + ENTER + ENTER + ":exit" + ENTER + ENTER);
        createInjector(new UserInputSimulationModule(in, out));
        invokerMock = spy(injector.getInstance(ICommandInvoker.class));
        editorMock = spy(injector.getInstance(IEditor.class));

        // Create a user input simulated Repl with the mock invoker and mock editor.
        createRepl(new UserInputSimulationModule(in, out), new MockModule(invokerMock, editorMock));
    }
}
