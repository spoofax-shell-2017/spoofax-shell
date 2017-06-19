package org.metaborg.spoofax.shell.commands;

import java.util.Objects;

import javax.annotation.Nullable;

import org.metaborg.core.action.ITransformAction;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.shell.functions.FailableFunction;
import org.metaborg.spoofax.shell.functions.FunctionComposer;
import org.metaborg.spoofax.shell.functions.IFunctionFactory;
import org.metaborg.spoofax.shell.output.AnalyzeResult;
import org.metaborg.spoofax.shell.output.EvaluateResult;
import org.metaborg.spoofax.shell.output.FailOrSuccessResult;
import org.metaborg.spoofax.shell.output.IResult;
import org.metaborg.spoofax.shell.output.InputResult;
import org.metaborg.spoofax.shell.output.ParseResult;
import org.metaborg.spoofax.shell.output.TransformResult;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

/**
 * Builds commands by composing {@link FailableFunction}s. This builder provides several functions
 * that are set out of the box, namely:
 *
 * <ol>
 * <li>{@link #input()},</li>
 * <li>{@link #parse()},</li>
 * <li>{@link #analyze()},</li>
 * <li>{@link #transformParsed(ITransformAction)} and {@link #transformAnalyzed(ITransformAction)},
 * </li>
 * <li>{@link #evalParsed()} and {@link #evalAnalyzed()}</li>
 * </ol>
 *
 * It also allows composing ones own {@link FailableFunction}s in a builder like manner. Use
 * {@link #function(FailableFunction)} for setting the initial function, which always starts from an
 * array of {@link String}s and can return any {@link IResult}. Then one can use
 * {@link #compose(FailableFunction)} repeatedly to compose from that function (see also
 * {@link FailableFunction#kleisliCompose(FailableFunction)}.
 *
 * Lastly, the description of the command to be built can be set with {@link #description(String)}.
 * The command itself can be built using the {@link #build()} method.
 *
 * @param <R>
 *            the return type of the created command
 */
public class CommandBuilder<R extends IResult> {
    private final IFunctionFactory functionFactory;
    private final ILanguageImpl lang;
    private final IProject project;
    private final FunctionComposer composer;

    private final String description;
    private final @Nullable FailableFunction<String[], R, IResult> function;

    private CommandBuilder(IFunctionFactory functionFactory, IProject project, ILanguageImpl lang,
                           String description, FailableFunction<String[], R, IResult> function) {
        this.functionFactory = functionFactory;
        this.composer = functionFactory.createComposer(project, lang);
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

    /**
     * Returns a function that creates an {@link InputResult} from a String.
     *
     * @return the builder
     */
    public CommandBuilder<InputResult> input() {
        return function(composer.inputFunction());
    }

    /**
     * Returns a function that creates a {@link ParseResult} from a String.
     *
     * @return the builder
     */
    public CommandBuilder<ParseResult> parse() {
        return function(composer.parseFunction());
    }

    /**
     * Returns a function that creates a {@link AnalyzeResult} from a String.
     *
     * @return the builder
     */
    public CommandBuilder<AnalyzeResult> analyze() {
        return function(composer.analyzeFunction());
    }

    /**
     * Returns a function that creates a parsed {@link TransformResult} from a String.
     *
     * @param action
     *            the associated {@link ITransformAction}
     * @return the builder
     */
    public CommandBuilder<TransformResult> transformParsed(ITransformAction action) {
        return function(composer.pTransformFunction(action));
    }

    /**
     * Returns a function that creates an analyzed {@link TransformResult} from a String.
     *
     * @param action
     *            the associated {@link ITransformAction}
     * @return the builder
     */
    public CommandBuilder<TransformResult> transformAnalyzed(ITransformAction action) {
        return function(composer.aTransformFunction(action));
    }

    /**
     * Returns a function that creates a parsed {@link EvaluateResult} from a String.
     *
     * @return the builder
     */
    public CommandBuilder<EvaluateResult> evalParsed() {
        return function(composer.pEvaluateFunction());
    }

    /**
     * Returns a function that creates an analyzed {@link EvaluateResult} from a String.
     *
     * @return the builder
     */
    public CommandBuilder<EvaluateResult> evalAnalyzed() {
        return function(composer.aEvaluateFunction());
    }

    /**
     * Returns a function that creates a parsed {@link EvaluateResult} from a file path.
     *
     * @return the builder
     */
    public CommandBuilder<EvaluateResult> evalPOpen() {
        return function(functionFactory.createOpenInputFunction(project, lang)
                .kleisliCompose(functionFactory.createParseFunction(project, lang))
                .kleisliCompose(functionFactory.createEvaluateFunction(project, lang)));
    }

    /**
     * Returns a function that creates an analyzed {@link EvaluateResult} from a file path.
     *
     * @return the builder
     */
    public CommandBuilder<EvaluateResult> evalAOpen() {
        return function(functionFactory.createOpenInputFunction(project, lang)
                .kleisliCompose(functionFactory.createParseFunction(project, lang))
                .kleisliCompose(functionFactory.createAnalyzeFunction(project, lang))
                .kleisliCompose(functionFactory.createEvaluateFunction(project, lang)));
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
        // Must be an anonymous inner class instead of a lambda, due to a bug in the eclipse
        // compiler. See: https://bugs.eclipse.org/bugs/show_bug.cgi?id=436542
        return varFunction(new FailableFunction<String[], OtherR, IResult>() {
            @Override
            public FailOrSuccessResult<OtherR, IResult> apply(String[] input) {
                return func.apply(input[0]);
            }
        });
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
            public IResult execute(String... args) {
                return function.apply(args);
            }

            @Override
            public String description() {
                return description;
            }
        };
    }

}
