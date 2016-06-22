package org.metaborg.spoofax.shell.commands;

import java.util.Set;
import java.util.function.Function;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.MetaborgRuntimeException;
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
import org.metaborg.spoofax.shell.functions.IFunctionFactory;
import org.metaborg.spoofax.shell.invoker.CommandNotFoundException;
import org.metaborg.spoofax.shell.invoker.ICommandInvoker;
import org.metaborg.spoofax.shell.output.ExceptionResult;
import org.metaborg.spoofax.shell.output.IResult;
import org.metaborg.spoofax.shell.output.StyledText;
import org.metaborg.spoofax.shell.output.TransformResult;

import com.google.inject.Inject;

/**
 * Represents a command that loads a Spoofax language.
 */
public class LanguageCommand implements IReplCommand {
    private static final String[] ARCHIVES = { "zip", "jar", "tar", "tgz", "tbz2", };
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
     *
     * @param langloc
     *            the {@link FileObject} containing the {@link ILanguageImpl}
     * @return the {@link ILanguageImpl}
     * @throws MetaborgException
     *             when loading fails
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

    private FileObject resolveLanguage(String path) {
        String extension = resourceService.resolveToName(path).getExtension();
        for (String archive : ARCHIVES) {
            if (extension.equals(archive)) {
                return resourceService.resolve(extension + ":" + path + "!/");
            }
        }
        return resourceService.resolve(path);
    }

    private void loadCommands(ILanguageImpl lang) {
        boolean analyze = lang.hasFacet(AnalyzerFacet.class);
        CommandBuilder<?> builder = factory.createBuilder(project, lang);

        IReplCommand eval;
        Function<ITransformAction, CommandBuilder<TransformResult>> transform;

        invoker.resetCommands();
        invoker.addCommand("parse", builder.parse().description("Parse the expression").build());
        if (analyze) {
            invoker.addCommand("analyze", builder.analyze()
                .description("Analyze the expression").build());

            eval = builder.evalAnalyzed().description("Evaluate an analyzed expression").build();
            transform = builder::transformAnalyzed;
        } else {
            eval = builder.evalParsed().description("Evaluate a parsed expression").build();
            transform = builder::transformParsed;
        }
        invoker.addCommand("eval", eval);
        invoker.setDefault(eval);

        new TransformVisitor(menuService).getActions(lang).forEach((key, action) ->
                invoker.addCommand(key, transform.apply(action).description(action.name())
                        .build()));
    }

    @Override
    public IResult execute(String... args) {
        if (args.length == 0 || args.length > 1) {
            return new ExceptionResult(new CommandNotFoundException("Syntax: :lang <path>"));
        }

        try {
            ILanguageImpl lang = load(resolveLanguage(args[0]));
            loadCommands(lang);

            return (visitor) -> visitor.visitMessage(new StyledText("Loaded language " + lang));
        } catch (MetaborgException | MetaborgRuntimeException e) {
            return new ExceptionResult(e);
        }
    }

}
