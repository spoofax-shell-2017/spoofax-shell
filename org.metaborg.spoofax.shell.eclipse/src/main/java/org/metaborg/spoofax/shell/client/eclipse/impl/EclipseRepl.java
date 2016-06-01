package org.metaborg.spoofax.shell.client.eclipse.impl;

import java.awt.Color;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.style.Style;
import org.metaborg.spoofax.shell.core.IRepl;
import org.metaborg.spoofax.shell.invoker.CommandNotFoundException;
import org.metaborg.spoofax.shell.invoker.ICommandInvoker;
import org.metaborg.spoofax.shell.output.StyledText;

import com.google.inject.Inject;

import rx.Observer;

/**
 * An Eclipse-based REPL.
 *
 * It uses a multiline input editor with keyboard shortcuts, including persistent history, syntax
 * highlighting and error marking.
 */
public class EclipseRepl implements IRepl, Observer<String> {
    private static final int INPUT_RED = 232;
    private static final int INPUT_GREEN = 242;
    private static final int INPUT_BLUE = 254;
    private final EclipseDisplay display;
    private final ICommandInvoker invoker;

    /**
     * Instantiates a new EclipseRepl.
     *
     * @param display
     *            The {@link EclipseDisplay} to print results to.
     * @param invoker
     *            The {@link ICommandInvoker} for executing user input.
     */
    @Inject
    public EclipseRepl(EclipseDisplay display, ICommandInvoker invoker) {
        this.display = display;
        this.invoker = invoker;
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
        Color inputBackgroundColor = new Color(INPUT_RED, INPUT_GREEN, INPUT_BLUE);
        Style style = new Style(null, inputBackgroundColor, false, false, false);
        this.display.displayResult(new StyledText(style, input));
    }

    private void runAsJob(final String input) {
        Job job = new Job("Spoofax REPL") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    eval(input);
                    return Status.OK_STATUS;
                } catch (MetaborgException | CommandNotFoundException e) {
                    // TODO: use hooks directly so only hooks need to schedule things on the ui
                    // thread?
                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            display.displayError(new StyledText(Color.RED, e.getMessage()));
                        }
                    });
                    return Status.CANCEL_STATUS;
                }
            }
        };
        job.setSystem(true);
        job.schedule();
    }

}
