package org.metaborg.spoofax.shell.functions;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import org.metaborg.spoofax.core.unit.ISpoofaxTransformUnit;
import org.metaborg.spoofax.shell.client.IResult;
import org.metaborg.spoofax.shell.client.IResultVisitor;
import org.metaborg.spoofax.shell.commands.CommandBuilder;
import org.metaborg.spoofax.shell.commands.IReplCommand;
import org.metaborg.spoofax.shell.output.AnalyzeResult;
import org.metaborg.spoofax.shell.output.FailOrSuccessResult;
import org.metaborg.spoofax.shell.output.FailResult;
import org.metaborg.spoofax.shell.output.IResultFactory;
import org.metaborg.spoofax.shell.output.ISpoofaxResult;
import org.metaborg.spoofax.shell.output.InputResult;
import org.metaborg.spoofax.shell.output.ParseResult;
import org.metaborg.spoofax.shell.output.TransformResult;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test creating and using a {@link IReplCommand} created from the {@link TransformFunction}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ATransformFunctionTest {
    private static final String DESCRIPTION = "analyze";

    // Constructor mocks
    @Mock private IContextService contextService;
    @Mock private IFunctionFactory functionFactory;
    @Mock private ISpoofaxTransformService transformService;
    @Mock private IResultFactory resultFactory;

    @Mock private IProject project;
    @Mock private IContext context;
    @Mock private ILanguageImpl lang;

    @Mock private ISpoofaxAnalyzeUnit analyzeUnit;
    @Mock private ISpoofaxTransformUnit<ISpoofaxAnalyzeUnit> transformUnit;

    @Mock private InputResult inputResult;
    @Mock private ParseResult parseResult;
    @Mock private AnalyzeResult analyzeResult;
    @Mock private TransformResult transformResult;
    @Mock private ITransformAction action;

    @Mock private IResultVisitor visitor;
    @Captor private ArgumentCaptor<FailResult> failCaptor;
    @Captor private ArgumentCaptor<ISpoofaxResult<?>> resultCaptor;
    @Captor private ArgumentCaptor<Exception> exceptionCaptor;

    private FileObject sourceFile;
    private IReplCommand pTransformCommand;
    private IReplCommand aTransformCommand;

    /**
     * Set up mocks used in the test case.
     * @throws FileSystemException when resolving the temp file fails
     * @throws MetaborgException on unexpected Spoofax exceptions
     */
    @Before
    public void setup() throws FileSystemException, MetaborgException {
        sourceFile = VFS.getManager().resolveFile("ram://junit-temp");
        when(project.location()).thenReturn(sourceFile);

        mockFunctions();

        when(analyzeResult.context()).thenReturn(Optional.empty());
        when(transformResult.unit()).thenAnswer((invocation) -> transformUnit);
        when(resultFactory.createTransformResult(any())).thenReturn(transformResult);
        when(contextService.get(any(), any(), any())).thenReturn(context);
        ISpoofaxAnalyzeUnit unit = any(ISpoofaxAnalyzeUnit.class);
        when(transformService.transform(unit, any(), any(ITransformGoal.class)))
        .thenReturn(Collections.singletonList(transformUnit));

        pTransformCommand = new CommandBuilder<>(functionFactory, project, lang)
                .transformAnalyzed(action).description(DESCRIPTION).build();
        aTransformCommand = new CommandBuilder<>(functionFactory, project, lang)
                .transformAnalyzed(action).description(DESCRIPTION).build();
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
        assertEquals(DESCRIPTION, aTransformCommand.description());
    }

    /**
     * Test creating a valid {@link TransformResult}.
     * @throws MetaborgException on unexpected Spoofax exceptions
     */
    @Test
    public void testTransformValid() throws MetaborgException {
        when(transformResult.valid()).thenReturn(true);

        IResult execute = aTransformCommand.execute("test");
        verify(contextService, times(1)).get(any(), any(), any());
        verify(resultFactory, times(1)).createTransformResult(any());
        verify(transformResult, never()).accept(visitor);

        execute.accept(visitor);
        verify(transformResult, times(1)).accept(visitor);
    }

    /**
     * Test creating a valid {@link TransformResult} with an existing {@link IContext}.
     * @throws MetaborgException on unexpected Spoofax exceptions
     */
    @Test
    public void testTransformContext() throws MetaborgException {
        when(analyzeResult.context()).thenReturn(Optional.of(context));
        when(transformResult.valid()).thenReturn(true);

        IResult execute = aTransformCommand.execute("test");
        verify(contextService, never()).get(any(), any(), any());
        verify(resultFactory, times(1)).createTransformResult(any());
        verify(transformResult, never()).accept(visitor);

        execute.accept(visitor);
        verify(transformResult, times(1)).accept(visitor);
    }

    /**
     * Test creating an invalid {@link TransformResult}.
     * @throws MetaborgException on unexpected Spoofax exceptions
     */
    @Test
    public void testTransformInvalid() throws MetaborgException {
        when(transformResult.valid()).thenReturn(false);

        IResult execute = aTransformCommand.execute("test");
        verify(visitor, never()).visitFailure(any());

        execute.accept(visitor);
        verify(visitor, times(1)).visitFailure(failCaptor.capture());
        assertEquals(transformResult, failCaptor.getValue().getCause());
    }

    /**
     * Test creating a {@link TransformResult} resulting in an exception.
     * @throws MetaborgException on unexpected Spoofax exceptions
     */
    @Test
    public void testTransformException() throws MetaborgException {
        TransformException transformException = new TransformException("error");
        ISpoofaxAnalyzeUnit unit = any(ISpoofaxAnalyzeUnit.class);
        when(transformService.transform(unit, any(), any(ITransformGoal.class)))
        .thenThrow(transformException);

        IResult execute = aTransformCommand.execute("test");
        verify(visitor, never()).visitException(any());

        execute.accept(visitor);
        verify(visitor, times(1)).visitException(exceptionCaptor.capture());
        assertEquals(transformException, exceptionCaptor.getValue());
    }

}
