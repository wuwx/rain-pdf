package io.github.wuwx.rain.pdf;

import io.github.wuwx.rain.pdf.exception.PdfException;
import io.github.wuwx.rain.pdf.image.ImageOptions;
import io.github.wuwx.rain.pdf.image.PdfToImageConverter;
import io.github.wuwx.rain.pdf.watermark.PdfWatermarker;
import io.github.wuwx.rain.pdf.watermark.WatermarkOptions;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.nio.file.Path;

public final class PdfUtil {
    private static final PdfWatermarker WATERMARKER = new PdfWatermarker();
    private static final PdfToImageConverter IMAGE_CONVERTER = new PdfToImageConverter();

    private PdfUtil() {
    }

    /**
     * Add a text watermark with default visual options.
     */
    public static void addWatermark(Path inputPath, Path outputPath, String text) {
        Objects.requireNonNull(text, "text must not be null");
        try {
            WATERMARKER.addTextWatermark(inputPath, outputPath, WatermarkOptions.ofText(text));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new PdfException("Unexpected error while adding watermark.", e);
        }
    }

    /**
     * Add a text watermark with customized options.
     */
    public static void addWatermark(Path inputPath, Path outputPath, WatermarkOptions options) {
        Objects.requireNonNull(options, "options must not be null");
        try {
            WATERMARKER.addTextWatermark(inputPath, outputPath, options);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new PdfException("Unexpected error while adding watermark.", e);
        }
    }

    /**
     * Add a text watermark from an input stream to an output stream with default visual options.
     */
    public static void addWatermark(InputStream inputStream, OutputStream outputStream, String text) {
        Objects.requireNonNull(text, "text must not be null");
        try {
            WATERMARKER.addTextWatermark(inputStream, outputStream, WatermarkOptions.ofText(text));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new PdfException("Unexpected error while adding watermark.", e);
        }
    }

    /**
     * Add a text watermark from an input stream to an output stream with customized options.
     */
    public static void addWatermark(InputStream inputStream, OutputStream outputStream, WatermarkOptions options) {
        Objects.requireNonNull(options, "options must not be null");
        try {
            WATERMARKER.addTextWatermark(inputStream, outputStream, options);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new PdfException("Unexpected error while adding watermark.", e);
        }
    }

    /**
     * Convert a PDF to an image-based PDF with default DPI (150).
     */
    public static void convertToImagePdf(Path inputPath, Path outputPath) {
        try {
            IMAGE_CONVERTER.convertToImagePdf(inputPath, outputPath, ImageOptions.ofDpi(ImageOptions.DEFAULT_DPI));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new PdfException("Unexpected error while converting PDF to image.", e);
        }
    }

    /**
     * Convert a PDF to an image-based PDF with customized options.
     */
    public static void convertToImagePdf(Path inputPath, Path outputPath, ImageOptions options) {
        Objects.requireNonNull(options, "options must not be null");
        try {
            IMAGE_CONVERTER.convertToImagePdf(inputPath, outputPath, options);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new PdfException("Unexpected error while converting PDF to image.", e);
        }
    }

    /**
     * Convert a PDF stream to an image-based PDF with default DPI (150).
     */
    public static void convertToImagePdf(InputStream inputStream, OutputStream outputStream) {
        try {
            IMAGE_CONVERTER.convertToImagePdf(inputStream, outputStream, ImageOptions.ofDpi(ImageOptions.DEFAULT_DPI));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new PdfException("Unexpected error while converting PDF to image.", e);
        }
    }

    /**
     * Convert a PDF stream to an image-based PDF with customized options.
     */
    public static void convertToImagePdf(InputStream inputStream, OutputStream outputStream, ImageOptions options) {
        Objects.requireNonNull(options, "options must not be null");
        try {
            IMAGE_CONVERTER.convertToImagePdf(inputStream, outputStream, options);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new PdfException("Unexpected error while converting PDF to image.", e);
        }
    }
}
