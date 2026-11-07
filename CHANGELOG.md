# Changelog

## v0.34.0 (core) — 2026-11-07
### Lengths
- **Breaking:** a table's `cols` with a width that is none of `fr`, `pt`, `mm`, `in`, `%` or `auto` is an error that names them, such as `cols="40cm 1fr"`. It was dropped, and the table read as one column. A bare number is `fr`, as before
- Every attribute that takes a length reads `mm` and `in` as well as `pt`, as the schema and the docs always said. It was `pt` only for most of them, so `<quote padding="2mm"/>` failed with "invalid space value". The places are `padding`, `gap`, `width`, `height`, `radius` and `font-size` of a column, a row or a text, the rules of a Markdown (`padding`, `font-size`, `bar-width`, `radius`, `gap`, `indent` and `thickness`), the rows of the tokens, the `cols` of a table, and the `width` of `text-at` and `img-at`
- A custom page `size`, a `margin`, a `padding` of one value, and the `border` and `column-rule` shorthands take `mm` and `in` too
- An error about a length names `pt`, `mm` and `in`. A bare number is rejected except `0`, as before

### Markdown
- The rule `hr`, the thematic break (`---`) with its `color` and `thickness`, has a builder in every SDK: `LMarkdown.hr` (`Hr` in .NET) with `MarkdownHrAttr`. It could only be written as XML. Generate Code writes it in all five languages
- `bar-width` of the `quote` rule takes a slot of the space row, `xs` to `xxl`, or an alias the tokens define for one, as the schema and the docs said, and draws a bar of that width. It failed with an error about the quote's table columns, and only a length worked
- An error in a `quote` rule names the rule once, "the quote rule: padding: …", where it said "the quote rule: the quote rule: padding: …"

### PHP
- The PHP SDK renders through the engine as a native library, loaded with PHP's FFI, where it can: on Linux with glibc 2.17 or newer, on x86_64 or arm64, with `ffi.enable=true` under a web server. It needs no `wasmtime` program and starts no process per render. Everywhere else, such as macOS, Windows, Alpine and hosts that refuse FFI, it renders with `wasmtime` as before. `runner` on `EngineOptions` picks how (`'auto'`, the default, `'ffi'` or `'wasm'`), `libPath` loads the library from another path, and `$engine->runner()` says which one renders. `'ffi'` throws an `EngineException` that names what stands in the way
- A render through the native library stops at the timeout with an `EngineException` that names the limit, as with `wasmtime`, and the next render in the process is unaffected
- Under PHP-FPM, the `wasmtime` runner renders out of the box. It failed with "failed to create cache directory" wherever PHP's user can't write its home, as `www-data` can't, because `wasmtime` could not keep its compiled engine. The SDK now keeps it in a folder of its own. On Linux and macOS that needs PHP's POSIX extension (`ext-posix`)

### Memory
- The .NET, PHP (through `wasmtime`), Python and Java SDKs have a smaller memory footprint when they render a large PDF, because the engine writes its output as it encodes the PDF

### VS Code extension
- **Lpdf: Generate Code** offers Java beside TypeScript, C#, Python and PHP, and **Lpdf: Generate Java from XML** is a command of its own. A text or an attribute value over 65,534 bytes is refused with an error that names the element, since a Java string holds no more
- Hover, autocomplete and validation follow the schema, so they accept `mm` and `in` wherever the schema now does

## v0.33.0 (core) — 2026-11-06
### E-invoices and PDF/A
- `pdfa` takes `3b` and `3u`: PDF/A-3, which carries attachments of any kind. It is the base of the hybrid e-invoices that France and Germany accept, a PDF people read carrying the EN 16931 XML that accounting software reads
- In a PDF/A-3 document, an attachment named `factur-x.xml`, or `xrechnung.xml`, is the invoice. The engine reads its profile from the XML and writes the Factur-X metadata, so the file is a Factur-X and ZUGFeRD invoice (Factur-X 1.09.2, ZUGFeRD 2.5.2), in all six profiles: MINIMUM, BASIC WL, BASIC, EN 16931, EXTENDED and XRECHNUNG. Left out, its `mime` is `text/xml` and its `relationship` the one its profile asks for. The XML is attached as given: check its content with the Factur-X schemas or the Mustang CLI
- `facturx-strict="true"` on `<output>` refuses an invoice `mime` or `relationship` its profile doesn't allow, and another attachment of a format the spec doesn't list
- `pdfa` takes `2a` and `3a`: PDF/A with the tagged structure screen readers read, the same tagging `pdfua` writes, and the level the Factur-X spec recommends. PDF/UA's own checks still come only with `pdfua`, and `pdfa="3a" pdfua="1"` meets both
- Errors that say what to change: the invoice in a document that isn't PDF/A-3, XML that doesn't parse or names no profile, `factur-x.xml` beside `xrechnung.xml`, and, at level a, a `lang` that isn't a language tag or a character of Unicode's private use area

### Stamping existing PDFs
- `stamp` on the engine (`Stamp` in .NET) draws PNG images over the pages of a PDF that Lpdf didn't make: a signature on a contract, a company seal, a PAID stamp on an invoice, a QR code on a certificate. A stamp names its pages and a box placed as a layer's element is (`x`, `y`, `anchor`, `width`, `height`). The image is fitted in the box without distortion and drawn upright on the page as a viewer shows it, its crop box and rotation included
- The PDF comes back with an incremental update, so its original bytes stay as they were: an encrypted file keeps its encryption and permissions, a digital signature stays valid for the revision it signed, a PDF/A file stays PDF/A, and a tagged file stays valid
- Errors that say what to change: not a PDF, a PDF that needs a password to open, a page it doesn't have, an image that isn't a PNG or is interlaced, and a transparent PNG on a PDF/A-1 file

