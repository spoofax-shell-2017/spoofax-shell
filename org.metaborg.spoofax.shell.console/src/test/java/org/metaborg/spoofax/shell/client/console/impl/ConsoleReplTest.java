package org.metaborg.spoofax.shell.client.console.impl;

import static org.metaborg.spoofax.shell.client.console.impl.TerminalUserInterfaceTest.C_D;
import static org.metaborg.spoofax.shell.client.console.impl.TerminalUserInterfaceTest.ENTER;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.junit.Test;
import org.metaborg.spoofax.shell.ReplModule;
import org.metaborg.spoofax.shell.client.ConsoleReplModule;
import org.metaborg.spoofax.shell.client.console.commands.ExitCommand;
import org.metaborg.spoofax.shell.invoker.CommandNotFoundException;
import org.metaborg.spoofax.shell.invoker.ICommandInvoker;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

/**
 * Tests {@link ConsoleRepl}.
 */
public class ConsoleReplTest {
    private Injector injector;
    private ConsoleRepl repl;
    private ByteArrayInputStream in;
    private ByteArrayOutputStream out;
    private TerminalUserInterface ifaceSpy;
    private ICommandInvoker invokerMock;

    /**
     * Create an {@link Injector} with {@link Module} {@code overrides}.
     *
     * @param overrides
     *            The module overrides.
     */
    private void createInjector(Module... overrides) {
        Module overridden = Modules.override(replModule()).with(overrides);
        injector = Guice.createInjector(overridden);
    }

    private ReplModule replModule() {
        return new ConsoleReplModule();
    }

    /**
     * Setup and inject the {@link InputStream}s and {@link OutputStream}s.
     *
     * @param overrides
     *            Module overrides w.r.t. ReplModule.
     */
    private void createRepl(Module... overrides) {
        createInjector(overrides);
        repl = injector.getInstance(ConsoleRepl.class);
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

    private void setUpCtrlD() throws IOException {
        setUp(String.valueOf(C_D));
        invokerMock = mock(ICommandInvoker.class, RETURNS_MOCKS);

        // Create a user input simulated ConsoleRepl with the mock invoker.
        createRepl(new UserInputSimulationModule(in, out), new MockModule(invokerMock));
    }

    /**
     * Test {@link ConsoleRepl#runOnce(String)}.
     *
     * @throws IOException
     *             Should not happen.
     * @throws CommandNotFoundException
     *             Should not happen.
     */
    @Test
    public void testRunOnce() throws IOException, CommandNotFoundException {
        String fake = ":fakecommand foo";
        setUp(fake);
        invokerMock = mock(ICommandInvoker.class, RETURNS_MOCKS);

        // Create a user input simulated ConsoleRepl with the mock invoker.
        createRepl(new UserInputSimulationModule(in, out), new MockModule(invokerMock));

        repl.runOnce(fake);
        verify(invokerMock, times(1)).execute(fake);
    }

    /**
     * Test whether the REPl exits when Control-D is pressed.
     *
     * @throws IOException
     *             Should not happen.
     * @throws CommandNotFoundException
     *             Should not happen.
     */
    @Test
    public void testCtrlDDoesExit() throws CommandNotFoundException, IOException {
        setUpCtrlD();
        repl.run();
        // Ensure that the command invoker is never called with any command.
        verify(invokerMock, never()).execute(anyString());
    }

    private void setUpExit() throws IOException {
        setUp(":exit" + ENTER + ENTER + ":exit" + ENTER + ENTER);
        createInjector(new UserInputSimulationModule(in, out));
        invokerMock = spy(injector.getInstance(ICommandInvoker.class));
        ifaceSpy = spy(injector.getInstance(TerminalUserInterface.class));

        // Create a user input simulated ConsoleRepl with the mock invoker and mock editor.
        createRepl(new UserInputSimulationModule(in, out), new MockModule(invokerMock, ifaceSpy));
    }

    /**
     * Tests the {@link ExitCommand}.
     *
     * @throws IOException
     *             Should not happen.
     * @throws CommandNotFoundException
     *             Should not happen.
     */
    @Test
    public void testExitCommand() throws IOException, CommandNotFoundException {
        setUpExit();

        // Stub the invoker so that it returns an exit command which we can spy on.
        ExitCommand exitCommandMock = spy(new ExitCommand(() -> repl));
        when(invokerMock.commandFromName("exit")).thenReturn(exitCommandMock);

        repl.run();

        // Ensure that the command was given to the invoker just once.
        verify(invokerMock, times(1)).execute(":exit");

        // Ensure that exitCommand was executed once.
        verify(exitCommandMock, times(1)).execute();

        // Verify that the Editor was not asked for input after the exit command was executed.
        verify(ifaceSpy, times(1)).getInput();
    }
}
