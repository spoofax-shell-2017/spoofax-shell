package org.metaborg.spoofax.shell.invoker;

import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.shell.commands.CommandBuilder;

/**
 * Factory for creating Spoofax commands.
 * @author gerlof
 *
 */
public interface ICommandFactory {
    CommandBuilder createBuilder(IProject project, ILanguageImpl lang);
}
