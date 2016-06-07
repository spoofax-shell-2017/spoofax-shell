package org.metaborg.spoofax.shell.core;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.anyString;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.meta.lang.dynsem.interpreter.nodes.matching.ITermInstanceChecker;
import org.metaborg.meta.lang.dynsem.interpreter.nodes.rules.RuleResult;
import org.metaborg.meta.lang.dynsem.interpreter.terms.ITerm;
import org.metaborg.meta.lang.dynsem.interpreter.terms.ITermTransformer;
import org.metaborg.spoofax.core.SpoofaxModule;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.shell.output.AnalyzeResult;
import org.metaborg.spoofax.shell.util.StrategoUtil;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.oracle.truffle.api.vm.PolyglotEngine;
import com.oracle.truffle.api.vm.PolyglotEngine.Value;

/**
 * Tests the {@link DynSemEvaluationStrategy}.
 */
@RunWith(Parameterized.class)
public class DynSemEvaluationStrategyTest {
    private final DynSemEvaluationStrategy evalStrategy;
    private IStrategoTerm input;
    private static final int DEFAULT_SHELL_RULE_ANSWER = 42;
    private static final String NO_EXCEPTION = "No exception should be thrown";
    private final String expectedExceptionMessage;

    private static final Injector INJECTOR = Guice.createInjector(new SpoofaxModule());

    /**
     * Set up the evaluation strategy with the {@link MockInterpreterLoader}.
     *
     * @param ruleKey
     *            The key of the shell rule, to cover the branch in the private rule lookup method.
     * @param input
     *            The {@link IStrategoTerm} test input.
     * @param expectedExceptionMessage
     *            The exception message that is expected when an exception has been thrown.
     */
    public DynSemEvaluationStrategyTest(String ruleKey, IStrategoTerm input,
                                        String expectedExceptionMessage) {
        this.input = input;
        this.expectedExceptionMessage = expectedExceptionMessage;
        ITermFactoryService termFactService = INJECTOR.getInstance(ITermFactoryService.class);
        evalStrategy =
            new DynSemEvaluationStrategy(new MockInterpreterLoader(ruleKey), termFactService);
    }

    /**
     * @return Parameters for the tests.
     */
    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> inputAndRuleParameters() {
        ITermFactoryService termFactService = INJECTOR.getInstance(ITermFactoryService.class);
        ITermFactory termFact = termFactService.getGeneric();
        IStrategoAppl existingTerm =
            termFact.makeAppl(termFact.makeConstructor("Add", 2),
                              termFact.makeInt(DEFAULT_SHELL_RULE_ANSWER - 2), termFact.makeInt(2));
        StrategoUtil.setSortForTerm(existingTerm, "Expr");

        IStrategoAppl nonExistingRuleForTerm =
            termFact.makeAppl(termFact.makeConstructor("nonExistingRuleForTerm", 2),
                              termFact.makeInt(0), termFact.makeInt(0));
        StrategoUtil.setSortForTerm(nonExistingRuleForTerm, "Expr");

        IStrategoTerm wrongTypeTerm = termFact.makeList();

        //@formatter:off
        return Arrays
            .asList(new Object[][] { { "shell/_Expr/1", existingTerm, NO_EXCEPTION },
                                     { "shell/Add/2", existingTerm, NO_EXCEPTION },
                                     { "shell/noSuchRule/2", nonExistingRuleForTerm,
                                       "No shell rule found to be applied "
                                       + "to term \"nonExistingRuleForTerm(0,0)\"" },
                                     { "shell/wrongArgumentType/0", wrongTypeTerm,
                                       "Expected a StrategoAppl, but a "
                                       + "StrategoList was found: \"[]\"" } });
        //@formatter:on
    }

    /**
     * Tests the propagation of the execution environment (the semantic components).
     */
    @Test
    public void testEnvironmentPropagation() {
        AnalyzeResult analyzeResultMock =
            when(mock(AnalyzeResult.class).ast()).thenReturn(Optional.of(input)).getMock();
        try {
            // First invocation causes an update in the environment.
            IStrategoTerm firstResult = evalStrategy
                .evaluate(analyzeResultMock, mock(IContext.class, Mockito.RETURNS_MOCKS));
            assertTrue(firstResult.toString().contains("0"));

            // Second invocation has a different result due to a different environment.
            IStrategoTerm secondResult = evalStrategy
                .evaluate(analyzeResultMock, mock(IContext.class, Mockito.RETURNS_MOCKS));
            assertTrue(secondResult.toString().contains(String.valueOf(DEFAULT_SHELL_RULE_ANSWER)));
        } catch (MetaborgException e) {
            assertEquals(expectedExceptionMessage, e.getMessage());
        }
    }

    /**
     * An {@link IInterpreterLoader} that returns a mock interpreter.
     */
    final class MockInterpreterLoader implements IInterpreterLoader {
        private String ruleKey;

        private MockInterpreterLoader(String ruleKey) {
            this.ruleKey = ruleKey;
        }

        // Method length is ignored because this is just a test.
        // CHECKSTYLE.OFF: MethodLength
        @Override
        public PolyglotEngine loadInterpreterForLanguage(ILanguageImpl langImpl)
            throws InterpreterLoadException {
            try {
                PolyglotEngine mockInterpreter = mock(PolyglotEngine.class);
                when(mockInterpreter.findGlobalSymbol(anyString())).thenReturn(null);

                // Mock the init rule.
                Value mockInitRuleResult = when(mock(Value.class).as(RuleResult.class))
                    .thenReturn(new RuleResult(null,
                                               new Object[] { new HashMap<String, Integer>() }))
                    .getMock();
                Value mockInitRule =
                    when(mock(Value.class).execute(any())).thenReturn(mockInitRuleResult).getMock();
                when(mockInterpreter.findGlobalSymbol("init/ShellInit/0")).thenReturn(mockInitRule);

                // Mock the shell rule. It extends the environment.
                Value mockRule = when(mock(Value.class).execute(any(), anyMap()))
                    .thenAnswer(new Answer<Value>() {
                        @SuppressWarnings("unchecked")
                        @Override
                        public Value answer(InvocationOnMock invocation) throws Throwable {
                            Map<String, Integer> map = invocation.getArgumentAt(1, Map.class);
                            int result = map.getOrDefault("foo", 0);
                            map.put("foo", DEFAULT_SHELL_RULE_ANSWER);
                            return when(mock(Value.class).as(RuleResult.class))
                                .thenReturn(new RuleResult(result, new Object[] { map })).getMock();
                        }
                    }).getMock();
                when(mockInterpreter.findGlobalSymbol(ruleKey)).thenReturn(mockRule);

                return mockInterpreter;
            } catch (IOException e) {
                fail("Should never happen.");
                throw new InterpreterLoadException(e);
            }
        }
        // CHECKSTYLE.ON: MethodLength

        @Override
        public ITermTransformer getTransformer() {
            return new ITermTransformer.IDENTITY();
        }

        @Override
        public ITerm getProgramTerm(IStrategoTerm term) {
            return new ITerm() {

                @Override
                public ITermInstanceChecker getCheck() {
                    return null;
                }

                @Override
                public String constructor() {
                    if (!(term instanceof IStrategoAppl)) {
                        return "";
                    }
                    return ((IStrategoAppl) term).getConstructor().getName();
                }

                @Override
                public int arity() {
                    if (!(term instanceof IStrategoAppl)) {
                        return 0;
                    }
                    return ((IStrategoAppl) term).getConstructor().getArity();
                }

                @Override
                public Object[] allSubterms() {
                    return null;
                }
            };
        }

    }
}
