package org.metaborg.spoofax.shell.commands;

import java.util.function.Consumer;

import org.metaborg.spoofax.shell.core.StyledText;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Command for evaluating the String as an expression in some language.
 */
public class SpoofaxEvaluationCommand implements IReplCommand {
    private static final String DESCRIPTION = "Given an expression, evaluate it and show the result"
                                              + " of it when it succeeded, otherwise show the"
                                              + " error.";
    private Consumer<StyledText> onSuccess;
    private Consumer<StyledText> onError;

    /**
     * Create an {@link SpoofaxEvaluationCommand}.
     *
     * TODO: Create more specific interfaces for the hooks, which can accept more parameters. The
     * parameters that are needed are currently unknown.
     *
     * @param onSuccess
     *            Called upon success by the created {@link SpoofaxEvaluationCommand}.
     * @param onError
     *            Called upon an error by the created {@link SpoofaxEvaluationCommand}.
     */
    @Inject
    SpoofaxEvaluationCommand(@Named("onSuccess") Consumer<StyledText> onSuccess,
                             @Named("onError") Consumer<StyledText> onError) {
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
    @Override
    public void execute(String... args) {
        onSuccess.accept(new StyledText("Hai, good job."));
        onError.accept(new StyledText("Oh no.."));
    }
}
