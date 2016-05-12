package org.metaborg.spoofax.shell.client.console;

import org.metaborg.spoofax.shell.client.IEditor;
import org.metaborg.spoofax.shell.commands.ICommandInvoker;

import com.google.inject.AbstractModule;

/**
 * Allows for injecting mock {@link ICommandInvoker}s, for spying/verifying.
 */
public class MockModule extends AbstractModule {
    private ICommandInvoker invokerMock;
    private IEditor editorMock;

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
        this.invokerMock = invokerMock;
        this.editorMock = editorMock;
    }

    @Override
    protected void configure() {
        if (invokerMock != null) {
            bind(ICommandInvoker.class).toInstance(invokerMock);
        }
        if (editorMock != null) {
            bind(IEditor.class).toInstance(editorMock);
        }
    }

}
