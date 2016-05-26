package org.metaborg.spoofax.shell.core;

import static org.apache.commons.lang3.reflect.MethodUtils.invokeMethod;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;

import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.meta.lang.dynsem.interpreter.DynSemLanguage;
import org.metaborg.meta.lang.dynsem.interpreter.nodes.rules.RuleResult;
import org.metaborg.spoofax.shell.core.IInterpreterLoader.InterpreterLoadException;
import org.metaborg.spoofax.shell.output.AnalyzeResult;
import org.metaborg.spoofax.shell.output.ParseResult;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.StrategoString;
import org.spoofax.terms.TermFactory;

import com.google.inject.Inject;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.vm.PolyglotEngine;
import com.oracle.truffle.api.vm.PolyglotEngine.Value;

/**
 * An {@link IEvaluationStrategy} for DynSem-based languages.
 */
public class DynSemEvaluationStrategy implements IEvaluationStrategy {
    @Inject
    private IInterpreterLoader interpLoader;

    private PolyglotEngine polyglotEngine;
    private DynSemLanguage language;

    @Override
    public String name() {
        return "dynsem";
    }

    @Override
    public IStrategoTerm evaluate(ParseResult parsed, IContext context) {
        return evaluate(parsed.unit().input().text(), parsed.ast().get(), context.language());
    }

    @Override
    public IStrategoTerm evaluate(AnalyzeResult analyzed, IContext context) {
        return evaluate(analyzed.unit().input().input().text(), analyzed.ast().get(),
                        context.language());
    }

    @SuppressWarnings("deprecation")
    private IStrategoTerm evaluate(String origSource, IStrategoTerm input, ILanguageImpl langImpl) {
        if (uninitialized()) {
            initialize(langImpl);
        }
        try {
            invokeMethod(language, "setParsedAST", input);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException
                 | IllegalArgumentException | InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            Source source = Source.fromNamedText(origSource, String.valueOf(input.hashCode()))
                .withMimeType("application/x-simpl");
            polyglotEngine.eval(source);
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
            return new StrategoString(callable.call().result.toString(), TermFactory.EMPTY_LIST,
                                      IStrategoTerm.IMMUTABLE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean uninitialized() {
        return language == null && polyglotEngine == null;
    }

    private void initialize(ILanguageImpl langImpl) {
        try {
            language = interpLoader.loadInterpreterForLanguage(langImpl);
        } catch (InterpreterLoadException e) {
            // TODO: Show message to user.
            e.getMessage();
        }
        polyglotEngine = PolyglotEngine.newBuilder().build();
    }
}
