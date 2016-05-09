package org.metaborg.spoofax.shell.client.console;

import java.awt.Color;
import java.io.IOException;
import java.util.Set;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageDiscoveryRequest;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.LanguageUtils;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.ISimpleProjectService;
import org.metaborg.core.project.SimpleProjectService;
import org.metaborg.spoofax.core.Spoofax;
import org.metaborg.spoofax.shell.client.IDisplay;
import org.metaborg.spoofax.shell.client.IEditor;
import org.metaborg.spoofax.shell.client.Repl;
import org.metaborg.spoofax.shell.core.StyledText;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * This class launches a console based REPL.
 *
 * It uses a GNU Readline-like input buffer with multiline editing capabilities, keyboard shortcuts
 * and persistent history. ANSI color codes are used to display colors.
 */
public final class ConsoleRepl {

    private ConsoleRepl() {
    }

    private static IProject createProject(final Spoofax spoofax) throws MetaborgException {
        final ISimpleProjectService projectService =
            spoofax.injector.getInstance(SimpleProjectService.class);
        final FileObject projectPath = spoofax.resourceService.resolve("tmp://");
        return projectService.create(projectPath);
    }

    /*
     * Finds a (the first) language from a parent location. TODO: Eventually, this should find out
     * which language is actually in use instead of taking the first language found.
     */
    private static ILanguageImpl findLanguage(final Spoofax spoofax, final FileObject langPath)
        throws MetaborgException {
        final Iterable<ILanguageDiscoveryRequest> requests =
            spoofax.languageDiscoveryService.request(langPath);
        final Iterable<ILanguageComponent> components =
            spoofax.languageDiscoveryService.discover(requests);

        final Set<ILanguageImpl> implementations = LanguageUtils.toImpls(components);
        final ILanguageImpl lang = LanguageUtils.active(implementations);
        if (lang == null) {
            throw new MetaborgException("Cannot find a language implementation");
        }
        return lang;
    }

    private static IContext createContext(final Spoofax spoofax, final String langPath)
        throws MetaborgException {
        final IProject project = createProject(spoofax);
        final FileObject zipFile = spoofax.resourceService.resolve(langPath);
        // TODO: do not hardcode zipfiles
        final FileObject langFile = spoofax.resourceService.resolve("zip:" + zipFile + "!/");
        final ILanguageImpl lang = findLanguage(spoofax, langFile);
        return spoofax.contextService.getTemporary(project.location(), project, lang);
    }

    /**
     * Instantiates and runs a new Repl.
     *
     * @param args
     *            The path to a language implementation location, using any URI supported by Apache
     *            VFS.
     * @throws IOException
     *             When an IO error occurs during execution.
     */
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            throw new IllegalArgumentException("Expected a path to a language "
                                               + "implementation location");
        }

        try (final Spoofax spoofax = new Spoofax()) {
            final IContext context = createContext(spoofax, args[0]);
            final Injector injector = Guice.createInjector(new ConsoleReplModule(context));
            final IEditor editor = injector.getInstance(IEditor.class);
            final IDisplay display = injector.getInstance(IDisplay.class);
            final Repl repl = injector.getInstance(Repl.class);
            final StyledText message = new StyledText(Color.BLUE, "Welcome to the ")
                .append(Color.GREEN, "Spoofax").append(Color.BLUE, " REPL");
            display.displayResult(message);
            editor.history().loadFromDisk();
            repl.run();
            editor.history().persistToDisk();
        } catch (IOException | MetaborgException e) {
            e.printStackTrace();
        }
    }
}
