// Generated from lpdf.xsd by scripts/gen-sdk-api.mjs.
// Do not edit: change the schema and run `make gen-sdk-api`.
package io.lpdf;

/**
 * A chart drawn from numbers, written like a table: series are its columns, each category a row and each value a cell,
 * a value for each series in the series' order. A chart with one series needs no series. It takes its box in the flow
 * as an image does, never splits across pages, and moves whole to the next page when it doesn't fit. The engine draws
 * its axis, steps, gridlines, labels and legend by fixed rules: the value axis of bars and areas starts at zero, and a
 * line's fits its values, from a round step under the lowest to one over the highest; the axis runs in round steps,
 * each 1, 2 or 5 times a power of ten, written in full; a negative value takes the axis below zero, where a zero line
 * is drawn; a label wider than its room wraps at spaces onto three lines at most, and what still doesn't fit ends in an
 * ellipsis, so a chart always renders. A line or an area writes every 2nd, 3rd or 5th label where they don't all fit,
 * and every point keeps its place. A missing value breaks a line, an area and a sparkline, and counts as zero in a
 * stacked area. A sparkline draws no axis, labels or legend. A chart is a leaf, with no padding, background or border:
 * a column around it gives them, and a text above it is its title.
 */
public final class ChartAttr extends Attributes {
    /** Attributes with none set. */
    public ChartAttr() {
        super("chart", "type", "height");
    }

    /**
     * The kind of chart. bar draws a bar for each value, side by side within a category, or stacked with
     * stacked="true". line draws a line through the values of each series, on an axis that fits them. area draws a line
     * filled down to zero, or with stacked="true" each series filled down to the one under it. sparkline draws one
     * small line through the whole box, with no axis, labels or legend, for a table cell.
     */
    public ChartAttr type(String type) {
        set("type", type);
        return this;
    }

    /**
     * Height of the whole chart, a length or a name from the height row of the tokens. The axis, the labels and the
     * legend take room inside it and the width, and the plot gets the rest. The width is fill unless it is set.
     */
    public ChartAttr height(String height) {
        set("height", height);
        return this;
    }

    /**
     * Which way the bars run. vertical, the default, puts the categories along the bottom and the value axis on the
     * left; horizontal lists the categories down the left side, which suits long labels, with the value axis along the
     * bottom. Bars only: refused on any other kind.
     */
    public ChartAttr direction(String direction) {
        set("direction", direction);
        return this;
    }

    /**
     * Stacks the bars of a category on each other, in the order of the series, or the areas of the series, each filled
     * down to the one under it, so the axis runs to the largest total. A stacked value can't be negative, and a missing
     * one counts as zero. Default false: side by side, or areas filled down to zero. Bars and areas only: refused on
     * any other kind.
     */
    public ChartAttr stacked(boolean stacked) {
        set("stacked", stacked);
        return this;
    }

    /**
     * Width of the element. A length fixes it. fill, in a row, takes the width its line leaves after the other
     * children, shared equally if several on the line use fill, and never moves to the next line; in a column it takes
     * the full width.
     */
    public ChartAttr width(String width) {
        set("width", width);
        return this;
    }

    /** Written before every number of the chart, the steps and the value labels: $ writes $41,200. */
    public ChartAttr prefix(String prefix) {
        set("prefix", prefix);
        return this;
    }

    /**
     * Written after every number of the chart: % writes 12%, and " ms" writes 12 ms. With values in thousands,
     * suffix="k" writes $412k.
     */
    public ChartAttr suffix(String suffix) {
        set("suffix", suffix);
        return this;
    }

    /**
     * The decimals of the value labels, 0 to 6. Left out, a value is written with the decimals it has. The steps of the
     * axis take the decimals their size needs.
     */
    public ChartAttr decimals(int decimals) {
        set("decimals", decimals);
        return this;
    }

    /**
     * A number the value axis reaches down to at least, which takes it below zero: min="-50". It only widens the axis:
     * a value below it still fits. On a line, whose axis fits its values, min="0" brings the axis down to zero. A
     * sparkline reaches it too, though it draws no axis.
     */
    public ChartAttr min(double min) {
        set("min", min);
        return this;
    }

    /**
     * A number the value axis reaches at least: max="100" for percentages, or the same max on charts meant to be
     * compared side by side, such as the sparklines of a table. It only widens the axis: a value above it still fits.
     */
    public ChartAttr max(double max) {
        set("max", max);
        return this;
    }

    /**
     * The character between the groups of three digits of a number, or empty for none. Default ,: with decimal-mark=","
     * and thousands-mark=".", 1234.5 is written 1.234,5; with a space, 1 234,5.
     */
    public ChartAttr thousandsMark(String thousandsMark) {
        set("thousands-mark", thousandsMark);
        return this;
    }

    /** The character before the decimals of a number. Default ., and it can't be the thousands-mark. */
    public ChartAttr decimalMark(String decimalMark) {
        set("decimal-mark", decimalMark);
        return this;
    }

    /**
     * The space between the plot and the axis labels, and between them and the legend. Default s, from the space row of
     * the tokens.
     */
    public ChartAttr gap(String gap) {
        set("gap", gap);
        return this;
    }

    /** The space between the bars of one category, side by side. Default a tenth of a bar. */
    public ChartAttr barGap(String barGap) {
        set("bar-gap", barGap);
        return this;
    }

    /**
     * The space between categories. Default a quarter of the room of a category. The bars take the room the two gaps
     * leave.
     */
    public ChartAttr categoryGap(String categoryGap) {
        set("category-gap", categoryGap);
        return this;
    }

    /**
     * Where the legend goes, which lists each series with its label and its color: bottom, top, right, or none. Default
     * bottom with two series or more, and none with one.
     */
    public ChartAttr legend(String legend) {
        set("legend", legend);
        return this;
    }

    /**
     * Writes each value at the end of its bar, or over its point and under a point lower than the points beside it,
     * where its line doesn't run, and the total of a stack at the end of the stack or over its top. Default false. A
     * sparkline writes none.
     */
    public ChartAttr valueLabels(boolean valueLabels) {
        set("value-labels", valueLabels);
        return this;
    }

    /**
     * The width of every line of a line, an area or a sparkline chart, a length such as 2pt: 1.5pt by default, and 1pt
     * for a sparkline. 0 draws no line, so an area is its fill alone. Every line takes the same width: which line is
     * the current one and which the comparison is told by the series' color.
     */
    public ChartAttr strokeWidth(String strokeWidth) {
        set("stroke-width", strokeWidth);
        return this;
    }

    public ChartAttr font(String font) {
        set("font", font);
        return this;
    }

    public ChartAttr fontSize(String fontSize) {
        set("font-size", fontSize);
        return this;
    }

    /**
     * The color of the chart's text, its labels, steps and legend, and of its zero line. Default the color of the text
     * around it. A sparkline, which has no text, draws its line in it, and without it in the first color of the
     * palette.
     */
    public ChartAttr color(String color) {
        set("color", color);
        return this;
    }

    /**
     * What the chart shows, which a screen reader reads in a tagged document, one with pdfua or a pdfa of level a:
     * "Revenue per month, highest in March". alt="" marks it as decoration. A PDF/UA document needs it on every chart
     * of the layout; at PDF/A level a without pdfua, a chart without it is decoration. A document that is not tagged
     * ignores it.
     */
    public ChartAttr alt(String alt) {
        set("alt", alt);
        return this;
    }
}
