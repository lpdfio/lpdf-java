package io.lpdf;

import static io.lpdf.L.NoAttr;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import run.endive.runtime.ByteArrayMemory;
import run.endive.runtime.Instance;
import run.endive.runtime.Memory;
import run.endive.runtime.TrapException;
import run.endive.wabt.Wat2Wasm;
import run.endive.wasm.Parser;
import run.endive.wasm.types.MemoryLimits;

/** Big documents, the memory cap of a render, how deep a document may nest, and an engine that crashes. */
class MemoryTest {
    private static final String CAP_ERROR = "the render needs more memory than its cap of ";
    private static final String DEPTH_ERROR = "is nested 101 levels deep, past the limit of 100";

    @Test
    void bench_max_copied_16_times_renders_its_3008_pages() {
        assumeTrue(Snapshots.present(), "the fixtures of core are not here");
        var xml = benchMaxCopied(16);
        assertTrue(xml.length() > 16_000_000);

        var pdf = L.engine(new EngineOptions().timeout(0)).render(xml);

        assertEquals(3008, pageCount(pdf));
    }

    @Test
    void a_document_tree_whose_json_is_over_4_mb_renders() {
        var line = "A paragraph of a long report, with enough words in it to fill a line or two of the page. ".repeat(2);
        var sections = IntStream.range(0, 1200)
                .mapToObj(s -> L.section(NoAttr, List.of(L.layout(NoAttr, IntStream.range(0, 20)
                        .mapToObj(i -> L.text(NoAttr, List.of(s + "." + i + " " + line)))
                        .toList()))))
                .toList();
        var tree = L.document(NoAttr, sections);
        assertTrue(Json.write(tree).length() > 4 * 1_048_576);

        var pdf = L.engine(new EngineOptions().timeout(0)).render(tree);

        assertTrue(pageCount(pdf) >= 1200);
    }

    @Test
    void a_render_past_the_cap_stops_with_its_error_and_the_next_renders_right() {
        assumeTrue(Snapshots.present(), "the fixtures of core are not here");
        var engine = L.engine(new EngineOptions().maxMemoryMb(8)).setLicenseKey("test-key");

        var error = assertThrows(EngineException.class, () -> engine.render(Snapshots.fixture("bench_max")));

        assertTrue(error.getMessage().startsWith(CAP_ERROR + "8 MB"), error.getMessage());
        Snapshots.assertMatches("bench_m",
                engine.render(Snapshots.fixture("bench_m"), new RenderOptions().skipEncryption(true)));
    }

    @Test
    void the_same_document_renders_under_a_higher_cap() {
        assumeTrue(Snapshots.present(), "the fixtures of core are not here");

        var pdf = L.engine(new EngineOptions().maxMemoryMb(64)).render(Snapshots.fixture("bench_max"));

        assertEquals(188, pageCount(pdf));
    }

    @Test
    void a_big_data_object_counts_toward_the_cap() {
        var xml = "<lpdf version=\"1\"><document><section><layout><text data-value=\"title\">x</text></layout></section></document></lpdf>";
        var rows = IntStream.range(0, 200_000).mapToObj(i -> Map.of("name", "Row " + i, "note", "Some text that is bound")).toList();
        var data = Map.of("title", "Report", "rows", rows);

        var error = assertThrows(EngineException.class,
                () -> L.engine(new EngineOptions().maxMemoryMb(8)).render(xml, new RenderOptions().data(data)));
        assertTrue(error.getMessage().startsWith(CAP_ERROR + "8 MB"), error.getMessage());
        var pdf = L.engine(new EngineOptions().maxMemoryMb(256)).render(xml, new RenderOptions().data(data));
        assertEquals(1, pageCount(pdf));
    }

