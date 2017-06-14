package org.metaborg.spoofax.shell.functions;

import org.metaborg.core.context.IContext;
import org.metaborg.core.context.IContextService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.core.source.ISourceLocation;
import org.metaborg.core.style.IRegionCategory;
import org.metaborg.core.style.IRegionStyle;
import org.metaborg.spoofax.core.style.CategorizerValidator;
import org.metaborg.spoofax.core.style.ISpoofaxCategorizerService;
import org.metaborg.spoofax.core.style.ISpoofaxStylerService;
import org.metaborg.spoofax.core.tracing.ISpoofaxTracingService;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.shell.output.FailOrSuccessResult;
import org.metaborg.spoofax.shell.output.IResult;
import org.metaborg.spoofax.shell.output.IResultFactory;
import org.metaborg.spoofax.shell.output.ParseResult;
import org.metaborg.spoofax.shell.output.StyleResult;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class StyleFunction extends ContextualSpoofaxFunction<ParseResult, StyleResult> {

	private final ISpoofaxStylerService stylerService;
	private final ISpoofaxCategorizerService categorizer;
	private final ISpoofaxTracingService tracer;

	@Inject
	public StyleFunction(ISpoofaxStylerService stylerService,
			ISpoofaxCategorizerService categorizer, ISpoofaxTracingService tracer,
			IContextService contextService, IResultFactory resultFactory,
			@Assisted IProject project, @Assisted ILanguageImpl lang) {
		super(contextService, resultFactory, project, lang);
		this.stylerService = stylerService;
		this.categorizer = categorizer;
		this.tracer = tracer;
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

		regions.forEach(region -> {
			ISourceLocation loc = tracer.location(region.fragment());
			System.out.println(loc.toString());
		});

		return FailOrSuccessResult
				.ofSpoofaxResult(resultFactory.createStyleResult(regions, spoofaxParseUnit));

	}

}
