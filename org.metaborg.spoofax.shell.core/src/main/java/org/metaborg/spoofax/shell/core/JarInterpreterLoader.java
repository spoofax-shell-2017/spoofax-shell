package org.metaborg.spoofax.shell.core;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.meta.lang.dynsem.interpreter.DynSemLanguage;
import org.metaborg.spoofax.core.shell.ShellFacet;

import com.google.inject.Inject;

/**
 * Loads an interpreter from a jar archive.
 */
public class JarInterpreterLoader implements IInterpreterLoader {
    @Inject
    private IResourceService resourceService;
    // Holds the interpreter package containing the generated Java classes.
    private String targetPackage;

    @Override
    public DynSemLanguage loadInterpreterForLanguage(ILanguageImpl langImpl)
        throws InterpreterLoadException {

        ShellFacet shellFacet = langImpl.facet(ShellFacet.class);
        String interpreterJar = shellFacet.getInterpreterPath();
        Properties dynSemProperties = dynSemProperties(langImpl);

        Class<DynSemLanguage> languageClass = getLanguageClass(interpreterJar, dynSemProperties);
        DynSemLanguage language = getDynSemLanguageSingleton(languageClass);

        FileObject specificationFile = getSpecificationFile(interpreterJar, dynSemProperties);
        FileObject parseTableFile = getParseTableFile(interpreterJar, dynSemProperties);
        try {
            language.setParseTableInput(parseTableFile.getContent().getInputStream());
        } catch (FileSystemException e) {
            throw new InterpreterLoadException("Error getting file contents for parse table.");
        }
        try {
            language.setSpecificationInput(specificationFile.getContent().getInputStream());
        } catch (FileSystemException e) {
            throw new InterpreterLoadException("Error getting file contents for specification"
                                               + " term.");
        }

        return language;
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

    private FileObject getParseTableFile(String interpreterJar, Properties dynSemProperties) {
        String tablePathString = dynSemProperties.getProperty("target.table");
        return resourceService.resolve(relativizeToJarPath(interpreterJar, tablePathString));
    }

    /*
     * Returns the dynsem.properties file parsed as a Properties object.
     */
    private Properties dynSemProperties(ILanguageImpl langImpl) throws InterpreterLoadException {
        FileObject dynSemPropertiesFile = null;
        for (FileObject fo : langImpl.locations()) {
            try {
                dynSemPropertiesFile = fo.getChild("dynsem.properties");
                if (dynSemPropertiesFile.exists()) {
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

    private DynSemLanguage getDynSemLanguageSingleton(Class<DynSemLanguage> languageClass)
        throws InterpreterLoadException {
        try {
            // passing null to Field#get means that it is a static field.
            return (DynSemLanguage) languageClass.getField("INSTANCE").get(null);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException
                 | SecurityException e) {
            throw new InterpreterLoadException("Could not access singleton field \"INSTANCE\""
                                               + " of language entrypoint.");
        }
    }

    @SuppressWarnings("unchecked")
    private Class<DynSemLanguage> getLanguageClass(String interpreterJar,
                                                   Properties dynSemProperties)
        throws InterpreterLoadException {
        ClassLoader classLoader = this.getClass().getClassLoader();
        targetPackage = dynSemProperties.getProperty("target.package");
        String languageName = dynSemProperties.getProperty("source.langname");

        try {
            return (Class<DynSemLanguage>) classLoader
                .loadClass(targetPackage + "." + languageName + "ASTLanguage");
        } catch (ClassNotFoundException e) {
            throw new InterpreterLoadException(e);
        }
    }

}
