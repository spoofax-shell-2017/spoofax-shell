package org.metaborg.spoofax.shell.functions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.context.IContextService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.core.shell.ShellFacet;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.shell.commands.CommandBuilder;
import org.metaborg.spoofax.shell.commands.IReplCommand;
import org.metaborg.spoofax.shell.core.IEvaluationStrategy;
import org.metaborg.spoofax.shell.output.AnalyzeResult;
import org.metaborg.spoofax.shell.output.EvaluateResult;
import org.metaborg.spoofax.shell.output.FailOrSuccessResult;
import org.metaborg.spoofax.shell.output.FailResult;
import org.metaborg.spoofax.shell.output.IResult;
import org.metaborg.spoofax.shell.output.IResultFactory;
import org.metaborg.spoofax.shell.output.IResultVisitor;
import org.metaborg.spoofax.shell.output.ISpoofaxResult;
import org.metaborg.spoofax.shell.output.InputResult;
import org.metaborg.spoofax.shell.output.ParseResult;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * Test creating and using a {@link IReplCommand} created from the {@link AEvalFunction} and
 * {@link PEvalFunction}.
 */
@RunWith(Parameterized.class)
public class EvaluateFunctionTest {
    private static final String PARSE = "parse";
    private static final String ANALYZE = "analyze";

    // Constructor mocks
    @Mock
    private IContextService contextService;
    @Mock
    private IFunctionFactory functionFactory;
    @Mock
    private IResultFactory resultFactory;

    @Mock
    private IProject project;
    @Mock
    private IContext context;
    @Mock
    private ILanguageImpl lang;

    @Mock
    private IEvaluationStrategy evalStrategy;

    @Mock
    private InputResult inputResult;
    @Mock
    private ParseResult parseResult;
    @Mock
    private AnalyzeResult analyzeResult;

    @Mock
    private IResultVisitor visitor;
    @Captor
    private ArgumentCaptor<FailResult> failCaptor;
    @Captor
    private ArgumentCaptor<ISpoofaxResult<?>> resultCaptor;
    @Captor
    private ArgumentCaptor<Exception> exceptionCaptor;

    private final String description;
    private final EvaluateResult result;
    private final IReplCommand command;
    private final Function<IResultFactory, EvaluateResult> check;

    /**
     * Parameters to test {@link AEvalFunction} and {@link PEvalFunction}.
     *
     * @return a list of parameters
     */
    @Parameters(name = "{index}: {0}")
    public static List<Object[]> functions() {
        Function<CommandBuilder<?>, CommandBuilder<?>> parsedEval = CommandBuilder::evalParsed;
        Function<CommandBuilder<?>, CommandBuilder<?>> analyzedEval = CommandBuilder::evalAnalyzed;
        Function<IResultFactory, EvaluateResult> checkParsed =
            (factory) -> factory.createEvaluateResult(any(ParseResult.class), any());
        Function<IResultFactory, EvaluateResult> checkAnalyzed =
            (factory) -> factory.createEvaluateResult(any(AnalyzeResult.class), any());

        EvaluateResult parsedEvalMock = mock(EvaluateResult.class);
        EvaluateResult analyzedEvalMock = mock(EvaluateResult.class);
        when(parsedEvalMock.unit()).thenReturn(mock(ISpoofaxParseUnit.class));
        when(analyzedEvalMock.unit()).thenReturn(mock(ISpoofaxAnalyzeUnit.class));
        return Arrays
            .asList(new Object[][] { { PARSE, parsedEvalMock, parsedEval, checkParsed },
                                     { ANALYZE, analyzedEvalMock, analyzedEval, checkAnalyzed } });
    }

