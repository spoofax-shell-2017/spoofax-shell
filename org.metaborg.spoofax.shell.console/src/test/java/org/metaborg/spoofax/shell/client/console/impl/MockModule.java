package org.metaborg.spoofax.shell.client.console.impl;

import org.metaborg.spoofax.shell.client.IDisplay;
import org.metaborg.spoofax.shell.invoker.ICommandInvoker;

import com.google.inject.AbstractModule;

/**
 * Allows for injecting mock {@link ICommandInvoker}s, for spying/verifying.
 */
public class MockModule extends AbstractModule {
    private final ICommandInvoker invokerMock;
    private final TerminalUserInterface ifaceMock;
    private final IDisplay displayMock;

    /**
     * Instantiates a new MockModule with a {@code null} @{link TerminalUserInterface} and a
     * {@code null} {@link IDisplay}.
     *
     * @param invokerMock
     *            The mock {@link ICommandInvoker}.
     */
    public MockModule(ICommandInvoker invokerMock) {
        this(invokerMock, null, null);
    }

    /**
     * Instantiates a new MockModule with a {@code null} {@link IDisplay}.
     *
     * @param invokerMock
     *            The mock {@link ICommandInvoker}.
     * @param ifaceMock
     *            The mock {@link TerminalUserInterface}.
     */
    public MockModule(ICommandInvoker invokerMock, TerminalUserInterface ifaceMock) {
        this(invokerMock, ifaceMock, null);
    }

    /**
     * Instantiates a new MockModule with a {@code null} {@link ICommandInvoker}.
     *
     * @param ifaceMock
     *            The mock {@link TerminalUserInterface}.
     * @param displayMock
     *            The mock {@link IDisplay}.
     */
    public MockModule(TerminalUserInterface ifaceMock, IDisplay displayMock) {
        this(null, ifaceMock, displayMock);
    }

    /**
     * Instantiates a new MockModule.
     *
     * @param invokerMock
     *            The mock {@link ICommandInvoker}.
     * @param ifaceMock
     *            The mock {@link TerminalUserInterface}.
     * @param displayMock
     *            The mock {@link IDisplay}.
     */
    public MockModule(ICommandInvoker invokerMock, TerminalUserInterface ifaceMock,
                      IDisplay displayMock) {
        this.invokerMock = invokerMock;
        this.ifaceMock = ifaceMock;
        this.displayMock = displayMock;
    }

    @Override
    protected void configure() {
        if (invokerMock != null) {
            bind(ICommandInvoker.class).toInstance(invokerMock);
        }
        if (ifaceMock != null) {
            bind(TerminalUserInterface.class).toInstance(ifaceMock);
        }
        if (displayMock != null) {
            bind(IDisplay.class).toInstance(displayMock);
        }
    }
}
