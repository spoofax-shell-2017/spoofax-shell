package org.metaborg.spoofax.shell.commands;

import java.util.Set;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.analysis.AnalyzerFacet;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageDiscoveryRequest;
import org.metaborg.core.language.ILanguageDiscoveryService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.LanguageUtils;
import org.metaborg.core.project.IProject;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.shell.hooks.IMessageHook;
import org.metaborg.spoofax.shell.invoker.ICommandFactory;
import org.metaborg.spoofax.shell.invoker.ICommandInvoker;
import org.metaborg.spoofax.shell.output.StyledText;

import com.google.inject.Inject;

/**
 * Represents a command that loads a Spoofax language.
 */
public class LanguageCommand implements IReplCommand {

    private final IMessageHook messageHook;
    private final ILanguageDiscoveryService langDiscoveryService;
    private final IResourceService resourceService;
    private final ICommandInvoker invoker;
    private final IProject project;
    private ILanguageImpl lang;

    /**
     * Instantiate a {@link LanguageCommand}. Loads all commands applicable to a lanugage.
     *
     * @param messageHook
     *            the {@link IMessageHook} to send messages to.
     * @param langDiscoveryService
     *            the {@link ILanguageDiscoveryService}
     * @param resourceService
     *            the {@link IResourceService}
     * @param invoker
     *            the {@link ICommandInvoker}
     * @param project
     *            the associated {@link IProject}
     */
    @Inject
    public LanguageCommand(IMessageHook messageHook, ILanguageDiscoveryService langDiscoveryService,
                           IResourceService resourceService, ICommandInvoker invoker,
                           IProject project) { // FIXME: don't use the hardcoded @Provides
        this.messageHook = messageHook;
        this.langDiscoveryService = langDiscoveryService;
        this.resourceService = resourceService;
        this.invoker = invoker;
        this.project = project;
    }

    @Override
    public String description() {
        return "Load a language from a path.";
    }

    /**
     * Load a {@link ILanguageImpl} from a {@link FileObject}.
     * @param langloc the {@link FileObject} containing the {@link ILanguageImpl}
     * @return        the {@link ILanguageImpl}
     * @throws MetaborgException when loading fails
     */
    public ILanguageImpl load(FileObject langloc) throws MetaborgException {
        Iterable<ILanguageDiscoveryRequest> requests = langDiscoveryService.request(langloc);
        Iterable<ILanguageComponent> components = langDiscoveryService.discover(requests);

        Set<ILanguageImpl> implementations = LanguageUtils.toImpls(components);
        lang = LanguageUtils.active(implementations);

        if (lang == null) {
            throw new MetaborgException("Cannot find a language implementation");
        }
        return lang;
    }

    @Override
    public void execute(String... args) throws MetaborgException {
        if (args.length == 0 || args.length > 1) {
            throw new MetaborgException("Syntax: :lang <path>");
        }

        FileObject resolve = resourceService.resolve("zip:" + args[0] + "!/");
        ILanguageImpl lang = load(resolve);
        boolean analyze = lang.hasFacet(AnalyzerFacet.class);

        invoker.resetCommands();
        ICommandFactory commandFactory = invoker.getCommandFactory();
        invoker.addCommand("parse", commandFactory.createParse(project, lang));
        invoker.addCommand("transform", commandFactory.createTransform(project, lang, analyze));

        if (analyze) {
            invoker.addCommand("analyze", commandFactory.createAnalyze(project, lang));
        }

        messageHook.accept(new StyledText("Loaded language" + lang));
    }

}
