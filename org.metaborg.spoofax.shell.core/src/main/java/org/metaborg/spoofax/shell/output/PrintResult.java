/**
 *
 */
package org.metaborg.spoofax.shell.output;

import java.util.Collections;
import java.util.List;

import org.metaborg.core.source.ISourceRegion;

/**
 * A result that represents output that is human readable.
 */
public class PrintResult implements IResult {

    private final StyledText text;
    private final List<ISourceRegion> regions;

    /**
     * Creates a {@link PrintResult}.
     * @param text The original text.
     */
    public PrintResult(String text) {
        this(text, Collections.emptyList());
    }

    /**
     * Creates a {@link PrintResult} with given fold regions.
     * @param text The (human readable) text
     * @param regions Foldregions that correspond to {@link #text}
     */
    public PrintResult(StyledText text, List<ISourceRegion> regions) {
        this.text = text;
        this.regions = regions;
    }

    /**
     * Creates a {@link PrintResult} with given fold regions.
     * @param text The (human readable) text
     * @param regions Foldregions that correspond to {@link #text}
     */
    public PrintResult(String text, List<ISourceRegion> regions) {
        this(new StyledText(text), regions);
    }

    /**
     * Gets the fold regions contained in this result (if applicable).
     * @return a {@link List} of regions.
     */
    public List<ISourceRegion> getRegions() {
        return regions;
    }

    /**
     * Gets the test. This text is human readable.
     * @return a {@link StyledText}
     */
    public StyledText getText() {
        return text;
    }

    @Override
    public void accept(IResultVisitor visitor) {
        visitor.visitMessage(text);
    }

}
