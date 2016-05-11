package org.metaborg.spoofax.shell.commands;

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
 * Represents a styled text containing several styled strings,
 * each represented by a {@link IRegionStyle}.
 */
public class StyledText {
    private List<IRegionStyle<String>> source;

    /**
     * Create a styled text from a string with no style.
     * @param text the text
     */
    public StyledText(String text) {
        this.source = Lists.newArrayList();
        this.append(text);
    }

    /**
     * Create a styled text from a colored string.
     * @param color the color
     * @param text the text
     */
    public StyledText(Color color, String text) {
        this.source = Lists.newArrayList();
        this.append(color, text);
    }

    /**
     * Create a styled text from a string with a style.
     * @param style the style
     * @param text the text
     */
    public StyledText(Style style, String text) {
        this.source = Lists.newArrayList();
        this.append(style, text);
    }

    /**
     * Create a styled text from a list of styled Stratego terms.
     * @param sourceRegions the list of styled terms
     */
    public StyledText(Iterable<IRegionStyle<StrategoString>> sourceRegions) {
        this.source = Lists.newArrayList();
        sourceRegions.forEach(e -> this.append(e.region(), e.style(), e.fragment().stringValue()));
    }

    /**
     * Return all the styled strings in this text.
     * @return the source
     */
    public List<IRegionStyle<String>> getSource() {
        return source;
    }

    /**
     * Append a string with a region and a style to this styled text.
     * @param region the region
     * @param style the style
     * @param text the text
     * @return this
     */
    public StyledText append(ISourceRegion region, IStyle style, String text) {
        this.source.add(new RegionStyle<String>(region, style, text));
        return this;
    }

    /**
     * Append a string with no style to this styled text.
     * @param text the text
     * @return this
     */
    public StyledText append(String text) {
        return this.append(null, null, text);
    }

    /**
     * Append a colored string to this styled text.
     * @param color the color
     * @param text the text
     * @return this
     */
    public StyledText append(Color color, String text) {
        return this.append(null, new Style(color, null, false, false, false), text);
    }

    /**
     * Append a string with an arbitrary style to this styled text.
     * @param style the style
     * @param text the text
     * @return this
     */
    public StyledText append(IStyle style, String text) {
        return this.append(null, style, text);
    }

    @Override
    public String toString() {
        return source.stream().map(e -> e.fragment()).collect(Collectors.joining());
    }
}
