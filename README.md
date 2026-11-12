![Lpdf - PDF as Code](https://raw.githubusercontent.com/lpdfio/lpdf-java/main/lpdf-mark.svg)

# io.lpdf:lpdf

`io.lpdf:lpdf` is the Java SDK for [Lpdf](https://lpdf.io?utm_campaign=sdk-java&utm_medium=referral&utm_source=readme), the PDF engine that renders a document written in XML or built in code to the same PDF in every language.

## Installation

Maven, with the newest version on [Maven Central](https://central.sonatype.com/artifact/io.lpdf/lpdf):

```xml
<dependency>
  <groupId>io.lpdf</groupId>
  <artifactId>lpdf</artifactId>
  <version>VERSION</version>
</dependency>
```

Gradle:

```kotlin
implementation("io.lpdf:lpdf:VERSION")
```

## Usage

```java
import io.lpdf.*;
import java.util.List;
import static io.lpdf.L.NoAttr;

var engine = L.engine();

var doc = L.document(new DocumentAttr().size("letter"), List.of(
    L.section(NoAttr, List.of(
        L.layout(new LayoutAttr().margin("48pt"), List.of(
            L.column(new ColumnAttr().gap("24pt"), List.of(
                L.row(new RowAttr().justify("between"), List.of(
                    L.text(new TextAttr().fontSize("8pt").color("#888888"), List.of("ACME CORP")),
                    L.text(new TextAttr().fontSize("22pt").bold(true), List.of("Project Proposal"))
                )),
                L.divider(new DividerAttr().thickness("xs")),
                L.text(new TextAttr().fontSize("13pt").bold(true), List.of("Scope of Work")),
                L.row(new RowAttr().gap("12pt").align("start"), List.of(
                    L.text(new TextAttr().color("#888888").width("24pt"), List.of("01")),
                    L.text(new TextAttr().width("fill"), List.of("Discovery & Research"))
                ))
            ))
        ))
    ))
));

byte[] pdf = engine.render(doc);
```

## Requirements

- Java 17 or later
- No native code: the engine is compiled to JVM classes inside the jar, and runs on [Endive](https://github.com/bytecodealliance/endive), a WebAssembly runtime written in Java, which Maven or Gradle installs with the SDK

## Docs

[lpdf.io/docs](https://lpdf.io/docs/install/?sdk=java&utm_campaign=sdk-java&utm_medium=referral&utm_source=readme)

## Issues

Report bugs and request features at [github.com/lpdfio/lpdf/issues](https://github.com/lpdfio/lpdf/issues), the one tracker for the engine, the SDKs and the VS Code extension. Pull requests are not accepted.

---

Licensed under the Lpdf License. See [LICENSE](https://github.com/lpdfio/lpdf-java/blob/main/LICENSE) for full terms.
