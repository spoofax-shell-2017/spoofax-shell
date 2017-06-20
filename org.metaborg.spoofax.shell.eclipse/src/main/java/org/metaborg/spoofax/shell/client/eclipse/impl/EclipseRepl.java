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
import org.metaborg.spoofax.shell.output.ExceptionResult;
import org.metaborg.spoofax.shell.output.FailOrSuccessResult;
import org.metaborg.spoofax.shell.output.FailOrSuccessVisitor;
import org.metaborg.spoofax.shell.output.IResult;
import org.metaborg.spoofax.shell.output.StyleResult;
import org.metaborg.spoofax.shell.output.StyledText;
import org.metaborg.spoofax.shell.services.IEditorServices;

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
public class EclipseRepl implements IRepl {
    private final IDisplay display;
    private final EclipseEditor editor;
    private final ICommandInvoker invoker;
    private final IEditorServices services;
    private final ExecutorService pool;

    private final Observer<String> lineInputObserver;
    private final Observer<String> liveInputObserver;

    /**
     * Instantiates a new EclipseRepl.
     *
     * @param invoker
     *            The {@link ICommandInvoker} for executing user input.
     * @param services
     *            The {@link IEditorServices} for requesting editor features.
     * @param display
     *            The {@link IDisplay} to send results to.
     * @param editor
     *            The {@link EclipseEditor} to send input results to.
     */
    @AssistedInject
    public EclipseRepl(ICommandInvoker invoker, IEditorServices services,
            @Assisted IDisplay display, @Assisted EclipseEditor editor) {
        this.display = display;
        this.editor = editor;
        this.invoker = invoker;
        this.services = services;
        pool = Executors.newSingleThreadExecutor();
        this.lineInputObserver = new LineInputObserver();
        this.liveInputObserver = new LiveInputObserver();
    }

    @Override
    public ICommandInvoker getInvoker() {
        return this.invoker;
    }

    @Override
    public IEditorServices getServices() {
        return services;
    }

    /**
     * The line observer.
     *
     * @return {@link Observer} for input strings.
     */
    public Observer<String> getLineInputObserver() {
        return this.lineInputObserver;
    }

    /**
     * The live observer.
     *
     * @return {@link Observer} for input strings.
     */
    public Observer<String> getLiveInputObserver() {
        return this.liveInputObserver;
    }

    private void appendInputToDisplay(String input) {
        // TODO: Style input! Output cannot be styled since there is no way to "pretty-prettyprint"
        // it back to a format of the language currently being used. As such, it cannot be
        // highlighted.
        Style style = new Style(null, null, true, false, false, false);
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

    private void runSyntaxHighlighting(final String source) {
        Job job = new Job("Spoofax REPL evaluation job") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    FailOrSuccessResult<StyleResult, IResult> result = pool
                            .submit(() -> services.highlight(source)).get();
                    runSyntaxAsUIJob(result);
                    return Status.OK_STATUS;
                } catch (InterruptedException | ExecutionException e) {
                    return Status.CANCEL_STATUS;
                }
            }
        };
        job.setSystem(true);
        job.schedule();
    }

    private void runSyntaxAsUIJob(FailOrSuccessResult<StyleResult, IResult> result) {
        Job job = new UIJob("Spoofax REPL display job") {
            @Override
            public IStatus runInUIThread(IProgressMonitor arg0) {
                result.accept(syntaxVisitor);
                return Status.OK_STATUS;
            }
        };
        job.setPriority(Job.SHORT);
        job.setSystem(true);
        job.schedule();
    }

    // CHECKSTYLE.OFF: LineLength
    private FailOrSuccessVisitor<StyleResult, IResult> syntaxVisitor = new FailOrSuccessVisitor<StyleResult, IResult>() {

        @Override
        public void visitSuccess(StyleResult result) {
            editor.applyStyle(result);
        }

        @Override
        public void visitFailure(IResult result) {
            // TODO Auto-generated method stub
        }

        @Override
        public void visitException(ExceptionResult result) {
            // TODO Auto-generated method stub
        }

    };
    // CHECKSTYLE.ON: LineLength

    /**
     * Abstract observer class implementing common behaviour for both observers.
     */
    private abstract static class InputObserver implements Observer<String> {

        @Override
        public final void onCompleted() {
            // We don't ever call onCompleted ourselves, so if it's called it is unexpectedly and
            // probably an error somewhere. The pipeline cannot be restored, either.
            System.err
                    .println("The observer/observable pipeline has completed unexpectedly."
                            + "There is nothing more to do, try restarting the REPL.");
        }

        @Override
        public final void onError(Throwable t) {
            // Do not display this to the user, as it is an internal exception.
            t.printStackTrace();
        }

    }

    /**
     * Line observer to be notified when the user presses enter.
     */
    private class LineInputObserver extends InputObserver {

        @Override
        public void onNext(String input) {
            appendInputToDisplay(input);
            runAsJob(input);
        }

    }

    /**
     * Live observer to be notified when input is being typed.
     */
    private class LiveInputObserver extends InputObserver {

        @Override
        public void onNext(String input) {
            runSyntaxHighlighting(input);
        }

    }
}
