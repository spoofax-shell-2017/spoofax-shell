package org.metaborg.spoofax.shell.commands;

import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageDiscoveryRequest;
import org.metaborg.core.language.ILanguageDiscoveryService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.LanguageUtils;
import org.metaborg.core.project.IProject;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.core.analysis.AnalysisFacet;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.shell.core.StyledText;
import org.metaborg.spoofax.shell.invoker.ICommandFactory;
import org.metaborg.spoofax.shell.invoker.ICommandInvoker;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Represents a command that loads a Spoofax language.
 */
public class LanguageCommand extends SpoofaxCommand {

    private ILanguageDiscoveryService langDiscoveryService;
    private IResourceService resourceService;
    private ICommandInvoker invoker;

    /**
     * Instantiate a {@link LanguageCommand}. Loads all commands applicable to a lanugage.
     * @param common    The {@link IStrategoCommon} service.
     * @param langDiscoveryService the {@link ILanguageDiscoveryService}
     * @param resourceService      the {@link IResourceService}
     * @param invoker   the {@link ICommandInvoker}
     * @param onSuccess called when a language was loaded successfully
     * @param onError   called when loading a language has failed
     * @param project   the associated {@link IProject}
     */
    @Inject
    public LanguageCommand(IStrategoCommon common, // FIXME: consider storing terms in result class
                           ILanguageDiscoveryService langDiscoveryService,
                           IResourceService resourceService,
                           ICommandInvoker invoker,
                           @Named("onSuccess") Consumer<StyledText> onSuccess,
                           @Named("onError") Consumer<StyledText> onError,
                           IProject project) { // FIXME: don't use the hardcoded @Provides
        super(common, onSuccess, onError, project, null);
        this.langDiscoveryService = langDiscoveryService;
        this.resourceService = resourceService;
        this.invoker = invoker;
    }

    @Override
    public String description() {
        return "Load a language from a path.";
    }

    @Override
    public void execute(String... args) {
        try {
            if (args.length == 0 || args.length > 1) {
                throw new MetaborgException("Syntax: :lang <path>");
            }

            FileObject resolve = resourceService.resolve("zip:" + args[0] + "!/");
            Iterable<ILanguageDiscoveryRequest> requests = langDiscoveryService.request(resolve);
            Iterable<ILanguageComponent> components = langDiscoveryService.discover(requests);

            Set<ILanguageImpl> implementations = LanguageUtils.toImpls(components);
            lang = LanguageUtils.active(implementations);

            if (lang == null) {
                throw new MetaborgException("Cannot find a language implementation");
            }

            invoker.resetCommands();
            ICommandFactory commandFactory = invoker.getCommandFactory();
            invoker.addCommand("parse", commandFactory.createParse(project, lang));
            invoker.addCommand("eval", commandFactory.createEvaluate(project, lang));
            if (lang.hasFacet(AnalysisFacet.class)) {
                invoker.addCommand("analyze", commandFactory.createAnalyze(project, lang));
            }

            onSuccess.accept(new StyledText("Loaded language " + lang));
        } catch (MetaborgException e) {
            onError.accept(new StyledText(e.getMessage()));
        }
    }

}