    @Test
    void the_cap_is_1024_mb_when_left_out() {
        assertEquals(1024, new EngineOptions().maxMemoryMb());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 4097})
    void a_cap_below_1_or_above_4096_is_refused(int megabytes) {
        assertThrows(IllegalArgumentException.class, () -> new EngineOptions().maxMemoryMb(megabytes));
    }

    @Test
    void a_document_nested_100_levels_deep_renders_as_xml_and_as_a_tree() {
        var engine = L.engine();

        assertEquals(1, pageCount(engine.render(nestedXml(100))));
        assertEquals(1, pageCount(engine.render(nestedTree(100))));
    }

    @Test
    void a_document_nested_101_levels_deep_fails_with_the_depth_error_as_xml_and_as_a_tree() {
        var engine = L.engine();

        var xml = assertThrows(EngineException.class, () -> engine.render(nestedXml(101)));
        var tree = assertThrows(EngineException.class, () -> engine.render(nestedTree(101)));

        assertTrue(xml.getMessage().contains(DEPTH_ERROR), xml.getMessage());
        assertTrue(xml.getMessage().startsWith("<text> at line 1"), xml.getMessage());
        assertTrue(tree.getMessage().contains(DEPTH_ERROR), tree.getMessage());
        assertTrue(tree.getMessage().startsWith("the node 'text'"), tree.getMessage());
    }

    @Test
    void a_tree_nested_100_levels_deep_reads_the_src_of_the_file_it_carries() throws IOException {
        var terms = Files.createTempFile("terms", ".txt");
        Files.writeString(terms, "Terms of sale");
        try {
            var attrs = new DocumentAttr().attachments(List.of(new FileAttr().name("terms.txt").src(terms.toString())));
            var engine = L.engine(new EngineOptions().srcFallback(src -> Files.readAllBytes(Path.of(src))));

            var pdf = engine.render(nestedTree(100, attrs));

            assertEquals(1, Pattern.compile("/Type /EmbeddedFile").matcher(latin1(pdf)).results().count());
        } finally {
            Files.delete(terms);
        }
    }

    /**
     * A stand-in module that traps in place of the engine, as the engine does when its memory runs out: Rust aborts,
     * which is WebAssembly's <code>unreachable</code>.
     */
    @Test
    void a_crash_throws_the_crash_message_with_the_trap_inside() {
        var engine = TimeoutTest.standIn("(module (func (export \"_start\") unreachable))", new EngineOptions());

        var error = assertThrows(EngineException.class, () -> engine.render(nestedXml(10)));

        assertEquals("The engine crashed during the render. This is a bug in Lpdf, or the render ran out of memory before "
                + "its cap stopped it: please report it with the document at https://github.com/lpdfio/lpdf/issues.",
                error.getMessage());
        assertInstanceOf(TrapException.class, error.getCause());
    }

    /**
     * A stand-in module whose memory can't grow, as when the JVM's heap is full: Endive keeps the engine's memory on the
     * heap, so a heap with less room than the cap runs out before the cap stops the render.
     */
    @Test
    void a_heap_that_runs_out_before_the_cap_throws_an_engine_exception_saying_so() {
        var module = Parser.parse(Wat2Wasm.parse(
                "(module (memory 1) (func (export \"_start\") (drop (memory.grow (i32.const 1)))))"));
        var options = new EngineOptions().maxMemoryMb(512);
        var engine = new PdfEngine(options, new WasmRunner(
                () -> Instance.builder(module).withMemoryFactory(MemoryTest::fullHeap),
                options.timeout(), options.maxMemoryMb()));

        var error = assertThrows(EngineException.class, () -> engine.render(nestedXml(10)));

        assertEquals("The JVM's heap ran out during the render, before its cap of 512 MB stopped it: give the JVM more "
                + "heap (-Xmx) or set a lower maxMemoryMb.", error.getMessage());
        assertInstanceOf(OutOfMemoryError.class, error.getCause());
    }

    /** Endive's memory on a heap with no room for another page: growing it throws, as Endive's does then. */
    private static Memory fullHeap(MemoryLimits limits) {
        var memory = new ByteArrayMemory(limits);
        return (Memory) Proxy.newProxyInstance(Memory.class.getClassLoader(), new Class<?>[] {Memory.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("grow")) throw new OutOfMemoryError("Java heap space");
                    try {
                        return method.invoke(memory, args);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                });
    }

    /** <code>bench_max.xml</code> with its sections copied: 188 pages and 1 MB of XML for each copy. */
    private static String benchMaxCopied(int copies) {
        var xml = Snapshots.fixture("bench_max");
        var start = xml.indexOf("<section");
        var end = xml.lastIndexOf("</section>") + "</section>".length();
        return xml.substring(0, start) + xml.substring(start, end).repeat(copies) + xml.substring(end);
    }

    /** A document whose columns nest so that its deepest element is {@code depth} levels deep. */
    private static String nestedXml(int depth) {
        // <lpdf>, <document>, <section>, <layout> and the <text> in the deepest column.
        var columns = depth - 5;
        return "<lpdf version=\"1\"><document><section><layout>" + "<column>".repeat(columns) + "<text>deep</text>"
                + "</column>".repeat(columns) + "</layout></section></document></lpdf>";
    }

    /** The tree of {@link #nestedXml}. */
    private static PdfDocument nestedTree(int depth) {
        return L.document(NoAttr, nestedSections(depth));
    }

    /** The tree of {@link #nestedXml}, with the document's {@code attrs}. */
    private static PdfDocument nestedTree(int depth, DocumentAttr attrs) {
        return L.document(attrs, nestedSections(depth));
    }

    private static List<SectionNode> nestedSections(int depth) {
        Node node = L.text(NoAttr, List.of("deep"));
        for (var i = 0; i < depth - 5; i++) node = L.column(NoAttr, List.of(node));
        return List.of(L.section(NoAttr, List.of(L.layout(NoAttr, List.of(node)))));
    }

    /** The number of pages of a PDF: its page objects, which the engine writes uncompressed. */
    static long pageCount(byte[] pdf) {
        return Pattern.compile("/Type /Page\\b(?!s)").matcher(latin1(pdf)).results().count();
    }

    private static String latin1(byte[] pdf) {
        return new String(pdf, StandardCharsets.ISO_8859_1);
    }
}
