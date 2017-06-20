package org.metaborg.spoofax.shell.functions;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.analysis.AnalysisException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.context.IContextService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalysisService;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzeResult;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test creating and using a {@link IReplCommand} created from the {@link AnalyzeFunction}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AnalyzeFunctionTest {
    private static final String DESCRIPTION = "analyze";

    // Constructor mocks
    @Mock private IContextService contextService;
    @Mock private IFunctionFactory functionFactory;
    @Mock private ISpoofaxAnalysisService analysisService;
    @Mock private IResultFactory resultFactory;

    @Mock private IProject project;
    @Mock private IContext context;
    @Mock private ILanguageImpl lang;

    @Mock private ISpoofaxAnalyzeUnit analyzeUnit;
    @Mock private ISpoofaxAnalyzeResult spoofaxAnalyzeResult;

    @Mock private InputResult inputResult;
    @Mock private ParseResult parseResult;
    @Mock private AnalyzeResult analyzeResult;

    @Mock private IResultVisitor visitor;
    @Captor private ArgumentCaptor<FailResult> failCaptor;
    @Captor private ArgumentCaptor<ISpoofaxResult<?>> resultCaptor;
    @Captor private ArgumentCaptor<Exception> exceptionCaptor;

    private IReplCommand analyzeCommand;

    /**
     * Set up mocks used in the test case.
     * @throws FileSystemException when resolving the temp file fails
     * @throws MetaborgException on unexpected Spoofax exceptions
     */
    @Before
    public void setup() throws FileSystemException, MetaborgException {
        FileObject sourceFile = VFS.getManager().resolveFile("ram://junit-temp");
        when(project.location()).thenReturn(sourceFile);

        AnalyzeFunction analyzeFunction = new AnalyzeFunction(contextService, analysisService,
                                                              resultFactory, project, lang);

        when(functionFactory.createInputFunction(any(), any())).thenReturn((input) ->
            FailOrSuccessResult.successful(inputResult)
        );
        when(functionFactory.createParseFunction(any(), any())).thenReturn((input) ->
            FailOrSuccessResult.successful(parseResult)
        );
        when(functionFactory.createAnalyzeFunction(any(), any())).thenReturn(analyzeFunction);

        FunctionComposer composer = new FunctionComposer(functionFactory, project, lang);
        when(functionFactory.createComposer(any(), any())).thenReturn(composer);

        when(parseResult.context()).thenReturn(Optional.empty());
        when(analyzeResult.unit()).thenReturn(analyzeUnit);
        when(resultFactory.createAnalyzeResult(any())).thenReturn(analyzeResult);

        when(contextService.get(any(), any(), any())).thenReturn(context);
        when(analysisService.analyze(any(), any())).thenReturn(spoofaxAnalyzeResult);

        analyzeCommand = new CommandBuilder<>(functionFactory, project, lang)
                .analyze().description(DESCRIPTION).build();
    }

    /**
     * Verify that the description of the command is correct.
     */
    @Test
    public void testDescription() {
        assertEquals(DESCRIPTION, analyzeCommand.description());
    }

    /**
     * Test creating a valid {@link AnalyzeResult}.
     * @throws MetaborgException on unexpected Spoofax exceptions
     */
    @Test
    public void testAnalyzeValid() throws MetaborgException {
        when(analyzeResult.valid()).thenReturn(true);

        IResult execute = analyzeCommand.execute("test");
        verify(contextService, times(1)).get(any(), any(), any());
        verify(resultFactory, times(1)).createAnalyzeResult(any());
        verify(analyzeResult, never()).accept(visitor);

        execute.accept(visitor);
        verify(analyzeResult, times(1)).accept(visitor);
    }

    /**
     * Test creating a valid {@link AnalyzeResult} with an existing {@link IContext}.
     * @throws MetaborgException on unexpected Spoofax exceptions
     */
    @Test
    public void testAnalyzeContext() throws MetaborgException {
        when(parseResult.context()).thenReturn(Optional.of(context));
        when(analyzeResult.valid()).thenReturn(true);

        IResult execute = analyzeCommand.execute("test");
        verify(contextService, never()).get(any(), any(), any());
        verify(resultFactory, times(1)).createAnalyzeResult(any());
        verify(analyzeResult, never()).accept(visitor);

        execute.accept(visitor);
        verify(analyzeResult, times(1)).accept(visitor);
    }

    /**
     * Test creating an invalid {@link AnalyzeResult}.
     * @throws MetaborgException on unexpected Spoofax exceptions
     */
    @Test
    public void testAnalyzeInvalid() throws MetaborgException {
        when(analyzeResult.valid()).thenReturn(false);

        IResult execute = analyzeCommand.execute("test");
        verify(visitor, never()).visitFailure(any());

        execute.accept(visitor);
        verify(visitor, times(1)).visitFailure(failCaptor.capture());
        assertEquals(analyzeResult, failCaptor.getValue().getCause());
    }

    /**
     * Test creating a {@link AnalyzeResult} resulting in an exception.
     * @throws MetaborgException on unexpected Spoofax exceptions
     */
    @Test
    public void testAnalyzeException() throws MetaborgException {
        AnalysisException analysisException = new AnalysisException(null);
        when(analysisService.analyze(any(), any())).thenThrow(analysisException);

        IResult execute = analyzeCommand.execute("test");
        verify(visitor, never()).visitException(any());

        execute.accept(visitor);
        verify(visitor, times(1)).visitException(exceptionCaptor.capture());
        assertEquals(analysisException, exceptionCaptor.getValue());
    }
}
