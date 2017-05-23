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

public class StyleResult extends ParseResult {

    private final Iterable<IRegionStyle<String>> regions;
    
    @Inject
    public StyleResult(IStrategoCommon common,
                       @Assisted ISpoofaxParseUnit unit,
                       @Assisted  Iterable<IRegionStyle<IStrategoTerm>> regions) {
        super(common, unit);
        this.regions = StreamSupport.stream(regions.spliterator(), true).map(termRegion -> {
            ISourceRegion region = termRegion.region();
            return new RegionStyle<String>(
                    region,
                    termRegion.style(),
                    termRegion.toString());
            
        }).collect(Collectors.toList());
    }

    @Override
    public StyledText styled() {
        return new StyledText(this.regions);
    }

    
}
