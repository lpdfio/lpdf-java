# Changelog

## v0.36.0 (core) — 2026-11-19
### Invoices
- `LCrossIndustryInvoice` makes a Factur-X / ZUGFeRD invoice from its XML, in all five SDKs. `generatePdf` takes a template, or a document built in code, and the invoice's XML, a Cross Industry Invoice, and returns the invoice PDF. The template only says how the invoice looks: the engine reads the values from the XML, binds them, makes the file PDF/A-3 and attaches the XML
- `extractInvoiceData` gives an invoice's values as plain data, for a document built in code, and `defaultInvoiceMap` gives the map that says where each value sits in the XML. The default map has the values EN 16931 numbers, and the parties' identifiers BT-29, BT-46, BT-60 and BT-71, what a line may hold several of, and an attached file's content. A map of your own takes its place, and each of its paths is checked against FeRD's EXTENDED schema before an invoice is read
- It reads the profiles BASIC, EN 16931, EXTENDED and XRECHNUNG. MINIMUM and BASIC WL are refused, since their XML is only a booking aid and the lines their PDF shows are not in it
- `generatePdf` makes the file PDF/A-3b, or keeps a template's 3a or 3u, and attaches the XML as `factur-x.xml`, or `xrechnung.xml` for XRECHNUNG. It refuses another PDF/A level, `encrypt`, `pdfx`, and a document that declares the invoice file itself
- Lpdf never writes the XML and doesn't validate all of it. XML with a DTD, XML of more than 2,000,000 nodes and a root other than `rsm:CrossIndustryInvoice` are refused, a value the map marks `required` that the invoice lacks is an error that names it, and so is a value that finds several in the invoice
- A path in a template may start with a prefix, `invoice:seller.name`, which reads data bound under that prefix beside the data of the render. A prefix with nothing bound under it leaves the element as the document wrote it, as a render with no data does

