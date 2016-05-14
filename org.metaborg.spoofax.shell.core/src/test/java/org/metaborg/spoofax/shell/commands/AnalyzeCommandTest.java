package org.metaborg.spoofax.shell.commands;

import static com.google.inject.Guice.createInjector;
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
//import org.metaborg.core.context.IContext;
import org.metaborg.core.messages.IMessage;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalysisService;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzeResult;
//import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.shell.core.CoreModule;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
//import org.spoofax.interpreter.terms.IStrategoAppl;
//import org.spoofax.interpreter.terms.ITermFactory;

import com.google.common.collect.Lists;
//import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

/**
 * Test creating and using the {@link AnalyzeCommand}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AnalyzeCommandTest {
//    private static final int PAPLJPROGRAMARITY = 3;
//    private static final int PAPLJCLASSARITY = 4;

    private Module testModule;
    private Module mockModule;

//    private IContext context;
//    private ITermFactoryService termService;

    @Mock private ISpoofaxAnalysisService analysisService;
    @Mock private ISpoofaxAnalyzeResult analyzeResult;
    @Mock private ISpoofaxAnalyzeUnit analyzeUnit;

    @Mock private FileContent sourceContent;
    @Mock private FileObject sourceFile;
    @Mock private FileName sourceName;
    @Mock private OutputStream output;

    /**
     * Set up initial Guice module and injector, inject Spoofax services and mock where needed.
     *
     * @throws FileSystemException
     *             When the contents of the sourceFile cannot be accessed.
     * @throws MetaborgException
     *             When parsing fails.
     */
    @Before
    public void setup() throws FileSystemException, MetaborgException {
        testModule = Modules.override(new CoreModule()).with(new TestCommandModule());
        mockModule = Modules.override(testModule)
                .with(e -> e.bind(ISpoofaxAnalysisService.class)
                            .toInstance(analysisService));

//        Injector injector = createInjector(testModule);
//        termService = injector.getInstance(ITermFactoryService.class);
//        context = injector.getProvider(IContext.class).get();

        Mockito.when(analysisService.analyze(any(), any())).thenReturn(analyzeResult);
        Mockito.when(analyzeResult.result()).thenReturn(analyzeUnit);
        Mockito.when(analyzeUnit.messages()).thenReturn(Lists.<IMessage>newArrayList());

        Mockito.when(sourceContent.getOutputStream()).thenReturn(output);
        Mockito.when(sourceFile.getContent()).thenReturn(sourceContent);
        Mockito.when(sourceFile.getName()).thenReturn(sourceName);
        Mockito.when(sourceName.getURI()).thenReturn("tmp://");
    }

    /**
     * Verify that the description of a command is never null.
     */
    @Test
    public void testDescription() {
        AnalyzeCommand instance = createInjector(mockModule).getInstance(AnalyzeCommand.class);
        assertNotNull(instance.description());
    }

    /**
     * Test analyzing and writing to a temporary file once.
     *
     * @throws IOException
     *             When the file could not be opened.
     * @throws MetaborgException
     *             When analyzing fails.
     */
    @Test(expected = MetaborgException.class)
    public void testAnalyzeOnce() throws IOException, MetaborgException {
        AnalyzeCommand instance = createInjector(mockModule).getInstance(AnalyzeCommand.class);
        instance.analyze("test", sourceFile);
        Mockito.verify(output, Mockito.times(1)).write("test".getBytes(Charset.forName("UTF-8")));
    }

    /**
     * Test analyzing and writing to a temporary file twice.
     *
     * @throws IOException
     *             When the file could not be opened.
     * @throws MetaborgException
     *             When analyzing fails.
     */
    @Test(expected = MetaborgException.class)
    public void testAnalyzeTwice() throws IOException, MetaborgException {
        AnalyzeCommand instance = createInjector(mockModule).getInstance(AnalyzeCommand.class);
        instance.analyze("test", sourceFile);
        Mockito.verify(output, Mockito.times(2)).write("test".getBytes(Charset.forName("UTF-8")));
    }

    /**
     * Test parsing and writing to an ast, without mock temp files..
     * @throws IOException when the file could not be opened
     * @throws MetaborgException when the file contains invalid syntax
     */
//    @Test
//    public void testAnalyzeAst() throws IOException, MetaborgException {
//        AnalyzeCommand instance = createInjector(testModule).getInstance(AnalyzeCommand.class);
//        ITermFactory tf = termService.get(context.language());
//        IStrategoAppl term = tf.makeAppl(
//            tf.makeConstructor("Program", PAPLJPROGRAMARITY),
//            tf.makeString("let"),
//            tf.makeList(),
//            tf.makeAppl(tf.makeConstructor("Num", 1), tf.makeString("3"))
//        );
//        assertEquals(term, instance.analyze("let x = 3", sourceFile));
//    }

    /**
     * Test parsing and writing to an ast, without mock temp files..
     * @throws IOException when the file could not be opened
     * @throws MetaborgException when the file contains invalid syntax
     */
//    @Test
//    public void testAnalyzePartialAst() throws IOException, MetaborgException {
//        AnalyzeCommand instance = createInjector(testModule).getInstance(AnalyzeCommand.class);
//        ITermFactory tf = termService.get(context.language());
//        IStrategoAppl term = tf.makeAppl(
//            tf.makeConstructor("Program", PAPLJPROGRAMARITY),
//            tf.makeString(""),
//            tf.makeList(
//                tf.makeAppl(
//                    tf.makeConstructor("Class", PAPLJCLASSARITY),
//                    tf.makeString("Fib"),
//                    tf.makeAppl(tf.makeConstructor("NoExtends", 0)),
//                    tf.makeList(),
//                    tf.makeList()
//                )
//            ),
//            tf.makeAppl(tf.makeConstructor("Num", 1), tf.makeString("3"))
//        );
//        String program = "class Fib { }\nrun 3";
//        assertEquals(term, instance.analyze(program, sourceFile).ast());
//    }
}