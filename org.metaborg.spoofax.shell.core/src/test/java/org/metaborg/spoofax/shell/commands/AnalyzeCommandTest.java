package org.metaborg.spoofax.shell.commands;

import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
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
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalysisService;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzeResult;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.shell.core.StyledText;
import org.metaborg.spoofax.shell.invoker.ICommandFactory;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Lists;

/**
 * Test creating and using the {@link AnalyzeCommand}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AnalyzeCommandTest {
    @Mock private IStrategoCommon common;
    @Mock private IContextService contextService;
    @Mock private ISpoofaxAnalysisService analysisService;
    @Mock private ICommandFactory commandFactory;
    @Mock private Consumer<StyledText> onSuccess;
    @Mock private Consumer<StyledText> onError;
    @Mock private IProject project;
    @Mock private IContext context;
    @Mock private ILanguageImpl lang;

    @Mock private ParseCommand parseCommand;
    @Mock private ISpoofaxParseUnit parseUnit;
    @Mock private ISpoofaxAnalyzeResult analyzeResult;
    @Mock private ISpoofaxAnalyzeUnit analyzeUnit;

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

        when(contextService.get(any(), any(), any())).thenReturn(context);
        when(analysisService.analyze(any(), any())).thenReturn(analyzeResult);
        when(commandFactory.createParse(any(), any())).thenReturn(parseCommand);

        when(project.location()).thenReturn(sourceFile);
        when(parseCommand.parse(any(), any())).thenReturn(parseUnit);

        when(analyzeResult.result()).thenReturn(analyzeUnit);
        when(analyzeUnit.messages()).thenReturn(Lists.<IMessage>newArrayList());

        analyzeCommand = new AnalyzeCommand(common, contextService, analysisService,
                                            commandFactory, onSuccess, onError, project, lang);

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
     * @throws MetaborgException when the source contains invalid syntax
     * @throws IOException when reading from file fails
     */
    @Test
    public void testParseValid() throws MetaborgException {
        when(analyzeUnit.valid()).thenReturn(true);

        ISpoofaxAnalyzeUnit actual = analyzeCommand.analyze("test", sourceFile);
        assertEquals(actual, analyzeUnit);
    }

    /**
     * Test parsing source that results in an invalid {@link ISpoofaxParseUnit}.
     * @throws MetaborgException when the source contains invalid syntax
     */
    @Test(expected = MetaborgException.class)
    public void testParseInvalid() throws MetaborgException {
        when(analyzeUnit.valid()).thenReturn(false);

        analyzeCommand.analyze("test", sourceFile);
    }
    /**
     * Test the {@link ParseCommand} for source resulting in a valid {@link ISpoofaxParseUnit}.
     * @throws MetaborgException when the source contains invalid syntax
     */
    @Test
    public void testExecuteValid() throws MetaborgException {
        when(analyzeUnit.valid()).thenReturn(true);

        analyzeCommand.execute("test");
        verify(onSuccess, times(1)).accept(any(StyledText.class));
        verify(onError, never()).accept(any());
    }

    /**
     * Test the {@link ParseCommand} for source resulting in a valid {@link ISpoofaxParseUnit}.
     * @throws MetaborgException when the source contains invalid syntax
     * @throws FileSystemException when the temporary file is not resolved
     */
    @Test
    public void testExecuteInvalid() throws MetaborgException, FileSystemException {
        when(analyzeUnit.valid()).thenReturn(false);

        analyzeCommand.execute("test");
        verify(onSuccess, never()).accept(any());
        verify(onError, times(1)).accept(any(StyledText.class));
    }
}
