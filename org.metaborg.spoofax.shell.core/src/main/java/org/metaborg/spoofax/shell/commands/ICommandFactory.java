package org.metaborg.spoofax.shell.commands;

import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;

/**
 * Factory for creating Spoofax commands.
 * @author gerlof
 *
 */
public interface ICommandFactory {
    /**
     * Factory method for creating a parse command.
     * @param project The associated {@link IProject}
     * @param lang    The associated {@link ILanguageImpl}
     * @return        a {@link ParseCommand}
     */
    ParseCommand createParse(IProject project, ILanguageImpl lang);

    /**
     * Factory method for creating an analyze command.
     * @param project The associated {@link IProject}
     * @param lang    The associated {@link ILanguageImpl}
     * @return        an {@link AnalyzeCommand}
     */
    AnalyzeCommand createAnalyze(IProject project, ILanguageImpl lang);
}
