package org.metaborg.spoofax.shell.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.meta.lang.dynsem.interpreter.nodes.rules.RuleResult;
import org.metaborg.meta.lang.dynsem.interpreter.terms.ITerm;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.shell.core.IInterpreterLoader.InterpreterLoadException;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.StrategoString;
import org.spoofax.terms.TermFactory;

import com.google.inject.Inject;
import com.oracle.truffle.api.vm.PolyglotEngine;
import com.oracle.truffle.api.vm.PolyglotEngine.Value;

/**
 * An {@link IEvaluationStrategy} for DynSem-based languages.
 */
public class DynSemEvaluationStrategy implements IEvaluationStrategy {
    private final IInterpreterLoader interpLoader;
    private final ITermFactoryService termFactService;

    private PolyglotEngine polyglotEngine;
    private Object[] rwSemanticComponents;

    /**
     * Construct a new {@link DynSemEvaluationStrategy}. This does not yet load the interpreter for
     * the language. Rather, this is done when invoking {@link #evaluate(IStrategoTerm, IContext)}
     * for the first time.
     *
     * @param interpLoader
     *            The loader for a generated DynSem interpreter.
     * @param termFactService
     *            The {@link ITermFactoryService} for retrieving an {@link ITermFactory}.
     */
    @Inject
    public DynSemEvaluationStrategy(IInterpreterLoader interpLoader,
                                    ITermFactoryService termFactService) {
        this.interpLoader = interpLoader;
        this.termFactService = termFactService;
    }

    @Override
    public String name() {
        return "dynsem";
    }

    @Override
    public IStrategoTerm evaluate(IStrategoTerm term, IContext context) throws MetaborgException {
        return evaluate(term, context.language());
    }

    private IStrategoTerm evaluate(IStrategoTerm input, ILanguageImpl langImpl)
        throws MetaborgException {
        if (uninitialized()) {
            initialize(langImpl);
        }

        IStrategoTerm desugared = desugar(input);

        IStrategoAppl appl = toAppl(desugared);

        ITerm programTerm = interpLoader.getProgramTerm(appl);

        Value rule = lookupRuleForInput(appl);

        return invoke(rule, programTerm);
    }

    private IStrategoTerm desugar(IStrategoTerm input) {
        IStrategoTerm desugared = interpLoader.getTransformer().transform(input);
        return desugared;
    }

    private IStrategoAppl toAppl(IStrategoTerm input) throws MetaborgException {
        if (!Tools.isTermAppl(input)) {
            throw new MetaborgException("Expected a StrategoAppl, but a "
                                        + input.getClass().getSimpleName() + " was found: \""
                                        + input.toString(1) + "\".");
        }
        return (IStrategoAppl) input;
    }

    private Value lookupRuleForInput(IStrategoAppl appl) throws MetaborgException {
        return lookupRuleForInput("shell", appl);
    }

    private Value lookupRuleForInput(String ruleName, IStrategoAppl appl)
        throws MetaborgException {
        // Look up "-shell->" rule. This automatically dispatches to sort rules if there is no
        // constructor rule to be found for this term.
        Value rule = lookupRule(ruleName, appl);

        if (rule == null) {
            throw new MetaborgException("No shell rule found to be applied to term \""
                                        + appl.toString(1) + "\".");
        }
        return rule;
    }

    private @Nullable Value lookupRule(String ruleName, IStrategoAppl appl) {
        IStrategoConstructor inputCtor = appl.getConstructor();
        String ctorName = inputCtor.getName();
        int arity = inputCtor.getArity();

        return polyglotEngine.findGlobalSymbol(ruleName + "/" + ctorName + "/" + arity);
    }

    private IStrategoTerm invoke(Value rule, ITerm programTerm) throws MetaborgException {
        try {
            // Add the arguments.
            List<Object> arguments = new ArrayList<>(1 + rwSemanticComponents.length);
            arguments.add(programTerm);
            arguments.addAll(Arrays.asList(rwSemanticComponents));

            // Execute the rule with the arguments, and update the execution environment.
            RuleResult ruleResult = rule.execute(arguments.toArray()).as(RuleResult.class);
            rwSemanticComponents = ruleResult.components;

            // Return the result as IStrategoTerm.
            return new StrategoString(ruleResult.result.toString(), TermFactory.EMPTY_LIST,
                                      IStrategoTerm.IMMUTABLE);
        } catch (IOException e) {
            throw new MetaborgException("Input/output error while evaluating.", e);
        }
    }

    private boolean uninitialized() {
        return polyglotEngine == null || rwSemanticComponents == null;
    }

    private void initialize(ILanguageImpl langImpl) throws MetaborgException {
        polyglotEngine = interpLoader.loadInterpreterForLanguage(langImpl);

        initializeExecutionEnvironment();
    }

    private void initializeExecutionEnvironment() throws MetaborgException {
        ITermFactory termFactory = termFactService.getGeneric();
        IStrategoConstructor termConstr = termFactory.makeConstructor("ShellInit", 0);
        IStrategoAppl shellInitAppl = termFactory.makeAppl(termConstr);
        try {
            Value shellInitRule = lookupRuleForInput("init", shellInitAppl);
            RuleResult ruleResult = shellInitRule
                .execute(interpLoader.getProgramTerm(shellInitAppl)).as(RuleResult.class);
            rwSemanticComponents = ruleResult.components;
        } catch (IOException e) {
            throw new InterpreterLoadException(e);
        } catch (MetaborgException e) {
            throw new InterpreterLoadException("No shell initialization rule found.\n"
                                               + "Initialize the semantic components for the"
                                               + " \"shell\" rules with a rule of the form "
                                               + "\"ShellInit() -init-> ShellInit() :: <RW>*\".",
                                               e);
        }
    }
}
