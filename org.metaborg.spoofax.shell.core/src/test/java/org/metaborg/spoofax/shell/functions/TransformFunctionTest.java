package org.metaborg.spoofax.shell.functions;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.action.ITransformAction;
import org.metaborg.core.action.ITransformGoal;
import org.metaborg.core.context.IContext;
import org.metaborg.core.context.IContextService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.core.transform.TransformException;
import org.metaborg.spoofax.core.transform.ISpoofaxTransformService;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxTransformUnit;
import org.metaborg.spoofax.shell.commands.CommandBuilder;
import org.metaborg.spoofax.shell.commands.IReplCommand;
import org.metaborg.spoofax.shell.output.AnalyzeResult;
import org.metaborg.spoofax.shell.output.FailOrSuccessResult;
import org.metaborg.spoofax.shell.output.FailResult;
import org.metaborg.spoofax.shell.output.IResult;
import org.metaborg.spoofax.shell.output.IResultFactory;
import org.metaborg.spoofax.shell.output.IResultVisitor;
import org.metaborg.spoofax.shell.output.ISpoofaxResult;
import org.metaborg.spoofax.shell.output.InputResult;
import org.metaborg.spoofax.shell.output.ParseResult;
import org.metaborg.spoofax.shell.output.TransformResult;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

/**
 * Test creating and using a {@link IReplCommand} created from the {@link TransformFunction}.
 */
@RunWith(Parameterized.class)
public class TransformFunctionTest {
    private static final String PARSE = "parse";
    private static final String ANALYZE = "analyze";

    // Constructor mocks
    @Mock private IContextService contextService;
    @Mock private IFunctionFactory functionFactory;
    @Mock private ISpoofaxTransformService transformService;
    @Mock private IResultFactory resultFactory;

    @Mock private IProject project;
    @Mock private IContext context;
    @Mock private ILanguageImpl lang;

    @Mock private ISpoofaxTransformUnit<ISpoofaxParseUnit> pTransformUnit;
    @Mock private ISpoofaxTransformUnit<ISpoofaxAnalyzeUnit> aTransformUnit;

    @Mock private InputResult inputResult;
    @Mock private ParseResult parseResult;
    @Mock private AnalyzeResult analyzeResult;
    @Mock private ITransformAction action;

    @Mock private IResultVisitor visitor;
    @Captor private ArgumentCaptor<FailResult> failCaptor;
    @Captor private ArgumentCaptor<ISpoofaxResult<?>> resultCaptor;
    @Captor private ArgumentCaptor<Exception> exceptionCaptor;

    private FileObject sourceFile;
    private String description;
    private TransformResult result;
    private IReplCommand command;
    private Function<IResultFactory, TransformResult> check;

    /**
     * Parameters to test {@link ATransformFunction} and {@link PTransformFunction}.
     * @return a list of parameters
     */
    @Parameters(name = "{index}: {0}")
    public static List<Object[]> functions() {
        BiFunction<CommandBuilder<?>, ITransformAction, CommandBuilder<?>> parsedTransform
            = (builder, action) -> builder.transformParsed(action);
        BiFunction<CommandBuilder<?>, ITransformAction, CommandBuilder<?>> analyzedTransform
            = (builder, action) -> builder.transformAnalyzed(action);
        Function<IResultFactory, TransformResult> checkParsed
            = (factory) -> factory.createPTransformResult(any());
        Function<IResultFactory, TransformResult> checkAnalyzed
            = (factory) -> factory.createATransformResult(any());

        return Arrays.asList(new Object[][] {
            { PARSE, mock(TransformResult.class), parsedTransform, checkParsed },
            { ANALYZE, mock(TransformResult.class), analyzedTransform, checkAnalyzed }
        });
    }

    /**
     * Constructor for parameterized tests.
     * @param description  description of the command
     * @param result       result of the command
     * @param func         function that creates the command
     * @param check        checks creation of the result
     * @throws FileSystemException when resolving the temp file fails
     * @throws MetaborgException on unexpected Spoofax exceptions
     */
    public TransformFunctionTest(String description,
                                  TransformResult result,
                                  BiFunction<CommandBuilder<?>, ITransformAction,
                                             CommandBuilder<?>> func,
                                  Function<IResultFactory, TransformResult> check)
            throws FileSystemException, MetaborgException {
        this.description = description;
        this.result = result;
        this.check = check;

        initMocks(this);
        mockServices();
        mockFunctions();

        command = func.apply(new CommandBuilder<>(functionFactory, project, lang), action)
                .description(description).build();
    }

