package io.github.wuwx.rain.pdf;

import io.github.wuwx.rain.pdf.rasterize.RasterizeOptions;
import io.github.wuwx.rain.pdf.watermark.WatermarkOptions;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.junit.Test;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class PdfUtilTest {

    @Test
    public void shouldAddWatermarkUsingSimpleOverload() throws IOException {
        Path tempDir = Files.createTempDirectory("rain-pdf-test");
        Path input = tempDir.resolve("input.pdf");
        Path output = tempDir.resolve("output.pdf");

        createSimplePdf(input, 2);
        PdfUtil.watermark(input, output, "DRAFT");

        assertTrue(Files.exists(output));
        assertTrue(Files.size(output) > 0);

        try (PDDocument inDoc = Loader.loadPDF(input.toFile());
             PDDocument outDoc = Loader.loadPDF(output.toFile())) {
            assertEquals(inDoc.getNumberOfPages(), outDoc.getNumberOfPages());
        }
    }

    @Test
    public void shouldFailWhenInputDoesNotExist() throws IOException {
        Path tempDir = Files.createTempDirectory("rain-pdf-test-missing-input");
        Path input = tempDir.resolve("missing.pdf");
        Path output = tempDir.resolve("output.pdf");

        try {
            PdfUtil.watermark(input, output, "DRAFT");
            fail("Expected runtime exception for missing input");
        } catch (RuntimeException expected) {
            assertTrue(expected.getMessage().contains("Input PDF does not exist"));
        }
    }

    @Test
    public void shouldFailWhenTextIsNull() throws IOException {
        Path tempDir = Files.createTempDirectory("rain-pdf-test-null-text");
        Path input = tempDir.resolve("input.pdf");
        Path output = tempDir.resolve("output.pdf");
        createSimplePdf(input, 1);

        try {
            PdfUtil.watermark(input, output, (String) null);
            fail("Expected NullPointerException for null text");
        } catch (NullPointerException expected) {
            assertEquals("text must not be null", expected.getMessage());
        }
    }

    @Test
    public void shouldFailWhenOptionsIsNull() throws IOException {
        Path tempDir = Files.createTempDirectory("rain-pdf-test-null-options");
        Path input = tempDir.resolve("input.pdf");
        Path output = tempDir.resolve("output.pdf");
        createSimplePdf(input, 1);

        try {
            PdfUtil.watermark(input, output, (WatermarkOptions) null);
            fail("Expected NullPointerException for null options");
        } catch (NullPointerException expected) {
            assertEquals("options must not be null", expected.getMessage());
        }
    }

    @Test
    public void shouldAddWatermarkForChineseTextUsingBundledFont() throws IOException {
        Path tempDir = Files.createTempDirectory("rain-pdf-test-cn");
        Path input = tempDir.resolve("input.pdf");
        Path output = tempDir.resolve("output.pdf");
        createSimplePdf(input, 1);

        PdfUtil.watermark(input, output, "内部资料");

        assertTrue(Files.exists(output));
        assertTrue(Files.size(output) > 0);
    }

    @Test
    public void shouldFailWhenChineseTextAndCustomFontPathMissing() throws IOException {
        Path tempDir = Files.createTempDirectory("rain-pdf-test-cn-missing-font");
        Path input = tempDir.resolve("input.pdf");
        Path output = tempDir.resolve("output.pdf");
        createSimplePdf(input, 1);

        WatermarkOptions options = WatermarkOptions.builder()
                .text("内部资料")
                .fontResourcePath("/fonts/NotFound.otf")
                .build();

        try {
            PdfUtil.watermark(input, output, options);
            fail("Expected runtime exception for missing custom CJK font");
        } catch (RuntimeException expected) {
            assertTrue(expected.getMessage().contains("no CJK font was found at"));
        }
    }

    @Test
    public void shouldWatermarkJapaneseKanaUsingBundledFont() throws IOException {
        Path tempDir = Files.createTempDirectory("rain-pdf-test-kana");
        Path input = tempDir.resolve("input.pdf");
        Path output = tempDir.resolve("output.pdf");
        createSimplePdf(input, 1);

        PdfUtil.watermark(input, output, "こんにちは");

        assertTrue(Files.exists(output));
        assertTrue(Files.size(output) > 0);
    }

    @Test
    public void shouldWrapUnsupportedCharactersInPdfException() throws IOException {
        Path tempDir = Files.createTempDirectory("rain-pdf-test-unsupported-char");
        Path input = tempDir.resolve("input.pdf");
        Path output = tempDir.resolve("output.pdf");
        createSimplePdf(input, 1);

        try {
            PdfUtil.watermark(input, output, "REPORT \uD83D\uDE00");
            fail("Expected PdfException for characters the font cannot render");
        } catch (PdfException expected) {
            assertTrue(expected.getMessage().contains("cannot be rendered"));
        }
    }

    @Test
    public void shouldKeepSourceFileWhenWatermarkingInPlace() throws IOException {
        Path tempDir = Files.createTempDirectory("rain-pdf-test-in-place");
        Path file = tempDir.resolve("in-place.pdf");
        createSimplePdf(file, 2);

        PdfUtil.watermark(file, file, "DRAFT");

        assertTrue(Files.size(file) > 0);
        try (PDDocument outDoc = Loader.loadPDF(file.toFile())) {
            assertEquals(2, outDoc.getNumberOfPages());
        }
    }

    @Test
    public void shouldKeepSourceFileWhenRasterizingInPlace() throws IOException {
        Path tempDir = Files.createTempDirectory("rain-pdf-test-image-in-place");
        Path file = tempDir.resolve("in-place.pdf");
        createSimplePdf(file, 1);

        PdfUtil.rasterize(file, file);

        assertTrue(Files.size(file) > 0);
        try (PDDocument outDoc = Loader.loadPDF(file.toFile())) {
            assertEquals(1, outDoc.getNumberOfPages());
        }
    }

    @Test
    public void shouldKeepExistingOutputWhenWatermarkFails() throws IOException {
        Path tempDir = Files.createTempDirectory("rain-pdf-test-watermark-failure");
        Path input = tempDir.resolve("input.pdf");
        Path output = tempDir.resolve("output.pdf");
        createSimplePdf(input, 1);
        Files.write(output, "KEEP ME".getBytes(StandardCharsets.UTF_8));

        try {
            PdfUtil.watermark(input, output, "REPORT \uD83D\uDE00");
            fail("Expected PdfException for characters the font cannot render");
        } catch (PdfException expected) {
            assertTrue(expected.getMessage().contains("cannot be rendered"));
        }

        assertEquals("KEEP ME", new String(Files.readAllBytes(output), StandardCharsets.UTF_8));
        try (Stream<Path> files = Files.list(tempDir)) {
            assertEquals(2L, files.count());
        }
    }

    @Test
    public void shouldKeepExistingOutputWhenRasterizingFails() throws IOException {
        Path tempDir = Files.createTempDirectory("rain-pdf-test-rasterize-failure");
        Path input = tempDir.resolve("input.pdf");
        Path output = tempDir.resolve("output.pdf");
        Files.write(input, "not a pdf".getBytes(StandardCharsets.UTF_8));
        Files.write(output, "KEEP ME".getBytes(StandardCharsets.UTF_8));

        try {
            PdfUtil.rasterize(input, output);
            fail("Expected PdfException for a broken input PDF");
        } catch (PdfException expected) {
            assertTrue(expected.getMessage().contains("Failed to rasterize PDF."));
        }

        assertEquals("KEEP ME", new String(Files.readAllBytes(output), StandardCharsets.UTF_8));
        try (Stream<Path> files = Files.list(tempDir)) {
            assertEquals(2L, files.count());
        }
    }

    @Test
    public void shouldRasterizeRotatedPageKeepingOrientation() throws IOException {
        Path tempDir = Files.createTempDirectory("rain-pdf-test-rotate");
        Path input = tempDir.resolve("input.pdf");
        Path output = tempDir.resolve("output.pdf");

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            page.setRotation(90);
            document.addPage(page);
            document.save(input.toFile());
        }

        PdfUtil.rasterize(input, output);

        try (PDDocument outDoc = Loader.loadPDF(output.toFile())) {
            PDRectangle box = outDoc.getPage(0).getMediaBox();
            assertEquals(PDRectangle.A4.getHeight(), box.getWidth(), 1.0f);
            assertEquals(PDRectangle.A4.getWidth(), box.getHeight(), 1.0f);
        }
    }

    @Test
    public void shouldRasterizeUsingCropBoxSize() throws IOException {
        Path tempDir = Files.createTempDirectory("rain-pdf-test-crop-box");
        Path input = tempDir.resolve("input.pdf");
        Path output = tempDir.resolve("output.pdf");

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A3);
            page.setCropBox(new PDRectangle(20, 20, PDRectangle.A5.getWidth(), PDRectangle.A5.getHeight()));
            document.addPage(page);
            document.save(input.toFile());
        }

        PdfUtil.rasterize(input, output);

        try (PDDocument outDoc = Loader.loadPDF(output.toFile())) {
            PDRectangle box = outDoc.getPage(0).getMediaBox();
            assertEquals(PDRectangle.A5.getWidth(), box.getWidth(), 1.0f);
            assertEquals(PDRectangle.A5.getHeight(), box.getHeight(), 1.0f);
        }
    }

    @Test
    public void shouldComputeWatermarkGridFromCropBox() throws IOException {
        Path tempDir = Files.createTempDirectory("rain-pdf-test-watermark-crop-box");
        Path input = tempDir.resolve("input.pdf");
        Path output = tempDir.resolve("output.pdf");

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A3);
            page.setCropBox(new PDRectangle(20, 20, PDRectangle.A5.getWidth(), PDRectangle.A5.getHeight()));
            document.addPage(page);
            document.save(input.toFile());
        }

        PdfUtil.watermark(input, output, "DRAFT");

        try (PDDocument outDoc = Loader.loadPDF(output.toFile())) {
            PDRectangle cropBox = outDoc.getPage(0).getCropBox();
            int columns = (int) Math.ceil(cropBox.getWidth() / WatermarkOptions.DEFAULT_HORIZONTAL_SPACING);
            int rows = (int) Math.ceil(cropBox.getHeight() / WatermarkOptions.DEFAULT_VERTICAL_SPACING);
            assertEquals(columns * rows, countTextPlacements(outDoc.getPage(0)));
        }
    }

    @Test
    public void shouldAddWatermarkUsingOptionsOverload() throws IOException {
        Path tempDir = Files.createTempDirectory("rain-pdf-test-opts");
        Path input = tempDir.resolve("input.pdf");
        Path output = tempDir.resolve("output.pdf");

        createSimplePdf(input, 1);
        WatermarkOptions options = WatermarkOptions.builder()
                .text("CONFIDENTIAL")
                .fontSize(36)
                .opacity(0.3f)
                .rotation(-45.0f)
                .color(Color.GRAY)
                .build();

        PdfUtil.watermark(input, output, options);

        assertTrue(Files.exists(output));
        assertTrue(Files.size(output) > 0);

        try (PDDocument inDoc = Loader.loadPDF(input.toFile());
             PDDocument outDoc = Loader.loadPDF(output.toFile())) {
            assertEquals(inDoc.getNumberOfPages(), outDoc.getNumberOfPages());
        }
    }

    @Test
    public void shouldAddWatermarkUsingStreams() throws IOException {
        byte[] inputBytes = createSimplePdfBytes(2);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        PdfUtil.watermark(new ByteArrayInputStream(inputBytes), outputStream, "STREAM");

        byte[] outputBytes = outputStream.toByteArray();
        assertTrue(outputBytes.length > 0);

        try (PDDocument inDoc = Loader.loadPDF(inputBytes);
             PDDocument outDoc = Loader.loadPDF(outputBytes)) {
            assertEquals(inDoc.getNumberOfPages(), outDoc.getNumberOfPages());
        }
    }

    @Test
    public void shouldAddWatermarkWithCustomSpacing() throws IOException {
        Path tempDir = Files.createTempDirectory("rain-pdf-test-spacing");
        Path input = tempDir.resolve("input.pdf");
        Path output = tempDir.resolve("output.pdf");

        createSimplePdf(input, 1);
        WatermarkOptions options = WatermarkOptions.builder()
                .text("DRAFT")
                .fontSize(24)
                .horizontalSpacing(200.0f)
                .verticalSpacing(200.0f)
                .build();

        PdfUtil.watermark(input, output, options);

        assertTrue(Files.exists(output));
        assertTrue(Files.size(output) > 0);

        try (PDDocument inDoc = Loader.loadPDF(input.toFile());
             PDDocument outDoc = Loader.loadPDF(output.toFile())) {
            assertEquals(inDoc.getNumberOfPages(), outDoc.getNumberOfPages());
        }
    }

    @Test
    public void shouldConvertToImagePdfUsingDefaultDpi() throws IOException {
        Path tempDir = Files.createTempDirectory("rain-pdf-test-image-default");
        Path input = tempDir.resolve("input.pdf");
        Path output = tempDir.resolve("output.pdf");

        createSimplePdf(input, 2);
        PdfUtil.rasterize(input, output);

        assertTrue(Files.exists(output));
        assertTrue(Files.size(output) > 0);

        try (PDDocument inDoc = Loader.loadPDF(input.toFile());
             PDDocument outDoc = Loader.loadPDF(output.toFile())) {
            assertEquals(inDoc.getNumberOfPages(), outDoc.getNumberOfPages());
        }
    }

    @Test
    public void shouldConvertToImagePdfUsingCustomOptions() throws IOException {
        Path tempDir = Files.createTempDirectory("rain-pdf-test-image-custom");
        Path input = tempDir.resolve("input.pdf");
        Path output = tempDir.resolve("output.pdf");

        createSimplePdf(input, 1);
        RasterizeOptions options = RasterizeOptions.builder()
                .dpi(72.0f)
                .imageFormat("png")
                .build();

        PdfUtil.rasterize(input, output, options);

        assertTrue(Files.exists(output));
        assertTrue(Files.size(output) > 0);

        try (PDDocument inDoc = Loader.loadPDF(input.toFile());
             PDDocument outDoc = Loader.loadPDF(output.toFile())) {
            assertEquals(inDoc.getNumberOfPages(), outDoc.getNumberOfPages());
        }
    }

    @Test
    public void shouldConvertToImagePdfUsingStreams() throws IOException {
        byte[] inputBytes = createSimplePdfBytes(1);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        PdfUtil.rasterize(new ByteArrayInputStream(inputBytes), outputStream);

        byte[] outputBytes = outputStream.toByteArray();
        assertTrue(outputBytes.length > 0);

        try (PDDocument inDoc = Loader.loadPDF(inputBytes);
             PDDocument outDoc = Loader.loadPDF(outputBytes)) {
            assertEquals(inDoc.getNumberOfPages(), outDoc.getNumberOfPages());
        }
    }

    @Test
    public void shouldFailWhenConvertToImagePdfAndInputDoesNotExist() throws IOException {
        Path tempDir = Files.createTempDirectory("rain-pdf-test-image-missing");
        Path input = tempDir.resolve("missing.pdf");
        Path output = tempDir.resolve("output.pdf");

        try {
            PdfUtil.rasterize(input, output);
            fail("Expected runtime exception for missing input");
        } catch (RuntimeException expected) {
            assertTrue(expected.getMessage().contains("Input PDF does not exist"));
        }
    }

    @Test
    public void shouldFailWhenConvertToImagePdfAndOptionsIsNull() throws IOException {
        Path tempDir = Files.createTempDirectory("rain-pdf-test-image-null-options");
        Path input = tempDir.resolve("input.pdf");
        Path output = tempDir.resolve("output.pdf");
        createSimplePdf(input, 1);

        try {
            PdfUtil.rasterize(input, output, (RasterizeOptions) null);
            fail("Expected NullPointerException for null options");
        } catch (NullPointerException expected) {
            assertEquals("options must not be null", expected.getMessage());
        }
    }

    @Test
    public void shouldProcessWithFluentApiWatermarkAndRasterize() throws IOException {
        Path tempDir = Files.createTempDirectory("rain-pdf-test-fluent");
        Path input = tempDir.resolve("input.pdf");
        Path output = tempDir.resolve("output.pdf");

        createSimplePdf(input, 2);

        PdfUtil.process(input)
                .watermark("DRAFT")
                .rasterize()
                .write(output);

        assertTrue(Files.exists(output));
        assertTrue(Files.size(output) > 0);
    }

    @Test
    public void shouldProcessWithFluentApiWatermarkOnly() throws IOException {
        Path tempDir = Files.createTempDirectory("rain-pdf-test-fluent-watermark");
        Path input = tempDir.resolve("input.pdf");
        Path output = tempDir.resolve("output.pdf");

        createSimplePdf(input, 1);

        PdfUtil.process(input)
                .watermark(WatermarkOptions.builder()
                        .text("CONFIDENTIAL")
                        .fontSize(36)
                        .build())
                .write(output);

        assertTrue(Files.exists(output));
        assertTrue(Files.size(output) > 0);
    }

    @Test
    public void shouldProcessWithFluentApiRasterizeOnly() throws IOException {
        Path tempDir = Files.createTempDirectory("rain-pdf-test-fluent-rasterize");
        Path input = tempDir.resolve("input.pdf");
        Path output = tempDir.resolve("output.pdf");

        createSimplePdf(input, 1);

        PdfUtil.process(input)
                .rasterize(RasterizeOptions.ofDpi(72.0f))
                .write(output);

        assertTrue(Files.exists(output));
        assertTrue(Files.size(output) > 0);
    }

    @Test
    public void shouldProcessWithFluentApiToOutputStream() throws IOException {
        Path tempDir = Files.createTempDirectory("rain-pdf-test-fluent-stream");
        Path input = tempDir.resolve("input.pdf");

        createSimplePdf(input, 1);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        PdfUtil.process(input)
                .watermark("TEST")
                .write(outputStream);

        byte[] outputBytes = outputStream.toByteArray();
        assertTrue(outputBytes.length > 0);
    }

    @Test
    public void shouldProcessWithFluentApiFromInputStream() throws IOException {
        byte[] inputBytes = createSimplePdfBytes(1);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        PdfUtil.process(new ByteArrayInputStream(inputBytes))
                .watermark("STREAM")
                .write(outputStream);

        byte[] outputBytes = outputStream.toByteArray();
        assertTrue(outputBytes.length > 0);
    }

    @Test
    public void shouldProcessWithFluentApiToByteArray() throws IOException {
        Path tempDir = Files.createTempDirectory("rain-pdf-test-fluent-bytes");
        Path input = tempDir.resolve("input.pdf");

        createSimplePdf(input, 1);

        byte[] result = PdfUtil.process(input)
                .watermark("TEST")
                .toByteArray();

        assertTrue(result.length > 0);
    }

    private int countTextPlacements(PDPage page) throws IOException {
        PDFStreamParser parser = new PDFStreamParser(page);
        try {
            int count = 0;
            for (Object token : parser.parse()) {
                if (token instanceof Operator && "Tm".equals(((Operator) token).getName())) {
                    count++;
                }
            }
            return count;
        } finally {
            parser.close();
        }
    }

    private void createSimplePdf(Path output, int pages) throws IOException {
        try (PDDocument document = new PDDocument()) {
            for (int i = 0; i < pages; i++) {
                document.addPage(new PDPage());
            }
            document.save(output.toFile());
        }
    }

    private byte[] createSimplePdfBytes(int pages) throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            for (int i = 0; i < pages; i++) {
                document.addPage(new PDPage());
            }
            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }
}
