package org.metaborg.spoofax.shell.core;

import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.StreamSupport;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.meta.lang.dynsem.interpreter.DynSemContext;
import org.metaborg.meta.lang.dynsem.interpreter.nodes.rules.RuleRoot;
import org.metaborg.meta.lang.dynsem.interpreter.terms.ITerm;
import org.metaborg.spoofax.core.shell.ShellFacet;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Inject;

/**
 * An {@link IEvaluationStrategy} for DynSem-based languages.
 */
public class DynSemEvaluationStrategy implements IEvaluationStrategy {
    private ILanguageImpl langImpl;
    private ClassLoader classLoader;
    private DynSemContext context;

    /**
     * Creates a new {@link DynSemEvaluationStrategy} for evaluating the given {@link ILanguageImpl
     * language implementation}.
     *
     * @param langImpl
     *            The language implementation for evaluating with DynSem.
     */
    @Inject
    DynSemEvaluationStrategy(ILanguageImpl langImpl) {
        this.langImpl = langImpl;
    }

    @Override
    public String name() {
        return "dynsem";
    }

    @SuppressWarnings("deprecation")
    @Override
    public IStrategoTerm evaluate(IStrategoTerm input) {
        if (uninitialized()) {
            initialize();
        }
        // Create DynSem node from the AST input
        ITerm program = IExprTerm.create(input);
        RuleRoot root =
            context.getRuleRegistry().lookupRule("default", program.constructor(), program.arity());
        root.getCallTarget().call(program);
    }

    private boolean uninitialized() {
        return classLoader == null && context == null;
    }

    /**
     * Initialize the DynSemContext and ClassLoader.
     */
    protected void initialize() throws ClassNotFoundException, MalformedURLException,
        IllegalAccessException, IllegalArgumentException, NoSuchFieldException, SecurityException,
        NoSuchMethodException, InvocationTargetException {
        classLoader = classLoader();
        Properties dynSemProperties = dynSemProperties();
        String targetPackage = dynSemProperties.getProperty("target.package");
        String languageName = dynSemProperties.getProperty("source.langname");

        Class<?> languageClass =
            classLoader.loadClass(targetPackage + "." + languageName + "Language");

        // passing null to Field#get means that it is a static field.
        Object language = languageClass.getField("INSTANCE").get(null);

        Method createDynsemContext =
            languageClass.getMethod("createDynSemContext", InputStream.class, PrintStream.class);
        context = (DynSemContext) createDynsemContext.invoke(language, null, null);
    }

    /*
     * Returns the dynsem.properties file parsed as a Properties object.
     */
    private Properties dynSemProperties() throws Exception {
        Optional<FileObject> optDynSemPropertiesFile = StreamSupport
            .stream(langImpl.locations().spliterator(), false)
            .filter(file -> file.getName().getBaseName().equals("dynsem.properties")).findFirst();

        FileObject dynSemPropertiesFile = optDynSemPropertiesFile
            .orElseThrow(() -> new Exception("Missing \"dynsem.properties\" file"));

        Properties dynSemProperties = new Properties();
        InputStream in = dynSemPropertiesFile.getContent().getInputStream();
        dynSemProperties.load(in);
        return dynSemProperties;
    }

    /*
     * Initialize the ClassLoader for reflectively loading and instantiating classes.
     */
    private ClassLoader classLoader() throws MalformedURLException {
        ShellFacet shellFacet = langImpl.facet(ShellFacet.class);
        String interpreterJar = shellFacet.getInterpreterPath();
        URL[] url = { new URL(interpreterJar) };
        return new URLClassLoader(url, this.getClass().getClassLoader());
    }
}
