package org.metaborg.spoofax.shell.client.console.strategies;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.meta.lang.dynsem.interpreter.DynSemVM;

import com.oracle.truffle.api.vm.PolyglotEngine;

/**
 * Loads an interpreter that is present in the class path.
 * This {@link IInterpreterLoader} uses reflection to load the generated {@link DynSemEntryPoint}
 * subclass.
 * It instantiates a {@link PolyglotEngine} and initializes the interpreter by evaluating the DynSem
 * specification term.
 */
public class ClassPathInterpreterLoader implements IInterpreterLoader {

	@Override
	public DynSemVM createInterpreterForLanguage(ILanguageImpl langImpl) throws MetaborgException {
		try {
			Properties props = loadDynSemProperties(langImpl);
			String className = targetPackage(props) + "." + langName(props) + "Main";
			Class<?> mainClass = ClassUtils.getClass(className);
			DynSemVM vm = (DynSemVM) MethodUtils.invokeStaticMethod(mainClass, "createVM");
			return vm;
		} catch (ReflectiveOperationException | ClassCastException e) {
			throw new MetaborgException("Could not find the main class of the "
					+ "interpreter.\nIs the generated interpreter on your classpath?");
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

	/*
	 * Loads the required configurations from the dynsem.properties file parsed as a Properties
	 * object.
	 */
	private static Properties loadDynSemProperties(ILanguageImpl langImpl)
			throws MetaborgException {
		FileObject dynSemPropertiesFile = findDynSemPropertiesFileForLanguage(langImpl);
		Properties dynSemProperties = new Properties();
		try (InputStream in = dynSemPropertiesFile.getContent().getInputStream()) {
			dynSemProperties.load(in);
		} catch (IOException e) {
			throw new MetaborgException("Error when trying to load \"dynsem.properties\".");
		}

		return dynSemProperties;
	}

	private static FileObject findDynSemPropertiesFileForLanguage(ILanguageImpl langImpl)
			throws MetaborgException {
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
			throw new MetaborgException("Missing \"dynsem.properties\" file");
		}
		return dynSemPropertiesFile;
	}

}