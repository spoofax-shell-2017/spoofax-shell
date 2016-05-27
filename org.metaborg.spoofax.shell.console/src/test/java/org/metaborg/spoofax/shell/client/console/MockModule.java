package org.metaborg.spoofax.shell.client.console;

import org.metaborg.spoofax.shell.invoker.ICommandInvoker;

import com.google.inject.AbstractModule;

/**
 * Allows for injecting mock {@link ICommandInvoker}s, for spying/verifying.
 */
public class MockModule extends AbstractModule {
    private final ICommandInvoker invokerMock;
    private final IEditor editorMock;
    private final IDisplay displayMock;

    /**
     * Instantiates a new MockModule with a {@code null} @{link IEditor} and a {@code null}
     * {@link IDisplay}.
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
     * @param editorMock
     *            The mock {@link IEditor}.
     */
    public MockModule(ICommandInvoker invokerMock, IEditor editorMock) {
        this(invokerMock, editorMock, null);
    }

    /**
     * Instantiates a new MockModule with a {@code null} {@link ICommandInvoker}.
     *
     * @param editorMock
     *            The mock {@link IEditor}.
     * @param displayMock
     *            The mock {@link IDisplay}.
     */
    public MockModule(IEditor editorMock, IDisplay displayMock) {
        this(null, editorMock, displayMock);
    }

    /**
     * Instantiates a new MockModule.
     *
     * @param invokerMock
     *            The mock {@link ICommandInvoker}.
     * @param editorMock
     *            The mock {@link IEditor}.
     * @param displayMock
     *            The mock {@link IDisplay}.
     */
    public MockModule(ICommandInvoker invokerMock, IEditor editorMock, IDisplay displayMock) {
        this.invokerMock = invokerMock;
        this.editorMock = editorMock;
        this.displayMock = displayMock;
    }

    @Override
    protected void configure() {
        if (invokerMock != null) {
            bind(ICommandInvoker.class).toInstance(invokerMock);
        }
        if (editorMock != null) {
            bind(IEditor.class).toInstance(editorMock);
        }
        if (displayMock != null) {
            bind(IDisplay.class).toInstance(displayMock);
        }
    }
}
