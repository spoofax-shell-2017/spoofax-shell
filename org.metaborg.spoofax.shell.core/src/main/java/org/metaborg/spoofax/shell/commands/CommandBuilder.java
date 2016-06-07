package org.metaborg.spoofax.shell.commands;

import java.awt.Color;
import java.io.IOException;
import java.util.Objects;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.action.ITransformAction;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.core.shell.ShellFacet;
import org.metaborg.spoofax.core.syntax.JSGLRParserConfiguration;
import org.metaborg.spoofax.shell.client.IHook;
import org.metaborg.spoofax.shell.functions.IFunctionFactory;
import org.metaborg.spoofax.shell.output.AnalyzeResult;
import org.metaborg.spoofax.shell.output.IResultFactory;
import org.metaborg.spoofax.shell.output.ISpoofaxResult;
import org.metaborg.spoofax.shell.output.InputResult;
import org.metaborg.spoofax.shell.output.ParseResult;
import org.metaborg.spoofax.shell.output.StyledText;
import org.metaborg.spoofax.shell.output.TransformResult;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

/**
 * Builds commands by composing several smaller functional interfaces.
 * @param <R> the return type of the created command
 */
public class CommandBuilder<R extends ISpoofaxResult<?>> {
    private final IResultFactory resultFactory;
    private final IFunctionFactory functionFactory;
    private final ILanguageImpl lang;
    private final IProject project;

    private final String description;
    private final Throwing<String, R> function;

    /**
     * Reimplements java's Function class with exceptions.
     * @param <A>
     * @param <B>
     */
    private interface Throwing<A, B> {
        default <C> Throwing<A, C> andThen(Throwing<? super B, ? extends C> after) {
            Objects.requireNonNull(after);
            return (A a) -> after.apply(apply(a));
        };

        B apply(A a) throws MetaborgException, IOException;
    }

    /**
     * Constructs a new {@link CommandBuilder} from the given parameters.
     * @param resultFactory    the {@link IResultFactory}
     * @param functionFactory  the {@link IFunctionFactory}
     * @param project          the {@link IProject} associated with all created commands
     * @param lang             the {@link ILanguageImpl} associated with all created commands
     */
    @AssistedInject
    public CommandBuilder(IResultFactory resultFactory, IFunctionFactory functionFactory,
                          @Assisted IProject project, @Assisted ILanguageImpl lang) {
        this.resultFactory = resultFactory;
        this.functionFactory = functionFactory;
        this.project = project;
        this.lang = lang;
        this.description = null;
        this.function = null;
    }

    /**
    * Constructs a new {@link CommandBuilder} from a parent.
    * @param parent       the parent {@link CommandBuilder}
    * @param description  the description of the created command
    * @param function     the function the created command will execute
    */
    private CommandBuilder(CommandBuilder<?> parent, String description,
                           Throwing<String, R> function) {
        this.resultFactory = parent.resultFactory;
        this.functionFactory = parent.functionFactory;
        this.project = parent.project;
        this.lang = parent.lang;
        this.description = description;
        this.function = function;
    }

    private Throwing<String, InputResult> inputFunction(ILanguageImpl lang) {
        return (source) -> {
            try {
                ShellFacet shellFacet = lang.facet(ShellFacet.class);
                FileObject file = project.location().resolveFile("temp");
                // FIXME: find a way to fall back to no start symbols
                return resultFactory.createInputResult(lang, file, source,
                    new JSGLRParserConfiguration(shellFacet.getShellStartSymbol()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private Throwing<InputResult, ParseResult> parseFunction() {
        return (InputResult input) -> {
            return functionFactory.createParseFunction(project, lang).apply(input);
        };
    }

    private Throwing<ParseResult, AnalyzeResult> analyzeFunction() {
        return (ParseResult parse) -> {
            return functionFactory.createAnalyzeFunction(project, lang).apply(parse);
        };
    }

    private Throwing<ParseResult, TransformResult> pTransformFunction(ITransformAction action) {
        return (ParseResult parse) -> {
            return functionFactory.createPTransformFunction(project, lang, action).apply(parse);
        };
    }

    private Throwing<AnalyzeResult, TransformResult> aTransformFunction(ITransformAction action) {
        return (AnalyzeResult analyze) -> {
            return functionFactory.createATransformFunction(project, lang, action).apply(analyze);
        };
    }

    /**
     * Returns a function that creates an {@link InputResult} from a String.
     * @return the builder
     */
    public CommandBuilder<InputResult> input() {
        return new CommandBuilder<>(this, description, inputFunction(lang));
    }

    /**
     * Returns a function that creates a {@link ParseResult} from a String.
     * @return the builder
     */
    public CommandBuilder<ParseResult> parse() {
        return new CommandBuilder<>(this, description, inputFunction(lang)
                .andThen(parseFunction()));
    }

    /**
     * Returns a function that creates a {@link AnalyzeResult} from a String.
     * @return the builder
     */
    public CommandBuilder<AnalyzeResult> analyze() {
        return new CommandBuilder<>(this, description, inputFunction(lang)
                .andThen(parseFunction())
                .andThen(analyzeFunction()));
    }

    /**
     * Returns a function that creates a parsed {@link TransformResult} from a String.
     * @param action  the associated {@link ITransformAction}
     * @return the builder
     */
    public CommandBuilder<TransformResult> transformParsed(ITransformAction action) {
        return new CommandBuilder<>(this, description, inputFunction(lang)
                .andThen(parseFunction())
                .andThen(pTransformFunction(action)));
    }

    /**
     * Returns a function that creates an analyzed {@link TransformResult} from a String.
     * @param action  the associated {@link ITransformAction}
     * @return the builder
     */
    public CommandBuilder<TransformResult> transformAnalyzed(ITransformAction action) {
        return new CommandBuilder<>(this, description, inputFunction(lang)
                .andThen(parseFunction())
                .andThen(analyzeFunction())
                .andThen(aTransformFunction(action)));
    }

    /**
     * Sets the description of the created command.
     * @param description  the description of the command
     * @return the builder
     */
    public CommandBuilder<R> description(String description) {
        return new CommandBuilder<>(this, description, function);
    }

    /**
     * Returns an {@link IReplCommand} given a function.
     * @return an {@link IReplCommand}
     */
    public IReplCommand build() {
        return new IReplCommand() {
            @Override
            public IHook execute(String... arg) throws MetaborgException {
                return (display) -> {
                    try {
                        display.displayResult(function.apply(arg[0]));
                    } catch (IOException | MetaborgException e) {
                        display.displayMessage(new StyledText(Color.RED, e.getMessage()));
                    }
                };
            }

            @Override
            public String description() {
                return description;
            }
        };
    }

}
