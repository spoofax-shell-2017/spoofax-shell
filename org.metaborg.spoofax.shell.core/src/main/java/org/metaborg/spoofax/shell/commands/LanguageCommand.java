package org.metaborg.spoofax.shell.commands;

import java.util.Set;
import java.util.function.Function;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.action.ITransformAction;
import org.metaborg.core.analysis.AnalyzerFacet;
import org.metaborg.core.language.ILanguageDiscoveryService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.menu.IMenuService;
import org.metaborg.core.project.IProject;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.shell.functions.FunctionComposer;
import org.metaborg.spoofax.shell.functions.IFunctionFactory;
import org.metaborg.spoofax.shell.invoker.ICommandInvoker;
import org.metaborg.spoofax.shell.output.ExceptionResult;
import org.metaborg.spoofax.shell.output.IResult;
import org.metaborg.spoofax.shell.output.StyledText;
import org.metaborg.spoofax.shell.output.TransformResult;
import org.metaborg.spoofax.shell.services.IEditorServices;

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
	private final IEditorServices editorServices;

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
	 */
	@Inject
	public LanguageCommand(ILanguageDiscoveryService langDiscoveryService,
			IResourceService resourceService, IMenuService menuService, ICommandInvoker invoker,
			IEditorServices editorServices, IFunctionFactory factory, IProject project) {
		// FIXME: don't use the hardcoded @Provides
		this.langDiscoveryService = langDiscoveryService;
		this.resourceService = resourceService;
		this.menuService = menuService;
		this.invoker = invoker;
		this.editorServices = editorServices;
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
		Set<ILanguageImpl> langs = langDiscoveryService.scanLanguagesInDirectory(langloc);

		if (langs == null || langs.isEmpty()) {
			throw new MetaborgException("Cannot find a language implementation");
		}

		if (langs.size() > 1) {
			throw new MetaborgException(
					"Multiple language implementations found. Please provide an unambiguous path.");
		}

		// take the first (and only one).
		return langs.iterator().next();
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
