package io.lpdf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * The {@code src} of a font, an image, a file and a file of Markdown rules is found in an XML document as the engine
 * reads the document: entities decoded, a {@code >} in a quoted value, either quote, and what is in a comment or a
 * CDATA section is text.
 */
class XmlSrcTest {
    private static final byte[] PIXEL = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==");

    private static final String RED_TITLES = "<markdown-styles><h1 color=\"#ff0000\"/></markdown-styles>";

    /** A src fallback that hands out the files it holds and keeps each {@code src} it is asked for. */
    private static final class RecordingFiles implements SrcReader {
        final List<String> asked = new ArrayList<>();
        private final Map<String, byte[]> files;

        RecordingFiles(Map<String, byte[]> files) {
            this.files = files;
        }

        @Override
        public byte[] read(String src) throws FileNotFoundException {
            asked.add(src);
            var bytes = files.get(src);
            if (bytes == null) throw new FileNotFoundException(src);
            return bytes;
        }
    }

    private static String xml(String assets, String attachments, String section) {
        return "<lpdf version=\"1\"><assets>" + assets + "</assets><document><attachments>" + attachments
                + "</attachments><section>" + section + "</section></document></lpdf>";
    }

    private static String markdownXml(String styles) {
        return xml("", "", "<markdown>" + styles + "<markdown-content># Title</markdown-content></markdown>");
    }

    private static PdfEngine engine(RecordingFiles files) {
        return L.engine(new EngineOptions().srcFallback(files)).setLicenseKey("test-key");
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        "<font name=\"brand\" ref=\"R&amp;D Sans\" src=\"R&amp;D Sans.ttf\"/> | R&D Sans.ttf | R&D Sans",
        "<font name=\"brand\" ref=\"rows > 1000\" src=\"brand.ttf\"/>          | brand.ttf    | rows > 1000",
        "<font name='brand' src='brand.ttf'/>                                 | brand.ttf    | brand",
    })
    void a_font_is_read_from_its_src_and_loaded_under_its_ref_or_name(String font, String src, String key) {
        var files = new RecordingFiles(Map.of(src, "not a font".getBytes(StandardCharsets.UTF_8)));

        // The bytes are not a font, so the engine says which font it was given them for.
        var error = assertThrows(EngineException.class,
                () -> engine(files).render(xml(font, "", "<layout><text font=\"brand\">x</text></layout>")));

        assertEquals(List.of(src), files.asked);
        assertTrue(error.getMessage().contains("font '" + key + "'"), error.getMessage());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        "<image name=\"logo\" ref=\"R&amp;D logo\" src=\"R&amp;D logo.png\"/> | R&D logo.png",
        "<image name=\"logo\" ref=\"rows > 1000\" src=\"logo.png\"/>          | logo.png",
        "<image name='logo' src='logo.png'/>                                 | logo.png",
    })
    void an_image_is_read_from_its_src_and_loaded_under_its_ref_or_name(String image, String src) {
        var files = new RecordingFiles(Map.of(src, PIXEL));

        // The engine refuses an image it has no bytes for under its ref, or with no ref its name.
        var pdf = engine(files).render(xml(image, "", "<layout><img name=\"logo\" width=\"40pt\"/></layout>"));

        assertEquals(List.of(src), files.asked);
        assertEquals("%PDF-", new String(pdf, 0, 5, StandardCharsets.US_ASCII));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        "<file name=\"R&amp;D.csv\" src=\"R&amp;D.csv\"/>                     | R&D.csv",
        "<file name=\"rows.csv\" description=\"rows > 1000\" src=\"rows.csv\"/> | rows.csv",
        "<file name='rows.csv' src='rows.csv'/>                              | rows.csv",
    })
    void a_file_is_read_from_its_src_and_loaded_under_its_ref_or_name(String file, String src) {
        var files = new RecordingFiles(Map.of(src, "a,b".getBytes(StandardCharsets.UTF_8)));

        // The engine refuses a file it has no bytes for under its ref, or with no ref its name.
        var pdf = engine(files).render(xml("", file, "<layout><text>x</text></layout>"));

        assertEquals(List.of(src), files.asked);
        var latin1 = new String(pdf, StandardCharsets.ISO_8859_1);
        assertEquals(1, Pattern.compile("/Type /EmbeddedFile").matcher(latin1).results().count());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        "<markdown-styles src=\"R&amp;D styles.xml\"/>                       | R&D styles.xml",
        "<markdown-styles xmlns:rows=\"urn:rows>1000\" src=\"styles.xml\"/>   | styles.xml",
        "<markdown-styles src='styles.xml'/>                                | styles.xml",
    })
    void a_file_of_rules_is_read_from_the_src_of_its_styles(String styles, String src) {
        var files = new RecordingFiles(Map.of(src, RED_TITLES.getBytes(StandardCharsets.UTF_8)));
        var engine = engine(files);

        var inline = engine.render(markdownXml(RED_TITLES));
        // The engine refuses a src whose file was not loaded.
        var fromSrc = engine.render(markdownXml(styles));

        assertEquals(List.of(src), files.asked);
        assertEquals(normalised(inline), normalised(fromSrc));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "<assets><font name=\"brand\" src=\"hidden.ttf\"/></assets>",
        "<assets><image name=\"logo\" src=\"hidden.png\"/></assets>",
        "<attachments><file name=\"rows.csv\" src=\"hidden.csv\"/></attachments>",
        "<markdown><markdown-styles src=\"hidden.xml\"/></markdown>",
    })
    void a_src_in_a_comment_or_a_cdata_section_is_text_and_is_not_read(String declaration) {
        var files = new RecordingFiles(Map.of());

        var pdf = engine(files).render(xml("", "",
                "<layout><!-- " + declaration + " --><code><![CDATA[" + declaration + "]]></code></layout>"));

        assertEquals(List.of(), files.asked);
        assertEquals("%PDF-", new String(pdf, 0, 5, StandardCharsets.US_ASCII));
    }

    private static String normalised(byte[] pdf) {
        var text = new String(pdf, StandardCharsets.ISO_8859_1);
        text = text.replaceAll("/CreationDate[^\\n]*", "");
        return text.replaceAll("/ID *\\[[^\\]]*\\]", "");
    }
}
