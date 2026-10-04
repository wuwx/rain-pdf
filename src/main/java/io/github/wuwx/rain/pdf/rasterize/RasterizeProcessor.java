package io.github.wuwx.rain.pdf.rasterize;

import io.github.wuwx.rain.pdf.PdfException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
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

public final class RasterizeProcessor {
    public void rasterize(Path inputPath, Path outputPath, RasterizeOptions options) {
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
                    rasterize(inputStream, outputStream, options);
                }
                Files.move(tempFile, outputPath, StandardCopyOption.REPLACE_EXISTING);
                moved = true;
            } finally {
                if (!moved) {
                    Files.deleteIfExists(tempFile);
                }
            }
        } catch (IOException e) {
            throw new PdfException("Failed to rasterize PDF.", e);
        }
    }

    public void rasterize(InputStream inputStream, OutputStream outputStream, RasterizeOptions options) {
        Objects.requireNonNull(inputStream, "inputStream must not be null");
        Objects.requireNonNull(outputStream, "outputStream must not be null");
        Objects.requireNonNull(options, "options must not be null");

        try (PDDocument sourceDoc = Loader.loadPDF(toByteArray(inputStream));
             PDDocument targetDoc = new PDDocument()) {
            PDFRenderer renderer = new PDFRenderer(sourceDoc);
            String format = options.getImageFormat();
            float dpi = options.getDpi();

            for (int i = 0; i < sourceDoc.getNumberOfPages(); i++) {
                BufferedImage image = renderer.renderImageWithDPI(i, dpi);
                PDImageXObject pdImage = createImageXObject(targetDoc, image, format);

                // 位图已按 CropBox 与 /Rotate 渲染完成，页面尺寸必须由位图反推，否则图像会被拉伸
                float width = image.getWidth() * 72.0f / dpi;
                float height = image.getHeight() * 72.0f / dpi;
                PDPage targetPage = new PDPage(new PDRectangle(width, height));
                targetDoc.addPage(targetPage);

                try (PDPageContentStream contentStream = new PDPageContentStream(targetDoc, targetPage)) {
                    contentStream.drawImage(pdImage, 0, 0, width, height);
                }
            }

            targetDoc.save(outputStream);
            outputStream.flush();
        } catch (IOException e) {
            throw new PdfException("Failed to rasterize PDF.", e);
        }
    }

    private PDImageXObject createImageXObject(PDDocument document, BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, format, baos);
        byte[] imageBytes = baos.toByteArray();
        return PDImageXObject.createFromByteArray(document, imageBytes, "image." + format);
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
