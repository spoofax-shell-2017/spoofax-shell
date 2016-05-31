package org.metaborg.spoofax.shell.core;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.meta.lang.dynsem.interpreter.DynSemContext;
import org.metaborg.meta.lang.dynsem.interpreter.DynSemEntryPoint;
import org.metaborg.meta.lang.dynsem.interpreter.DynSemLanguage;
import org.metaborg.meta.lang.dynsem.interpreter.nodes.rules.RuleRegistry;
import org.metaborg.spoofax.core.shell.ShellFacet;
import org.metaborg.spoofax.shell.core.DynSemEvaluationStrategy.NonParser;

import com.oracle.truffle.api.vm.PolyglotEngine;

/**
 * Loads an interpreter from a jar archive.
 */
public class JarInterpreterLoader implements IInterpreterLoader {
    private NonParser nonParser;
    private IResourceService resourceService;

    /**
     * @param nonParser
     *            The {@link NonParser} to inject as configuration parameter to the VM Builder.
     * @param resourceService
     *            The resourceService for resolving the specification term file.
     */
    public JarInterpreterLoader(NonParser nonParser, IResourceService resourceService) {
        this.nonParser = nonParser;
        this.resourceService = resourceService;
    }

    @Override
    public PolyglotEngine loadInterpreterForLanguage(ILanguageImpl langImpl)
        throws InterpreterLoadException {
        ShellFacet shellFacet = langImpl.facet(ShellFacet.class);
        String interpreterJar = shellFacet.getInterpreterPath();
        Properties dynSemProperties = dynSemProperties(langImpl);

        DynSemLanguage language = getDynSemLanguageSingleton(dynSemProperties);
        // LANGUAGE must be set for constructing the RuleRegistry.
        DynSemContext.LANGUAGE = language;

        DynSemEntryPoint entryPoint = getEntryPoint(dynSemProperties);
        RuleRegistry ruleRegistry = getRuleRegistry(interpreterJar, dynSemProperties);

        String mimeType = entryPoint.getMimeType();
        return PolyglotEngine.newBuilder().config(mimeType, DynSemLanguage.PARSER, nonParser)
            .config(mimeType, DynSemLanguage.RULE_REGISTRY, ruleRegistry)
            .config(mimeType, DynSemLanguage.TERM_REGISTRY, entryPoint.getTermRegistry()).build();
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

    private String relativizeToJarPath(String interpreterJar, String pathString) {
        String baseNameString =
            FilenameUtils.getBaseName(pathString) + '.' + FilenameUtils.getExtension(pathString);
        String jarURI = "jar://" + interpreterJar + '!' + File.separator + baseNameString;
        return jarURI;
    }

    private FileObject getSpecificationFile(String interpreterJar, Properties dynSemProperties) {
        String tablePathString = dynSemProperties.getProperty("target.specterm");
        return resourceService.resolve(relativizeToJarPath(interpreterJar, tablePathString));
    }

    private RuleRegistry getRuleRegistry(String interpreterJar, Properties dynSemProperties)
        throws InterpreterLoadException {
        FileObject specificationFile = getSpecificationFile(interpreterJar, dynSemProperties);
        String ruleRegistryClass = dynSemProperties.getProperty("target.ruleregistry");
        try {
            InputStream specInput = specificationFile.getContent().getInputStream();
            if (ruleRegistryClass == null) {
                return new RuleRegistry(specInput);
            }
            Class<?> clazz = ClassUtils.getClass(ruleRegistryClass);
            return (RuleRegistry) ConstructorUtils.invokeConstructor(clazz, specInput);
        } catch (FileSystemException | InstantiationException | IllegalAccessException
                 | ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
            throw new InterpreterLoadException("Error constructing rule registry.");
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
