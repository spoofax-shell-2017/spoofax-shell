package org.metaborg.spoofax.shell.client;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Test;
import org.metaborg.core.context.IContext;
import org.metaborg.spoofax.shell.commands.CommandNotFoundException;
import org.metaborg.spoofax.shell.commands.ExitCommand;
import org.metaborg.spoofax.shell.commands.ICommandInvoker;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

/**
 * Tests the REPL by simulating user input.
 */
public class ReplTest {
    protected ICommandInvoker invokerMock;
    protected IEditor editorMock;
    protected Repl repl;
    protected Injector injector;

    /**
     * @return The {@link ReplModule} for this ReplTest.
     */
    protected ReplModule replModule() {
        return new ReplModule(mock(IContext.class, RETURNS_MOCKS));
    }

    /**
     * Create an injector with Module overrides.
     *
     * @param overrides
     *            The module overrides.
     */
    protected void createInjector(Module... overrides) {
        Module overridden = Modules.override(replModule()).with(overrides);
        injector = Guice.createInjector(overridden);
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
     * Setup the mock editor for Control-D.
     *
     * @throws IOException
     *             When {@link IEditor#getInput()} causes IO errors (cannot happen because it is a
     *             mock).
     */
    protected void setUpCtrlD() throws IOException {
        editorMock = mock(IEditor.class, RETURNS_MOCKS);
        IDisplay displayMock = mock(IDisplay.class, RETURNS_MOCKS);
        invokerMock = mock(ICommandInvoker.class, RETURNS_MOCKS);
        when(editorMock.getInput()).thenReturn(null);
        createRepl(new MockModule(invokerMock, editorMock, displayMock));
    }

    /**
     * Test whether the REPl exits when Control-D is pressed.
     */
    @Test
    public void testCtrlDDoesExit() {
        try {
            setUpCtrlD();
            repl.run();
            // Ensure that the command invoker is never called with any command.
            verify(invokerMock, never()).execute(anyString());
        } catch (IOException | CommandNotFoundException e) {
            fail("Should not happen");
        }
    }

    /**
     * Setup the mock invoker and editor.
     *
     * @throws IOException
     *             When {@link IEditor#getInput()} causes IO errors (cannot happen because it is a
     *             mock).
     * @throws CommandNotFoundException
     *             When the command was not found.
     */
    protected void setUpExit() throws IOException, CommandNotFoundException {
        editorMock = mock(IEditor.class, RETURNS_MOCKS);
        IDisplay displayMock = mock(IDisplay.class, RETURNS_MOCKS);
        when(editorMock.getInput()).thenReturn(":exit");

        // Create an injector which has bindings for IEditor and IDisplay. Then use it to create an
        // invoker.
        createInjector(new MockModule(editorMock, displayMock));
        invokerMock = spy(injector.getInstance(ICommandInvoker.class));

        // Now use all of the mocks to create a Repl.
        createRepl(new MockModule(invokerMock, editorMock, displayMock));
    }

    /**
     * Tests the {@link Repl.ExitCommand}.
     */
    @Test
    public void testExitCommand() {
        try {
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
            verify(editorMock, times(1)).getInput();
        } catch (IOException | CommandNotFoundException e) {
            fail("Should not happen");
        }
    }
}