### Merging PDFs
- `merge` joins existing PDFs, which may come from different tools and be of different PDF versions, into one PDF made for printing. It sits beside `render` and `stamp`, in the five SDKs: `merge(sources, options)` takes a list of `MergeSource`, each a name and the bytes of a PDF, and an optional `MergeOptions`
- Each page is drawn as it looked, on a page of the size a viewer shows (its crop box, turned by its `/Rotate`), so pages of mixed sizes keep their sizes. An annotation that prints is drawn from its stored appearance, and layers cross over, each on or off as it prints in its file. The merged file holds no annotation: forms and their fields, links, bookmarks, named destinations, the structure of a tagged file, the document's properties and embedded files are left behind. It claims no PDF/A, PDF/UA or PDF/X, and is not encrypted. The version is the highest among the files'
- `doubleSided` adds a blank page, the size of a document's last page, after each document with an odd number of pages, so each document starts on a front side. None goes after the last
- Refused, each with an error that names the file: a PDF that needs a password to open, a file that can't be read, a file whose owner restricts printing or copying (unless `allowRestricted`), a signed file (unless `allowSigned`, which drops its signature), a form field that has a value and no stored appearance, and a form field that has a value in a form that sets `NeedAppearances`, which asks a viewer to redraw its fields, so that what the file stored may be out of date. A merge of no files or of more than 1,000 is refused too
- A merge in the native library stops past its request's `timeout`, checked once a page, as a render does there
- A `stamp` in the native library stops past its request's `timeout` too, where the `timeout` was ignored and a large PDF could run past it. The error says so: `the stamp ran past its timeout of N seconds: raise the timeout in the engine's options, or stamp the pages in smaller groups`

### Layout
- **Breaking:** a page size under 3pt in either size, on `<document>` or `<section>`, is an error that names the minimum: `page size '80mm 0' is under 3pt high, the smallest page PDF 1.7 recommends`. A bare `0` in a size is no longer in the schema either. A page 0 high put each element on a page of its own
- `grow` on `<section>` lets its page grow to fit its content instead of starting a new page or squeezing the content: `grow="height"`, `grow="width"` or `grow="width height"`. `size` stays the page's starting size, the smallest it can be. A till receipt on an 80mm roll is `size="80mm 40mm" grow="height"`, one page as long as the order, and a table wider than the page gets a page as wide as its columns need. Without `grow` a section paginates as before
- With `grow="height"` the flow is one page, as tall as the content and the margin, and `break-before`, `break-after` and `keep-next` start no new page. With `grow="width"` the page grows until each `auto` column holds its cells on one line and a box or an image of a fixed width fits. A column of a fixed width keeps it and wraps its cells, and `fr` and `%` columns are measured as `auto` ones for the growth, then take their share, so a long cell can still wrap there
- On a grown page the footer and the footnotes sit right under the content, `height="fill"` is as tall as its content and `full` is the room of the starting size, a layer's `anchor` places against the final page, and the background and the ruling cover the page
- A page grows to 14,400pt (200in) at most in each size, the largest PDF 1.7 recommends: content that needs more is an error that names the section. `grow` with `columns` above 1 or with an inset is an error
- Every SDK takes `grow` on a section and a `Grow` type with its values, and Generate Code writes it in all five languages
- A table whose columns are set in `fr`, lengths and `%`, with no `auto` column, no longer runs past its width by its gaps: `cols="1fr 1fr" gap="20pt"` on A5 ended 20pt past the right margin. The `fr` columns share what the gaps leave, as `auto` columns do. This reaches every page of such a table, the part of a table that continues on later pages, a bulk table and a table in a section that grows in width, so a document with one lays out narrower columns than before
- A table nested in a cell or a box measures its width right when a cell spans a fixed-width column and an `auto` one. The fixed column's share of the cell was lost, so the nested table measured too narrow and a long cell could wrap where it fit

### Third-party licenses
- Every package that ships the engine carries `THIRD_PARTY_LICENSES_ENGINE.txt`: the Rust crates the engine links, each with its license and the license text, and the parts of the Rust toolchain and wasi-libc the builds hold. It is in the JavaScript, .NET, PHP, Python and Java packages and in the VS Code extension, and it is an asset of the GitHub release beside the engine files, listed in `SHA256SUMS`, and linked from the docs site's license page
- The file is made from the engine's dependencies, so it can't fall behind them. The hand-written table of crates in the Java, .NET and VS Code packages, which had, is gone, and their `THIRD_PARTY_LICENSES` points to the file

### Documentation
- `FEATURES.md` lists `merge` in a new area, Merging PDFs for print, and lists `LCrossIndustryInvoice`, values under a prefix, `grow` and the page-size limits, each with its limits, since which version and its docs page

### VS Code extension
- Hover, autocomplete and validation take `grow` and its values from the schema
- The extension carries `THIRD_PARTY_LICENSES_ENGINE.txt`

## v0.35.0 (core) — 2026-11-15
### Images
- **Breaking:** a document with a CMYK JPEG, a JPEG of two components or an interlaced PNG used to render, wrongly, and now stops with an error that says what to save instead: `image 'photo': the JPEG is in CMYK, which Lpdf doesn't read: save it in RGB`. Save the file in RGB, or without interlacing
- SVG images are drawn as vector, wherever an image goes: `<img>`, `<img-at>`, a header, a footer and a Markdown `![](logo.svg)`. Shapes, paths, groups, `use`, markers, clip paths, `<style>`, colors, strokes, dashes and opacity are drawn. The picture is written once however often it is drawn, and `currentColor` is the color of the text where the image sits, so one icon takes the color of each place it is used
- An SVG the engine can't draw is refused with an error that names the image, the tag and its line: text, embedded images, `foreignObject`, references to other files, masks, filters, patterns, gradients and blend modes. A file that expands past 100,000 elements, 1,000,000 path commands or 100 levels of nesting is refused with an error that names the limit
- An SVG sized in `pt`, `pc`, `in`, `mm` or `cm` is drawn at the size it says: `72pt` is 72pt and `210mm` is 595.3pt. A size in `px`, or with none, is a pixel to the point, as for any image
- WebP, GIF, TIFF and BMP images embed beside JPEG and PNG. `loadImage(name, bytes)` takes them as it takes the others, the engine tells the format from the bytes, and no element or attribute changes. The alpha of each goes in as a soft mask
- An animated GIF or WebP, a TIFF of several pages, and a TIFF in CMYK, YCbCr or Lab or compressed as JPEG are refused with an error that names the image and says how to save it. JPEG 2000 is refused by name, beside AVIF and HEIC
- A gray JPEG is written as `DeviceGray`. It was labelled RGB, so its colors came out wrong or it did not show
- A palette PNG with a `tRNS` chunk keeps its transparency, and a gray or RGB PNG with one keeps the color it makes transparent. Both were dropped

