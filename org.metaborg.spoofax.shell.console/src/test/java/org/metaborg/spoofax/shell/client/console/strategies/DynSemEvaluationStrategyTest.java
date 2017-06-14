//package org.metaborg.spoofax.shell.client.console.strategies;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
//import static org.junit.Assert.fail;
//import static org.mockito.Matchers.any;
//import static org.mockito.Matchers.anyMap;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//import java.io.IOException;
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.Map;
//
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.junit.runners.Parameterized;
//import org.junit.runners.Parameterized.Parameters;
//import org.metaborg.core.MetaborgException;
//import org.metaborg.core.context.IContext;
//import org.metaborg.core.language.ILanguageImpl;
//import org.metaborg.meta.lang.dynsem.interpreter.DynSemVM;
//import org.metaborg.meta.lang.dynsem.interpreter.nodes.matching.ITermInstanceChecker;
//import org.metaborg.meta.lang.dynsem.interpreter.nodes.rules.RuleResult;
//import org.metaborg.meta.lang.dynsem.interpreter.terms.ITerm;
//import org.metaborg.meta.lang.dynsem.interpreter.terms.ITermTransformer;
//import org.metaborg.spoofax.core.SpoofaxModule;
//import org.metaborg.spoofax.core.terms.ITermFactoryService;
//import org.mockito.Mockito;
//import org.mockito.invocation.InvocationOnMock;
//import org.mockito.stubbing.Answer;
//import org.spoofax.interpreter.terms.IStrategoAppl;
//import org.spoofax.interpreter.terms.IStrategoTerm;
//import org.spoofax.interpreter.terms.ITermFactory;
//
//import com.google.inject.Guice;
//import com.google.inject.Injector;
//import com.oracle.truffle.api.vm.PolyglotEngine;
//import com.oracle.truffle.api.vm.PolyglotEngine.Value;
//
///**
// * Tests the {@link DynSemEvaluationStrategy}.
// */
//@RunWith(Parameterized.class)
//public class DynSemEvaluationStrategyTest {
//    private final DynSemEvaluationStrategy evalStrategy;
//    private final IStrategoTerm input;
//    private static final int DEFAULT_SHELL_RULE_ANSWER = 42;
//    private static final String NO_EXCEPTION = "No exception should be thrown";
//    private final String expectedExceptionMessage;
//
//    private static final Injector INJECTOR = Guice.createInjector(new SpoofaxModule());
//
//    /**
//     * Set up the evaluation strategy with the {@link MockInterpreterLoader}.
//     *
//     * @param ruleKey
//     *            The key of the shell rule, to cover the branch in the private rule lookup method.
//     * @param mockInitRule
//     *            The rule for initializing the environment.
//     * @param input
//     *            The {@link IStrategoTerm} test input.
//     * @param expectedExceptionMessage
//     *            The exception message that is expected when an exception has been thrown.
//     */
//    public DynSemEvaluationStrategyTest(String ruleKey, Value mockInitRule,
//                                        IStrategoTerm input, String expectedExceptionMessage) {
//        this.input = input;
//        this.expectedExceptionMessage = expectedExceptionMessage;
//        ITermFactoryService termFactService = INJECTOR.getInstance(ITermFactoryService.class);
//        evalStrategy =
//            new DynSemEvaluationStrategy(new MockInterpreterLoader(ruleKey, mockInitRule),
//                                         termFactService);
//    }
//
//    /**
//     * @return Parameters for the tests.
//     * @throws IOException
//     *             Should not happen.
//     */
//    // CHECKSTYLE.OFF: MethodLength
//    @Parameters(name = "{index}: {0}")
//    public static Collection<Object[]> inputAndRuleParameters() throws IOException {
//        ITermFactory termFact = INJECTOR.getInstance(ITermFactoryService.class).getGeneric();
//        IStrategoAppl existingTerm =
//            termFact.makeAppl(termFact.makeConstructor("Add", 2),
//                              termFact.makeInt(0), termFact.makeInt(0));
//
//        IStrategoAppl nonExistingRuleForTerm =
//            termFact.makeAppl(termFact.makeConstructor("nonExistingRuleForTerm", 2),
//                              termFact.makeInt(0), termFact.makeInt(0));
//
//        IStrategoTerm wrongTypeTerm = termFact.makeList();
//        IStrategoTerm noSortTerm = termFact.makeAppl(termFact.makeConstructor("Thing", 0));
//
//        return Arrays
//            .asList(new Object[][] { { "shell/Add/2", mockInitRule(), existingTerm, NO_EXCEPTION },
//                                     { "shell/noSuchRule/2", mockInitRule(), nonExistingRuleForTerm,
//                                       "No shell rule found to be applied "
//                                       + "to term \"nonExistingRuleForTerm(0,0)\"." },
//                                     { "shell/wrongArgumentType/0", mockInitRule(), wrongTypeTerm,
//                                       "Expected a StrategoAppl, but a "
//                                       + "StrategoList was found: \"[]\"." },
//                                     { "shell/_Expr/1", mockInitRule(), noSortTerm,
//                                       "No shell rule found to be applied "
//                                       + "to term \"Thing\"." },
//                                     { "shell/Thing/0", mockInitRule(), noSortTerm, NO_EXCEPTION },
//                                     { "Non-existing init rule", null,
//                                       noSortTerm, "No shell initialization rule found.\n"
//                                               + "Initialize the semantic components for the"
//                                               + " \"shell\" rules with a rule of the form "
//                                               + "\"ShellInit() -init-> ShellInit() :: "
//                                               + "<RW>*\"." } });
//    }
//    // CHECKSTYLE.ON: MethodLength
//
//    private static Value mockInitRule() throws IOException {
//        // Mock the init rule.
//        Value mockInitRuleResult = when(mock(Value.class).as(RuleResult.class))
//            .thenReturn(new RuleResult(null, new Object[] { new HashMap<String, Integer>() }))
//            .getMock();
//        Value mockInitRule =
//            when(mock(Value.class).execute(any())).thenReturn(mockInitRuleResult).getMock();
//        return mockInitRule;
//    }
//
//    /**
//     * Tests the propagation of the execution environment (the semantic components).
//     */
//    @Test
//    public void testEnvironmentPropagation() {
//        try {
//            // First invocation causes an update in the environment.
//            IStrategoTerm firstResult =
//                evalStrategy.evaluate(input, mock(IContext.class, Mockito.RETURNS_MOCKS));
//            assertEquals("\"0\"", firstResult.toString());
//
//            // Second invocation has a different result due to a different environment.
//            IStrategoTerm secondResult =
//                evalStrategy.evaluate(input, mock(IContext.class, Mockito.RETURNS_MOCKS));
//            assertTrue(secondResult.toString().contains(String.valueOf(DEFAULT_SHELL_RULE_ANSWER)));
//            if (expectedExceptionMessage != NO_EXCEPTION) {
//                fail("Exception was expected, but not thrown.");
//            }
//        } catch (MetaborgException e) {
//            assertEquals(expectedExceptionMessage, e.getMessage());
//        }
//    }
//
//    /**
//     * An {@link IInterpreterLoader} that returns a mock interpreter.
//     */
//    static final class MockInterpreterLoader implements IInterpreterLoader {
//        private final String ruleKey;
//        private final Value mockInitRule;
//
//        private MockInterpreterLoader(String ruleKey, Value mockInitRule) {
//            this.ruleKey = ruleKey;
//            this.mockInitRule = mockInitRule;
//        }
//
//        // Method length is ignored because this is just a test.
//        // CHECKSTYLE.OFF: MethodLength
//        @Override
//        public DynSemVM createInterpreterForLanguage(ILanguageImpl langImpl)
//            throws MetaborgException {
//            DynSemVM mockInterpreter = mock(DynSemVM.class);
//
//            when(mockInterpreter.findGlobalSymbol("init/ShellInit/0")).thenReturn(mockInitRule);
//
//            // Mock the shell rule. It extends the environment.
//            Value mockRule = when(mock(Value.class).execute(any(), anyMap()))
//                .thenAnswer(new Answer<Value>() {
//                    @SuppressWarnings("unchecked")
//                    @Override
//                    public Value answer(InvocationOnMock invocation) throws Throwable {
//                        Map<String, Integer> map = invocation.getArgument(1);
//                        int result = map.getOrDefault("foo", 0);
//                        map.put("foo", DEFAULT_SHELL_RULE_ANSWER);
//                        return when(mock(Value.class).as(RuleResult.class))
//                            .thenReturn(new RuleResult(result, new Object[] { map })).getMock();
//                    }
//                }).getMock();
//            when(mockInterpreter.findGlobalSymbol(ruleKey)).thenReturn(mockRule);
//
//            return mockInterpreter;
//        }
//        // CHECKSTYLE.ON: MethodLength
//
//    }
//}
