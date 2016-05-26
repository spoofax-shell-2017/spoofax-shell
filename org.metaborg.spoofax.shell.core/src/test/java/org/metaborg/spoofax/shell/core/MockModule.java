package org.metaborg.spoofax.shell.core;

import org.metaborg.spoofax.shell.invoker.ICommandInvoker;

import com.google.inject.AbstractModule;

/**
 * Allows for injecting mock {@link ICommandInvoker}s, for spying/verifying.
 */
public class MockModule extends AbstractModule {
    private final Repl replMock;
    private final ICommandInvoker invokerMock;

    /**
     * Instantiates a new MockModule with a {@code null} @{link Repl}.
     *
     * @param invokerMock
     *            The mock {@link ICommandInvoker}.
     */
    public MockModule(ICommandInvoker invokerMock) {
        this(null, invokerMock);
    }

    /**
     * Instantiates a new MockModule with a {@code null} @{link ICommandInvoker}.
     *
     * @param replMock
     *            The mock {@link Repl}.
     */
    public MockModule(Repl replMock) {
        this(replMock, null);
    }

    /**
     * Instantiates a new MockModule.
     *
     * @param replMock
     *            The mock {@link Repl}.
     * @param invokerMock
     *            The mock {@link ICommandInvoker}.
     */
    public MockModule(Repl replMock, ICommandInvoker invokerMock) {
        this.replMock = replMock;
        this.invokerMock = invokerMock;
    }

    @Override
    protected void configure() {
        if (replMock != null) {
            bind(Repl.class).toInstance(replMock);
        }
        if (invokerMock != null) {
            bind(ICommandInvoker.class).toInstance(invokerMock);
        }
    }
}