### Columns
- `columns` on `<section>`, 1 to 12, sets its flow in columns that fill like small pages, left to right and then on to the next page, a Markdown's included: terms and conditions, a glossary, a price list, a newsletter. `column-gap` sets the space between them, `column-rule` draws a line in it, and `column-fill="balance"` levels the columns of the last page
- `break-before`, `break-after` and `keep-next` act per column. Headers, footers, footnotes and layers keep the page's full width, and the footnotes go under all the columns
- `<region pin="inset">`, with `top`, `height`, `col` and `colspan`, places a block inside the columns, such as a pull quote or a figure, and the text goes around it

### Bulk data
- `bulk="true"` on a table, list, grid or column whose child has `data-source` keeps the records as data and lays them out a page at a time, for ledgers, logs, audit exports and label sheets of hundreds of thousands of records. Without it, every copy of the template stays in memory until the PDF is written
- A bulk container holds plain text, one text to a table cell, and sits directly in the layout. A record never splits across pages. Links, footnotes, bookmarks, `rowspan`, `keep`, a table of contents, bookmark references, PDF/UA and PDF/A level a don't go with it, and each is an error that names it
- Every document holds less memory until its PDF is written, since each page's content is compressed as the page is drawn

### Ruled and squared pages
- `<rulings>` in `<tokens>` defines ruled or squared paper, and `ruling` on `<section>` draws it under everything else on the section's pages: notes, worksheets, planners, or a grid to read places off while editing beside the preview. Each `<lines>` of a ruling is cut by a `step`, such as `5mm`, or by a `count` of equal parts, in x, in y or both, with its own `color` and `thickness`
- `origin="margin"` keeps the lines inside the margin, `edges` turns the lines on the edges on and off, and `ocg` keeps them off paper or off screen. A ruling is written once and shared by every page that draws it, so it adds next to nothing to the file

### Limits
- **Breaking:** a render stops after 5 seconds by default, in every SDK, with "The render timed out after 5 seconds." It had no limit in JavaScript and .NET, and 30 seconds in Python and PHP. Set `timeout` on the engine's options (`Timeout` in .NET), in seconds, 0 for no limit. In PHP, `timeout: 0` means no limit, where every render failed at once
- There is no limit on the size of the input, where XML over 1 MB and document trees over 4 MB were refused. A render's memory is capped instead: 1024 MB by default, from 1 to 4096 with `maxMemoryMb` on the engine's options (`MaxMemoryMb` in .NET, `max_memory_mb` in Python). A render that needs more stops with an error that names the cap, the same in every SDK, where it crashed the engine
- A document nests 100 levels at most, `<lpdf>` included, and a deeper one is an error that names the element and its line, where a deep enough document crashed the engine. In Markdown, quotes and list items count toward it, so a Markdown nests 31 quotes or 47 list items in one another at most, and emphasis and links nest 100 deep
- An engine crash throws the same message in every SDK

### Page numbers
- **Breaking:** `page="odd"` and `page="even"` on a region or a layer follow the number printed on the page, so a book's left and right running heads stay on their sides in a chapter that starts on page 4. In a section that counts on from the one before, or starts above 1, they can change sides: `page-start="1"` on it brings the old sides back. `first`, `last` and ranges such as `2-last` still count the section's own pages

### Layout
- Long columns, lists, tables, grids and rows lay out in time that grows with their length, where each page redid the work for everything left of them: a table of 24,000 rows took 16 seconds and takes about half a second
- Grids, tables, links and boxes of a set height nested in one another lay out in time that grows with their depth, where each level doubled the work: 24 nested grids took 8 seconds, and 22 nested tables 4.7 seconds, where 31 nested tables now take 13 ms
- A child of `full` height inside a link is as tall as the link's other content, where it took a height the link didn't have: it covered the text after the link, ran out of its row or grid, or spread a link and a line of text over three pages
- Spans in centered and right-aligned text are placed one after the other, where a span in another color or font, or with a link, a line, a background or a footnote number, was drawn over the words before it or past the edge. A Markdown table's centered and right-aligned columns did the same

### SDKs
- **Breaking:** in JavaScript, renders run on a worker, a worker thread in Node.js and a Web Worker in the browser, so a render that runs long holds neither the event loop nor the page. In the browser the worker starts from a `blob:` URL, so a page with a Content-Security-Policy must allow `blob:` in `worker-src`. The browser's render errors are `LpdfRenderError`s
- The Java SDK, `io.lpdf:lpdf` on Maven Central, for Java 17 and later. It runs the engine inside the JVM, with nothing native and no temporary files, and has the other SDKs' API in Java's terms: `L.` builders, attribute classes with fluent setters (`new TextAttr().fontSize("xl")`), children as `List.of(…)`, and `PdfEngine` to render, stamp, and load fonts, images and profiles
- The builders have the new attributes: `columns`, `column-gap`, `column-rule`, `column-fill` and `ruling` on sections, `top`, `height`, `col` and `colspan` on inset regions, `bulk`, `facturx-strict` and the new `pdfa` levels, with `rulings` in the tokens and `StampAttr` for stamps

### VS Code extension
- Preview and export render with the new engine, and hover, autocomplete and validation follow the schema, so they cover every attribute above
