package io.lpdf;

import java.util.List;

/**
 * The fonts, images and color profiles the document declares, as the <code>assets</code> element of the XML does. A
 * font or an image is picked by its name: a text sets <code>font</code> to a font's name, an <code>img</code> sets
 * <code>name</code> to an image's, and the output's <code>profile</code> names a profile.
 */
public final class DocumentAssets extends Attributes {
    /** Assets with none declared. */
    public DocumentAssets() {
        super("assets");
    }

    /** The fonts a text can be set in. */
    public DocumentAssets fonts(List<FontAttr> fonts) {
        set("fonts", fonts == null ? null : List.copyOf(fonts));
        return this;
    }

    /** The images an <code>img</code> can show. */
    public DocumentAssets images(List<ImageAttr> images) {
        set("images", images == null ? null : List.copyOf(images));
        return this;
    }

    /**
     * ICC color profiles: the profile of the printing press a PDF/X document is made for, which the printer gives out.
     * Each is read from its <code>src</code> through the engine's <code>srcFallback</code>, or loaded with
     * <code>loadProfile</code> under its <code>ref</code> or name.
     */
    public DocumentAssets profiles(List<ProfileAttr> profiles) {
        set("profiles", profiles == null ? null : List.copyOf(profiles));
        return this;
    }
}
