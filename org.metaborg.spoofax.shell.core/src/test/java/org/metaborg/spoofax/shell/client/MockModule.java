package org.metaborg.spoofax.shell.client;

import org.metaborg.spoofax.shell.client.IEditor;
import org.metaborg.spoofax.shell.commands.ICommandInvoker;

import com.google.inject.AbstractModule;

/**
 * Allows for injecting mock {@link ICommandInvoker}s, for spying/verifying.
 */
public class MockModule extends AbstractModule {
    private ICommandInvoker invokerMock;
    private IEditor editorMock;
    private IDisplay displayMock;

    /**
     * @param invokerMock
     *            The mock {@link ICommandInvoker}.
     */
    public MockModule(ICommandInvoker invokerMock) {
        this(invokerMock, null);
    }

    /**
     * @param invokerMock
     *            The mock {@link ICommandInvoker}.
     * @param editorMock
     *            The mock {@link IEditor}.
     */
    public MockModule(ICommandInvoker invokerMock, IEditor editorMock) {
        this(invokerMock, editorMock, null);
    }

    /**
     * @param editorMock
     *            The mock {@link IEditor}.
     * @param displayMock
     *            The mock {@link IDisplay}.
     */
    public MockModule(IEditor editorMock, IDisplay displayMock) {
        this(null, editorMock, displayMock);
    }

    /**
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
