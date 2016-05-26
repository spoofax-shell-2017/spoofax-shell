package org.metaborg.spoofax.shell.invoker;

import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.shell.commands.AnalyzeCommand;
import org.metaborg.spoofax.shell.commands.EvaluateCommand;
import org.metaborg.spoofax.shell.commands.ParseCommand;
import org.metaborg.spoofax.shell.commands.TransformCommand;

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

    /**
     * Factory method for creating an evaluation command.
     * @param project The associated {@link IProject}
     * @param lang    The associated {@link ILanguageImpl}
     * @return        an {@link EvaluateCommand}
     */
    EvaluateCommand createEvaluate(IProject project, ILanguageImpl lang);

    /**
     * Factory method for creating a transform command.
     * @param project The associated {@link IProject}
     * @param lang    The associated {@link ILanguageImpl}
     * @return        an {@link TransformCommand}
     */
    TransformCommand createTransform(IProject project, ILanguageImpl lang);
}
