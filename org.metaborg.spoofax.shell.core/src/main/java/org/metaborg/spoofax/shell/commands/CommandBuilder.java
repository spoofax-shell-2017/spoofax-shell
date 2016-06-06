package org.metaborg.spoofax.shell.commands;

import java.io.IOException;
import java.util.function.Function;

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
import org.metaborg.spoofax.shell.output.TransformResult;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

/**
 * Builds commands by composing several smaller functional interfaces.
 */
public class CommandBuilder {
    private IResultFactory resultFactory;
    private IFunctionFactory functionFactory;
    private ILanguageImpl lang;
    private IProject project;

    /**
     * @param <A>
     * @param <R>
     */
    private interface FunctionThrows<A, R> {
        R apply(A t) throws MetaborgException, IOException;
    }

    /**
     *
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
    }

    private <A, R> Function<A, R> wrap(FunctionThrows<A, R> function) {
        return (A input) -> {
            try {
                return function.apply(input);
            } catch (MetaborgException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private Function<InputResult, ParseResult> parseFunction() {
        return wrap((InputResult input) -> {
            return functionFactory.createParseFunction(project, lang).execute(input);
        });
    }

    private Function<ParseResult, AnalyzeResult> analyzeFunction() {
        return wrap((ParseResult parse) -> {
            return functionFactory.createAnalyzeFunction(project, lang).execute(parse);
        });
    }

    private Function<ParseResult, TransformResult> pTransformFunction(ITransformAction action) {
        return wrap((ParseResult parse) -> {
            return functionFactory.createPTransformFunction(project, lang, action).execute(parse);
        });
    }

    private Function<AnalyzeResult, TransformResult> aTransformFunction(ITransformAction action) {
        return wrap((AnalyzeResult analyze) -> {
            return functionFactory.createATransformFunction(project, lang, action).execute(analyze);
        });
    }

    /**
     * Returns a function that creates an {@link InputResult} from a String.
     * @return a {@link Function}
     */
    public Function<String, InputResult> input() {
        return wrap((String source) -> {
            ShellFacet shellFacet = lang.facet(ShellFacet.class);
            FileObject file = project.location().resolveFile("temp");
            // FIXME: find a way to fall back to no start symbols
            return resultFactory.createInputResult(lang, file, source,
                new JSGLRParserConfiguration(shellFacet.getShellStartSymbol()));
        });
    }

    /**
     * Returns a function that creates a {@link ParseResult} from a String.
     * @return a {@link Function}
     */
    public Function<String, ParseResult> parse() {
        return input().andThen(parseFunction());
    }

    /**
     * Returns a function that creates a {@link AnalyzeResult} from a String.
     * @return a {@link Function}
     */
    public Function<String, AnalyzeResult> analyze() {
        return parse().andThen(analyzeFunction());
    }

    /**
     * Returns a function that creates a parsed {@link TransformResult} from a String.
     * @param action  the associated {@link ITransformAction}
     * @return a {@link Function}
     */
    public Function<String, TransformResult> transformParsed(ITransformAction action) {
        return parse().andThen(pTransformFunction(action));
    }

    /**
     * Returns a function that creates an analyzed {@link TransformResult} from a String.
     * @param action  the associated {@link ITransformAction}
     * @return a {@link Function}
     */
    public Function<String, TransformResult> transformAnalyzed(ITransformAction action) {
        return analyze().andThen(aTransformFunction(action));
    }

    /**
     * Returns an {@link IReplCommand} given a function.
     * @param function  the {@link Function}
     * @param desc      the description of the command
     * @return an {@link IReplCommand}
     */
    public IReplCommand build(Function<String, ? extends ISpoofaxResult<?>> function, String desc) {
        return new IReplCommand() {
            @Override
            public IHook execute(String... arg) throws MetaborgException {
                return (display) -> display.displayResult(function.apply(arg[0]));
            }

            @Override
            public String description() {
                return desc;
            }
        };
    }

}
