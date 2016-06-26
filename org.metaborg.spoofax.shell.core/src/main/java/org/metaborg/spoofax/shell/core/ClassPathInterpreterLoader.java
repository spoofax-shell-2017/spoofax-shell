package org.metaborg.spoofax.shell.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.meta.lang.dynsem.interpreter.DynSemEntryPoint;
import org.metaborg.meta.lang.dynsem.interpreter.DynSemLanguage;
import org.metaborg.meta.lang.dynsem.interpreter.IDynSemLanguageParser;
import org.metaborg.meta.lang.dynsem.interpreter.ITermRegistry;
import org.metaborg.meta.lang.dynsem.interpreter.nodes.rules.RuleRegistry;
import org.metaborg.meta.lang.dynsem.interpreter.terms.ITerm;
import org.metaborg.meta.lang.dynsem.interpreter.terms.ITermTransformer;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoConstructor;

import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.vm.PolyglotEngine;

/**
 * Loads an interpreter that is present in the class path. This {@link IInterpreterLoader} uses
 * reflection to load the generated {@link DynSemEntryPoint} subclass. It instantiates a
 * {@link PolyglotEngine} and initializes the interpreter by evaluating the DynSem specification
 * term.
 */
public class ClassPathInterpreterLoader implements IInterpreterLoader {
    private DynSemEntryPoint entryPoint;

    @Override
    public PolyglotEngine loadInterpreterForLanguage(ILanguageImpl langImpl)
            throws InterpreterLoadException {
        entryPoint = getEntryPoint(loadDynSemProperties(langImpl));

        IDynSemLanguageParser parser = entryPoint.getParser();
        RuleRegistry ruleRegistry = entryPoint.getRuleRegistry();
        ITermRegistry termRegistry = entryPoint.getTermRegistry();
        String mimeType = entryPoint.getMimeType();

        PolyglotEngine builtEngine =
            PolyglotEngine.newBuilder().config(mimeType, DynSemLanguage.PARSER, parser)
                .config(mimeType, DynSemLanguage.RULE_REGISTRY, ruleRegistry)
                .config(mimeType, DynSemLanguage.TERM_REGISTRY, termRegistry).build();
        try {
            InputStreamReader specTermReader =
                new InputStreamReader(entryPoint.getSpecificationTerm(), "UTF-8");
            builtEngine.eval(Source.fromReader(specTermReader, "Evaluate to interpreter.")
                .withMimeType(mimeType));
        } catch (IOException e) {
            throw new InterpreterLoadException(e);
        }
        return builtEngine;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ITerm getProgramTerm(IStrategoAppl appl) throws InterpreterLoadException {
        IStrategoConstructor constructor = appl.getConstructor();
        try {
            // Get the generated class of the term.
            Class<? extends ITerm> generatedTermClass =
                    (Class<? extends ITerm>) entryPoint.getTermRegistry()
                    .getConstructorClass(constructor.getName(),
                                         constructor.getArity());
            return (ITerm) MethodUtils.invokeStaticMethod(generatedTermClass, "create", appl);
        } catch (ReflectiveOperationException e) {
            throw new InterpreterLoadException("Error constructing program term from input.", e);
        }
    }

    @Override
    public ITermTransformer getTransformer() {
        return entryPoint.getTransformer();
    }

    @SuppressWarnings("unchecked")
    private static DynSemEntryPoint getEntryPoint(Properties props)
        throws InterpreterLoadException {
        try {
            String className = targetPackage(props) + "." + langName(props) + "EntryPoint";
            Class<DynSemEntryPoint> entryPointClass =
                (Class<DynSemEntryPoint>) ClassUtils.getClass(className);
            return ConstructorUtils.invokeConstructor(entryPointClass);
        } catch (ReflectiveOperationException e) {
            throw new InterpreterLoadException(e);
        }
    }

    private static String langName(Properties props) {
        return props.getProperty("source.langname");
    }

    private static String targetPackage(Properties props) {
        String groupId = props.getProperty("project.groupid");
        String artifactId = props.getProperty("project.artifactid");

        return props.getProperty("project.javapackage", groupId + '.' + artifactId + ".generated");
    }

    /* Loads the required configurations from the dynsem.properties file parsed as a Properties
     * object. */
    private static Properties loadDynSemProperties(ILanguageImpl langImpl)
            throws InterpreterLoadException {
        FileObject dynSemPropertiesFile = findDynSemPropertiesFileForLanguage(langImpl);
        Properties dynSemProperties = new Properties();
        try (InputStream in = dynSemPropertiesFile.getContent().getInputStream()) {
            dynSemProperties.load(in);
        } catch (IOException e) {
            throw new InterpreterLoadException("Error when trying to load \"dynsem.properties\".");
        }

        return dynSemProperties;
    }

    private static FileObject findDynSemPropertiesFileForLanguage(ILanguageImpl langImpl)
            throws InterpreterLoadException {
        FileObject dynSemPropertiesFile = null;
        for (FileObject fo : langImpl.locations()) {
            try {
                dynSemPropertiesFile = fo.getChild("dynsem.properties");
                if (dynSemPropertiesFile != null) {
                    break;
                }
            } catch (FileSystemException e) {
                continue;
            }
        }

        if (dynSemPropertiesFile == null) {
            throw new InterpreterLoadException("Missing \"dynsem.properties\" file");
        }
        return dynSemPropertiesFile;
    }
}
