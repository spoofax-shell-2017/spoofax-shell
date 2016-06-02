package org.metaborg.spoofax.shell.client.eclipse.impl.hooks;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.progress.UIJob;
import org.metaborg.spoofax.shell.client.IDisplay;
import org.metaborg.spoofax.shell.client.eclipse.impl.EclipseRepl;
import org.metaborg.spoofax.shell.client.hooks.IResultHook;
import org.metaborg.spoofax.shell.output.ISpoofaxResult;

import com.google.inject.Inject;

/**
 * An Eclipse-based implementation of {@link IResultHook}.
 *
 * Note that hooks do not run in the UI thread, since they are automatically called by the
 * {@link ISpoofaxCommand}s which are started by the {@link EclipseRepl} in its own thread.
 */
public class EclipseResultHook implements IResultHook {
    private final IDisplay display;

    /**
     * Instantiates a new EclipseResultHook.
     *
     * @param display
     *            The {@link IDisplay} to display the messages on.
     */
    @Inject
    public EclipseResultHook(IDisplay display) {
        this.display = display;
    }

    @Override
    public void accept(ISpoofaxResult<?> result) {
        Job job = new UIJob("REPL Result Hook") {
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                display.displayResult(result.styled());
                return Status.OK_STATUS;
            }
        };
        job.setPriority(Job.SHORT);
        job.setSystem(true);
        job.schedule();
    }

}
