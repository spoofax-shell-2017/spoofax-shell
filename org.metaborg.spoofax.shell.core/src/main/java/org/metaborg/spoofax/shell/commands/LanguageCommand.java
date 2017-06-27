package org.metaborg.spoofax.shell.commands;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.action.ITransformAction;
import org.metaborg.core.analysis.AnalyzerFacet;
import org.metaborg.core.config.IExportConfig;
import org.metaborg.core.config.IProjectConfig;
import org.metaborg.core.config.IProjectConfigService;
import org.metaborg.core.config.LangFileExport;
import org.metaborg.core.config.ProjectConfigBuilder;
import org.metaborg.core.language.ILanguageDiscoveryService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.ResourceExtensionFacet;
import org.metaborg.core.menu.IMenuService;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.ISimpleProjectService;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.core.resource.ResourceUtils;
import org.metaborg.spoofax.shell.functions.FunctionComposer;
import org.metaborg.spoofax.shell.functions.IFunctionFactory;
import org.metaborg.spoofax.shell.invoker.ICommandInvoker;
import org.metaborg.spoofax.shell.output.ExceptionResult;
import org.metaborg.spoofax.shell.output.IResult;
import org.metaborg.spoofax.shell.output.StyledText;
import org.metaborg.spoofax.shell.output.TransformResult;
import org.metaborg.spoofax.shell.services.IEditorServices;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.metaborg.util.resource.ExtensionFileSelector;

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
	private IProject project;
	private final IFunctionFactory factory;
	private final IEditorServices editorServices;
    private final ISimpleProjectService projectService;
    private final ProjectConfigBuilder builder;
    private  IProjectConfigService configService;

	/**
	 * Instantiate a {@link LanguageCommand}. Loads all commands applicable to a language.
	 *
	 * @param langDiscoveryService
	 *            the {@link ILanguageDiscoveryService}
	 * @param resourceService
	 *            the {@link IResourceService}
	 * @param invoker
	 *            the {@link ICommandInvoker}
	 * @param factory
	 *            the {@link IFunctionFactory}
	 * @param editorServices
	 *            the {@link IEditorServices}
	 * @param menuService
	 *            the {@link IMenuService}
	 * @param project
	 *            the associated {@link IProject}
     * @param projectSerivce
     *            the {@link ISimpleProjectService}
	 */
	@Inject
	public LanguageCommand(ILanguageDiscoveryService langDiscoveryService,
			IResourceService resourceService, IMenuService menuService, ICommandInvoker invoker,
			IEditorServices editorServices, IFunctionFactory factory, ISimpleProjectService projectService,
			ProjectConfigBuilder configBuilder) {
		// FIXME: don't use the hardcoded @Provides
		this.langDiscoveryService = langDiscoveryService;
		this.resourceService = resourceService;
		this.menuService = menuService;
		this.invoker = invoker;
		this.editorServices = editorServices;
		this.factory = factory;
		this.project = project;
        this.projectService = projectService;
        this.builder = configBuilder;
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
	 * @throws
	 */
	public ILanguageImpl load(FileObject langloc) throws MetaborgException {
		Set<ILanguageImpl> langs = langDiscoveryService.scanLanguagesInDirectory(langloc);

		if (langs == null || langs.isEmpty()) {
			throw new MetaborgException("Cannot find a language implementation");
		}

		if (langs.size() > 1) {
			throw new MetaborgException(
					"Multiple language implementations found. Please provide an unambiguous path.");
		}

		// take the first (and only one).
		ILanguageImpl lang = langs.iterator().next();

		ResourceExtensionFacet facet = lang.facet(ResourceExtensionFacet.class);
		ExtensionFileSelector selector = new ExtensionFileSelector(facet.extensions());
		try {
            final Iterable<FileObject> sourceFiles = ResourceUtils.find(langloc, selector);
//            projectService.create(langloc)
//            if (configService.available(langloc)) {
//                ConfigRequest<IProjectConfig> request = configService.get(langloc);
//                if (request.valid()) {
//                    IProjectConfig config = request.config();
//                }
//            } else {
//            IProjectConfig config = configService.defaultConfig(langloc);
            Set<IExportConfig> fileExports = new HashSet<>();
            sourceFiles.forEach(file -> fileExports.add(new LangFileExport(langloc.toString(), file.toString())));
            builder.addSources(fileExports);
            IProjectConfig config = builder.build(langloc);
//            }
            // FIXME: 2nd load gives exception that project already exists.
            project = projectService.create(langloc);


		} catch(FileSystemException e) {
            throw new MetaborgException("Cannot scan " + langloc + ", unexpected I/O error", e);
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

	/**
	 * Initializes the {@link IEditorServices} based on the language
	 * implementation.
	 *
	 * @param lang
	 *            {@link ILanguageImpl} The language implementation.
	 */
	private void loadEditorServices(ILanguageImpl lang) {
		FunctionComposer composer = factory.createComposer(project, lang);
		editorServices.load(composer);
	}

	private void loadCommands(ILanguageImpl lang) {
		boolean analyze = lang.hasFacet(AnalyzerFacet.class);
		CommandBuilder<?> builder = factory.createBuilder(project, lang);

		IReplCommand eval, open;
		Function<ITransformAction, CommandBuilder<TransformResult>> transform;

		invoker.resetCommands();
		invoker.addCommand("parse", builder.parse().description("Parse the expression").build());
		if (analyze) {
			invoker.addCommand("analyze",
					builder.analyze().description("Analyze the expression").build());

			eval = builder.evalAnalyzed().description("Evaluate an analyzed expression").build();
			open = builder.evalAOpen().description("Evaluate and analyze a file").build();
			transform = builder::transformAnalyzed;
		} else {
			eval = builder.evalParsed().description("Evaluate a parsed expression").build();
			open = builder.evalPOpen().description("Evaluate and parse a file").build();
			transform = builder::transformParsed;
		}
		invoker.addCommand("eval", eval);
		invoker.addCommand("open", open);

		invoker.setDefault(eval);

		new TransformVisitor(menuService).getActions(lang).forEach((key, action) -> invoker
				.addCommand(key, transform.apply(action).description(action.name()).build()));
	}

	@Override
	public IResult execute(String... args) {
		if (args.length == 0 || args.length > 1) {
			return new ExceptionResult(new MetaborgException("Syntax: :load <path>"));
		}

		try {
			ILanguageImpl lang = load(resolveLanguage(args[0]));
			loadCommands(lang);
			loadEditorServices(lang);

			return (visitor) -> visitor
					.visitMessage(new StyledText("Loaded language " + lang.id().toString()));
		} catch (MetaborgException | MetaborgRuntimeException e) {
			return new ExceptionResult(e);
		}
	}
}