    /**
     * Constructor for parameterized tests.
     *
     * @param description
     *            description of the command
     * @param result
     *            result of the command
     * @param func
     *            function that creates the command
     * @param check
     *            checks creation of the result
     * @throws FileSystemException
     *             when resolving the temp file fails
     * @throws MetaborgException
     *             on unexpected Spoofax exceptions
     */
    public EvaluateFunctionTest(String description, EvaluateResult result,
                                Function<CommandBuilder<?>, CommandBuilder<?>> func,
                                Function<IResultFactory, EvaluateResult> check)
        throws FileSystemException, MetaborgException {
        this.description = description;
        this.result = result;
        this.check = check;

        initMocks(this);
        mockContext();
        mockServices();
        mockFunctions();

        command = func.apply(new CommandBuilder<>(functionFactory, project, lang))
            .description(description).build();
    }

    private void mockContext() {
        ShellFacet facetMock =
            when(mock(ShellFacet.class).getEvaluationMethod()).thenReturn("mock").getMock();
        ILanguageImpl langMock =
            when(mock(ILanguageImpl.class).facet(ShellFacet.class)).thenReturn(facetMock).getMock();
        when(context.language()).thenReturn(langMock);
    }

    private void mockServices() throws FileSystemException, MetaborgException {
        FileObject sourceFile = VFS.getManager().resolveFile("ram://junit-temp");
        when(project.location()).thenReturn(sourceFile);
        when(parseResult.ast()).thenReturn(Optional.of(mock(IStrategoTerm.class)));
        when(analyzeResult.ast()).thenReturn(Optional.of(mock(IStrategoTerm.class)));
        when(parseResult.context()).thenReturn(Optional.empty());
        when(analyzeResult.context()).thenReturn(Optional.empty());
        doCallRealMethod().when(parseResult).accept(any(IResultVisitor.class));
        doCallRealMethod().when(analyzeResult).accept(any(IResultVisitor.class));
        when(contextService.get(any(), any(), any())).thenReturn(context);

        when(resultFactory.createEvaluateResult(any(ParseResult.class), any())).thenReturn(result);
        when(resultFactory.createEvaluateResult(any(AnalyzeResult.class), any()))
            .thenReturn(result);
    }

    private void mockFunctions() {
        Map<String, IEvaluationStrategy> evalStrategies = new HashMap<>(1);
        evalStrategies.put("mock", evalStrategy);
        evalStrategies.put("anotherOne", null);
        EvaluateFunction pEvalFunction =
            new EvaluateFunction(evalStrategies, contextService, resultFactory, project, lang);

        when(functionFactory.createInputFunction(any(), any()))
            .thenReturn((input) -> FailOrSuccessResult.successful(inputResult));
        when(functionFactory.createParseFunction(any(), any()))
            .thenReturn((input) -> FailOrSuccessResult.successful(parseResult));
        when(functionFactory.createAnalyzeFunction(any(), any()))
            .thenReturn((input) -> FailOrSuccessResult.successful(analyzeResult));

        when(functionFactory.createEvaluateFunction(any(), any())).thenReturn(pEvalFunction);

        FunctionComposer composer = new FunctionComposer(functionFactory, project, lang);
        when(functionFactory.createComposer(any(), any())).thenReturn(composer);
    }

    /**
     * Verify that the description of the command is correct.
     */
    @Test
    public void testDescription() {
        assertEquals(description, command.description());
    }

    /**
     * Test failing of command when input has no AST present.
     *
     * @throws MetaborgException
     *             on unexpected Spoofax exceptions
     */
    @Test
    public void testEvaluateNoAST() throws MetaborgException {
        when(parseResult.ast()).thenReturn(Optional.empty());
        when(analyzeResult.ast()).thenReturn(Optional.empty());

        IResult execute = command.execute("test");
        assertTrue(execute instanceof FailOrSuccessResult);

        execute.accept(visitor);
        verify(visitor, times(1)).visitFailure(failCaptor.capture());
        ISpoofaxResult<?> failureCause = failCaptor.getValue().getCause();
        assertTrue(parseResult == failureCause || analyzeResult == failureCause);
    }

