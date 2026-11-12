package io.lpdf;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Renders PDFs: an XML template, or a document built with {@link L}. Make one with {@link L#engine()}, load the fonts,
 * images and files it needs, and render as many times as needed, from any thread: each render runs on the caller's
 * thread, in a fresh instance of the engine.
 *
 * <pre>{@code
 * var engine = L.engine().setLicenseKey(key);
 * byte[] pdf = engine.render(xml);
 * }</pre>
 */
public final class PdfEngine {
    /** Bytes of entropy sent with a request that has a password: one 256-bit key's worth. */
    private static final int ENTROPY_BYTES = 32;

    private static final SecureRandom RANDOM = new SecureRandom();

    private volatile String licenseKey = "";
    private final EngineOptions options;
    private final WasmRunner wasm;
    private final Map<String, byte[]> fonts = new ConcurrentHashMap<>();
    private final Map<String, byte[]> images = new ConcurrentHashMap<>();
    private final Map<String, byte[]> files = new ConcurrentHashMap<>();
    private final Map<String, byte[]> profiles = new ConcurrentHashMap<>();
    private final Map<String, String> markdownStyles = new ConcurrentHashMap<>();

    /** An engine with the options left out: a timeout of 5 seconds and a memory cap of 1024 MB. */
    public PdfEngine() {
        this(new EngineOptions());
    }

    /** An engine with {@code options}, which it reads now: a later change to them doesn't change the engine. */
    public PdfEngine(EngineOptions options) {
        this.options = copy(Objects.requireNonNull(options, "options"));
        this.wasm = new WasmRunner(this.options.timeout(), this.options.maxMemoryMb());
    }

    /** An engine that runs {@code wasm} in place of the engine's own, for the tests. */
    PdfEngine(EngineOptions options, WasmRunner wasm) {
        this.options = copy(options);
        this.wasm = wasm;
    }

    private static EngineOptions copy(EngineOptions options) {
        return new EngineOptions().timeout(options.timeout()).maxMemoryMb(options.maxMemoryMb())
                .srcFallback(options.srcFallback());
    }

    // ── License ───────────────────────────────────────────────────────────────

    /**
     * Sets the license key, and returns this engine. Without one, or with <code>""</code>, each PDF carries the
     * attribution line.
     */
    public PdfEngine setLicenseKey(String key) {
        licenseKey = Objects.requireNonNull(key, "key");
        return this;
    }

    /** What the engine makes of the key set on it: see {@link #checkLicenseKey(String)}. */
    public LicenseCheck checkLicenseKey() {
        return checkLicenseKey(licenseKey);
    }

    /**
     * What the engine makes of {@code key}: valid, expired, for another product, and which license and key it is. It
     * is the engine's own verdict, reached by the code a render runs, so {@link LicenseCheck#isLicensed()} means PDFs
     * come out without the attribution line here. A key the portal considers good still reads
     * <code>unknown_key</code> in a build that doesn't trust the key it was signed with.
     */
    public LicenseCheck checkLicenseKey(String key) {
        var answer = wasm.checkLicense(Objects.requireNonNull(key, "key"));
        return new LicenseCheck(
                answer.get("status") instanceof String status ? status : "malformed",
                answer.get("product") instanceof String product ? product : null,
                answer.get("tier") instanceof String tier ? tier : null,
                answer.get("expires") instanceof String expires ? expires : null,
                answer.get("license") instanceof String license ? license : null,
                answer.get("key") instanceof Number number ? Integer.valueOf(number.intValue()) : null);
    }

    // ── Loading ───────────────────────────────────────────────────────────────

    /** Loads a TTF or OTF font for the font of the assets whose <code>ref</code>, or with no ref whose name, is {@code name}. */
    public PdfEngine loadFont(String name, byte[] bytes) {
        fonts.put(Objects.requireNonNull(name, "name"), Objects.requireNonNull(bytes, "bytes"));
        return this;
    }

    /** Loads an image (JPEG, PNG, WebP, GIF, TIFF or BMP) for the image of the assets whose <code>ref</code>, or with no ref whose name, is {@code name}. */
    public PdfEngine loadImage(String name, byte[] bytes) {
        images.put(Objects.requireNonNull(name, "name"), Objects.requireNonNull(bytes, "bytes"));
        return this;
    }

    /**
     * Loads the bytes of a file the document carries, for the <code>file</code> of its <code>attachments</code> whose
     * <code>ref</code>, or with no ref whose name, is {@code name}. The PDF carries them as they are.
     */
    public PdfEngine loadFile(String name, byte[] bytes) {
        files.put(Objects.requireNonNull(name, "name"), Objects.requireNonNull(bytes, "bytes"));
        return this;
    }

    /**
     * Loads an ICC color profile, for the <code>profile</code> of the assets whose <code>ref</code>, or with no ref
     * whose name, is {@code name}: the press profile a PDF/X document names in its output.
     */
    public PdfEngine loadProfile(String name, byte[] bytes) {
        profiles.put(Objects.requireNonNull(name, "name"), Objects.requireNonNull(bytes, "bytes"));
        return this;
    }

    /**
     * Loads the text of a file of Markdown rules, whose root is a <code>markdown-styles</code>, for the {@code src} that
     * names it in <code>&lt;markdown-styles src="…"&gt;</code>.
     */
    public PdfEngine loadMarkdownStyles(String src, String text) {
        markdownStyles.put(Objects.requireNonNull(src, "src"), Objects.requireNonNull(text, "text"));
        return this;
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    /** Renders an XML template to a PDF: see {@link #render(String, RenderOptions)}. */
    public byte[] render(String xml) {
        return render(xml, new RenderOptions());
    }

    /**
     * Renders an XML template to a PDF, on the caller's thread.
     *
     * @throws EngineException          when the engine refuses the document, saying what to change, the render runs past
     *                                  the timeout or needs more memory than the cap or the JVM's heap has, or the
     *                                  engine crashes
     * @throws IllegalArgumentException when the data can't be written as JSON the engine reads: see
     *                                  {@link RenderOptions#data(Object)}
     */
    public byte[] render(String xml, RenderOptions options) {
        Objects.requireNonNull(xml, "xml");
        Objects.requireNonNull(options, "options");
        var request = request("render_pdf", xml, Sources.ofXml(xml), options);
        if (options.data() != null) request.put("data", new Json.Data(options.data()));
        else if (options.dataJson() != null) request.put("data", new Json.Raw(options.dataJson()));
        return wasm.pdf(request);
    }

    /** Renders a document built with {@link L} to a PDF: see {@link #render(PdfDocument, RenderOptions)}. */
    public byte[] render(PdfDocument document) {
        return render(document, new RenderOptions());
    }

    /**
     * Renders a document built with {@link L} to a PDF, on the caller's thread. Its data, which binds the
     * <code>data-*</code> attributes of an XML template, is not used.
     *
     * @throws EngineException when the engine refuses the document, saying what to change, the render runs past the
     *                         timeout or needs more memory than the cap or the JVM's heap has, or the engine crashes
     */
    public byte[] render(PdfDocument document, RenderOptions options) {
        Objects.requireNonNull(document, "document");
        Objects.requireNonNull(options, "options");
        return wasm.pdf(request("render_tree_pdf", Json.write(document), Sources.ofTree(document), options));
    }

    /**
     * Draws PNG images over the pages of an existing PDF, and returns the PDF with them, on the caller's thread. Nothing
     * is laid out: each image is drawn in its stamp's box, over the pages its <code>page</code> names.
     *
     * <p>The output is the input, byte for byte, with an incremental update after it, so an owner password and its
     * permissions stay, and so does a digital signature, for the revision it signed. A PDF/A file stays PDF/A. Without
     * a license key each stamped page gets the attribution line in its top-right corner. A stamp covers what is under
     * it and doesn't remove it: the text under an image can still be selected and found, so a stamp is not a
     * redaction.
     *
     * @param pdf    the bytes of the PDF to stamp
     * @param stamps the stamps, drawn in this order
     * @throws IllegalArgumentException when a stamp lacks its image, page, width or height
     * @throws EngineException          when the input is not a PDF, needs a password to open, or doesn't have a page a
     *                                  stamp names; an image is not a PNG or a stamp's options are wrong; or the stamp
     *                                  runs past the timeout or needs more memory than the cap or the JVM's heap has,
     *                                  or the engine crashes
     */
    public byte[] stamp(byte[] pdf, List<StampAttr> stamps) {
        Objects.requireNonNull(pdf, "pdf");
        Objects.requireNonNull(stamps, "stamps");
        var images = new LinkedHashMap<String, Object>();
        var names = new IdentityHashMap<byte[], String>();
        var list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < stamps.size(); i++) {
            var stamp = stamps.get(i);
            if (stamp == null) throw new NullPointerException("stamp " + (i + 1) + " is null");
            var node = new LinkedHashMap<>(stamp.values());
            var image = (byte[]) node.remove("image");
            var name = names.computeIfAbsent(image, bytes -> {
                var named = "stamp-" + names.size();
                images.put(named, Base64.getEncoder().encodeToString(bytes));
                return named;
            });
            var stamped = new LinkedHashMap<String, Object>();
            stamped.put("image", name);
            stamped.putAll(node);
            list.add(stamped);
        }
        var request = new LinkedHashMap<String, Object>();
        request.put("method", "stamp");
        request.put("key", licenseKey);
        request.put("pdf", Base64.getEncoder().encodeToString(pdf));
        request.put("images", images);
        request.put("stamps", list);
        return wasm.pdf(request);
    }

    // ── Requests ──────────────────────────────────────────────────────────────

    /** A render's request: the input, the files it names and the loaded ones, and the options of the render. */
    private Map<String, Object> request(String method, String input, Sources sources, RenderOptions options) {
        var reader = this.options.srcFallback();
        // Fonts without a reader are the loaded ones alone, as the .NET SDK sends them.
        var fonts = reader == null ? new LinkedHashMap<>(this.fonts) : Sources.resolve(sources.fonts, this.fonts, reader);
        var images = Sources.resolve(sources.images, this.images, reader);
        var files = Sources.require(sources.files, this.files, reader, "file", "attachments", "loadFile");
        var profiles = Sources.require(sources.profiles, this.profiles, reader, "profile", "assets", "loadProfile");
        var styles = Sources.styles(sources.markdownStyles, markdownStyles, reader);

        var request = new LinkedHashMap<String, Object>();
        request.put("method", method);
        request.put("key", licenseKey);
        request.put("input", input);
        request.put("fonts", base64(fonts));
        if (!images.isEmpty()) request.put("images", base64(images));
        if (!files.isEmpty()) request.put("files", base64(files));
        if (!profiles.isEmpty()) request.put("profiles", base64(profiles));
        if (!styles.isEmpty()) request.put("markdown_styles", styles);

        if (options.userPassword() != null) request.put("user_password", options.userPassword());
        if (options.ownerPassword() != null) request.put("owner_password", options.ownerPassword());
        if (options.skipEncryption()) request.put("skip_encryption", true);
        // Entropy for AES-256 with a password: random bytes the engine doesn't make, from the platform's secure
        // generator, fresh for each render.
        if (options.userPassword() != null || options.ownerPassword() != null) {
            var entropy = new byte[ENTROPY_BYTES];
            RANDOM.nextBytes(entropy);
            request.put("entropy", Base64.getEncoder().encodeToString(entropy));
        }
        if (options.createdOn() != null) request.put("created_on", options.createdOn());
        return request;
    }

    private static Map<String, String> base64(Map<String, byte[]> bytes) {
        var encoded = new LinkedHashMap<String, String>();
        bytes.forEach((name, value) -> encoded.put(name, Base64.getEncoder().encodeToString(value)));
        return encoded;
    }
}
