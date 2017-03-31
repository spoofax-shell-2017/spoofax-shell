package org.metaborg.spoofax.shell.commands;

import org.metaborg.spoofax.shell.output.IResult;
import org.metaborg.spoofax.shell.output.StyledText;

/**
 * Dummy class returned only when no language is loaded.
 */
public class DefaultCommand implements IReplCommand {
    @Override
    public String description() {
        return "No language is loaded yet, type :help for more information.";
    }

    @Override
    public IResult execute(String... args) {
        return (visitor) -> visitor.visitMessage(new StyledText(description()));
    }
}