package org.metaborg.spoofax.shell.output;

import java.awt.Color;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import org.metaborg.core.source.ISourceRegion;
import org.metaborg.core.source.SourceRegion;
import org.metaborg.core.style.IRegionStyle;
import org.metaborg.core.style.IStyle;
import org.metaborg.core.style.RegionStyle;
import org.metaborg.core.style.Style;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

/**
 * Represents a styled text containing several styled strings, each represented by an
 * {@link IRegionStyle}.
 */
public class StyledText {
    private final List<IRegionStyle<String>> source;

    /**
     * Create a styled text from a string with no style.
     *
     * @param text
     *            The unstyled text.
     */
    public StyledText(String text) {
        this((IStyle) null, text);
    }

    /**
     * Create a styled text from a colored string.
     *
     * @param color
     *            The color to apply.
     * @param text
     *            The unstyled text.
     */
    public StyledText(Color color, String text) {
        this(colorStyle(color, null), text);
    }

    /**
     * Create a styled text from a string with a style.
     *
     * @param style
     *            the style
     * @param text
     *            The unstyled text.
     */
    public StyledText(IStyle style, String text) {
        this.source = Lists.newArrayList();

        this.append(style, text);
    }

    /**
     * Create a styled text from a list of styled strings.
     *
     * @param sourceRegions
     *            The list of styled strings.
     */
    public StyledText(Iterable<IRegionStyle<String>> sourceRegions) {
        this.source = Lists.newArrayList(sourceRegions);
    }

    /**
     * Create an empty {@link StyledText}.
     */
    public StyledText() {
        this.source = Lists.newArrayList();
    }

    /**
     * Return all the styled strings in this text.
     *
     * @return All the styled strings in this text.
     */
    public List<IRegionStyle<String>> getSource() {
        return source;
    }

    /**
     * Append a string with no style to this styled text.
     *
     * @param text
     *            The unstyled text.
     * @return The styled text.
     */
    public StyledText append(String text) {
        return this.append((IStyle) null, text);
    }

    /**
     * Append a colored string to this styled text.
     *
     * @param color
     *            The color to apply.
     * @param text
     *            The unstyled text.
     * @return The styled text.
     */
    public StyledText append(Color color, String text) {
        return this.append(colorStyle(color, null), text);
    }

    /**
     * Append a string with an arbitrary style to this styled text.
     *
     * @param style
     *            The style to apply.
     * @param text
     *            The unstyled text.
     * @return The styled text.
     */
    public StyledText append(IStyle style, String text) {
        int start = this.toString().length();
        return this.append(new SourceRegion(start, start + text.length() - 1), style, text);
    }

    /**
     * Append a string with multiple regions and a style to this styled text. All of the regions in
     * the given text will be styled with the given style, the rest of the text will have no style.
     * Internally, the given regions will be sorted.
     *
     * @param regions
     *            The regions to style.
     * @param style
     *            The style to apply.
     * @param text
     *            The unstyled text.
     * @return The styled text.
     */
    public StyledText append(Iterable<ISourceRegion> regions, IStyle style, String text) {
        RangeSet<Integer> rangeSet = TreeRangeSet.create();
        StreamSupport.stream(regions.spliterator(), false)
            .map(r -> Range.closed(r.startOffset(), r.endOffset())).forEach(rangeSet::add);
        List<Range<Integer>> sortedRanges = rangeSet.asRanges().stream()
            .sorted((a, b) -> a.lowerEndpoint() - b.lowerEndpoint()).collect(Collectors.toList());
        int curOffset = 0;
        for (Range<Integer> r : sortedRanges) {
            String sub = text.substring(curOffset, r.lowerEndpoint());
            this.append(sub);
            String errorRegion = text.substring(r.lowerEndpoint(), r.upperEndpoint() + 1);
            this.append(style, errorRegion);
            curOffset = r.upperEndpoint() + 1;
        }
        // Add the rest.
        this.append(text.substring(curOffset));
        return this;
    }

    /**
     * Append a string with a region and a style to this styled text.
     *
     * @param region
     *            The region to style.
     * @param style
     *            The style to apply.
     * @param text
     *            The unstyled text.
     * @return The styled text.
     */
    public StyledText append(ISourceRegion region, IStyle style, String text) {
        this.source.add(new RegionStyle<>(region, style, text));

        return this;
    }

    private static IStyle colorStyle(Color fg, Color bg) {
        return new Style(fg, bg, false, false, false);
    }

    private static <T> boolean equals(T a, T b) {
        return a == b || a != null && a.equals(b) || b != null && b.equals(a);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof StyledText)) {
            return false;
        }

        StyledText other = (StyledText) obj;
        if (source.size() != other.source.size()) {
            return false;
        }

        return IntStream.range(0, source.size())
            .mapToObj(e -> {
                IRegionStyle<?> s = source.get(e);
                IRegionStyle<?> o = other.source.get(e);

                return equals(s.style(), o.style()) && equals(s.fragment(), o.fragment());
            })
            .allMatch(e -> e);
    }

    @Override
    public int hashCode() {
        return source.stream()
            .map(e -> (e.fragment() == null ? 1 : e.fragment().hashCode())
                      * (e.style() == null ? 1 : e.style().hashCode()))
            .reduce(1, (a, b) -> a * b);
    }

    @Override
    public String toString() {
        return source.stream().map(IRegionStyle::fragment).collect(Collectors.joining());
    }
}
