package org.metaborg.spoofax.shell.client.eclipse.impl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.progress.UIJob;
import org.metaborg.core.style.Style;
import org.metaborg.spoofax.shell.client.IDisplay;
import org.metaborg.spoofax.shell.client.IRepl;
import org.metaborg.spoofax.shell.invoker.ICommandInvoker;
import org.metaborg.spoofax.shell.output.IResult;
import org.metaborg.spoofax.shell.output.StyledText;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import rx.Observer;

/**
 * An Eclipse-based implementation of {@link IRepl}.
 *
 * It uses a multiline input editor with keyboard shortcuts, including persistent history, syntax
 * highlighting and error marking.
 *
 * Note that this class evaluates input in a separate thread.
 */
public class EclipseRepl implements IRepl, Observer<String> {
    private final IDisplay display;
    private final ICommandInvoker invoker;
    private final ExecutorService pool;

    /**
     * Instantiates a new EclipseRepl.
     *
     * @param invoker
     *            The {@link ICommandInvoker} for executing user input.
     * @param display
     *            The {@link IDisplay} to send results to.
     */
    @AssistedInject
    public EclipseRepl(ICommandInvoker invoker, @Assisted IDisplay display) {
        this.display = display;
        this.invoker = invoker;
        pool = Executors.newSingleThreadExecutor();
    }

    @Override
    public ICommandInvoker getInvoker() {
        return this.invoker;
    }

    @Override
    public void onCompleted() {
        // We don't ever call onCompleted ourselves, so if it's called it is unexpectedly and
        // probably an error somewhere. The pipeline cannot be restored, either.
        System.err
            .println("The observer/observable pipeline has completed unexpectedly."
                     + "There is nothing more to do, try restarting the REPL.");
    }

    @Override
    public void onError(Throwable t) {
        // Do not display this to the user, as it is an internal exception.
        t.printStackTrace();
    }

    @Override
    public void onNext(String input) {
        appendInputToDisplay(input);
        runAsJob(input);
    }

    private void appendInputToDisplay(String input) {
        // TODO: Style input! Output cannot be styled since there is no way to "pretty-prettyprint"
        // it back to a format of the language currently being used. As such, it cannot be
        // highlighted.
        Style style = new Style(null, null, true, false, false);
        this.display.displayStyledText(new StyledText(style, input));
    }

    private void runAsJob(final String input) {
        Job job = new Job("Spoofax REPL evaluation job") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    IResult result = pool.submit(() -> eval(input)).get();
                    runAsUIJob(result);
                    return Status.OK_STATUS;
                } catch (InterruptedException | ExecutionException e) {
                    return Status.CANCEL_STATUS;
                }
            }
        };
        job.setSystem(true);
        job.schedule();
    }

    private void runAsUIJob(IResult result) {
        Job job = new UIJob("Spoofax REPL display job") {
            @Override
            public IStatus runInUIThread(IProgressMonitor arg0) {
                result.accept(display);
                display.displayStyledText(new StyledText());
                return Status.OK_STATUS;
            }
        };
        job.setPriority(Job.SHORT);
        job.setSystem(true);
        job.schedule();
    }

}
