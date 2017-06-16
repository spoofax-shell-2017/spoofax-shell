package org.metaborg.spoofax.shell.functions;

import org.metaborg.core.action.ITransformAction;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.shell.output.AnalyzeResult;
import org.metaborg.spoofax.shell.output.EvaluateResult;
import org.metaborg.spoofax.shell.output.IResult;
import org.metaborg.spoofax.shell.output.InputResult;
import org.metaborg.spoofax.shell.output.ParseResult;
import org.metaborg.spoofax.shell.output.TransformResult;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

/**
 * Provides function compositions based on the {@link IFunctionFactory}.
 *
 */
public class FunctionComposer {

    private IFunctionFactory functionFactory;
    private IProject project;
    private ILanguageImpl lang;

    /**
     * Constructs a new {@link FunctionComposer} from the given parameters.
     *
     * @param functionFactory
     *            the {@link IFunctionFactory}
     * @param project
     *            the {@link IProject} associated with all created commands
     * @param lang
     *            the {@link ILanguageImpl} associated with all created commands
     */
    @AssistedInject
    public FunctionComposer(IFunctionFactory functionFactory, @Assisted IProject project,
            @Assisted ILanguageImpl lang) {
                this.functionFactory = functionFactory;
                this.project = project;
                this.lang = lang;
    }

	/**
	 * Expose the default {@link InputFunction}.
	 *
	 * @return {@link InputFunction} - The input function as defined in{@link IFunctionFactory}.
	 */
    public FailableFunction<String, InputResult, IResult> inputFunction() {
        return functionFactory.createInputFunction(project, lang);
    }

	/**
	 * Compose the default {@link ParseFunction}.
	 *
	 * @return {@link ParseFunction} - The parse function defined in {@link IFunctionFactory}.
	 */
    public FailableFunction<String, ParseResult, IResult> parseFunction() {
        return inputFunction().kleisliCompose(functionFactory.createParseFunction(project, lang));
    }

	/**
	 * Compose the default {@link AnalyzeFunction}.
	 *
	 * @return {@link AnalyzeFunction} - The analyze function defined in {@link IFunctionFactory}.
	 */
    public FailableFunction<String, AnalyzeResult, IResult> analyzeFunction() {
        return parseFunction().kleisliCompose(functionFactory.createAnalyzeFunction(project, lang));
    }

	/**
	 * Compose a {@link PTransformFunction}, which transforms after the parse step.
	 *
	 * @param action
	 *            {@link ITransformAction} - The transformation action
	 * @return {@link FailableFunction} returning a successful {@link TransformResult}.
	 */
    public FailableFunction<String, TransformResult, IResult>
            pTransformFunction(ITransformAction action) {
        return parseFunction()
            .kleisliCompose(functionFactory.createPTransformFunction(project, lang, action));
    }

	/**
	 * Compose an {@link ATransformFunction}, which transforms after the analyze step.
	 *
	 * @param action
	 *            {@link ITransformAction} - The transformation action
	 * @return {@link FailableFunction} returning a successful {@link TransformResult}.
	 */
    public FailableFunction<String, TransformResult, IResult>
            aTransformFunction(ITransformAction action) {
        return analyzeFunction()
            .kleisliCompose(functionFactory.createATransformFunction(project, lang, action));
    }

	/**
	 * Compose an {@link EvaluateFunction} that evaluates after the parse step.
	 *
	 * @return {@link EvaluateFunction} - The evaluation function.
	 */
    public FailableFunction<String, EvaluateResult, IResult> pEvaluateFunction() {
        return parseFunction()
            .kleisliCompose(functionFactory.createEvaluateFunction(project, lang));
    }

	/**
	 * Compose an {@link EvaluateFunction} that evaluates after the analyze step.
	 *
	 * @return {@link EvaluateFunction} - The evaluation function.
	 */
    public FailableFunction<String, EvaluateResult, IResult> aEvaluateFunction() {
        return analyzeFunction()
            .kleisliCompose(functionFactory.createEvaluateFunction(project, lang));
    }
}
