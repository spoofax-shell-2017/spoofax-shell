package org.metaborg.spoofax.shell.functions;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
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
import org.metaborg.spoofax.core.syntax.ISpoofaxSyntaxService;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;
import org.metaborg.spoofax.shell.commands.CommandBuilder;
import org.metaborg.spoofax.shell.commands.IReplCommand;
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
 * Test creating and using a {@link IReplCommand} created from the {@link ParseFunction}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ParseFunctionTest {
    private static final String DESCRIPTION = "parse";

    @Mock private ISpoofaxSyntaxService syntaxService;
    @Mock private ISpoofaxUnitService unitService;
    @Mock private IFunctionFactory functionFactory;
    @Mock private IResultFactory resultFactory;

    @Mock private IProject project;
    @Mock private ILanguageImpl lang;

    @Mock private ISpoofaxInputUnit inputUnit;
    @Mock private ISpoofaxParseUnit parseUnit;

    @Mock private InputFunction inputFunction;
    @Mock private InputResult inputResult;
    @Mock private ParseResult parseResult;

    @Mock private IResultVisitor visitor;
    @Captor private ArgumentCaptor<FailResult> failCaptor;
    @Captor private ArgumentCaptor<ISpoofaxResult<?>> resultCaptor;
    @Captor private ArgumentCaptor<Exception> exceptionCaptor;

    private IReplCommand parseCommand;

    /**
     * Set up mocks used in the test case.
     * @throws FileSystemException when resolving the temp file fails
     * @throws ParseException on unexpected Spoofax exceptions
     */
    @Before
    public void setup() throws FileSystemException, ParseException {
        FileObject sourceFile = VFS.getManager().resolveFile("ram://junit-temp");
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

        parseCommand = new CommandBuilder<>(functionFactory, project, lang)
                .parse().description(DESCRIPTION).build();
    }

    /**
     * Verify that the description of the command is correct.
     */
    @Test
    public void testDescription() {
        assertEquals(DESCRIPTION, parseCommand.description());
    }

    /**
     * Test creating a valid {@link ParseResult}.
     * @throws MetaborgException on unexpected Spoofax exceptions
     */
    @Test
    public void testParseValid() throws MetaborgException {
        when(parseResult.valid()).thenReturn(true);

        IResult execute = parseCommand.execute("test");
        verify(resultFactory, times(1)).createParseResult(any());
        verify(parseResult, never()).accept(visitor);

        execute.accept(visitor);
        verify(parseResult, times(1)).accept(visitor);
    }

    /**
     * Test creating an invalid {@link ParseResult}.
     * @throws MetaborgException on unexpected Spoofax exceptions
     */
    @Test
    public void testParseInvalid() throws MetaborgException {
        when(parseResult.valid()).thenReturn(false);

        IResult execute = parseCommand.execute("test");
        verify(visitor, never()).visitFailure(any());

        execute.accept(visitor);
        verify(visitor, times(1)).visitFailure(failCaptor.capture());
        assertEquals(parseResult, failCaptor.getValue().getCause());
    }

    /**
     * Test creating a {@link ParseResult} resulting in an exception.
     * @throws MetaborgException on unexpected Spoofax exceptions
     */
    @Test
    public void testParseException() throws MetaborgException {
        ParseException parseException = new ParseException(null);
        when(syntaxService.parse(any())).thenThrow(parseException);

        IResult execute = parseCommand.execute("test");
        verify(visitor, never()).visitException(any());

        execute.accept(visitor);
        verify(visitor, times(1)).visitException(exceptionCaptor.capture());
        assertEquals(parseException, exceptionCaptor.getValue());
    }
}
