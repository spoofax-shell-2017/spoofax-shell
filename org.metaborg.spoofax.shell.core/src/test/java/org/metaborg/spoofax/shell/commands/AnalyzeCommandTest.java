package org.metaborg.spoofax.shell.commands;

import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.context.IContextService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalysisService;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzeResult;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.shell.hooks.IResultHook;
import org.metaborg.spoofax.shell.invoker.ICommandFactory;
import org.metaborg.spoofax.shell.output.AnalyzeResult;
import org.metaborg.spoofax.shell.output.IResultFactory;
import org.metaborg.spoofax.shell.output.ISpoofaxResult;
import org.metaborg.spoofax.shell.output.ParseResult;
import org.metaborg.spoofax.shell.output.StyledText;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test creating and using the {@link AnalyzeCommand}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AnalyzeCommandTest {
    // Constructor mocks
    @Mock private IContextService contextService;
    @Mock private ISpoofaxAnalysisService analysisService;
    @Mock private ICommandFactory commandFactory;
    @Mock private IResultFactory resultFactory;
    @Mock
    private IResultHook resultHook;
    @Mock private Consumer<StyledText> onError;
    @Mock private IProject project;
    @Mock private ILanguageImpl lang;

    @Mock private IContext context;

    @Mock private ParseCommand parseCommand;
    @Mock private ParseResult parseResult;
    @Mock private ISpoofaxAnalyzeResult spoofaxAnalyzeResult;
    @Mock private AnalyzeResult analyzeResult;

    private FileObject sourceFile;
    private AnalyzeCommand analyzeCommand;


    /**
     * Set up mocks used in the test case.
     * @throws FileSystemException when resolving the temp file fails
     * @throws MetaborgException when parsing fails
     */
    @Before
    public void setup() throws FileSystemException, MetaborgException {
        sourceFile = VFS.getManager().resolveFile("ram://junit-temp");

        when(project.location()).thenReturn(sourceFile);

        when(commandFactory.createParse(any(), any())).thenReturn(parseCommand);
        when(parseCommand.parse(any())).thenReturn(parseResult);
        when(parseResult.context()).thenReturn(Optional.of(context));

        when(analysisService.analyze(any(), any())).thenReturn(spoofaxAnalyzeResult);
        when(resultFactory.createAnalyzeResult(any())).thenReturn(analyzeResult);

        analyzeCommand = new AnalyzeCommand(contextService, analysisService,
                                            commandFactory, resultHook, resultFactory, project,
                                            lang);
    }

    /**
     * Verify that the description of a command is never null.
     */
    @Test
    public void testDescription() {
        assertThat(analyzeCommand.description(), isA(String.class));
    }

    /**
     * Test parsing source that results in a valid {@link ISpoofaxParseUnit}.
     * @throws MetaborgException whParseResulten the source contains invalid syntax
     * @throws IOException when reading from file fails
     */
    @Test
    public void testAnalyzeValid() throws MetaborgException {
        when(analyzeResult.valid()).thenReturn(true);

        AnalyzeResult actual = analyzeCommand.analyze(parseResult);
        assertEquals(actual, analyzeResult);
    }

    /**
     * Test parsing source that results in an invalid {@link ISpoofaxParseUnit}.
     * @throws MetaborgException when the source contains invalid syntax
     */
    @Test(expected = MetaborgException.class)
    public void testAnalyzeInvalid() throws MetaborgException {
        when(analyzeResult.valid()).thenReturn(false);

        analyzeCommand.analyze(parseResult);
    }
    /**
     * Test the {@link ParseCommand} for source resulting in a valid {@link ISpoofaxParseUnit}.
     * @throws MetaborgException when the source contains invalid syntax
     */
    @Test
    public void testExecuteValid() {
        when(analyzeResult.valid()).thenReturn(true);

        try {
            analyzeCommand.execute("test");
            verify(resultHook, times(1)).accept(any(ISpoofaxResult.class));
        } catch (MetaborgException e) {
            fail("Should not happen");
        }
    }

    /**
     * Test the {@link ParseCommand} for source resulting in a valid {@link ISpoofaxParseUnit}.
     * @throws MetaborgException when the source contains invalid syntax
     * @throws FileSystemException when the temporary file is not resolved
     */
    @Test(expected = MetaborgException.class)
    public void testExecuteInvalid() throws MetaborgException, FileSystemException {
        when(analyzeResult.valid()).thenReturn(false);

        analyzeCommand.execute("test");
        verify(resultHook, never()).accept(any());
    }
}
