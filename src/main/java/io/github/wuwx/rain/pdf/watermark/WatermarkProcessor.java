package io.github.wuwx.rain.pdf.watermark;

import io.github.wuwx.rain.pdf.PdfException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.util.Matrix;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

public final class WatermarkProcessor {
    public void addTextWatermark(Path inputPath, Path outputPath, WatermarkOptions options) {
        Objects.requireNonNull(options, "options must not be null");
        if (inputPath == null || outputPath == null) {
            throw new PdfException("inputPath and outputPath must not be null.");
        }
        if (!Files.exists(inputPath) || !Files.isRegularFile(inputPath)) {
            throw new PdfException("Input PDF does not exist: " + inputPath);
        }
        if (Files.isDirectory(outputPath)) {
            throw new PdfException("Output path must be a file, but got directory: " + outputPath);
        }

        Path parent = outputPath.getParent();
        Path targetDir = parent != null ? parent : outputPath.toAbsolutePath().getParent();
        try {
            // 必须先读完输入再开输出流：inputPath 与 outputPath 相同时，后者会先把源文件清空
            byte[] source = Files.readAllBytes(inputPath);
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Path tempFile = Files.createFile(
                    targetDir.resolve("." + outputPath.getFileName() + "." + UUID.randomUUID() + ".tmp"));
            boolean moved = false;
            try {
                try (InputStream inputStream = new ByteArrayInputStream(source);
                     OutputStream outputStream = Files.newOutputStream(tempFile)) {
                    addTextWatermark(inputStream, outputStream, options);
                }
                Files.move(tempFile, outputPath, StandardCopyOption.REPLACE_EXISTING);
                moved = true;
            } finally {
                if (!moved) {
                    Files.deleteIfExists(tempFile);
                }
            }
        } catch (IOException e) {
            throw new PdfException("Failed to add watermark.", e);
        }
    }

    public void addTextWatermark(InputStream inputStream, OutputStream outputStream, WatermarkOptions options) {
        Objects.requireNonNull(inputStream, "inputStream must not be null");
        Objects.requireNonNull(outputStream, "outputStream must not be null");
        Objects.requireNonNull(options, "options must not be null");

        try (PDDocument document = Loader.loadPDF(toByteArray(inputStream))) {
            PDFont font = resolveFont(document, options);
            for (PDPage page : document.getPages()) {
                addWatermarkToPage(document, page, font, options);
            }
            document.save(outputStream);
            outputStream.flush();
        } catch (IllegalArgumentException e) {
            throw new PdfException("Watermark text cannot be rendered by the selected font: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new PdfException("Failed to add watermark.", e);
        }
    }

    private void addWatermarkToPage(PDDocument document, PDPage page, PDFont font, WatermarkOptions options) throws IOException {
        PDRectangle box = page.getCropBox();
        float pageWidth = box.getWidth();
        float pageHeight = box.getHeight();

        float textWidth = font.getStringWidth(options.getText()) / 1000.0f * options.getFontSize();
        float textHeight = options.getFontSize();

        float horizontalSpacing = options.getHorizontalSpacing();
        float verticalSpacing = options.getVerticalSpacing();

        int columns = Math.max(1, (int) Math.ceil(pageWidth / horizontalSpacing));
        int rows = Math.max(1, (int) Math.ceil(pageHeight / verticalSpacing));

        float startX = box.getLowerLeftX() + (pageWidth - (columns - 1) * horizontalSpacing) / 2.0f;
        float startY = box.getLowerLeftY() + (pageHeight - (rows - 1) * verticalSpacing) / 2.0f;

        try (PDPageContentStream contentStream = new PDPageContentStream(
                document,
                page,
                PDPageContentStream.AppendMode.APPEND,
                true,
                true
        )) {
            PDExtendedGraphicsState graphicsState = new PDExtendedGraphicsState();
            graphicsState.setNonStrokingAlphaConstant(options.getOpacity());
            graphicsState.setStrokingAlphaConstant(options.getOpacity());
            contentStream.setGraphicsStateParameters(graphicsState);

            contentStream.beginText();
            contentStream.setFont(font, options.getFontSize());
            contentStream.setNonStrokingColor(options.getColor());

            float radians = (float) Math.toRadians(options.getRotation());

            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < columns; col++) {
                    float x = startX + col * horizontalSpacing - textWidth / 2.0f;
                    float y = startY + row * verticalSpacing - textHeight / 4.0f;
                    contentStream.setTextMatrix(Matrix.getRotateInstance(radians, x, y));
                    contentStream.showText(options.getText());
                }
            }

            contentStream.endText();
        }
    }

    private PDFont resolveFont(PDDocument document, WatermarkOptions options) throws IOException {
        String text = options.getText();
        PDFont standard = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        if (canEncode(standard, text)) {
            return standard;
        }

        String fontPath = options.getFontResourcePath();
        if (fontPath != null && !fontPath.trim().isEmpty()) {
            InputStream fontStream = WatermarkProcessor.class.getResourceAsStream(fontPath);
            if (fontStream != null) {
                try (InputStream in = fontStream) {
                    return PDType0Font.load(document, in);
                }
            }
        }

        throw new PdfException(
                "Watermark text contains characters that the standard font cannot encode, "
                        + "but no CJK font was found at "
                        + fontPath
                        + ". Add a font file to src/main/resources/fonts/ and keep the default path, "
                        + "or configure WatermarkOptions.fontResourcePath(...)."
        );
    }

    private boolean canEncode(PDFont font, String text) {
        try {
            font.encode(text);
            return true;
        } catch (IOException | IllegalArgumentException e) {
            return false;
        }
    }

    private byte[] toByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[8192];
        int read;
        while ((read = inputStream.read(chunk)) != -1) {
            buffer.write(chunk, 0, read);
        }
        return buffer.toByteArray();
    }
}
