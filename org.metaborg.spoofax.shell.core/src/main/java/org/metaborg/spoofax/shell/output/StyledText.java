package org.metaborg.spoofax.shell.output;

import java.awt.Color;
import java.util.List;
import java.util.stream.Collectors;

import org.metaborg.core.source.ISourceRegion;
import org.metaborg.core.style.IRegionStyle;
import org.metaborg.core.style.IStyle;
import org.metaborg.core.style.RegionStyle;
import org.metaborg.core.style.Style;
import org.spoofax.terms.StrategoString;

import com.google.common.collect.Lists;

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
        this.source = Lists.newArrayList();
        this.append(text);
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
        this.source = Lists.newArrayList();
        this.append(color, text);
    }

    /**
     * Create a styled text from a string with a style.
     *
     * @param style
     *            the style
     * @param text
     *            The unstyled text.
     */
    public StyledText(Style style, String text) {
        this.source = Lists.newArrayList();
        this.append(style, text);
    }

    /**
     * Create a styled text from a list of styled Stratego terms.
     *
     * @param sourceRegions
     *            The list of styled Stratego terms.
     */
    public StyledText(Iterable<IRegionStyle<StrategoString>> sourceRegions) {
        this.source = Lists.newArrayList();
        sourceRegions.forEach(e -> this.append(e.region(), e.style(), e.fragment().stringValue()));
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
        this.source.add(new RegionStyle<String>(region, style, text));
        return this;
    }

    /**
     * Append a string with no style to this styled text.
     *
     * @param text
     *            The unstyled text.
     * @return The styled text.
     */
    public StyledText append(String text) {
        return this.append(null, null, text);
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
        return this.append(null, new Style(color, null, false, false, false), text);
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
        return this.append(null, style, text);
    }

    @Override
    public String toString() {
        return source.stream().map(e -> e.fragment()).collect(Collectors.joining());
    }
}
