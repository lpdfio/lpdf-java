// Generated from lpdf.xsd by scripts/gen-sdk-api.mjs.
// Do not edit: change the schema and run `make gen-sdk-api`.
package io.lpdf;

/**
 * The flowing content of a section. Elements stack down the page, and a new page starts when one runs out of room. Put
 * headers and footers in a region, which sits outside the flow.
 */
public final class LayoutAttr extends Attributes {
    /** Attributes with none set. */
    public LayoutAttr() {
        super("layout");
    }

    /**
     * The space between the page edges and the flowing content and its regions: one value for all four sides, or two to
     * four in the order top, right, bottom, left. Defaults to 36pt. Layers ignore it and measure from the page edges.
     */
    public LayoutAttr margin(String margin) {
        set("margin", margin);
        return this;
    }
}
