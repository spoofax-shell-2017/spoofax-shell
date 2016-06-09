package org.metaborg.spoofax.shell.commands;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.action.ITransformAction;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.shell.client.IResult;
import org.metaborg.spoofax.shell.functions.FailableFunction;
import org.metaborg.spoofax.shell.functions.IFunctionFactory;
import org.metaborg.spoofax.shell.functions.InputFunction;
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
    private final FailableFunction<String, R, IResult> function;

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
        this.functionFactory = functionFactory;
        this.project = project;
        this.lang = lang;
        this.description = null;
        this.function = null;
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
                           FailableFunction<String, R, IResult> function) {
        this.functionFactory = parent.functionFactory;
        this.project = parent.project;
        this.lang = parent.lang;
        this.description = description;
        this.function = function;
    }

    private InputFunction inputFunction() {
        return functionFactory.createInputFunction(project, lang);
    }

    private FailableFunction<String, ParseResult, IResult> parseFunction() {
        return inputFunction()
            .kleisliCompose(functionFactory.createParseFunction(project, lang));
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
        return new CommandBuilder<>(this, description, inputFunction());
    }

    /**
     * Returns a function that creates a {@link ParseResult} from a String.
     *
     * @return the builder
     */
    public CommandBuilder<ParseResult> parse() {
        return new CommandBuilder<ParseResult>(this, description, parseFunction());
    }

    /**
     * Returns a function that creates a {@link AnalyzeResult} from a String.
     *
     * @return the builder
     */
    public CommandBuilder<AnalyzeResult> analyze() {
        return new CommandBuilder<>(this, description, analyzeFunction());
    }

    /**
     * Returns a function that creates a parsed {@link TransformResult} from a String.
     *
     * @param action
     *            the associated {@link ITransformAction}
     * @return the builder
     */
    public CommandBuilder<TransformResult> transformParsed(ITransformAction action) {
        return new CommandBuilder<>(this, description, pTransformFunction(action));
    }

    /**
     * Returns a function that creates an analyzed {@link TransformResult} from a String.
     *
     * @param action
     *            the associated {@link ITransformAction}
     * @return the builder
     */
    public CommandBuilder<TransformResult> transformAnalyzed(ITransformAction action) {
        return new CommandBuilder<>(this, description, aTransformFunction(action));
    }

    /**
     * Returns a function that creates a parsed {@link EvaluateResult} from a String.
     *
     * @return the builder
     */
    public CommandBuilder<EvaluateResult> evalParsed() {
        return new CommandBuilder<>(this, description, pEvaluateFunction());
    }

    /**
     * Returns a function that creates an analyzed {@link EvaluateResult} from a String.
     *
     * @return the builder
     */
    public CommandBuilder<EvaluateResult> evalAnalyzed() {
        return new CommandBuilder<>(this, description, aEvaluateFunction());
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
            public IResult execute(String... arg) throws MetaborgException {
                return function.apply(arg[0]);
            }

            @Override
            public String description() {
                return description;
            }
        };
    }

}
