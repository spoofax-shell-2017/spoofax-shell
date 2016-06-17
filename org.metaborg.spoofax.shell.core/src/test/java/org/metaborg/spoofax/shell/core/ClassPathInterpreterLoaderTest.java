package org.metaborg.spoofax.shell.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.meta.lang.dynsem.interpreter.DynSemEntryPoint;
import org.metaborg.meta.lang.dynsem.interpreter.DynSemLanguage;
import org.metaborg.meta.lang.dynsem.interpreter.DynSemRule;
import org.metaborg.meta.lang.dynsem.interpreter.IDynSemLanguageParser;
import org.metaborg.meta.lang.dynsem.interpreter.ITermRegistry;
import org.metaborg.meta.lang.dynsem.interpreter.nodes.rules.RuleRegistry;
import org.metaborg.meta.lang.dynsem.interpreter.nodes.rules.RuleRoot;
import org.metaborg.meta.lang.dynsem.interpreter.terms.ITermTransformer;
import org.metaborg.spoofax.shell.core.IInterpreterLoader.InterpreterLoadException;
import org.metaborg.util.resource.FileSelectorUtils;
import org.mockito.Mockito;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.vm.PolyglotEngine;
import com.oracle.truffle.api.vm.PolyglotEngine.Value;

/**
 * Tests loading of a "generated" interpreter that is on the class path. The interpreter entry point
 * is defined in this test class.
 */
@RunWith(Parameterized.class)
public class ClassPathInterpreterLoaderTest {
    private static final String NO_EXCEPTION = "No exception should be thrown";
    protected static final String SPEC_TERM_CONSTANT = "specification term";
    protected final ClassPathInterpreterLoader cpInterpLoader = new ClassPathInterpreterLoader();
    protected static final RuleRoot MOCK_RULE_ROOT = Mockito.mock(RuleRoot.class);
    protected ILanguageImpl mockLangImpl;
    @SuppressWarnings("unused")
    private static final TestCPLoaderLanguage LANG = TestCPLoaderLanguage.INSTANCE;

    private static boolean reachedLanguageParse = false;
    private final Iterable<FileObject> langImplLocations;
    private final String expectedExceptionMessage;
    private final Class<?> expectedExceptionCauseClass;

    /**
     * @param langImplLocations
     *            The locations to stub {@link ILanguageImpl#locations()} with.
     * @param expectedExceptionMessage
     *            The expected {@link InterpreterLoadException} message if one is to be thrown, or
     *            null if the exception has an expected cause instead.
     * @param expectedExceptionCauseClass
     *            The expected cause for the {@link InterpreterLoadException}, or null if not used.
     */
    public ClassPathInterpreterLoaderTest(Supplier<Iterable<FileObject>> langImplLocations,
                                          String expectedExceptionMessage,
                                          Class<?> expectedExceptionCauseClass) {
        this.langImplLocations = langImplLocations.get();
        this.expectedExceptionMessage = expectedExceptionMessage;
        this.expectedExceptionCauseClass = expectedExceptionCauseClass;
    }

    /**
     * Set up mocks.
     */
    @Before
    public void setUp() {
        mockLangImpl = Mockito.mock(ILanguageImpl.class);
        Mockito.when(mockLangImpl.locations()).thenReturn(langImplLocations);
    }

    /**
     * Delete RAM file system cache.
     *
     * @throws FileSystemException
     *             When something goes wrong when deleting.
     */
    @After
    public void tearDown() throws FileSystemException {
        FileSystemManager manager = VFS.getManager();
        FileObject root = manager.resolveFile("ram:///");
        manager.getFilesCache().clear(root.getFileSystem());
        root.delete(FileSelectorUtils.all());
    }

