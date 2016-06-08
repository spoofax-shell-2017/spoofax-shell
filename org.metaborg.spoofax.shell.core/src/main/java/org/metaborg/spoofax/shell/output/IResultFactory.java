package org.metaborg.spoofax.shell.output;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.syntax.IInputUnit;
import org.metaborg.spoofax.core.syntax.JSGLRParserConfiguration;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxTransformUnit;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.name.Named;

/**
 * Factory that creates {@link ISpoofaxResult}.
 */
public interface IResultFactory {

    /**
     * Create an {@link InputResult} that can be passed to the Repl.
     * @param unit  the wrapped {@link IInputUnit}
     * @return a {@link InputResult}
     */
    InputResult createInputResult(ISpoofaxInputUnit unit);

    /**
     * Create an {@link InputResult} that can be passed to the Repl.
     * @param lang    the language of this {@link InputResult}
     * @param file    the {@link FileObject} containing the source
     * @param source  the source
     * @return a {@link InputResult}
     */
    InputResult createInputResult(ILanguageImpl lang, FileObject file, String source);

    /**
     * Create an {@link InputResult} that can be passed to the Repl.
     * @param lang         the language of this {@link InputResult}
     * @param file         the {@link FileObject} containing the source
     * @param source       the source
     * @param parserConfig the parser configuration
     * @return a {@link InputResult}
     */
    InputResult createInputResult(ILanguageImpl lang, FileObject file, String source,
                                  JSGLRParserConfiguration parserConfig);

    /**
     * Create a {@link ParseResult} that can be passed to the Repl.
     * @param unit  the wrapped {@link ISpoofaxParseUnit}
     * @return a {@link ParseResult}
     */
    ParseResult createParseResult(ISpoofaxParseUnit unit);

    /**
     * Create an {@link AnalyzeResult} that can be passed to the Repl.
     * @param unit  the wrapped {@link ISpoofaxAnalyzeUnit}
     * @return an {@link AnalyzeResult}
     */
    AnalyzeResult createAnalyzeResult(ISpoofaxAnalyzeUnit unit);

    /**
     * Create a {@link TransformResult} that can be passed to the Repl.
     * @param unit  the wrapped {@link ISpoofaxTransformUnit}
     * @return a {@link TransformResult}
     */
    TransformResult createTransformResult(ISpoofaxTransformUnit<?> unit);

    /**
     * Create an {@link EvaluateResult} that can be passed to the Repl.
     * @param parsed the wrapped {@link ParseResult}
     * @param result the result of the evaluation.
     * @return a {@link EvaluateResult}
     */
    @Named("parsed")
    EvaluateResult createEvaluateResult(ParseResult parsed, IStrategoTerm result);

    /**
     * Create an {@link EvaluateResult} that can be passed to the Repl.
     * @param analyzed the wrapped {@link AnalyzeResult}
     * @param result the result of the evaluation.
     * @return a {@link EvaluateResult}
     */
    @Named("analyzed")
    EvaluateResult createEvaluateResult(AnalyzeResult analyzed, IStrategoTerm result);

}
