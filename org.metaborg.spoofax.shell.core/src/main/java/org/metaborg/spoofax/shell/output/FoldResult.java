package org.metaborg.spoofax.shell.output;

import java.util.List;

import org.metaborg.core.source.ISourceRegion;
import org.metaborg.core.style.IRegionStyle;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * Represents a result that is a term and foldable regions.
 */
public class FoldResult implements IResult {

    private final IStrategoTerm term;
    private final List<ISourceRegion> regions;

    /**
     * Create a Fold Result.
     *
     * @param term The {@ IStrategoTerm}.
     * @param regions
     *            The {@link IRegionStyle}s in terms of {@link IStrategoTerm}s.
     */
    public FoldResult(IStrategoTerm term, List<ISourceRegion> regions) {
        this.term = term;
        this.regions = regions;
    }

    /**
     * Gets the term contained in this result.
     * @return a {@link IStrategoTerm}.
     */
    public IStrategoTerm getTerm() {
        return term;
    }

    /**
     * Gets the regions contained in this result.
     * @return a {@link List} of regions.
     */
    public List<ISourceRegion> getRegions() {
        return regions;
    }

    @Override
    public void accept(IResultVisitor visitor) {
        // FIXME: not nice but as of now not used.
        visitor.visitMessage(new StyledText(term.toString(IStrategoTerm.INFINITE)));
    }


}
