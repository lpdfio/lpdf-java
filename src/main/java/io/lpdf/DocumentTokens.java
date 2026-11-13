package io.lpdf;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Design tokens for the whole document, as the <code>tokens</code> element of the XML: the colors, spot colors,
 * gradients and rulings it names, the aliases of the slots of its scales, and the rows of sizes, each from <code>xs</code> to
 * <code>xxl</code>. A row given here replaces the default one.
 */
public final class DocumentTokens extends Attributes {
    /** Tokens with none set: the defaults. */
    public DocumentTokens() {
        super("tokens");
    }

    /** The colors, by name: <code>Map.of("brand", "#1a73e8")</code> lets <code>color("brand")</code> name it. */
    public DocumentTokens colors(Map<String, String> colors) {
        set("colors", colors == null ? null : new LinkedHashMap<>(colors));
        return this;
    }

    /**
     * The spot colors, by name, each a {@link Spot}: an ink of its own printed from a plate of its own, with the color
     * that stands in for it on a screen or a proof, and whether it overprints. A color takes the name, or a tint of the
     * ink after a space: <code>background("brand 30%")</code>.
     */
    public DocumentTokens spots(Map<String, Spot> spots) {
        set("spots", spots == null ? null : new LinkedHashMap<>(spots));
        return this;
    }

    /**
     * The gradients, by name, each a {@link Gradient}: its type, <code>linear</code> or <code>radial</code>, from and to,
     * or its stops, and its angle in degrees. A background or a fill uses one as <code>gradient:name</code>.
     */
    public DocumentTokens gradients(Map<String, Gradient> gradients) {
        set("gradients", gradients == null ? null : new LinkedHashMap<>(gradients));
        return this;
    }

    /**
     * The rulings, by name, each a {@link Ruling}: its lines, each set cut by a step or a count in x, in y or both, and
     * its origin, edges and ocg. A section draws one on every page with <code>ruling("name")</code>.
     */
    public DocumentTokens rulings(Map<String, Ruling> rulings) {
        set("rulings", rulings == null ? null : new LinkedHashMap<>(rulings));
        return this;
    }

    /**
     * The aliases, each a name for the slot of a scale it stands for: <code>Map.of("caption", "xs")</code> lets
     * <code>fontSize("caption")</code> mean the text size <code>xs</code>, and <code>gap("caption")</code> the space
     * <code>xs</code>.
     */
    public DocumentTokens aliases(Map<String, String> aliases) {
        set("aliases", aliases == null ? null : new LinkedHashMap<>(aliases));
        return this;
    }

    /** The row of spaces, such as a gap or a padding, from <code>xs</code> to <code>xxl</code>. */
    public DocumentTokens space(Map<String, String> space) {
        set("space", space == null ? null : new LinkedHashMap<>(space));
        return this;
    }

    /** The row of grid gaps, from <code>xs</code> to <code>xxl</code>. */
    public DocumentTokens grid(Map<String, String> grid) {
        set("grid", grid == null ? null : new LinkedHashMap<>(grid));
        return this;
    }

    /** The row of border thicknesses, from <code>xs</code> to <code>xxl</code>. */
    public DocumentTokens border(Map<String, String> border) {
        set("border", border == null ? null : new LinkedHashMap<>(border));
        return this;
    }

    /** The row of corner radii, from <code>xs</code> to <code>xxl</code>. */
    public DocumentTokens radius(Map<String, String> radius) {
        set("radius", radius == null ? null : new LinkedHashMap<>(radius));
        return this;
    }

    /** The row of widths, from <code>xs</code> to <code>xxl</code>. */
    public DocumentTokens width(Map<String, String> width) {
        set("width", width == null ? null : new LinkedHashMap<>(width));
        return this;
    }

    /** The row of heights that <code>height("m")</code> names, from <code>xs</code> to <code>xxl</code>. */
    public DocumentTokens height(Map<String, String> height) {
        set("height", height == null ? null : new LinkedHashMap<>(height));
        return this;
    }

    /** The row of text sizes, from <code>xs</code> to <code>xxl</code>. */
    public DocumentTokens textSize(Map<String, String> textSize) {
        set("text-size", textSize == null ? null : new LinkedHashMap<>(textSize));
        return this;
    }

    /** The row of line heights, each a factor of the font size above 0, from <code>xs</code> to <code>xxl</code>. */
    public DocumentTokens lineHeight(Map<String, Double> lineHeight) {
        set("line-height", lineHeight == null ? null : new LinkedHashMap<>(lineHeight));
        return this;
    }
}
