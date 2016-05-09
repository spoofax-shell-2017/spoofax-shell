package org.metaborg.spoofax.shell.commands;

import java.util.function.Consumer;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Command for evaluating the String as an expression in some language.
 */
public class SpoofaxEvaluationCommand implements IReplCommand {
    private static final String DESCRIPTION = "Given an expression, evaluate it and show the result"
                                              + " of it when it succeeded, otherwise show the"
                                              + " error.";
    private Consumer<String> onSuccess;
    private Consumer<String> onError;

    /**
     * Create an {@link IEvaluationCommand}.
     *
     * TODO: Create more specific interfaces for the hooks, which can accept more parameters. The
     * parameters that are needed are currently unknown.
     *
     * @param onSuccess
     *            Called upon success by the created {@link IEvaluationCommand}.
     * @param onError
     *            Called upon an error by the created {@link IEvaluationCommand}.
     */
    @Inject
    SpoofaxEvaluationCommand(@Named("onSuccess") Consumer<String> onSuccess,
                             @Named("onError") Consumer<String> onError) {
        this.onSuccess = onSuccess;
        this.onError = onError;
    }

    @Override
    public String description() {
        return DESCRIPTION;
    }

    /**
     * Evaluate the given String as an expression in some language.
     *
     * @param args
     *            The String to be parsed and executed.
     */
    public void execute(String... args) {
        onSuccess.accept("Hai, good job.");
    }
}
