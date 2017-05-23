package org.metaborg.spoofax.shell.functions;

import javax.inject.Named;

import org.metaborg.core.action.ITransformAction;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.shell.commands.CommandBuilder;
import org.metaborg.spoofax.shell.commands.IReplCommand;
import org.metaborg.spoofax.shell.output.AnalyzeResult;
import org.metaborg.spoofax.shell.output.EvaluateResult;
import org.metaborg.spoofax.shell.output.IResult;
import org.metaborg.spoofax.shell.output.ISpoofaxTermResult;
import org.metaborg.spoofax.shell.output.InputResult;
import org.metaborg.spoofax.shell.output.ParseResult;
import org.metaborg.spoofax.shell.output.StyleResult;
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
    @Named("Source")
    FailableFunction<String, InputResult, IResult>
    createInputFunction(IProject project, ILanguageImpl lang);

    /**
     * Factory method for creating a {@link OpenInputFunction} that reads source by opening a file.
     * @param project  The associated {@link IProject}
     * @param lang     The associated {@link ILanguageImpl}
     * @return         a {@link InputFunction}
     */
    @Named("Open")
    FailableFunction<String, InputResult, IResult>
    createOpenInputFunction(IProject project, ILanguageImpl lang);

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
     * Factory method for creating a {@link EvaluateFunction}.
     * @param project   The associated {@link IProject}
     * @param lang      The associated {@link ILanguageImpl}
     * @return          an {@link PEvalFunction}
     */
    FailableFunction<ISpoofaxTermResult<?>, EvaluateResult, IResult>
    createEvaluateFunction(IProject project, ILanguageImpl lang);

    /**
     * Factory method for creating a {@link StyleFunction}.
     * @param project   The associated {@link IProject}
     * @param lang      The associated {@link ILanguageImpl}
     * @return          an {@link StyleFunction}
     */
    FailableFunction<ParseResult, StyleResult, IResult>
    createStyleFunction(IProject project, ILanguageImpl lang);

    /**
     * Factory method for creating a {@link CommandBuilder}.
     * The {@link CommandBuilder} composes an {@link IReplCommand}
     * from several {@link AbstractSpoofaxFunction}s.
     *
     * This factory method creates a {@link CommandBuilder} no initial function.
     * Before calling {@link CommandBuilder#build()}, the  caller is required to
     * set a function with one of the builder methods of the {@link CommandBuilder}.
     *
     * @param project   The associated {@link IProject}
     * @param lang      The associated {@link ILanguageImpl}
     * @return          an {@link CommandBuilder}
     */
    CommandBuilder<?> createBuilder(IProject project, ILanguageImpl lang);

}
