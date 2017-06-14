package org.metaborg.spoofax.shell.client.console.strategies;

import java.util.concurrent.Callable;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.meta.lang.dynsem.interpreter.DynSemVM;
import org.metaborg.meta.lang.dynsem.interpreter.nodes.rules.RuleResult;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.shell.core.IEvaluationStrategy;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import com.google.inject.Inject;

/**
 * An {@link IEvaluationStrategy} for DynSem-based languages.
 */
public class DynSemEvaluationStrategy implements IEvaluationStrategy {
    private final IInterpreterLoader interpLoader;
    private final ITermFactory termFactory;
    private final IStrategoAppl initAppl;

    private DynSemVM vm;
    private Object[] rwSemanticComponents;
    private ILanguageImpl langImpl;

    /**
     * Construct a new {@link DynSemEvaluationStrategy}. This does not yet load the interpreter for the language.
     * Rather, this is done when invoking {@link #evaluate(IStrategoTerm, IContext)} for the first time.
     *
     * @param interpLoader
     *            The loader for a generated DynSem interpreter.
     * @param termFactService
     *            The {@link ITermFactoryService} for retrieving an {@link ITermFactory}.
     */
    @Inject public DynSemEvaluationStrategy(IInterpreterLoader interpLoader, ITermFactoryService termFactoryService) {
        this.interpLoader = interpLoader;
        this.termFactory = termFactoryService.getGeneric();
        this.initAppl = termFactory.makeAppl(termFactory.makeConstructor("ShellInit", 0));
    }

    @Override public String name() {
        return "dynsem";
    }

    @Override public IStrategoTerm evaluate(IStrategoTerm term, IContext context) throws MetaborgException {
        ensureVMAndInit(context.language());
        Callable<RuleResult> rule = vm.getRuleCallable("shell", toAppl(term), rwSemanticComponents);
        try {
            RuleResult result = rule.call();
            rwSemanticComponents = result.components;
            return termFactory.makeString(result.result.toString());
        } catch(Exception e) {
            throw new MetaborgException(e);
        }
    }

    private IStrategoAppl toAppl(IStrategoTerm term) throws MetaborgException {
        if(!Tools.isTermAppl(term)) {
            throw new MetaborgException("Expected a StrategoAppl, but a " + term.getClass().getSimpleName()
                    + " was found: \"" + term.toString(1) + "\".");
        }
        return (IStrategoAppl) term;
    }

    private void ensureVMAndInit(ILanguageImpl langImpl) throws MetaborgException {
        if(vm == null || !langImpl.equals(this.langImpl)) {
            rwSemanticComponents = null;
            vm = interpLoader.createInterpreterForLanguage(langImpl);
            this.langImpl = langImpl;
        }
        if(rwSemanticComponents == null) {
            try {
                Callable<RuleResult> initRule = vm.getRuleCallable("init", initAppl, new Object[0]);
                RuleResult ruleResult = initRule.call();
                rwSemanticComponents = ruleResult.components;
            } catch(Exception e) {
                throw new MetaborgException("No shell initialization rule found.\n"
                        + "Initialize the semantic components for the" + " \"shell\" rules with a rule of the form "
                        + "\"ShellInit() -init-> ShellInit() :: <RW>*\".", e);
            }
        }
    }

}