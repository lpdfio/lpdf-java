// Minimal end-to-end example: render lpdf's examples, encrypt a fixture, and fill an invoice with data, using PdfEngine.
//
// Run from lpdf's root (after 'make build-sdk-java'):
//   make example-java
//
// Output: tmp/sdk-examples/ in lpdf's root, which the first argument names.

import io.lpdf.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

class Example {
    public static void main(String[] args) throws IOException {
        var lpdf = Path.of(args.length > 0 ? args[0] : "../..").toAbsolutePath().normalize();
        var examples = lpdf.resolve("examples");
        var fixtures = lpdf.resolve("test/fixtures");
        var output = lpdf.resolve("tmp/sdk-examples");
        Files.createDirectories(output);

        // ── examples/ ────────────────────────────────────────────────────────
        // Each folder examples/<n>-<id>/ has its document.xml, its document.json if it has data, and the fonts,
        // images and color profiles its <assets> name, by paths relative to the folder. A fresh engine for each
        // example, so a font one of them names does not reach the next; its srcFallback reads those paths from the
        // example's folder. Every render is dated the same, so the same example gives the same PDF; a PDF/X example,
        // such as the brochure, needs the date.
        var createdOn = "2026-01-01T00:00:00Z";
        try (var listed = Files.list(examples)) {
            var folders = listed
                    .filter(folder -> folder.getFileName().toString().matches("\\d+-.*"))
                    .sorted(Comparator.comparingInt(folder -> Integer.parseInt(folder.getFileName().toString().split("-")[0])))
                    .toList();
            for (var folder : folders) {
                var xml = Files.readString(folder.resolve("document.xml"));
                var dataFile = folder.resolve("document.json");
                var engine = L.engine(new EngineOptions().srcFallback(src -> Files.readAllBytes(folder.resolve(src))));
                var options = new RenderOptions().createdOn(createdOn);
                // The data as JSON text, as it is: Java has no JSON reader of its own, and dataJson needs none.
                if (Files.exists(dataFile)) options.dataJson(Files.readString(dataFile));
                write(output, folder.getFileName() + "-java.pdf", engine.render(xml, options));
            }
        }

        // ── encrypt-permissions-only ─────────────────────────────────────────
        // The fixture's <output> asks for AES-256 and allows neither copying nor modifying. No open password;
        // cooperative viewers enforce its allow.
        var encXml = Files.readString(fixtures.resolve("showcase-encryption.xml"));
        var encEngine = L.engine(new EngineOptions().srcFallback(src -> Files.readAllBytes(Path.of(src))));
        write(output, "encrypt-permissions-only-java.pdf", encEngine.render(encXml, new RenderOptions().ownerPassword("s3cr3t")));

        // ── encrypt-open-password ────────────────────────────────────────────
        // Viewers prompt for "password" before displaying content. The cipher and the permissions come from the
        // fixture's <output>, the passwords from the render.
        write(output, "encrypt-open-password-java.pdf",
                encEngine.render(encXml, new RenderOptions().userPassword("password").ownerPassword("owner")));

        // ── example-data ─────────────────────────────────────────────────────
        // Render the invoice in examples/ with its data from document.json.
        var invoice = examples.resolve("2-invoice");
        var dataEngine = L.engine(new EngineOptions().srcFallback(src -> Files.readAllBytes(invoice.resolve(src))));
        write(output, "example-data-java.pdf", dataEngine.render(Files.readString(invoice.resolve("document.xml")),
                new RenderOptions().dataJson(Files.readString(invoice.resolve("document.json")))));
    }

    private static void write(Path output, String file, byte[] pdf) throws IOException {
        Files.write(output.resolve(file), pdf);
        System.out.printf("output: %s (%,d bytes)%n", file, pdf.length);
    }
}
