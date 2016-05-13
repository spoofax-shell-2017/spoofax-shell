package org.metaborg.spoofax.shell.commands;

import static com.google.inject.Guice.createInjector;
import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.messages.IMessage;
import org.metaborg.spoofax.core.syntax.ISpoofaxSyntaxService;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.shell.core.CoreModule;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.ITermFactory;

import com.google.common.collect.Lists;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

/**
 * Test creating and using the {@link ParseCommand}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ParseCommandTest {
    private static final int PAPLJPROGRAMARITY = 3;
    private static final int PAPLJCLASSARITY = 4;

    private Module testModule;
    private Module mockModule;

    private ITermFactoryService termService;

    @Mock private IContext context;
    @Mock private ISpoofaxSyntaxService syntaxService;
    @Mock private ISpoofaxParseUnit parseUnit;

    @Mock private FileObject sourceFile;
    @Mock private FileName sourceName;
    @Mock private FileContent sourceContent;
    @Mock private OutputStream output;

    /**
     * Set up initial Guice module and injector, inject Spoofax services and mock where needed.
     * @throws FileSystemException when the contents of the sourceFile cannot be accessed
     * @throws MetaborgException when parsing fails
     */
    @Before
    public void setup() throws FileSystemException, MetaborgException {
        testModule = Modules.override(new CoreModule()).with(new TestCommandModule());
        mockModule = Modules.override(testModule)
                .with(e -> {
                    e.bind(ISpoofaxSyntaxService.class)
                     .toInstance(syntaxService);
                    e.bind(IContext.class)
                     .toInstance(context);
                });

        Injector injector = createInjector(testModule);
        termService = injector.getInstance(ITermFactoryService.class);
        ILanguageImpl lang = injector.getProvider(ILanguageImpl.class).get();

        Mockito.when(syntaxService.parse(any())).thenReturn(parseUnit);
        Mockito.when(parseUnit.messages()).thenReturn(Lists.<IMessage>newArrayList());

        Mockito.when(context.language()).thenReturn(lang);
        Mockito.when(sourceFile.getContent()).thenReturn(sourceContent);
        Mockito.when(sourceContent.getOutputStream()).thenReturn(output);
        Mockito.when(sourceFile.getName()).thenReturn(sourceName);
    }

    /**
     * Verify that the description of a command is never null.
     */
    @Test
    public void testDescription() {
        ParseCommand instance = createInjector(mockModule).getInstance(ParseCommand.class);
        assertNotNull(instance.description());
    }

    /**
     * Test parsing and writing to a temp file once.
     * @throws IOException when the file could not be opened
     * @throws MetaborgException when the file contains invalid syntax
     */
    @Test(expected = MetaborgException.class)
    public void testParseOnce() throws IOException, MetaborgException {
        ParseCommand instance = createInjector(mockModule).getInstance(ParseCommand.class);
        instance.parse("test", sourceFile);
        Mockito.verify(output, Mockito.times(1)).write("test".getBytes(Charset.forName("UTF-8")));
    }

    /**
     * Test parsing and writing to a temp file twice.
     * @throws IOException when the file could not be opened
     * @throws MetaborgException when the file contains invalid syntax
     */
    @Test(expected = MetaborgException.class)
    public void testParseTwice() throws IOException, MetaborgException {
        ParseCommand instance = createInjector(mockModule).getInstance(ParseCommand.class);
        instance.parse("test", sourceFile);
        Mockito.verify(output, Mockito.times(2)).write("test".getBytes(Charset.forName("UTF-8")));
    }

    /**
     * Test parsing and writing to an ast, without mock temp files..
     * @throws IOException when the file could not be opened
     * @throws MetaborgException when the file contains invalid syntax
     */
    @Test
    public void testParseAst() throws IOException, MetaborgException {
        ParseCommand instance = createInjector(testModule).getInstance(ParseCommand.class);
        ITermFactory tf = termService.get(context.language());
        IStrategoAppl term = tf.makeAppl(
            tf.makeConstructor("Program", PAPLJPROGRAMARITY),
            tf.makeString("let"),
            tf.makeList(),
            tf.makeAppl(tf.makeConstructor("Num", 1), tf.makeString("3"))
        );
        assertEquals(term, instance.parse("let x = 3", sourceFile).ast());
    }

    /**
     * Test parsing and writing to an ast, without mock temp files..
     * @throws IOException when the file could not be opened
     * @throws MetaborgException when the file contains invalid syntax
     */
    @Test
    public void testParsePartialAst() throws IOException, MetaborgException {
        ParseCommand instance = createInjector(testModule).getInstance(ParseCommand.class);
        ITermFactory tf = termService.get(context.language());
        IStrategoAppl term = tf.makeAppl(
            tf.makeConstructor("Program", PAPLJPROGRAMARITY),
            tf.makeString(""),
            tf.makeList(
                tf.makeAppl(
                    tf.makeConstructor("Class", PAPLJCLASSARITY),
                    tf.makeString("Fib"),
                    tf.makeAppl(tf.makeConstructor("NoExtends", 0)),
                    tf.makeList(),
                    tf.makeList()
                )
            ),
            tf.makeAppl(tf.makeConstructor("Num", 1), tf.makeString("3"))
        );
        String program = "class Fib { }\nrun 3";
        assertEquals(term, instance.parse(program, sourceFile).ast());
    }

}
