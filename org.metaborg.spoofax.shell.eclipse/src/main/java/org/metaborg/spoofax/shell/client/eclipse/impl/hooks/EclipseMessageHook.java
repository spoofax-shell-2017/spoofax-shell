package org.metaborg.spoofax.shell.client.eclipse.impl.hooks;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.progress.UIJob;
import org.metaborg.spoofax.shell.client.IDisplay;
import org.metaborg.spoofax.shell.client.eclipse.impl.EclipseRepl;
import org.metaborg.spoofax.shell.client.hooks.IMessageHook;
import org.metaborg.spoofax.shell.output.StyledText;

import com.google.inject.Inject;

/**
 * An Eclipse-based implementation of {@link IMessageHook}.
 *
 * Note that hooks do not run in the UI thread, since they are automatically called by the
 * {@link ISpoofaxCommand}s which are started by the {@link EclipseRepl} in its own thread.
 */
public class EclipseMessageHook implements IMessageHook {
    private final IDisplay display;

    /**
     * Instantiates a new EclipseMessageHook.
     *
     * @param display
     *            The {@link IDisplay} to display the messages on.
     */
    @Inject
    public EclipseMessageHook(IDisplay display) {
        this.display = display;
    }

    @Override
    public void accept(StyledText message) {
        Job job = new UIJob("REPL Message Hook") {
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                display.displayResult(message);
                return Status.OK_STATUS;
            }
        };
        job.setPriority(Job.SHORT);
        job.setSystem(true);
        job.schedule();
    }

}
