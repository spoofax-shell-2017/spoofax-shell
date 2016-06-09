package org.metaborg.spoofax.shell.client.console.impl;

import org.metaborg.spoofax.shell.client.IResultVisitor;
import org.metaborg.spoofax.shell.client.IEditor;
import org.metaborg.spoofax.shell.invoker.ICommandInvoker;

import com.google.inject.AbstractModule;

/**
 * Allows for injecting mock {@link ICommandInvoker}s, for spying/verifying.
 */
public class MockModule extends AbstractModule {
    private final ICommandInvoker invokerMock;
    private final IEditor editorMock;
    private final IResultVisitor displayMock;

    /**
     * Instantiates a new MockModule with a {@code null} @{link IEditor} and a {@code null}
     * {@link IResultVisitor}.
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
     *            The mock {@link IResultVisitor}.
     */
    public MockModule(IEditor editorMock, IResultVisitor displayMock) {
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
     *            The mock {@link IResultVisitor}.
     */
    public MockModule(ICommandInvoker invokerMock, IEditor editorMock, IResultVisitor displayMock) {
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
            bind(IResultVisitor.class).toInstance(displayMock);
        }
    }
}
