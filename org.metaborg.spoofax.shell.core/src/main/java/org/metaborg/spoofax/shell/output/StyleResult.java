package org.metaborg.spoofax.shell.output;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.metaborg.core.source.ISourceRegion;
import org.metaborg.core.style.IRegionStyle;
import org.metaborg.core.style.RegionStyle;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * Represents a parsed result with iterable style {@link IRegionStyle}s.
 */
public class StyleResult extends ParseResult {

	private final Iterable<IRegionStyle<String>> regions;

	/**
	 * Create a Style Result.
	 *
	 * @param common
	 *            The {@link IStrategoCommon} service.
	 * @param unit
	 *            The wrapped {@link ISpoofaxParseUnit}.
	 * @param regions
	 *            The {@link IRegionStyle}s in terms of {@link IStrategoTerm}s.
	 */
	@Inject
	public StyleResult(IStrategoCommon common, @Assisted ISpoofaxParseUnit unit,
			@Assisted Iterable<IRegionStyle<IStrategoTerm>> regions) {
		super(common, unit);
		this.regions = StreamSupport.stream(regions.spliterator(), true).map(termRegion -> {
			ISourceRegion region = termRegion.region();
			return new RegionStyle<String>(region, termRegion.style(), termRegion.toString());

		}).collect(Collectors.toList());
	}

	@Override
	public StyledText styled() {
		return new StyledText(this.regions);
	}

}
