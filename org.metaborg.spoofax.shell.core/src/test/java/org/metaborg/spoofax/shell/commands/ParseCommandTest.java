package org.metaborg.spoofax.shell.commands;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.metaborg.spoofax.core.shell.ShellFacet;
import org.metaborg.spoofax.core.syntax.ISpoofaxSyntaxService;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;
import org.metaborg.spoofax.shell.client.IResult;
import org.metaborg.spoofax.shell.client.IResultVisitor;
import org.metaborg.spoofax.shell.functions.IFunctionFactory;
import org.metaborg.spoofax.shell.functions.InputFunction;
import org.metaborg.spoofax.shell.functions.ParseFunction;
import org.metaborg.spoofax.shell.output.FailOrSuccessResult;
import org.metaborg.spoofax.shell.output.FailResult;
import org.metaborg.spoofax.shell.output.IResultFactory;
import org.metaborg.spoofax.shell.output.ISpoofaxResult;
import org.metaborg.spoofax.shell.output.InputResult;
import org.metaborg.spoofax.shell.output.ParseResult;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test creating and using the {@link ParseCommand}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ParseCommandTest {
    private static final String DESCRIPTION = "parse";

    @Mock private ISpoofaxSyntaxService syntaxService;
    @Mock private ISpoofaxUnitService unitService;
    @Mock private IFunctionFactory functionFactory;
    @Mock private IResultFactory resultFactory;

    @Mock private IProject project;
    @Mock private ILanguageImpl lang;
    @Mock private ShellFacet facet;

    @Mock private ISpoofaxInputUnit inputUnit;
    @Mock private ISpoofaxParseUnit parseUnit;

    @Mock private InputFunction inputFunction;
    @Mock private InputResult inputResult;
    @Mock private ParseResult parseResult;

    @Mock private IResultVisitor visitor;
    @Captor private ArgumentCaptor<FailResult> failCaptor;
    @Captor private ArgumentCaptor<ISpoofaxResult<?>> resultCaptor;
    @Captor private ArgumentCaptor<Exception> exceptionCaptor;

    private FileObject sourceFile;
    private IReplCommand parseCommand;

    /**
     * Set up mocks used in the test case.
     * @throws FileSystemException when resolving the temp file fails
     * @throws ParseException when parsing fails
     */
    @Before
    public void setup() throws FileSystemException, ParseException {
        sourceFile = VFS.getManager().resolveFile("ram://junit-temp");
        when(project.location()).thenReturn(sourceFile);

        ParseFunction parseFunction = new ParseFunction(syntaxService, unitService,
                                                        resultFactory, project, lang);

        when(functionFactory.createInputFunction(any(), any())).thenReturn((input) ->
            FailOrSuccessResult.successful(inputResult)
        );
        when(functionFactory.createParseFunction(any(), any())).thenReturn(parseFunction);

        when(inputResult.unit()).thenReturn(inputUnit);
        when(parseResult.unit()).thenReturn(parseUnit);
        when(resultFactory.createParseResult(any())).thenReturn(parseResult);

        when(syntaxService.parse(any())).thenReturn(parseUnit);
        when(lang.facet(ShellFacet.class)).thenReturn(facet);

        parseCommand = new CommandBuilder<>(functionFactory, project, lang)
                .parse().description(DESCRIPTION).build();
    }

    /**
     * Verify that the description of a command is never null.
     */
    @Test
    public void testDescription() {
        assertEquals(DESCRIPTION, parseCommand.description());
    }

    /**
     * Test parsing source that results in a valid {@link ISpoofaxParseUnit}.
     * @throws MetaborgException when the source contains invalid syntax
     */
    @Test
    public void testParseValid() throws MetaborgException {
        when(parseResult.valid()).thenReturn(true);

        IResult execute = parseCommand.execute("test");
        verify(resultFactory, times(1)).createParseResult(any());
        verify(parseResult, times(0)).accept(visitor);

        execute.accept(visitor);
        verify(parseResult, times(1)).accept(visitor);
    }

    /**
     * Test parsing source that results in an invalid {@link ISpoofaxParseUnit}.
     * @throws MetaborgException when the source contains invalid syntax
     */
    @Test
    public void testParseInvalid() throws MetaborgException {
        when(parseResult.valid()).thenReturn(false);

        IResult execute = parseCommand.execute("test");
        verify(visitor, times(0)).visitFailure(any());

        execute.accept(visitor);
        verify(visitor, times(1)).visitFailure(failCaptor.capture());
        assertEquals(parseResult, failCaptor.getValue().getCause());
    }

    /**
     * Test the {@link ParseCommand} for source resulting in a valid {@link ISpoofaxParseUnit}.
     * @throws MetaborgException when the source contains invalid syntax
     */
    @Test
    public void testParseException() throws MetaborgException {
        ParseException parseException = new ParseException(null);
        when(syntaxService.parse(any())).thenThrow(parseException);

        IResult execute = parseCommand.execute("test");
        verify(visitor, times(0)).visitException(any());

        execute.accept(visitor);
        verify(visitor, times(1)).visitException(exceptionCaptor.capture());
        assertEquals(parseException, exceptionCaptor.getValue());
    }
}