    /**
     * @return The language locations to test against, to cover all branches.
     * @throws FileSystemException
     *             When file creation goes wrong.
     */
    @Parameters
    public static Collection<Object[]> languageLocationsParameters() throws FileSystemException {
        // Let each dynsem.properties (or lack thereof) be in separate, isolated directories.
        return Arrays.asList(new Object[][] {
                                              { goodWeatherLocations(VFS.getManager()
                                                  .resolveFile("ram:///rootDirOne/")),
                                                NO_EXCEPTION,
                                                null },
                                              { missingDynSemProperties(VFS.getManager()
                                                  .resolveFile("ram:///rootDirTwo/")),
                                                "Missing \"dynsem.properties\" file",
                                                null },
                                              { multipleLocations(VFS.getManager()
                                                  .resolveFile("ram:///rootDirThree/")),
                                                NO_EXCEPTION,
                                                null },
                                              { badWeatherLocations(VFS.getManager()
                                                  .resolveFile("ram:///rootDirFour/")),
                                                null, ClassNotFoundException.class } });

    }

    private static Supplier<List<FileObject>> goodWeatherLocations(FileObject rootDir) {
        return () -> {
            try {
                rootDir.createFolder();
                FileObject dynSemProperties = rootDir.resolveFile("dynsem.properties");
                dynSemProperties.createFile();
                PrintWriter printWriter =
                    new PrintWriter(dynSemProperties.getContent().getOutputStream());
                printWriter.println("project.javapackage = org.metaborg.spoofax.shell.core");
                printWriter
                    .println("source.langname = ClassPathInterpreterLoaderTest$TestCPLoader");
                printWriter.flush();
                printWriter.close();
                dynSemProperties.close();
                List<FileObject> res = new ArrayList<>(1);
                res.add(rootDir);
                return res;
            } catch (FileSystemException e) {
                fail("Should not happen.");
            }
            return null;
        };
    }

    private static Supplier<List<FileObject>> missingDynSemProperties(FileObject rootDir) {
        return () -> {
            try {
                rootDir.createFolder();
                FileObject noDynSemProperties = rootDir.resolveFile("nodynsem.properties");
                noDynSemProperties.createFile();
                PrintWriter printWriter =
                    new PrintWriter(noDynSemProperties.getContent().getOutputStream());
                printWriter.println("blabla");
                printWriter.flush();
                printWriter.close();
                noDynSemProperties.close();
                List<FileObject> res = new ArrayList<>(1);
                res.add(rootDir);
                return res;
            } catch (FileSystemException e) {
                fail("Should not happen.");
            }
            return null;
        };
    }

    private static Supplier<Iterable<FileObject>> multipleLocations(FileObject rootDir) {
        return () -> {
            try {
                rootDir.createFolder();
                FileObject someRootDir = rootDir.resolveFile("someRootDir/");
                someRootDir.createFolder();
                FileObject anotherRootDir = rootDir.resolveFile("anotherRootDir/");
                anotherRootDir.createFolder();
                List<FileObject> missingDynSemProperties =
                    missingDynSemProperties(anotherRootDir).get();
                List<FileObject> goodWeatherLocations = goodWeatherLocations(someRootDir).get();
                missingDynSemProperties.addAll(goodWeatherLocations);

                return missingDynSemProperties;
            } catch (FileSystemException e) {
                fail("Should not happen.");
            }
            return null;
        };
    }

    private static Supplier<List<FileObject>> badWeatherLocations(FileObject rootDir) {
        return () -> {
            try {
                rootDir.createFolder();
                FileObject dynSemProperties = rootDir.resolveFile("dynsem.properties");
                dynSemProperties.createFile();
                PrintWriter printWriter =
                    new PrintWriter(dynSemProperties.getContent().getOutputStream());
                printWriter.println("project.javapackage = non.existing.package");
                printWriter.println("source.langname = NonExistingLanguage");
                printWriter.flush();
                printWriter.close();
                dynSemProperties.close();
                List<FileObject> res = new ArrayList<>(1);
                res.add(rootDir);
                return res;
            } catch (FileSystemException e) {
                fail("Should not happen.");
            }
            return null;
        };
    }

