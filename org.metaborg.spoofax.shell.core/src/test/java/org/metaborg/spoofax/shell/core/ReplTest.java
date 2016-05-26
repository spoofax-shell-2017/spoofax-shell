package org.metaborg.spoofax.shell.core;

import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.metaborg.spoofax.shell.invoker.ICommandInvoker;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

/**
 * Tests the REPL by simulating user input.
 */
public class ReplTest {
    protected ICommandInvoker invokerMock;
    protected Repl replMock;
    protected Injector injector;

    /**
     * @return The {@link ReplModule} for this ReplTest.
     */
    protected ReplModule replModule() {
        return new ReplModule();
    }

    /**
     * Create an {@link Injector} with {@link Module} {@code overrides}.
     *
     * @param overrides
     *            The module overrides.
     */
    protected void createInjector(Module... overrides) {
        Module overridden = Modules.override(replModule()).with(overrides);
        injector = Guice.createInjector(overridden);
    }


    /**
     * Setup the mock invoker.
     */
    protected void setUpExit() {
        replMock = mock(Repl.class, RETURNS_MOCKS);
        when(replMock.read()).thenReturn(":exit");

        // Create an injector Then use it to create an invoker.
        createInjector(new MockModule(replMock));
        invokerMock = spy(injector.getInstance(ICommandInvoker.class));
    }

    /// **
    // * Tests the {@link ExitCommand}.
    // */
    // @Test
    // public void testExitCommand() {
    // try {
    // setUpExit();

    // // Stub the invoker so that it returns an exit command which we can spy on.
    // ExitCommand exitCommandMock = spy(new ExitCommand(() -> replMock));
    // when(invokerMock.commandFromName("exit")).thenReturn(exitCommandMock);

    // replMock.run();

    // // Ensure that the command was given to the invoker just once.
    // verify(invokerMock, times(1)).execute(":exit");

    // // Ensure that exitCommand was executed once.
    // verify(exitCommandMock, times(1)).execute();

    // // Verify that the Editor was not asked for input after the exit command was executed.
    // verify(replMock, times(1)).read();
    // } catch (MetaborgException | CommandNotFoundException e) {
    // fail("Should not happen");
    // }
    // }
}
