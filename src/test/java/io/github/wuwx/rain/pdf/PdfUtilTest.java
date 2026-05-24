package io.github.wuwx.rain.pdf;

import io.github.wuwx.rain.pdf.watermark.WatermarkOptions;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.Test;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
        PdfUtil.addWatermark(input, output, "DRAFT");

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
            PdfUtil.addWatermark(input, output, "DRAFT");
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
            PdfUtil.addWatermark(input, output, (String) null);
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
            PdfUtil.addWatermark(input, output, (WatermarkOptions) null);
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

        PdfUtil.addWatermark(input, output, "内部资料");

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
            PdfUtil.addWatermark(input, output, options);
            fail("Expected runtime exception for missing custom CJK font");
        } catch (RuntimeException expected) {
            assertTrue(expected.getMessage().contains("Chinese text detected"));
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

        PdfUtil.addWatermark(input, output, options);

        assertTrue(Files.exists(output));
        assertTrue(Files.size(output) > 0);

        try (PDDocument inDoc = Loader.loadPDF(input.toFile());
             PDDocument outDoc = Loader.loadPDF(output.toFile())) {
            assertEquals(inDoc.getNumberOfPages(), outDoc.getNumberOfPages());
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
}
