# rain-pdf

A lightweight Java 8 PDF watermark utility with a Hutool-style static API.

## Features

- Add text watermark to each page in an existing PDF
- Stable defaults for quick usage:
  - font size: 48
  - opacity: 0.2
  - rotation: -30
  - color: light gray
  - position: center
- Supports custom style through WatermarkOptions
- Runtime exception style for simple integration
- Built-in CJK font support for Chinese/Japanese/Korean text
- Automatic output directory creation

## Dependencies

- Java 8+
- Apache PDFBox 3.0.0
- JUnit 4.13.2 (for testing)

## Installation

```xml
<dependency>
  <groupId>io.github.wuwx</groupId>
  <artifactId>rain-pdf</artifactId>
  <version>1.0.0</version>
</dependency>
```

## Quick Start

```java
import io.github.wuwx.rain.pdf.PdfUtil;

import java.nio.file.Paths;

PdfUtil.addWatermark(
    Paths.get("input.pdf"),
    Paths.get("output.pdf"),
    "DRAFT"
);
```

## Advanced Usage

```java
import io.github.wuwx.rain.pdf.PdfUtil;
import io.github.wuwx.rain.pdf.watermark.WatermarkOptions;

import java.awt.Color;
import java.nio.file.Paths;

WatermarkOptions options = WatermarkOptions.builder()
    .text("CONFIDENTIAL")
    .fontSize(36)
    .opacity(0.35f)
    .rotation(-45.0f)
    .color(new Color(128, 128, 128))
    .build();

PdfUtil.addWatermark(
    Paths.get("input.pdf"),
    Paths.get("output.pdf"),
    options
);
```

## WatermarkOptions Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| text | String | required | Watermark text content |
| fontSize | int | 48 | Font size in points |
| opacity | float | 0.2 | Opacity (0.0-1.0) |
| rotation | float | -30.0 | Rotation angle in degrees |
| color | Color | RGB(160,160,160) | Text color |
| fontResourcePath | String | /fonts/LXGWWenKai-Regular.ttf | Font resource path |

## Chinese Text Support

The library is designed to load a bundled font from:

- /fonts/LXGWWenKai-Regular.ttf

If your watermark text contains Han characters and the font resource is missing, the library throws a clear runtime exception.

For production usage, keep a CJK font file at that path in your runtime classpath, or configure a custom font resource path:

```java
WatermarkOptions options = WatermarkOptions.builder()
    .text("内部资料")
    .fontResourcePath("/fonts/LXGWWenKai-Regular.ttf")
    .build();
```

## Exception Handling

The library throws runtime exceptions for error conditions:

- `PdfException`: General PDF processing errors
- `PdfWatermarkException`: Watermark-specific errors (missing input, invalid paths, font issues)

```java
try {
    PdfUtil.addWatermark(inputPath, outputPath, "WATERMARK");
} catch (PdfWatermarkException e) {
    // Handle watermark-specific errors
    System.err.println("Watermark error: " + e.getMessage());
} catch (PdfException e) {
    // Handle general PDF errors
    System.err.println("PDF error: " + e.getMessage());
}
```

## Build and Test

```bash
mvn clean test
```

## Maven Central Release

Use a release profile with GPG signing and Nexus staging:

```bash
mvn -P release clean deploy
```

Before release, configure:

- OSSRH credentials in ~/.m2/settings.xml (server id: ossrh)
- GPG key and passphrase
- GitHub repository metadata in pom.xml

## Project Structure

```
rain-pdf/
├── src/main/java/io/github/wuwx/rain/pdf/
│   ├── PdfUtil.java              # Main API class
│   ├── exception/
│   │   ├── PdfException.java     # Base exception
│   │   └── PdfWatermarkException.java # Watermark exception
│   └── watermark/
│       ├── PdfWatermarker.java   # Core watermark logic
│       └── WatermarkOptions.java # Configuration builder
├── src/main/resources/fonts/
│   └── LXGWWenKai-Regular.ttf   # Bundled CJK font
└── src/test/java/io/github/wuwx/rain/pdf/
    ├── PdfUtilTest.java          # Main API tests
    └── watermark/
        └── WatermarkOptionsTest.java # Options tests
```

## License

Apache License 2.0
