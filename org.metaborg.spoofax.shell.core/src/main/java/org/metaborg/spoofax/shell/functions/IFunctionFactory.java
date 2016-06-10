package org.metaborg.spoofax.shell.functions;

import org.metaborg.core.action.ITransformAction;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.shell.client.IResult;
import org.metaborg.spoofax.shell.commands.CommandBuilder;
import org.metaborg.spoofax.shell.commands.IReplCommand;
import org.metaborg.spoofax.shell.output.AnalyzeResult;
import org.metaborg.spoofax.shell.output.EvaluateResult;
import org.metaborg.spoofax.shell.output.InputResult;
import org.metaborg.spoofax.shell.output.ParseResult;
import org.metaborg.spoofax.shell.output.TransformResult;

/**
 * Factory for creating a {@link CommandBuilder} and {@link AbstractSpoofaxFunction}.
 * The {@link CommandBuilder} composes an {@link IReplCommand}
 * from several {@link AbstractSpoofaxFunction}.
 */
public interface IFunctionFactory {

    /**
     * Factory method for creating a {@link InputFunction}.
     * @param project  The associated {@link IProject}
     * @param lang     The associated {@link ILanguageImpl}
     * @return         a {@link InputFunction}
     */
    FailableFunction<String, InputResult, IResult>
    createInputFunction(IProject project, ILanguageImpl lang);

    /**
     * Factory method for creating a {@link ParseFunction}.
     * @param project  The associated {@link IProject}
     * @param lang     The associated {@link ILanguageImpl}
     * @return         a {@link ParseFunction}
     */
    FailableFunction<InputResult, ParseResult, IResult>
    createParseFunction(IProject project, ILanguageImpl lang);

    /**
     * Factory method for creating an {@link AnalyzeFunction}.
     * @param project  The associated {@link IProject}
     * @param lang     The associated {@link ILanguageImpl}
     * @return         an {@link AnalyzeFunction}
     */
    FailableFunction<ParseResult, AnalyzeResult, IResult>
    createAnalyzeFunction(IProject project, ILanguageImpl lang);

    /**
     * Factory method for creating a {@link PTransformFunction} from a {@link ParseFunction}.
     * @param project   The associated {@link IProject}
     * @param lang      The associated {@link ILanguageImpl}
     * @param action    The associated {@link ITransformAction}
     * @return          an {@link PTransformFunction}
     */
    FailableFunction<ParseResult, TransformResult, IResult>
    createPTransformFunction(IProject project, ILanguageImpl lang,
                                                ITransformAction action);

    /**
     * Factory method for creating an {@link ATransformFunction} from an {@link AnalyzeFunction}.
     * @param project   The associated {@link IProject}
     * @param lang      The associated {@link ILanguageImpl}
     * @param action    The associated {@link ITransformAction}
     * @return          an {@link ATransformFunction}
     */
    FailableFunction<AnalyzeResult, TransformResult, IResult>
    createATransformFunction(IProject project, ILanguageImpl lang,
                                                ITransformAction action);

    /**
     * Factory method for creating a {@link PEvalFunction} from a {@link ParseFunction}.
     * @param project   The associated {@link IProject}
     * @param lang      The associated {@link ILanguageImpl}
     * @return          an {@link PEvalFunction}
     */
    FailableFunction<ParseResult, EvaluateResult, IResult>
    createPEvalFunction(IProject project, ILanguageImpl lang);

    /**
     * Factory method for creating an {@link AEvalFunction} from an {@link AnalyzeFunction}.
     * @param project   The associated {@link IProject}
     * @param lang      The associated {@link ILanguageImpl}
     * @return          an {@link AEvalFunction}
     */
    FailableFunction<AnalyzeResult, EvaluateResult, IResult>
    createAEvalFunction(IProject project, ILanguageImpl lang);

    /**
     * Factory method for creating a {@link CommandBuilder}.
     * The {@link CommandBuilder} composes an {@link IReplCommand}
     * from several {@link AbstractSpoofaxFunction}s.
     *
     * @param project   The associated {@link IProject}
     * @param lang      The associated {@link ILanguageImpl}
     * @return          an {@link CommandBuilder}
     */
    CommandBuilder<?> createBuilder(IProject project, ILanguageImpl lang);
}