### Barcodes
- Seven new values of `type` on `<barcode>` and `<barcode-at>`: `code39`, `ean8`, `upca`, `datamatrix`, `pdf417`, `gs1-128` and `gs1-datamatrix`. They are drawn as vector and never stretched, and `width` and `height` are a maximum, as for QR, Code 128 and EAN-13
- `code39`, `ean8`, `upca` and `gs1-128` take `hrt`, the text under the bars. EAN-8 and UPC-A add the check digit to 7 or 11 digits and check it on 8 or 12, as EAN-13 does
- Code 39 draws the standard's 43 characters. Any other character, a lowercase letter too, gets the placeholder box with the reason. `check="true"` adds the standard's mod 43 check character, which HIBC health care labels ask for
- `gs1-128` and `gs1-datamatrix` read their fields as written, `(01)09501101530003(17)261231(10)AB12`, and put the FNC1 marker first, so scanners read the data as GS1 fields
- `rows` and `columns` set the shape of a PDF417 or a Data Matrix. Unset, with both `width` and `height`, the code takes the shape that comes out largest in them. With one of them, a Data Matrix takes the smallest square that holds the data and a PDF417 the column count closest to three times wider than high. With neither, the document is refused, as a QR code without a size is. Data that needs more than the shape gives gets more rows, or the next Data Matrix size up
- A PDF417 takes `ec` as its own level, `0` to `8`. Unset, the level follows the length of the data. QR keeps `L`, `M`, `Q` and `H`
- An EAN-13 whose first digit is 1 to 9 did not scan, because the engine drew some of its digits in the wrong code. They are drawn as the standard says. With `hrt`, an EAN-13 given as 12 digits now shows all 13, its check digit included
- A Code 128 holding the symbol of value 61, such as a `]`, a check symbol of 61 or the digit pair 61 in a GS1-128, drew that symbol one module too wide and did not scan. It is drawn as the standard says
- Every SDK takes the new types and the attributes `rows`, `columns` and `check`, and Generate Code writes them in all five languages

### Color
- `<spots>` in `<tokens>` declares spot colors, the named inks of a print job, such as a brand color, a varnish or a die line. Each `<spot>` has a `name`, the `ink` as the printer knows it, a `value` in CMYK or RGB that stands in for the ink on a screen or a proof, and `overprint`
- A spot color goes wherever a color goes, by its name, with a tint after a space: `background="brand 30%"`. A tint of any other color, a tint out of range, or one without `%` is an error that says what to change
- The PDF draws each ink in a Separation color space named after it, so the printer gets it on a plate of its own. An ink with `overprint="true"` overprints, and what is drawn after it does not
- A gradient whose colors are all tints of one spot color is a shading in that ink. Mixing a spot color with another color in a gradient is an error that names both
- PDF/A refuses a spot color whose stand-in is `cmyk()`, as it refuses CMYK. PDF/X keeps each ink, and refuses a `cmyk()` stand-in when the press profile is not CMYK
- A document with no spot colors writes the same bytes as before
- Every SDK takes `spots` among its tokens, with a `Spot` class (`SpotToken` in the JavaScript SDK). Generate Code writes them

### Attachments
- The schema's text for `<attachments>` names PDF/A `2a`, `2b` and `2u` as refused, `3a`, `3b` and `3u` as carrying files, and attachments in a PDF/X file as an error. It left out `2a`, `3a` and PDF/X, which the engine refuses and takes

### Documentation
- `FEATURES.md`, beside the README, lists what Lpdf supports area by area: since which version, where each feature stops and its docs page. It also says what combines with what: each pair of `<output>` settings, and what a plain PDF, a PDF/A, a PDF/UA and a PDF/X file may hold. It links the docs on standards, on what combines with what, and on color and print
- The READMEs of Lpdf and of each SDK are shorter. The Lpdf README shows one XML example, which every SDK renders, and one entry per SDK with its package, minimum runtime and install command. The detail is in the docs
- The doc comments of `loadImage` list the image formats it takes

### VS Code extension
- The hover on `type` and `ec` of a barcode shows their first values and `…`, where it showed `type=""` and `ec=""` once the schema held more values than a hover lists. Hover, autocomplete and validation take the new barcode types, their attributes and the PDF417 levels from the schema
- The README keeps a short section per feature

### Java
- The Java package is described as the Java SDK for Lpdf

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