    /**
     * Tests loading the interpreter.
     *
     * @throws Exception
     *             when loading the interpreter throws one.
     */
    // Method length is ignored because this is just a test.
    // CHECKSTYLE.OFF: MethodLength
    @Test
    public void testLoadInterpreterForLanguage() throws Exception {
        try {
            // This should load the language, finding its canonical class name through the mocked
            // dynsem.properties initialized above.
            PolyglotEngine engine = cpInterpLoader.loadInterpreterForLanguage(mockLangImpl);

            // Test whether the test should not throw an exception, fail otherwise.
            assertEquals(expectedExceptionMessage, NO_EXCEPTION);
            assertNull(expectedExceptionCauseClass);

            // Test finding a rule.
            Value testRuleValue =
                engine.findGlobalSymbol(RuleRegistry.makeKey("testrule", "TestCtor", 0));
            DynSemRule testRule = testRuleValue.as(DynSemRule.class);

            assertEquals(testRule.getRuleTarget(), MOCK_RULE_ROOT);

            // Assert that the parse method of the test language class was reached.
            assertTrue(reachedLanguageParse);
        } catch (IOException e) {
            throw new Exception(e);
        } catch (InterpreterLoadException e) {
            if (expectedExceptionMessage != null) {
                assertEquals(expectedExceptionMessage, e.getMessage());
            } else {
                assertNotNull(expectedExceptionCauseClass);
                assertEquals(expectedExceptionCauseClass, e.getCause().getClass());
            }
        }
    }
    // CHECKSTYLE.ON: MethodLength

    /**
     * Test {@link DynSemEntryPoint}.
     */
    public static final class TestCPLoaderEntryPoint extends DynSemEntryPoint {

        /**
         * Inject mocked and stubbed dependencies.
         */
        public TestCPLoaderEntryPoint() {
            super(mockParser(), mockTransformer(), mockTermRegistry(), mockRuleRegistry());
        }

        private static IDynSemLanguageParser mockParser() {
            return null;
        }

        private static ITermTransformer mockTransformer() {
            return null;
        }

        private static ITermRegistry mockTermRegistry() {
            return null;
        }

        private static RuleRegistry mockRuleRegistry() {
            RuleRegistry mock = Mockito.mock(RuleRegistry.class);
            Mockito.when(mock.lookupRule("testrule", "TestCtor", 0)).thenReturn(MOCK_RULE_ROOT);
            return mock;
        }

        @Override
        public String getMimeType() {
            return "application/x-test-cp-loader-lang";
        }

        @Override
        public InputStream getSpecificationTerm() {
            // Specification term is just a test string to assert equality.
            return new ByteArrayInputStream(SPEC_TERM_CONSTANT.getBytes(Charset.forName("UTF-8")));
        }
    }

    /**
     * Test {@link TruffleLanguage}.
     */
    @TruffleLanguage.Registration(name = "TestDynSemLang", version = "0.1",
        mimeType = "application/x-test-cp-loader-lang")
    public static final class TestCPLoaderLanguage extends DynSemLanguage {
        public static final TestCPLoaderLanguage INSTANCE = new TestCPLoaderLanguage();

        @Override
        protected CallTarget parse(Source code, Node context, String... argumentNames)
            throws IOException {

            // Test that the specification term is passed correctly.
            assertTrue(code.getCode().equals(SPEC_TERM_CONSTANT));
            // Set boolean flag so that it can be asserted that this method was reached.
            reachedLanguageParse = true;

            return Truffle.getRuntime()
                .createCallTarget(new RootNode(TestCPLoaderLanguage.class, null, null) {
                    @Override
                    public Object execute(VirtualFrame frame) {
                        return null;
                    }
                });
        }

        @Override
        public boolean isSafeComponentsEnabled() {
            return false;
        }
    }
}
