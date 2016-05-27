package org.metaborg.spoofax.shell.commands;

import org.metaborg.spoofax.shell.core.Repl;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Exit the REPL.
 */
public class ExitCommand implements IReplCommand {

    private final Provider<Repl> replProvider;

    /**
     * Instantiates a new ExitCommand.
     *
     * @param replProvider
     *            Provides the REPL instance.
     */
    @Inject
    public ExitCommand(Provider<Repl> replProvider) {
        this.replProvider = replProvider;
    }

    @Override
    public String description() {
        return "Exit the REPL session.";
    }

    @Override
    public void execute(String... args) {
        replProvider.get().setRunning(false);
    }
}
