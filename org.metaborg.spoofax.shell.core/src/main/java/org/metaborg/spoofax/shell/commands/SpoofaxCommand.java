package org.metaborg.spoofax.shell.commands;

import java.util.function.Consumer;

import org.metaborg.core.context.IContext;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.shell.core.StyledText;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Command for processing a String as an expression in some language.
 */
public abstract class SpoofaxCommand implements IReplCommand {
    private static final String DESCRIPTION = "Process an expression in some language";
    protected Consumer<StyledText> onSuccess;
    protected Consumer<StyledText> onError;

    @Inject
    protected IStrategoCommon common;
    @Inject
    protected IContext context;

    /**
     * Instantiate a {@link SpoofaxCommand}.
     * @param onSuccess Called upon success by the created {@link SpoofaxCommand}.
     * @param onError   Called upon an error by the created {@link SpoofaxCommand}.
     */
    @Inject
    SpoofaxCommand(@Named("onSuccess") final Consumer<StyledText> onSuccess,
                   @Named("onError") final Consumer<StyledText> onError) {
        this.onSuccess = onSuccess;
        this.onError = onError;
    }

    @Override
    public String description() {
        return DESCRIPTION;
    }

    /**
     * Executes a command.
     * @param args the command arguments
     */
    @Override
    public abstract void execute(String... args);
}
