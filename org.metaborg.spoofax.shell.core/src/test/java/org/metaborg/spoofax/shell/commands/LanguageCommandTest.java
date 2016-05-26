package org.metaborg.spoofax.shell.commands;

import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeast;
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
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageDiscoveryRequest;
import org.metaborg.core.language.ILanguageDiscoveryService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.core.syntax.ParseException;
import org.metaborg.spoofax.core.analysis.AnalysisFacet;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.shell.invoker.ICommandFactory;
import org.metaborg.spoofax.shell.invoker.ICommandInvoker;
import org.metaborg.spoofax.shell.output.StyledText;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Lists;

/**
 * Test creating and using the {@link LanguageCommand}.
 */
@RunWith(MockitoJUnitRunner.class)
public class LanguageCommandTest {

    @Mock
    private IStrategoCommon common;
    @Mock
    private ILanguageDiscoveryService langDiscoveryService;
    @Mock
    private IResourceService resourceService;
    @Mock
    private ICommandInvoker invoker;
    @Mock
    private ICommandFactory commandFactory;
    @Mock
    private Consumer<StyledText> onSuccess;
    @Mock
    private Consumer<StyledText> onError;
    @Mock
    private IProject project;
    @Mock
    private ILanguageComponent langcomp;
    @Mock
    private ILanguageImpl lang;

    private FileObject langloc;

    /**
     * Set up mocks used in the test case.
     * @throws FileSystemException when resolving the temp file fails
     * @throws ParseException when parsing fails
     */
    @Before
    public void setup() throws FileSystemException, ParseException {
        langloc = VFS.getManager().resolveFile("res:paplj.full");
        when(invoker.getCommandFactory()).thenReturn(commandFactory);
        Mockito.<Iterable<? extends ILanguageImpl>>when(langcomp.contributesTo())
        .thenReturn(Lists.newArrayList(lang));
    }

    /**
     * Verify that the description of a command is never null.
     */
    @Test
    public void testDescription() {
        LanguageCommand langCommand = new LanguageCommand(common,
                                                          langDiscoveryService, resourceService,
                                                          invoker, onSuccess, onError, project);
        assertThat(langCommand.description(), isA(String.class));
    }

    /**
     * Test parsing source that results in a valid {@link ISpoofaxParseUnit}.
     * @throws MetaborgException when language discovery fails
     * @throws FileSystemException when the file could not be found
     */
    @Test(expected = MetaborgException.class)
    public void testLoadLanguageFail() throws MetaborgException, FileSystemException {
        Iterable<ILanguageDiscoveryRequest> langrequest = any();
        when(langDiscoveryService.discover(langrequest)).thenReturn(Lists.newArrayList());

        LanguageCommand langCommand = new LanguageCommand(common,
                                                          langDiscoveryService, resourceService,
                                                          invoker, onSuccess, onError, project);
        langCommand.load(langloc);
    }

    /**
     * Test parsing source that results in a valid {@link ISpoofaxParseUnit}.
     * @throws MetaborgException when language discovery fails
     * @throws FileSystemException when the file could not be found
     */
    @Test
    public void testLoadLanguage() throws MetaborgException, FileSystemException {
        Iterable<ILanguageDiscoveryRequest> langrequest = any();
        when(langDiscoveryService.discover(langrequest)).thenReturn(Lists.newArrayList(langcomp));

        LanguageCommand langCommand = new LanguageCommand(common,
                                                          langDiscoveryService, resourceService,
                                                          invoker, onSuccess, onError, project);
        ILanguageImpl actual = langCommand.load(langloc);

        verify(langDiscoveryService, times(1)).request(langloc);
        verify(langDiscoveryService, times(1)).discover(langrequest);
        assertEquals(lang, actual);
    }

    /**
     * Test execute with invalid input arguments.
     * @throws MetaborgException when language discovery fails
     */
    @Test
    public void testExecuteInvalidArgs() throws MetaborgException {
        Iterable<ILanguageDiscoveryRequest> langrequest = any();
        when(langDiscoveryService.discover(langrequest)).thenReturn(Lists.newArrayList(langcomp));

        LanguageCommand langCommand = new LanguageCommand(common,
                                                          langDiscoveryService, resourceService,
                                                          invoker, onSuccess, onError, project);
        langCommand.execute();
        verify(onError, times(1)).accept(any());

        langCommand.execute(new String[] { "", "" });
        verify(onError, times(2)).accept(any());
    }

    /**
     * Test execute with valid input arguments and without AnalysisFacet.
     * @throws MetaborgException when language discovery fails
     */
    @Test
    public void testExecute() throws MetaborgException {
        Iterable<ILanguageDiscoveryRequest> langrequest = any();
        when(langDiscoveryService.discover(langrequest)).thenReturn(Lists.newArrayList(langcomp));

        LanguageCommand langCommand = new LanguageCommand(common,
                                                          langDiscoveryService, resourceService,
                                                          invoker, onSuccess, onError, project);
        langCommand.execute("res:paplj.full");
        verify(invoker, times(1)).resetCommands();
        verify(invoker, atLeast(1)).addCommand(any(), any());
        verify(onSuccess, times(1)).accept(any());
    }

    /**
     * Test execute with valid input arguments and with AnalysisFacet.
     * @throws MetaborgException when language discovery fails
     */
    @Test
    public void testExecuteAnalyzed() throws MetaborgException {
        Iterable<ILanguageDiscoveryRequest> langrequest = any();
        when(langDiscoveryService.discover(langrequest)).thenReturn(Lists.newArrayList(langcomp));
        when(lang.hasFacet(AnalysisFacet.class)).thenReturn(true);

        LanguageCommand langCommand = new LanguageCommand(common,
                                                          langDiscoveryService, resourceService,
                                                          invoker, onSuccess, onError, project);
        langCommand.execute("res:paplj.full");
        verify(invoker, times(1)).resetCommands();
        verify(invoker, atLeast(1)).addCommand(any(), any());
        verify(onSuccess, times(1)).accept(any());
    }

}