    /**
     * Test creating a valid {@link EvaluateResult}.
     *
     * @throws MetaborgException
     *             on unexpected Spoofax exceptions
     */
    @Test
    public void testEvaluateValid() throws MetaborgException {
        when(result.valid()).thenReturn(true);

        IResult execute = command.execute("test");
        verify(contextService, times(1)).get(any(), any(), any());
        check.apply(verify(resultFactory, times(1)));
        verify(result, never()).accept(visitor);

        execute.accept(visitor);
        verify(result, times(1)).accept(visitor);
    }

    /**
     * Test creating a valid {@link EvaluateResult} with an existing {@link IContext}.
     *
     * @throws MetaborgException
     *             on unexpected Spoofax exceptions
     */
    @Test
    public void testEvaluateContext() throws MetaborgException {
        when(parseResult.context()).thenReturn(Optional.of(context));
        when(analyzeResult.context()).thenReturn(Optional.of(context));
        when(result.valid()).thenReturn(true);

        IResult execute = command.execute("test");
        verify(contextService, never()).get(any(), any(), any());
        check.apply(verify(resultFactory, times(1)));
        verify(result, never()).accept(visitor);

        execute.accept(visitor);
        verify(result, times(1)).accept(visitor);
    }

    /**
     * Test creating an invalid {@link EvaluateResult}.
     *
     * @throws MetaborgException
     *             on unexpected Spoofax exceptions
     */
    @Test
    public void testEvaluateInvalid() throws MetaborgException {
        when(result.valid()).thenReturn(false);

        IResult execute = command.execute("test");
        verify(visitor, never()).visitFailure(any());

        execute.accept(visitor);
        verify(visitor, times(1)).visitFailure(failCaptor.capture());
        assertEquals(result, failCaptor.getValue().getCause());
    }

    /**
     * Test evaluation strategy invocation resulting in an exception.
     *
     * @throws MetaborgException
     *             on unexpected Spoofax exceptions
     */
    @Test
    public void testEvaluationStrategyException() throws MetaborgException {
        MetaborgException evalException = new MetaborgException("error");

        when(evalStrategy.evaluate(any(IStrategoTerm.class), eq(context))).thenThrow(evalException);

        IResult execute = command.execute("test");
        verify(visitor, never()).visitException(any());

        execute.accept(visitor);
        verify(visitor, times(1)).visitException(exceptionCaptor.capture());
        assertEquals(evalException, exceptionCaptor.getValue());
    }

    /**
     * Test throwing an exception when there is no {@link ShellFacet}.
     *
     * @throws MetaborgException
     *             on unexpected Spoofax exceptions
     */
    @Test
    public void testAbsentShellFacetException() throws MetaborgException {
        ILanguageImpl langMock =
            when(mock(ILanguageImpl.class).facet(ShellFacet.class)).thenReturn(null).getMock();
        when(context.language()).thenReturn(langMock);

        IResult execute = command.execute("test");
        verify(visitor, never()).visitException(any());

        execute.accept(visitor);
        verify(visitor, times(1)).visitException(exceptionCaptor.capture());
        assertEquals("No ESV configuration found for the REPL.",
                     exceptionCaptor.getValue().getMessage());
    }

    /**
     * Test throwing an exception when evaluation method of the {@link ShellFacet} does not exist.
     *
     * @throws MetaborgException
     *             on unexpected Spoofax exceptions
     */
    @Test
    public void testMissingEvalMethodException() throws MetaborgException {
        ShellFacet facetMock = context.language().facet(ShellFacet.class);
        when(facetMock.getEvaluationMethod()).thenReturn("non-existing");

        IResult execute = command.execute("test");
        verify(visitor, never()).visitException(any());

        execute.accept(visitor);
        verify(visitor, times(1)).visitException(exceptionCaptor.capture());
        assertEquals("Evaluation method \"non-existing\" not supported.\n"
                     + "Supported evaluation method(s): \"mock\", \"anotherOne\"",
                     exceptionCaptor.getValue().getMessage());
    }

}
