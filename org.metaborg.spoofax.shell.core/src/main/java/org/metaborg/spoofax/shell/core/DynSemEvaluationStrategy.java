package org.metaborg.spoofax.shell.core;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.meta.lang.dynsem.interpreter.nodes.rules.RuleRegistry;
import org.metaborg.meta.lang.dynsem.interpreter.nodes.rules.RuleResult;
import org.metaborg.meta.lang.dynsem.interpreter.terms.ITerm;
import org.metaborg.spoofax.core.shell.ShellFacet;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.shell.core.IInterpreterLoader.InterpreterLoadException;
import org.metaborg.spoofax.shell.output.AnalyzeResult;
import org.metaborg.spoofax.shell.output.ParseResult;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
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
    private PolyglotEngine polyglotEngine;
    private String shellStartSymbol;
    private Object executionEnvironment;
    private Object[] rwSemanticComponents;

    @Inject
    private ITermFactoryService termFactService;

    /**
     * Construct a new {@link DynSemEvaluationStrategy}. This does not yet load the interpreter for
     * the language. Rather, this is done when first invoking
     * {@link #evaluate(AnalyzeResult, IContext)} or {@link #evaluate(ParseResult, IContext)}.
     */
    public DynSemEvaluationStrategy() {
        interpLoader = new ClassPathInterpreterLoader();
    }

    @Override
    public String name() {
        return "dynsem";
    }

    @Override
    public IStrategoTerm evaluate(ParseResult parsed, IContext context) throws MetaborgException {
        return evaluate(parsed.ast().get(), context.language());
    }

    @Override
    public IStrategoTerm evaluate(AnalyzeResult analyzed, IContext context)
        throws MetaborgException {
        return evaluate(analyzed.ast().get(), context.language());
    }

    private IStrategoTerm evaluate(IStrategoTerm input, ILanguageImpl langImpl)
        throws MetaborgException {
        if (uninitialized()) {
            initialize(langImpl);
        }

        ITerm programTerm = toProgramTerm(input);

        Value rule = lookupRuleForInput(input);

        return invoke(rule, programTerm);
    }

    private ITerm toProgramTerm(IStrategoTerm input) throws MetaborgException {
        ITerm programTerm = null;
        try {
            programTerm = getProgramTerm(input);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException
                 | InvocationTargetException cause) {
            throw new MetaborgException("Error constructing program term from input.", cause);
        }
        return programTerm;
    }

    private Value lookupRuleForInput(IStrategoTerm input) throws MetaborgException {
        if (!Tools.isTermAppl(input)) {
            throw new MetaborgException("Expected a StrategoAppl, but a \""
                                        + input.getClass().getName() + "\" was found: "
                                        + input.toString(1));
        }

        IStrategoConstructor inputCtor = ((IStrategoAppl) input).getConstructor();
        String ctorName = inputCtor.getName();
        int arity = inputCtor.getArity();

        // Look up "-shell->" rule.
        Value rule =
            polyglotEngine.findGlobalSymbol(RuleRegistry.makeKey("shell", ctorName, arity));
        return rule;
    }

    private IStrategoTerm invoke(Value rule, ITerm programTerm) throws MetaborgException {
        try {
            // Add the arguments.
            List<Object> arguments = new ArrayList<Object>(2 + rwSemanticComponents.length);
            arguments.add(programTerm);
            arguments.add(executionEnvironment);
            arguments.addAll(Arrays.asList(rwSemanticComponents));

            // Execute the rule with the arguments, and update the execution environment.
            RuleResult ruleResult = rule.execute(arguments.toArray()).as(RuleResult.class);
            Object[] components = ruleResult.components;
            executionEnvironment = components[0];
            rwSemanticComponents = ArrayUtils.subarray(components, 1, components.length);

            // Return the result as IStrategoTerm.
            return new StrategoString(ruleResult.result.toString(), TermFactory.EMPTY_LIST,
                                      IStrategoTerm.IMMUTABLE);
        } catch (IOException e) {
            throw new MetaborgException("Input/output error while evaluating.", e);
        }
    }

    @SuppressWarnings("unchecked")
    private ITerm getProgramTerm(IStrategoTerm input) throws ClassNotFoundException,
        NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        String termSort = getSortForTerm(input);
        // Get the abstract class for the sort of the term.
        Class<? extends ITerm> generatedTermClass = (Class<? extends ITerm>) ClassUtils
            .getClass(interpLoader.getTargetPackage() + ".terms.I" + termSort + "Term");
        return (ITerm) MethodUtils.invokeStaticMethod(generatedTermClass, "create", input);
    }

    private String getSortForTerm(IStrategoTerm input) {
        ImploderAttachment termAttachment = input.getAttachment(ImploderAttachment.TYPE);
        if (termAttachment == null) {
            return null;
        }
        return termAttachment.getElementSort();
    }

    private boolean uninitialized() {
        return polyglotEngine == null || shellStartSymbol == null;
    }

    private void initialize(ILanguageImpl langImpl) throws InterpreterLoadException {
        polyglotEngine = interpLoader.loadInterpreterForLanguage(langImpl);

        /** FIXME: {@link ShellFacet} might be null due to lang designer, what to do then? */
        ShellFacet shellFacet = langImpl.facet(ShellFacet.class);
        shellStartSymbol = shellFacet.getShellStartSymbol();

        initializeExecutionEnvironment(langImpl);
    }

    private void initializeExecutionEnvironment(ILanguageImpl langImpl)
        throws InterpreterLoadException {
        ITermFactory termFactory = termFactService.getGeneric();
        IStrategoConstructor termConstr = termFactory.makeConstructor("ShellInit", 0);
        IStrategoAppl shellInitAppl = termFactory.makeAppl(termConstr);
        ImploderAttachment.putImploderAttachment(shellInitAppl, false, "ShellInit", null, null);
        Value shellInitRule = polyglotEngine.findGlobalSymbol(RuleRegistry
            .makeKey("init", termConstr.getName(), termConstr.getArity()));
        try {
            RuleResult ruleResult =
                shellInitRule.execute(getProgramTerm(shellInitAppl)).as(RuleResult.class);
            executionEnvironment = ruleResult.result;
            rwSemanticComponents = ruleResult.components;
        } catch (IOException | ClassNotFoundException | NoSuchMethodException
                 | IllegalAccessException | InvocationTargetException e) {
            throw new InterpreterLoadException(e);
        }
    }
}
