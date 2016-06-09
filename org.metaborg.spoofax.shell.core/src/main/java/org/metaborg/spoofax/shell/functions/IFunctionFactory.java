package org.metaborg.spoofax.shell.functions;

import org.metaborg.core.action.ITransformAction;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.shell.commands.CommandBuilder;
import org.metaborg.spoofax.shell.commands.IReplCommand;

/**
 * Factory for creating a {@link CommandBuilder} and {@link AbstractFunction}.
 * The {@link CommandBuilder} composes an {@link IReplCommand}
 * from several {@link AbstractFunction}.
 */
public interface IFunctionFactory {
    /**
     * Factory method for creating a {@link ParseFunction}.
     * @param project  The associated {@link IProject}
     * @param lang     The associated {@link ILanguageImpl}
     * @return         a {@link ParseFunction}
     */
    ParseFunction createParseFunction(IProject project, ILanguageImpl lang);

    /**
     * Factory method for creating an {@link AnalyzeFunction}.
     * @param project  The associated {@link IProject}
     * @param lang     The associated {@link ILanguageImpl}
     * @return         an {@link AnalyzeFunction}
     */
    AnalyzeFunction createAnalyzeFunction(IProject project, ILanguageImpl lang);

    /**
     * Factory method for creating a {@link PTransformFunction} from a {@link ParseFunction}.
     * @param project   The associated {@link IProject}
     * @param lang      The associated {@link ILanguageImpl}
     * @param action    The associated {@link ITransformAction}
     * @return          an {@link PTransformFunction}
     */
    PTransformFunction createPTransformFunction(IProject project, ILanguageImpl lang,
                                                ITransformAction action);

    /**
     * Factory method for creating a {@link ATransformFunction} from an {@link AnalyzeFunction}.
     * @param project   The associated {@link IProject}
     * @param lang      The associated {@link ILanguageImpl}
     * @param action    The associated {@link ITransformAction}
     * @return          an {@link ATransformFunction}
     */
    ATransformFunction createATransformFunction(IProject project, ILanguageImpl lang,
                                                ITransformAction action);

    PEvalFunction createPEvalFunction(IProject project, ILanguageImpl lang);
    AEvalFunction createAEvalFunction(IProject project, ILanguageImpl lang);

    /**
     * Factory method for creating a {@link CommandBuilder}.
     * The {@link CommandBuilder} composes an {@link IReplCommand}
     * from several {@link AbstractFunction}
     * @param project   The associated {@link IProject}
     * @param lang      The associated {@link ILanguageImpl}
     * @return          an {@link CommandBuilder}
     */
    CommandBuilder<?> createBuilder(IProject project, ILanguageImpl lang);
}
