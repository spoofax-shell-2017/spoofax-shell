package org.metaborg.spoofax.shell.commands;

import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Consumer;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.core.syntax.ParseException;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.syntax.ISpoofaxSyntaxService;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.shell.output.IResultFactory;
import org.metaborg.spoofax.shell.output.StyledText;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test creating and using the {@link ParseCommand}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ParseCommandTest {
    @Mock private IStrategoCommon common;
    @Mock private ISpoofaxSyntaxService syntaxService;
    @Mock private IResultFactory unitFactory;
    @Mock private Consumer<StyledText> onSuccess;
    @Mock private Consumer<StyledText> onError;
    @Mock private IProject project;
    @Mock private ILanguageImpl lang;
    @Mock private ISpoofaxParseUnit parseUnit;

    private FileObject sourceFile;
    private ParseCommand parseCommand;

    /**
     * Set up mocks used in the test case.
     * @throws FileSystemException when resolving the temp file fails
     * @throws ParseException when parsing fails
     */
    @Before
    public void setup() throws FileSystemException, ParseException {
        sourceFile = VFS.getManager().resolveFile("ram://junit-temp");
        parseCommand = new ParseCommand(syntaxService, unitFactory,
                                        onSuccess, onError, project, lang);

        when(syntaxService.parse(any())).thenReturn(parseUnit);
        when(project.location()).thenReturn(sourceFile);
    }

    /**
     * Verify that the description of a command is never null.
     */
    @Test
    public void testDescription() {
        assertThat(parseCommand.description(), isA(String.class));
    }

    /**
     * Test parsing source that results in a valid {@link ISpoofaxParseUnit}.
     * @throws MetaborgException when the source contains invalid syntax
     */
    @Test
    public void testParseValid() throws MetaborgException {
        when(parseUnit.valid()).thenReturn(true);

        ISpoofaxParseUnit actual = parseCommand.parse();
        assertEquals(actual, parseUnit);
    }

    /**
     * Test parsing source that results in an invalid {@link ISpoofaxParseUnit}.
     * @throws MetaborgException when the source contains invalid syntax
     */
    @Test(expected = MetaborgException.class)
    public void testParseInvalid() throws MetaborgException {
        when(parseUnit.valid()).thenReturn(false);

        parseCommand.parse("test", sourceFile);
    }

    /**
     * Test the {@link ParseCommand} for source resulting in a valid {@link ISpoofaxParseUnit}.
     * @throws MetaborgException when the source contains invalid syntax
     */
    @Test
    public void testExecuteValid() throws MetaborgException {
        when(parseUnit.valid()).thenReturn(true);

        parseCommand.execute("test");
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
        when(parseUnit.valid()).thenReturn(false);

        parseCommand.execute("test");
        verify(onSuccess, never()).accept(any());
        verify(onError, times(1)).accept(any(StyledText.class));
    }
}
