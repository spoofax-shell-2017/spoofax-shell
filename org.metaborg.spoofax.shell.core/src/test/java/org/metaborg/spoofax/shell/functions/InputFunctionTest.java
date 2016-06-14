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
import org.metaborg.spoofax.core.shell.ShellFacet;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;
import org.metaborg.spoofax.shell.client.IResult;
import org.metaborg.spoofax.shell.client.IResultVisitor;
import org.metaborg.spoofax.shell.commands.CommandBuilder;
import org.metaborg.spoofax.shell.commands.IReplCommand;
import org.metaborg.spoofax.shell.output.FailResult;
import org.metaborg.spoofax.shell.output.IResultFactory;
import org.metaborg.spoofax.shell.output.ISpoofaxResult;
import org.metaborg.spoofax.shell.output.InputResult;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test creating and using a {@link IReplCommand} created from the {@link InputFunction}.
 */
@RunWith(MockitoJUnitRunner.class)
public class InputFunctionTest {
    private static final String DESCRIPTION = "input";

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

    @Mock private IResultVisitor visitor;
    @Captor private ArgumentCaptor<FailResult> failCaptor;
    @Captor private ArgumentCaptor<ISpoofaxResult<?>> resultCaptor;
    @Captor private ArgumentCaptor<Exception> exceptionCaptor;

    private FileObject sourceFile;
    private IReplCommand inputCommand;

    /**
     * Set up mocks used in the test case.
     * @throws FileSystemException when resolving the temp file fails
     */
    @Before
    public void setup() throws FileSystemException {
        sourceFile = VFS.getManager().resolveFile("ram://junit-temp");
        when(project.location()).thenReturn(sourceFile);

        InputFunction inputFunction = new InputFunction(resultFactory, project, lang);

        when(functionFactory.createInputFunction(any(), any())).thenReturn(inputFunction);

        when(inputResult.unit()).thenReturn(inputUnit);
        when(resultFactory.createInputResult(any(), any(), any(), any())).thenReturn(inputResult);

        when(lang.facet(ShellFacet.class)).thenReturn(facet);

        inputCommand = new CommandBuilder<>(functionFactory, project, lang)
                .input().description(DESCRIPTION).build();
    }

    /**
     * Verify that the description of the command is correct.
     */
    @Test
    public void testDescription() {
        assertEquals(DESCRIPTION, inputCommand.description());
    }

    /**
     * Test creating a valid {@link InputResult}.
     * @throws MetaborgException on unexpected Spoofax exceptions
     */
    @Test
    public void testInputValid() throws MetaborgException {
        when(inputResult.valid()).thenReturn(true);

        IResult execute = inputCommand.execute("test");
        verify(resultFactory, times(1)).createInputResult(any(), any(), any(), any());
        verify(inputResult, never()).accept(visitor);

        execute.accept(visitor);
        verify(inputResult, times(1)).accept(visitor);
    }

    /**
     * Test creating an invalid {@link InputResult}.
     * @throws MetaborgException when the source contains invalid syntax
     */
    @Test
    public void testInputInvalid() throws MetaborgException {
        when(inputResult.valid()).thenReturn(false);

        IResult execute = inputCommand.execute("test");
        verify(visitor, never()).visitFailure(any());

        execute.accept(visitor);
        verify(visitor, times(1)).visitFailure(failCaptor.capture());
        assertEquals(inputResult, failCaptor.getValue().getCause());
    }

}
