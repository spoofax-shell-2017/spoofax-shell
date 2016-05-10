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
 * Represents source code styled by Spoofax.
 */
public class StyledText {
    private List<IRegionStyle<String>> source;

    /**
     * Create source code styled by Spoofax.
     * @param color the color
     * @param text the text
     */
    public StyledText(Color color, String text) {
        this.source = Lists.newArrayList();
        this.append(color, text);
    }

    /**
     * Create source code styled by Spoofax.
     * @param text the text
     */
    public StyledText(String text) {
        this(null, text);
    }

    /**
     * Create source code styled by Spoofax.
     * @param sourceRegions the source
     */
    public StyledText(Iterable<IRegionStyle<StrategoString>> sourceRegions) {
        this.source = Lists.newArrayList();
        sourceRegions.iterator().forEachRemaining(e ->
                this.append(e.region(), e.style(), e.fragment().stringValue()));
    }

    /**
     * @return the source
     */
    public List<IRegionStyle<String>> getSource() {
        return source;
    }

    /**
     * Append to this styled source.
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
     * Append to this styled source.
     * @param style the style
     * @param text the text
     * @return this
     */
    public StyledText append(IStyle style, String text) {
        return this.append(null, style, text);
    }

    /**
     * Append to this source region.
     * @param color the color
     * @param text the text
     * @return this
     */
    public StyledText append(Color color, String text) {
        return this.append(null, new Style(color, null, false, false, false), text);
    }

    /**
     * Append to this source region.
     * @param text the text
     * @return this
     */
    public StyledText append(String text) {
        return this.append(null, null, text);
    }

    @Override
    public String toString() {
        return source.stream().map(e -> e.fragment()).collect(Collectors.joining());
    }
}