    private void mockServices() throws FileSystemException, MetaborgException {
        sourceFile = VFS.getManager().resolveFile("ram://junit-temp");
        when(project.location()).thenReturn(sourceFile);
        when(parseResult.context()).thenReturn(Optional.empty());
        when(analyzeResult.context()).thenReturn(Optional.empty());
        when(contextService.get(any(), any(), any())).thenReturn(context);

        when(result.unit()).thenAnswer((invocation) -> aTransformUnit);
        when(resultFactory.createPTransformResult(any())).thenReturn(result);
        when(resultFactory.createATransformResult(any())).thenReturn(result);

        ISpoofaxParseUnit punit = any(ISpoofaxParseUnit.class);
        when(transformService.transform(punit, any(), any(ITransformGoal.class)))
        .thenReturn(Collections.singletonList(pTransformUnit));
        ISpoofaxAnalyzeUnit aunit = any(ISpoofaxAnalyzeUnit.class);
        when(transformService.transform(aunit, any(), any(ITransformGoal.class)))
        .thenReturn(Collections.singletonList(aTransformUnit));
    }

    private void mockFunctions() {
        PTransformFunction pTransformFunction =
                new PTransformFunction(contextService, transformService,
                                       resultFactory, project, lang, action);
        ATransformFunction aTransformFunction =
                new ATransformFunction(contextService, transformService,
                                       resultFactory, project, lang, action);

        when(functionFactory.createInputFunction(any(), any())).thenReturn((input) ->
            FailOrSuccessResult.successful(inputResult)
        );
        when(functionFactory.createParseFunction(any(), any())).thenReturn((input) ->
            FailOrSuccessResult.successful(parseResult)
        );
        when(functionFactory.createAnalyzeFunction(any(), any())).thenReturn((input) ->
            FailOrSuccessResult.successful(analyzeResult)
        );

        when(functionFactory.createPTransformFunction(any(), any(), any()))
            .thenReturn(pTransformFunction);
        when(functionFactory.createATransformFunction(any(), any(), any()))
            .thenReturn(aTransformFunction);
    }

    /**
     * Verify that the description of the command is correct.
     */
    @Test
    public void testDescription() {
        assertEquals(description, command.description());
    }

    /**
     * Test creating a valid {@link TransformResult}.
     * @throws MetaborgException on unexpected Spoofax exceptions
     */
    @Test
    public void testTransformValid() throws MetaborgException {
        when(result.valid()).thenReturn(true);

        IResult execute = command.execute("test");
        verify(contextService, times(1)).get(any(), any(), any());
        check.apply(verify(resultFactory, times(1)));
        verify(result, never()).accept(visitor);

        execute.accept(visitor);
        verify(result, times(1)).accept(visitor);
    }

    /**
     * Test creating a valid {@link TransformResult} with an existing {@link IContext}.
     * @throws MetaborgException on unexpected Spoofax exceptions
     */
    @Test
    public void testTransformContext() throws MetaborgException {
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
     * Test creating an invalid {@link TransformResult}.
     * @throws MetaborgException on unexpected Spoofax exceptions
     */
    @Test
    public void testTransformInvalid() throws MetaborgException {
        when(result.valid()).thenReturn(false);

        IResult execute = command.execute("test");
        verify(visitor, never()).visitFailure(any());

        execute.accept(visitor);
        verify(visitor, times(1)).visitFailure(failCaptor.capture());
        assertEquals(result, failCaptor.getValue().getCause());
    }

    /**
     * Test creating a {@link TransformResult} resulting in an exception.
     * @throws MetaborgException on unexpected Spoofax exceptions
     */
    @Test
    public void testTransformException() throws MetaborgException {
        TransformException transformException = new TransformException("error");

        ISpoofaxParseUnit punit = any(ISpoofaxParseUnit.class);
        when(transformService.transform(punit, any(), any(ITransformGoal.class)))
        .thenThrow(transformException);
        ISpoofaxAnalyzeUnit aunit = any(ISpoofaxAnalyzeUnit.class);
        when(transformService.transform(aunit, any(), any(ITransformGoal.class)))
        .thenThrow(transformException);

        IResult execute = command.execute("test");
        verify(visitor, never()).visitException(any());

        execute.accept(visitor);
        verify(visitor, times(1)).visitException(exceptionCaptor.capture());
        assertEquals(transformException, exceptionCaptor.getValue());
    }

}
