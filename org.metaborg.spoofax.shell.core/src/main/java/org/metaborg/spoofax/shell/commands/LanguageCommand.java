package org.metaborg.spoofax.shell.commands;

import java.util.Set;
import java.util.function.Function;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.action.ITransformAction;
import org.metaborg.core.analysis.AnalyzerFacet;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageDiscoveryRequest;
import org.metaborg.core.language.ILanguageDiscoveryService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.LanguageUtils;
import org.metaborg.core.menu.IMenuService;
import org.metaborg.core.project.IProject;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.shell.client.IResult;
import org.metaborg.spoofax.shell.functions.IFunctionFactory;
import org.metaborg.spoofax.shell.invoker.ICommandInvoker;
import org.metaborg.spoofax.shell.output.StyledText;
import org.metaborg.spoofax.shell.output.TransformResult;

import com.google.inject.Inject;

/**
 * Represents a command that loads a Spoofax language.
 */
public class LanguageCommand implements IReplCommand {
    private final ILanguageDiscoveryService langDiscoveryService;
    private final IResourceService resourceService;
    private final IMenuService menuService;
    private final ICommandInvoker invoker;
    private final IProject project;
    private final IFunctionFactory factory;

    /**
     * Instantiate a {@link LanguageCommand}. Loads all commands applicable to a lanugage.
     *
     * @param langDiscoveryService
     *            the {@link ILanguageDiscoveryService}
     * @param resourceService
     *            the {@link IResourceService}
     * @param invoker
     *            the {@link ICommandInvoker}
     * @param factory
     *            the {@link IFunctionFactory}
     * @param menuService
     *            the {@link IMenuService}
     * @param project
     *            the associated {@link IProject}
     */
    @Inject
    public LanguageCommand(ILanguageDiscoveryService langDiscoveryService,
                           IResourceService resourceService, IMenuService menuService,
                           ICommandInvoker invoker, IFunctionFactory factory,
                           IProject project) { // FIXME: don't use the hardcoded @Provides
        this.langDiscoveryService = langDiscoveryService;
        this.resourceService = resourceService;
        this.menuService = menuService;
        this.invoker = invoker;
        this.factory = factory;
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
        ILanguageImpl lang = LanguageUtils.active(implementations);

        if (lang == null) {
            throw new MetaborgException("Cannot find a language implementation");
        }
        return lang;
    }

    // FIXME: there really should be a better way to go about this. Perhaps Apache Tika?
    private FileObject resolveLanguage(String path) {
        String extension = resourceService.resolveToName(path).getExtension();
        if (extension.equals("zip")) {
            return resourceService.resolve(extension + ":" + path + "!/");
        }
        return resourceService.resolve(path);
    }

    @Override
    public IResult execute(String... args) throws MetaborgException {
        if (args.length == 0 || args.length > 1) {
            throw new MetaborgException("Syntax: :lang <path>");
        }

        ILanguageImpl lang = load(resolveLanguage(args[0]));
        boolean analyze = lang.hasFacet(AnalyzerFacet.class);
        CommandBuilder<?> builder = factory.createBuilder(project, lang);
        Function<ITransformAction, CommandBuilder<TransformResult>> transform;

        invoker.resetCommands();
        invoker.addCommand("parse", builder.parse().description("Parse the expression").build());
        if (analyze) {
            invoker.addCommand("analyze", builder.analyze()
                               .description("Analyze the expression").build());
            invoker.addCommand("eval", builder.evalAnalyzed()
                               .description("Evaluate an analyzed expression").build());
            transform = (action) -> builder.transformAnalyzed(action);
        } else {
            invoker.addCommand("eval", builder.evalParsed()
                               .description("Evaluate a parsed expression").build());
            transform = (action) -> builder.transformParsed(action);
        }
        new TransformVisitor(menuService).getActions(lang).forEach((key, action) -> {
            invoker.addCommand(key, transform.apply(action).description(action.name()).build());
        });

        return (visitor) -> visitor
            .visitMessage(new StyledText("Loaded language " + lang));
    }

}
