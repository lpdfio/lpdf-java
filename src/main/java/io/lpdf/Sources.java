package io.lpdf;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The files a document names by their <code>src</code>, and their bytes: the fonts, images and color profiles of its
 * assets, the files of its attachments, and the files of Markdown rules of its markdowns. The engine is WebAssembly
 * with no file system, so the SDK reads them and sends them with the request.
 */
final class Sources {
    /** The <code>ref</code>, or with no ref the <code>name</code>, of each font of the assets, and its src. */
    final Map<String, String> fonts = new LinkedHashMap<>();
    final Map<String, String> images = new LinkedHashMap<>();
    final Map<String, String> profiles = new LinkedHashMap<>();
    /** The <code>ref</code>, or with no ref the <code>name</code>, of each file of the attachments, and its src. */
    final Map<String, String> files = new LinkedHashMap<>();
    /** The <code>src</code> of the styles of each markdown. */
    final List<String> markdownStyles = new ArrayList<>();

    private Sources() {
    }

    // ── Finding them ──────────────────────────────────────────────────────────

    /**
     * The files an XML document names, read with an XML parser, as the engine reads it: entities decoded, and what is
     * in a comment or CDATA left out. XML that doesn't parse, or has a DTD, names no file here; the engine says what is
     * wrong with it.
     */
    static Sources ofXml(String xml) {
        var sources = new Sources();
        try {
            var factory = SAXParserFactory.newInstance();
            // No DTD, so no entity of the document reaches outside it.
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.newSAXParser().parse(new InputSource(new StringReader(xml)), sources.new Reader());
        } catch (SAXException | IOException | ParserConfigurationException e) {
            return new Sources();
        }
        return sources;
    }

    private final class Reader extends DefaultHandler {
        private final ArrayDeque<String> open = new ArrayDeque<>();

        @Override
        public void startElement(String uri, String localName, String name, Attributes attributes) {
            var parent = open.peek();
            open.push(name);
            if ("assets".equals(parent)) {
                switch (name) {
                    case "font" -> declared(fonts, attributes);
                    case "image" -> declared(images, attributes);
                    case "profile" -> declared(profiles, attributes);
                    default -> { }
                }
            } else if ("attachments".equals(parent) && "file".equals(name)) {
                declared(files, attributes);
            } else if ("markdown".equals(parent) && "markdown-styles".equals(name)) {
                var src = attributes.getValue("src");
                if (src != null && !src.isEmpty()) markdownStyles.add(src);
            }
        }

        @Override
        public void endElement(String uri, String localName, String name) {
            open.pop();
        }

        private void declared(Map<String, String> into, Attributes attributes) {
            var ref = attributes.getValue("ref");
            var key = ref != null ? ref : attributes.getValue("name");
            var src = attributes.getValue("src");
            if (key != null && !key.isEmpty() && src != null && !src.isEmpty()) into.put(key, src);
        }
    }

    /** The files a document tree names: in its assets and attachments, and in the styles of its sections' markdowns. */
    static Sources ofTree(PdfDocument document) {
        var sources = new Sources();
        var attrs = document.attrs();
        if (attrs.get("assets") instanceof Map<?, ?> assets) {
            declared(sources.fonts, assets.get("fonts"));
            declared(sources.images, assets.get("images"));
            declared(sources.profiles, assets.get("profiles"));
        }
        declared(sources.files, attrs.get("attachments"));
        for (var section : document.nodes()) {
            for (var content : ((KitNode) section).nodes()) {
                if (!(content instanceof SectionMarkdown markdown)) continue;
                for (var part : markdown.nodes()) {
                    if (part instanceof MarkdownStylesNode styles && styles.attrs().get("src") instanceof String src
                            && !src.isEmpty()) {
                        sources.markdownStyles.add(src);
                    }
                }
            }
        }
        return sources;
    }

    private static void declared(Map<String, String> into, Object list) {
        if (!(list instanceof List<?> declared)) return;
        for (var item : declared) {
            if (!(item instanceof Map<?, ?> attrs) || !(attrs.get("src") instanceof String src)) continue;
            var key = attrs.get("ref") instanceof String ref ? ref : attrs.get("name");
            if (key instanceof String name) into.put(name, src);
        }
    }

    // ── Reading them ──────────────────────────────────────────────────────────

    /**
     * The fonts or images to send: the ones loaded, and the ones {@code reader} reads from the src of one that wasn't.
     * One that can't be read is left out, and the engine says the document names a font or an image it doesn't have.
     */
    static Map<String, byte[]> resolve(Map<String, String> srcs, Map<String, byte[]> loaded, SrcReader reader) {
        var merged = new LinkedHashMap<>(loaded);
        if (reader == null) return merged;
        srcs.forEach((key, src) -> {
            if (merged.containsKey(key)) return;
            try {
                merged.put(key, reader.read(src));
            } catch (IOException | RuntimeException e) {
                // The engine says that it has no bytes for it.
            }
        });
        return merged;
    }

    /**
     * The files a document carries, or its color profiles: the ones loaded, and the ones {@code reader} reads from the
     * src of one that wasn't. A src that can't be read, or that there is no reader for, is an error that names it, so
     * the engine's "no bytes" never stands for a path that was given.
     *
     * @param what    <code>file</code> or <code>profile</code>
     * @param element the element that holds them, <code>attachments</code> or <code>assets</code>
     * @param load    the method that loads one, <code>loadFile</code> or <code>loadProfile</code>
     */
    static Map<String, byte[]> require(Map<String, String> srcs, Map<String, byte[]> loaded, SrcReader reader,
                                       String what, String element, String load) {
        var merged = new LinkedHashMap<>(loaded);
        srcs.forEach((key, src) -> {
            if (merged.containsKey(key)) return;
            if (reader == null) {
                throw new EngineException(what + " '" + key + "' of <" + element + "> has the src '" + src
                        + "', and the engine has no srcFallback to read it: create it with new EngineOptions()"
                        + ".srcFallback(src -> Files.readAllBytes(Path.of(src))), or load the " + what + " with "
                        + load + "(\"" + key + "\", bytes)");
            }
            try {
                merged.put(key, reader.read(src));
            } catch (IOException | RuntimeException e) {
                throw new EngineException(what + " '" + key + "' of <" + element + ">: cannot read its src '" + src
                        + "': " + e.getMessage(), e);
            }
        });
        return merged;
    }

    /** The files of Markdown rules: the ones loaded, and the ones {@code reader} reads, as UTF-8 text. */
    static Map<String, String> styles(List<String> srcs, Map<String, String> loaded, SrcReader reader) {
        var merged = new LinkedHashMap<>(loaded);
        if (reader == null) return merged;
        for (var src : srcs) {
            if (merged.containsKey(src)) continue;
            try {
                merged.put(src, new String(reader.read(src), StandardCharsets.UTF_8));
            } catch (IOException | RuntimeException e) {
                // The engine says that the file was not loaded.
            }
        }
        return merged;
    }
}
