// Generated from lpdf.xsd by scripts/gen-sdk-api.mjs.
// Do not edit: change the schema and run `make gen-sdk-api`.
package io.lpdf;

/** Attributes of the <code>field</code> element. */
public final class FieldAttr extends Attributes {
    /** Attributes with none set. */
    public FieldAttr() {
        super("field", "type", "name");
    }

    public FieldAttr type(String type) {
        set("type", type);
        return this;
    }

    public FieldAttr name(String name) {
        set("name", name);
        return this;
    }

    public FieldAttr value(String value) {
        set("value", value);
        return this;
    }

    public FieldAttr label(String label) {
        set("label", label);
        return this;
    }

    public FieldAttr options(String options) {
        set("options", options);
        return this;
    }

    public FieldAttr group(String group) {
        set("group", group);
        return this;
    }

    public FieldAttr checked(boolean checked) {
        set("checked", checked);
        return this;
    }

    public FieldAttr required(boolean required) {
        set("required", required);
        return this;
    }

    public FieldAttr readonly(boolean readonly) {
        set("readonly", readonly);
        return this;
    }

    public FieldAttr maxLen(int maxLen) {
        set("max-len", maxLen);
        return this;
    }

    public FieldAttr actionUrl(String actionUrl) {
        set("action-url", actionUrl);
        return this;
    }

    /**
     * Width of the element. A length fixes it. fill, in a row, takes the width its line leaves after the other
     * children, shared equally if several on the line use fill, and never moves to the next line; in a column it takes
     * the full width.
     */
    public FieldAttr width(String width) {
        set("width", width);
        return this;
    }

    public FieldAttr height(String height) {
        set("height", height);
        return this;
    }

    public FieldAttr background(String background) {
        set("background", background);
        return this;
    }

    public FieldAttr border(String border) {
        set("border", border);
        return this;
    }

    public FieldAttr debug(boolean debug) {
        set("debug", debug);
        return this;
    }

    /**
     * Puts the element, and everything in it, in an optional content group, which the viewer shows or hides. print:
     * printed, and not shown on screen. screen: shown on screen, and not printed. The element keeps its room either
     * way, so nothing moves. An element with a group inside an element with another shows only where both show.
     * Acrobat, Chrome and Firefox switch the groups; Preview and Safari show everything, on screen and in print. Hidden
     * content is still in the file, where it can be selected and copied: it is not a way to keep anything secret. A
     * link in print content is drawn and does nothing, and a form field can't be in print content: use a frame with a
     * border for a box filled in by hand. With pdfa or pdfua on the output, everything shows. With pdfx, the file is
     * the print version: screen content stays in it, off, and a reader can turn it on in a layers panel.
     */
    public FieldAttr ocg(String ocg) {
        set("ocg", ocg);
        return this;
    }
}
