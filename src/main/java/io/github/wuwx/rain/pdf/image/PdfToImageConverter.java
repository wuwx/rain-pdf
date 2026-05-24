package io.github.wuwx.rain.pdf.image;

import io.github.wuwx.rain.pdf.exception.PdfWatermarkException;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class PdfToImageConverter {
    public void convertToImagePdf(Path inputPath, Path outputPath, ImageOptions options) {
        Objects.requireNonNull(options, "options must not be null");
        if (inputPath == null || outputPath == null) {
            throw new PdfWatermarkException("inputPath and outputPath must not be null.");
        }
        if (!Files.exists(inputPath) || !Files.isRegularFile(inputPath)) {
            throw new PdfWatermarkException("Input PDF does not exist: " + inputPath);
        }
        if (Files.isDirectory(outputPath)) {
            throw new PdfWatermarkException("Output path must be a file, but got directory: " + outputPath);
        }

        try (PDDocument sourceDoc = Loader.loadPDF(inputPath.toFile());
             PDDocument targetDoc = new PDDocument()) {
            PDFRenderer renderer = new PDFRenderer(sourceDoc);
            String format = options.getImageFormat();

            for (int i = 0; i < sourceDoc.getNumberOfPages(); i++) {
                BufferedImage image = renderer.renderImageWithDPI(i, options.getDpi());
                PDImageXObject pdImage = createImageXObject(targetDoc, image, format);

                PDPage sourcePage = sourceDoc.getPage(i);
                PDRectangle mediaBox = sourcePage.getMediaBox();
                PDPage targetPage = new PDPage(mediaBox);
                targetDoc.addPage(targetPage);

                try (PDPageContentStream contentStream = new PDPageContentStream(targetDoc, targetPage)) {
                    contentStream.drawImage(pdImage, 0, 0, mediaBox.getWidth(), mediaBox.getHeight());
                }
            }

            Path parent = outputPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            targetDoc.save(outputPath.toFile());
        } catch (IOException e) {
            throw new PdfWatermarkException("Failed to convert PDF to image PDF.", e);
        }
    }

    private PDImageXObject createImageXObject(PDDocument document, BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, format, baos);
        byte[] imageBytes = baos.toByteArray();
        return PDImageXObject.createFromByteArray(document, imageBytes, "image." + format);
    }
}
