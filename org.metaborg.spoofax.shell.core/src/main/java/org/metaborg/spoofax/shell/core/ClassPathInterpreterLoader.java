package org.metaborg.spoofax.shell.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.meta.lang.dynsem.interpreter.DynSemContext;
import org.metaborg.meta.lang.dynsem.interpreter.DynSemEntryPoint;
import org.metaborg.meta.lang.dynsem.interpreter.DynSemLanguage;
import org.metaborg.meta.lang.dynsem.interpreter.ITermRegistry;
import org.metaborg.meta.lang.dynsem.interpreter.nodes.rules.RuleRegistry;

import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.vm.PolyglotEngine;

/**
 * Loads an interpreter that is present in the class path. This {@link IInterpreterLoader} uses
 * reflection to load the generated {@link DynSemEntryPoint} and {@link DynSemLanguage} subclasses.
 * It instantiates a {@link PolyglotEngine} with the {@link NonParser} that is provided, by using
 * the supported {@link DynSemLanguage#PARSER configuration parameter}.
 */
public class ClassPathInterpreterLoader implements IInterpreterLoader {
    private String targetPackage;

    @Override
    public PolyglotEngine loadInterpreterForLanguage(ILanguageImpl langImpl)
        throws InterpreterLoadException {
        Properties dynSemProperties = dynSemProperties(langImpl);

        DynSemLanguage language = getDynSemLanguageSingleton(dynSemProperties);
        // LANGUAGE must be set for constructing the RuleRegistry.
        DynSemContext.LANGUAGE = language;

        DynSemEntryPoint entryPoint = getEntryPoint(dynSemProperties);

        targetPackage = dynSemProperties.getProperty("target.package");
        RuleRegistry ruleRegistry = entryPoint.getRuleRegistry();
        ITermRegistry termRegistry = entryPoint.getTermRegistry();

        String mimeType = entryPoint.getMimeType();
        PolyglotEngine builtEngine =
            PolyglotEngine.newBuilder().config(mimeType, DynSemLanguage.RULE_REGISTRY, ruleRegistry)
                .config(mimeType, DynSemLanguage.TERM_REGISTRY, termRegistry).build();
        try {
            builtEngine
                .eval(Source.fromReader(new InputStreamReader(entryPoint.getSpecificationTerm()),
                                        "Evaluate to interpreter.").withMimeType(mimeType));
        } catch (IOException e) {
            throw new InterpreterLoadException(e);
        }
        return builtEngine;
    }

    private DynSemLanguage getDynSemLanguageSingleton(Properties dynSemProperties)
        throws InterpreterLoadException {
        try {
            // passing null to Field#get means that it is a static field.
            return (DynSemLanguage) getGeneratedClass(dynSemProperties, "Language")
                .getField("INSTANCE").get(null);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException
                 | SecurityException e) {
            throw new InterpreterLoadException("Could not access singleton field \"INSTANCE\""
                                               + " of language entrypoint.");
        }
    }

    private DynSemEntryPoint getEntryPoint(Properties dynSemProperties)
        throws InterpreterLoadException {
        try {
            return (DynSemEntryPoint) getGeneratedClass(dynSemProperties, "EntryPoint")
                .newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new InterpreterLoadException(e);
        }
    }

    @Override
    public String getTargetPackage() {
        return targetPackage;
    }

    private Class<?> getGeneratedClass(Properties dynSemProperties, String className)
        throws InterpreterLoadException {
        String targetPackage = dynSemProperties.getProperty("target.package");
        String langName = dynSemProperties.getProperty("source.langname");
        try {
            return ClassUtils.getClass(targetPackage + "." + langName + className);
        } catch (ClassNotFoundException e) {
            throw new InterpreterLoadException(e);
        }
    }

    /*
     * Returns the dynsem.properties file parsed as a Properties object.
     */
    private Properties dynSemProperties(ILanguageImpl langImpl) throws InterpreterLoadException {
        FileObject dynSemPropertiesFile = null;
        for (FileObject fo : langImpl.locations()) {
            try {
                dynSemPropertiesFile = fo.getChild("dynsem.properties");
                if (dynSemPropertiesFile != null) {
                    break;
                }
            } catch (FileSystemException e) {
                // Keep looking.
            }
        }

        if (dynSemPropertiesFile == null) {
            throw new InterpreterLoadException("Missing \"dynsem.properties\" file");
        }

        Properties dynSemProperties = new Properties();
        InputStream in;
        try {
            in = dynSemPropertiesFile.getContent().getInputStream();
            dynSemProperties.load(in);
        } catch (Exception e) {
            throw new InterpreterLoadException("Error when trying to load \"dynsem.properties\".");
        }
        return dynSemProperties;
    }
}
