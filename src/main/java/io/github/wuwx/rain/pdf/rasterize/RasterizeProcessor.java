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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

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
        try {
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (InputStream inputStream = Files.newInputStream(inputPath);
                 OutputStream outputStream = Files.newOutputStream(outputPath)) {
                rasterize(inputStream, outputStream, options);
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
