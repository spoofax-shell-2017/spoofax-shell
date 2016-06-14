package org.metaborg.spoofax.shell.commands;

import java.util.Objects;

import javax.annotation.Nullable;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.action.ITransformAction;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.shell.client.IResult;
import org.metaborg.spoofax.shell.functions.FailableFunction;
import org.metaborg.spoofax.shell.functions.IFunctionFactory;
import org.metaborg.spoofax.shell.output.AnalyzeResult;
import org.metaborg.spoofax.shell.output.EvaluateResult;
import org.metaborg.spoofax.shell.output.InputResult;
import org.metaborg.spoofax.shell.output.ParseResult;
import org.metaborg.spoofax.shell.output.TransformResult;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

/**
 * Builds commands by composing several smaller functional interfaces.
 *
 * @param <R>
 *            the return type of the created command
 */
public class CommandBuilder<R extends IResult> {
    private final IFunctionFactory functionFactory;
    private final ILanguageImpl lang;
    private final IProject project;

    private final String description;
    private final @Nullable FailableFunction<String[], R, IResult> function;

    private CommandBuilder(IFunctionFactory functionFactory, IProject project, ILanguageImpl lang,
                           String description, FailableFunction<String[], R, IResult> function) {
        this.functionFactory = functionFactory;
        this.project = project;
        this.lang = lang;
        this.description = description;
        this.function = function;
    }

    /**
     * Constructs a new {@link CommandBuilder} from the given parameters.
     *
     * @param functionFactory
     *            the {@link IFunctionFactory}
     * @param project
     *            the {@link IProject} associated with all created commands
     * @param lang
     *            the {@link ILanguageImpl} associated with all created commands
     */
    @AssistedInject
    public CommandBuilder(IFunctionFactory functionFactory, @Assisted IProject project,
                          @Assisted ILanguageImpl lang) {
        this(functionFactory, project, lang, "", null);
    }

    /**
     * Constructs a new {@link CommandBuilder} from a parent.
     *
     * @param parent
     *            the parent {@link CommandBuilder}
     * @param description
     *            the description of the created command
     * @param function
     *            the function the created command will execute
     */
    private CommandBuilder(CommandBuilder<?> parent, String description,
                           FailableFunction<String[], R, IResult> function) {
        this(parent.functionFactory, parent.project, parent.lang, description, function);
    }

    private FailableFunction<String, InputResult, IResult> inputFunction() {
        return functionFactory.createInputFunction(project, lang);
    }

    private FailableFunction<String, ParseResult, IResult> parseFunction() {
        return inputFunction().kleisliCompose(functionFactory.createParseFunction(project, lang));
    }

    private FailableFunction<String, AnalyzeResult, IResult> analyzeFunction() {
        return parseFunction().kleisliCompose(functionFactory.createAnalyzeFunction(project, lang));
    }

    private FailableFunction<String, TransformResult, IResult>
            pTransformFunction(ITransformAction action) {
        return parseFunction()
            .kleisliCompose(functionFactory.createPTransformFunction(project, lang, action));
    }

    private FailableFunction<String, TransformResult, IResult>
            aTransformFunction(ITransformAction action) {
        return analyzeFunction()
            .kleisliCompose(functionFactory.createATransformFunction(project, lang, action));
    }

    private FailableFunction<String, EvaluateResult, IResult> pEvaluateFunction() {
        return parseFunction().kleisliCompose(functionFactory.createPEvalFunction(project, lang));
    }

    private FailableFunction<String, EvaluateResult, IResult> aEvaluateFunction() {
        return analyzeFunction().kleisliCompose(functionFactory.createAEvalFunction(project, lang));
    }

    /**
     * Returns a function that creates an {@link InputResult} from a String.
     *
     * @return the builder
     */
    public CommandBuilder<InputResult> input() {
        return function(inputFunction());
    }

    /**
     * Returns a function that creates a {@link ParseResult} from a String.
     *
     * @return the builder
     */
    public CommandBuilder<ParseResult> parse() {
        return function(parseFunction());
    }

    /**
     * Returns a function that creates a {@link AnalyzeResult} from a String.
     *
     * @return the builder
     */
    public CommandBuilder<AnalyzeResult> analyze() {
        return function(analyzeFunction());
    }

    /**
     * Returns a function that creates a parsed {@link TransformResult} from a String.
     *
     * @param action
     *            the associated {@link ITransformAction}
     * @return the builder
     */
    public CommandBuilder<TransformResult> transformParsed(ITransformAction action) {
        return function(pTransformFunction(action));
    }

    /**
     * Returns a function that creates an analyzed {@link TransformResult} from a String.
     *
     * @param action
     *            the associated {@link ITransformAction}
     * @return the builder
     */
    public CommandBuilder<TransformResult> transformAnalyzed(ITransformAction action) {
        return function(aTransformFunction(action));
    }

    /**
     * Returns a function that creates a parsed {@link EvaluateResult} from a String.
     *
     * @return the builder
     */
    public CommandBuilder<EvaluateResult> evalParsed() {
        return function(pEvaluateFunction());
    }

    /**
     * Returns a function that creates an analyzed {@link EvaluateResult} from a String.
     *
     * @return the builder
     */
    public CommandBuilder<EvaluateResult> evalAnalyzed() {
        return function(aEvaluateFunction());
    }

    /**
     * Set the function for a new builder with the current parameters. Discards the current function
     * of this builder.
     *
     * @param func
     *            An initial {@link FailableFunction} accepting a String array and returning some
     *            {@link IResult}.
     * @return A {@link CommandBuilder} with given function as current function.
     * @param <OtherR>
     *            The return type of the given function.
     */
    public <OtherR extends IResult> CommandBuilder<OtherR>
            varFunction(FailableFunction<String[], OtherR, IResult> func) {
        return new CommandBuilder<>(this, description, func);
    }

    /**
     * Variant of {@link #varFunction(FailableFunction)} accepting a single string.
     *
     * @param func
     *            An initial {@link FailableFunction} accepting a String and returning some
     *            {@link IResult}.
     * @return A {@link CommandBuilder} with given function as current function.
     * @param <OtherR>
     *            The return type of the given function.
     */
    public <OtherR extends IResult> CommandBuilder<OtherR>
            function(FailableFunction<String, OtherR, IResult> func) {
        return varFunction((String... args) -> func.apply(args[0]));
    }

    /**
     * Compose a given {@link FailableFunction} with the current function.
     *
     * @param func
     *            A {@link FailableFunction} accepting the return type of the current function, and
     *            returning a new return type.
     * @return A {@link CommandBuilder} with the composed {@link FailableFunction}.
     * @param <NewR>
     *            The return type of the given function.
     */
    public <NewR extends IResult> CommandBuilder<NewR>
            compose(FailableFunction<R, NewR, IResult> func) {
        Objects.requireNonNull(function,
                               "The current function cannot be null"
                                         + " before composing. Set a function with "
                                         + "one of the builder methods.");
        return new CommandBuilder<>(this, description, this.function.kleisliCompose(func));
    }

    /**
     * Sets the description of the created command.
     *
     * @param description
     *            the description of the command
     * @return the builder
     */
    public CommandBuilder<R> description(String description) {
        return new CommandBuilder<>(this, description, function);
    }

    /**
     * Returns an {@link IReplCommand} given a function.
     *
     * @return an {@link IReplCommand}
     */
    public IReplCommand build() {
        return new IReplCommand() {
            @Override
            public IResult execute(String... args) throws MetaborgException {
                return function.apply(args);
            }

            @Override
            public String description() {
                return description;
            }
        };
    }

}
