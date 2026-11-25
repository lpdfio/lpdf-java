package io.lpdf;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Builds the parts of a chart: its series, its categories and their values. The chart itself is {@link L#chart}.
 *
 * <pre>{@code
 * L.chart(new ChartAttr().type(ChartType.BAR).height("160pt").prefix("$"), List.of(
 *     LChart.series(new ChartSeriesAttr().label("Revenue")),
 *     LChart.category(new ChartCategoryAttr().label("Q1"), List.of(LChart.value(NoAttr, List.of(120000))))
 * ))
 * }</pre>
 */
public final class LChart {
    private LChart() {
    }

    /** Builds a series of a chart: its entry in the legend and the color of its bars, line or area. */
    public static ChartSeriesNode series(ChartSeriesAttr attrs) {
        return new ChartSeriesNode(Attributes.of(attrs));
    }

    /** Builds a series of a chart with no attributes. */
    public static ChartSeriesNode series(NoAttributes attrs) {
        return series((ChartSeriesAttr) null);
    }

    /** Builds a category of a chart: a value for each series, in their order, and its label. */
    public static ChartCategoryNode category(ChartCategoryAttr attrs, List<ChartValueNode> nodes) {
        return new ChartCategoryNode(Attributes.of(attrs), KitNode.children("category", nodes));
    }

    /** Builds a category of a chart with no attributes. */
    public static ChartCategoryNode category(NoAttributes attrs, List<ChartValueNode> nodes) {
        return category((ChartCategoryAttr) null, nodes);
    }

    /**
     * Builds a value of a category: a number, written in full with no exponent and no locale, or a string that is one,
     * such as <code>"0.250"</code>, taken as written so its label keeps its decimals. An empty list is no value, which
     * draws no bar and breaks a line; a stacked area counts it as zero. It has no attributes.
     *
     * @throws IllegalArgumentException when an item is not a number or a string, or a number is not finite
     */
    public static ChartValueNode value(NoAttributes attrs, List<?> nodes) {
        var texts = new ArrayList<String>(nodes == null ? 0 : nodes.size());
        for (var item : nodes == null ? List.of() : nodes) {
            texts.add(text(item, texts.size() + 1));
        }
        return new ChartValueNode(Collections.unmodifiableList(texts));
    }

    /** Builds no value, which draws no bar and breaks a line; a stacked area counts it as zero. */
    public static ChartValueNode value(NoAttributes attrs) {
        return value(attrs, List.of());
    }

    /** The text of a value: a string as written, a number in full. */
    private static String text(Object item, int place) {
        if (item instanceof String text) return text;
        if (item instanceof Integer || item instanceof Long || item instanceof Short || item instanceof Byte
                || item instanceof BigInteger) {
            return item.toString();
        }
        if (item instanceof BigDecimal number) return number.toPlainString();
        if (item instanceof Double number) return plainNumber(number, place);
        if (item instanceof Float number) return plainNumber(Double.parseDouble(number.toString()), place);
        if (item == null) throw new IllegalArgumentException("value " + place + " of <value> is null");
        throw new IllegalArgumentException(
                "value " + place + " of <value> must be a number or a string, not " + item.getClass().getSimpleName());
    }

    /**
     * A double in its shortest digits, with its exponent written out and a whole number without its <code>.0</code>:
     * <code>1.0E21</code> is <code>1000000000000000000000</code>, as the other SDKs write it.
     */
    private static String plainNumber(double value, int place) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("value " + place + " of <value> is " + value + ", and a chart's value is finite");
        }
        var plain = new BigDecimal(Double.toString(value)).stripTrailingZeros();
        return plain.signum() == 0 ? "0" : plain.toPlainString();
    }
}
