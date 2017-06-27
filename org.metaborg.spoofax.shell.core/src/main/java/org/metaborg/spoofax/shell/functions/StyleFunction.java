package org.metaborg.spoofax.shell.functions;

import org.metaborg.core.context.IContext;
import org.metaborg.core.context.IContextService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.core.style.IRegionCategory;
import org.metaborg.core.style.IRegionStyle;
import org.metaborg.spoofax.core.style.CategorizerValidator;
import org.metaborg.spoofax.core.style.ISpoofaxCategorizerService;
import org.metaborg.spoofax.core.style.ISpoofaxStylerService;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.shell.output.FailOrSuccessResult;
import org.metaborg.spoofax.shell.output.IResult;
import org.metaborg.spoofax.shell.output.IResultFactory;
import org.metaborg.spoofax.shell.output.ParseResult;
import org.metaborg.spoofax.shell.output.StyleResult;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * Provides syntax highlighting in terms of a {@link StyleResult} based on a {@link ParseResult}.
 */
public class StyleFunction extends ContextualSpoofaxFunction<ParseResult, StyleResult> {

	private final ISpoofaxStylerService stylerService;
	private final ISpoofaxCategorizerService categorizer;

	/**
	 * Instantiate the Style Function.
	 *
	 * @param stylerService
	 *            The {@link ISpoofaxStylerService} that applies a certain style to a region.
	 * @param categorizer
	 *            The {@link ISpoofaxCategorizerService} that defines regions.
	 * @param contextService
	 *            The {@link IContextService} with which to create a new {@link IContext} if
	 *            necessary.
	 * @param resultFactory
	 *            The {@link IResultFactory}.
	 * @param project
	 *            The {@link IProject} in which this funcction should operate.
	 * @param lang
	 *            The {@link ILanguageImpl} to which this function applies.
	 */
	@Inject
	public StyleFunction(ISpoofaxStylerService stylerService,
			ISpoofaxCategorizerService categorizer,
			IContextService contextService, IResultFactory resultFactory,
			@Assisted IProject project, @Assisted ILanguageImpl lang) {
		super(contextService, resultFactory, project, lang);
		this.stylerService = stylerService;
		this.categorizer = categorizer;
	}

	@Override
	protected FailOrSuccessResult<StyleResult, IResult> applyThrowing(IContext context,
			ParseResult parseResult) throws Exception {

		ILanguageImpl lang = context.language();

		ISpoofaxParseUnit spoofaxParseUnit = parseResult.unit();

		final Iterable<IRegionCategory<IStrategoTerm>> categories = CategorizerValidator
				.validate(categorizer.categorize(lang, spoofaxParseUnit));

		Iterable<IRegionStyle<IStrategoTerm>> regions = stylerService
				.styleParsed(context.language(), categories);

		return FailOrSuccessResult
				.ofSpoofaxResult(resultFactory.createStyleResult(regions, spoofaxParseUnit));

	}

}
