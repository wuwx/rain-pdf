# rain-pdf

A lightweight Java 8 PDF watermark utility with a Hutool-style static API.

## Features

- Add text watermark to each page in an existing PDF
- Support multiple watermarks per page with configurable horizontal and vertical spacing
- Convert PDF to image-based PDF to prevent text copying
- Stable defaults for quick usage:
  - font size: 48
  - opacity: 0.2
  - rotation: -30
  - color: light gray
  - position: center
  - horizontal spacing: 150
  - vertical spacing: 150
  - DPI: 150
- Supports custom style through WatermarkOptions and RasterizeOptions
- Runtime exception style for simple integration
- Built-in CJK font support for Chinese/Japanese/Korean text
- Automatic output directory creation

## Dependencies

- Java 8+
- Apache PDFBox 3.0.5
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

PdfUtil.watermark(
    Paths.get("input.pdf"),
    Paths.get("output.pdf"),
    "DRAFT"
);
```

## Stream Input and Output

Use stream overloads when PDFs come from object storage or need to be written directly to an HTTP response.

```java
import io.github.wuwx.rain.pdf.PdfUtil;
import io.github.wuwx.rain.pdf.rasterize.RasterizeOptions;
import io.github.wuwx.rain.pdf.watermark.WatermarkOptions;

try (InputStream inputStream = s3Object.getObjectContent();
     OutputStream outputStream = response.getOutputStream()) {
    PdfUtil.watermark(
        inputStream,
        outputStream,
        WatermarkOptions.ofText("CONFIDENTIAL")
    );
}

try (InputStream inputStream = s3Object.getObjectContent();
     OutputStream outputStream = response.getOutputStream()) {
    PdfUtil.rasterize(
        inputStream,
        outputStream,
        RasterizeOptions.ofDpi(150.0f)
    );
}
```

## Fluent API

Use the fluent API to chain multiple operations together.

```java
import io.github.wuwx.rain.pdf.PdfUtil;
import io.github.wuwx.rain.pdf.watermark.WatermarkOptions;
import io.github.wuwx.rain.pdf.rasterize.RasterizeOptions;

import java.nio.file.Paths;

// Watermark then rasterize
PdfUtil.process(Paths.get("input.pdf"))
        .watermark("DRAFT")
        .rasterize()
        .write(Paths.get("output.pdf"));

// Watermark with custom options
PdfUtil.process(Paths.get("input.pdf"))
        .watermark(WatermarkOptions.builder()
                .text("CONFIDENTIAL")
                .fontSize(36)
                .opacity(0.3f)
                .build())
        .write(Paths.get("output.pdf"));

// Rasterize with custom DPI
PdfUtil.process(Paths.get("input.pdf"))
        .rasterize(RasterizeOptions.ofDpi(72.0f))
        .write(Paths.get("output.pdf"));

// Write to OutputStream
PdfUtil.process(Paths.get("input.pdf"))
        .watermark("DRAFT")
        .rasterize()
        .write(response.getOutputStream());

// Get byte array
byte[] pdfBytes = PdfUtil.process(Paths.get("input.pdf"))
        .watermark("DRAFT")
        .toByteArray();

// Process from InputStream
PdfUtil.process(s3Object.getObjectContent())
        .watermark("DRAFT")
        .write(Paths.get("output.pdf"));
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

PdfUtil.watermark(
    Paths.get("input.pdf"),
    Paths.get("output.pdf"),
    options
);
```

## Multiple Watermarks

```java
import io.github.wuwx.rain.pdf.PdfUtil;
import io.github.wuwx.rain.pdf.watermark.WatermarkOptions;

import java.nio.file.Paths;

WatermarkOptions options = WatermarkOptions.builder()
    .text("DRAFT")
    .fontSize(24)
    .horizontalSpacing(200.0f)
    .verticalSpacing(200.0f)
    .build();

PdfUtil.watermark(
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
| horizontalSpacing | float | 150.0 | Horizontal spacing between watermarks in points |
| verticalSpacing | float | 150.0 | Vertical spacing between watermarks in points |

## PDF to Image Conversion

Convert a PDF to an image-based PDF to prevent text copying. Each page is rendered as an image and combined into a new PDF.

```java
import io.github.wuwx.rain.pdf.PdfUtil;

import java.nio.file.Paths;

// Using default DPI (150)
PdfUtil.rasterize(
    Paths.get("input.pdf"),
    Paths.get("output.pdf")
);
```

### Custom Image Options

```java
import io.github.wuwx.rain.pdf.PdfUtil;
import io.github.wuwx.rain.pdf.rasterize.RasterizeOptions;

import java.nio.file.Paths;

RasterizeOptions options = RasterizeOptions.builder()
    .dpi(72.0f)
    .imageFormat("png")
    .build();

PdfUtil.rasterize(
    Paths.get("input.pdf"),
    Paths.get("output.pdf"),
    options
);
```

### RasterizeOptions Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| dpi | float | 150.0 | Resolution in dots per inch |
| imageFormat | String | png | Image format (png, jpg, etc.) |

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

The library throws `PdfException` (a `RuntimeException`) for all error conditions:

```java
try {
    PdfUtil.watermark(inputPath, outputPath, "WATERMARK");
} catch (PdfException e) {
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
│   ├── PdfProcess.java           # Fluent API
│   ├── PdfException.java         # Exception class
│   ├── rasterize/
│   │   ├── RasterizeOptions.java # Rasterize options
│   │   └── RasterizeProcessor.java # PDF rasterizer
│   └── watermark/
│       ├── WatermarkProcessor.java # Core watermark logic
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
