package org.metaborg.spoofax.shell.core;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.meta.lang.dynsem.interpreter.IDynSemLanguageParser;
import org.metaborg.meta.lang.dynsem.interpreter.nodes.rules.RuleResult;
import org.metaborg.spoofax.shell.core.IInterpreterLoader.InterpreterLoadException;
import org.metaborg.spoofax.shell.output.AnalyzeResult;
import org.metaborg.spoofax.shell.output.ParseResult;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.StrategoString;
import org.spoofax.terms.TermFactory;

import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.vm.PolyglotEngine;
import com.oracle.truffle.api.vm.PolyglotEngine.Value;

/**
 * An {@link IEvaluationStrategy} for DynSem-based languages.
 */
public class DynSemEvaluationStrategy implements IEvaluationStrategy {
    private IInterpreterLoader interpLoader;
    private PolyglotEngine polyglotEngine;
    private NonParser nonParser;

    /**
     * Construct a new {@link DynSemEvaluationStrategy}. This does not yet load the interpreter for
     * the language. Rather, this is done when first invoking
     * {@link #evaluate(AnalyzeResult, IContext)} or {@link #evaluate(ParseResult, IContext)}.
     */
    public DynSemEvaluationStrategy() {
        nonParser = new NonParser();
        interpLoader = new JarInterpreterLoader(nonParser);
    }

    @Override
    public String name() {
        return "dynsem";
    }

    @Override
    public IStrategoTerm evaluate(ParseResult parsed, IContext context) throws MetaborgException {
        return evaluate(parsed.unit().input().text(), parsed.ast().get(), context.language());
    }

    @Override
    public IStrategoTerm evaluate(AnalyzeResult analyzed, IContext context)
        throws MetaborgException {
        return evaluate(analyzed.unit().input().input().text(), analyzed.ast().get(),
                        context.language());
    }

    private IStrategoTerm evaluate(String origSource, IStrategoTerm input, ILanguageImpl langImpl)
        throws MetaborgException {
        if (uninitialized()) {
            initialize(langImpl);
        }
        nonParser.setCurrentTerm(input);
        Value eval = null;
        try {
            Source source = Source.fromNamedText(origSource, Integer.toHexString(input.hashCode()))
                .withMimeType("application/x-simpl");
            eval = polyglotEngine.eval(source);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Value prog = polyglotEngine.findGlobalSymbol("INIT");
        try {
            Callable<RuleResult> callable = new Callable<RuleResult>() {
                @Override
                public RuleResult call() throws Exception {
                    return prog.execute().as(RuleResult.class);
                }
            };
            RuleResult ruleResult = callable.call();
            return new StrategoString(ruleResult.result.toString(), TermFactory.EMPTY_LIST,
                                      IStrategoTerm.IMMUTABLE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean uninitialized() {
        return polyglotEngine == null;
    }

    private void initialize(ILanguageImpl langImpl) throws InterpreterLoadException {
        polyglotEngine = interpLoader.loadInterpreterForLanguage(langImpl);
    }

    /**
     * An {@link IDynSemLanguageParser} which returns just the {@link IStrategoTerm} which is set by
     * the evaluation strategy. Since Truffle only supports programs parsed directly from source
     * code, we need this workaround to avoid having to parse source code to {@link IStrategoTerm
     * ASTs} again. Hence this class is called a {@link NonParser "non parser"}.
     */
    static class NonParser implements IDynSemLanguageParser {
        private IStrategoTerm currentTerm;

        /**
         * @return the current term.
         */
        public IStrategoTerm getCurrentTerm() {
            return currentTerm;
        }

        /**
         * Sets the term to be returned the next time the parser is called through
         * {@link PolyglotEngine#eval(Source)}.
         *
         * @param currentTerm
         *            the current term to set.
         */
        public void setCurrentTerm(IStrategoTerm currentTerm) {
            this.currentTerm = currentTerm;
        }

        @Override
        public IStrategoTerm parse(Source src) {
            return getCurrentTerm();
        }
    }
}
