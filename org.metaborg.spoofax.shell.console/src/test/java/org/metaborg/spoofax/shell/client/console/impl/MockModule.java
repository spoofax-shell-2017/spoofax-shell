package org.metaborg.spoofax.shell.client.console.impl;

import org.metaborg.spoofax.shell.client.IResultVisitor;
import org.metaborg.spoofax.shell.invoker.ICommandInvoker;

import com.google.inject.AbstractModule;

/**
 * Allows for injecting mock {@link ICommandInvoker}s, for spying/verifying.
 */
public class MockModule extends AbstractModule {
    private final ICommandInvoker invokerMock;
    private final TerminalUserInterface ifaceMock;
    private final IResultVisitor visitorMock;

    /**
     * Instantiates a new MockModule with a {@code null} @{link TerminalUserInterface} and a
     * {@code null} {@link IResultVisitor}.
     *
     * @param invokerMock
     *            The mock {@link ICommandInvoker}.
     */
    public MockModule(ICommandInvoker invokerMock) {
        this(invokerMock, null, null);
    }

    /**
     * Instantiates a new MockModule with a {@code null} {@link IResultVisitor}.
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
     * @param visitorMock
     *            The mock {@link IResultVisitor}.
     */
    public MockModule(TerminalUserInterface ifaceMock, IResultVisitor visitorMock) {
        this(null, ifaceMock, visitorMock);
    }

    /**
     * Instantiates a new MockModule.
     *
     * @param invokerMock
     *            The mock {@link ICommandInvoker}.
     * @param ifaceMock
     *            The mock {@link TerminalUserInterface}.
     * @param visitorMock
     *            The mock {@link IResultVisitor}.
     */
    public MockModule(ICommandInvoker invokerMock, TerminalUserInterface ifaceMock,
                      IResultVisitor visitorMock) {
        this.invokerMock = invokerMock;
        this.ifaceMock = ifaceMock;
        this.visitorMock = visitorMock;
    }

    @Override
    protected void configure() {
        if (invokerMock != null) {
            bind(ICommandInvoker.class).toInstance(invokerMock);
        }
        if (ifaceMock != null) {
            bind(TerminalUserInterface.class).toInstance(ifaceMock);
        }
        if (visitorMock != null) {
            bind(IResultVisitor.class).toInstance(visitorMock);
        }
    }
}
