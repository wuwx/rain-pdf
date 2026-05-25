package io.github.wuwx.rain.pdf;

import io.github.wuwx.rain.pdf.rasterize.RasterizeOptions;
import io.github.wuwx.rain.pdf.rasterize.RasterizeProcessor;
import io.github.wuwx.rain.pdf.watermark.WatermarkProcessor;
import io.github.wuwx.rain.pdf.watermark.WatermarkOptions;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.nio.file.Path;

public final class PdfUtil {
    private static final WatermarkProcessor WATERMARKER = new WatermarkProcessor();
    private static final RasterizeProcessor RASTERIZER = new RasterizeProcessor();

    private PdfUtil() {
    }

    /**
     * Add a text watermark with default visual options.
     */
    public static void watermark(Path inputPath, Path outputPath, String text) {
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
    public static void watermark(Path inputPath, Path outputPath, WatermarkOptions options) {
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
    public static void watermark(InputStream inputStream, OutputStream outputStream, String text) {
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
    public static void watermark(InputStream inputStream, OutputStream outputStream, WatermarkOptions options) {
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
    public static void rasterize(Path inputPath, Path outputPath) {
        try {
            RASTERIZER.rasterize(inputPath, outputPath, RasterizeOptions.ofDpi(RasterizeOptions.DEFAULT_DPI));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new PdfException("Unexpected error while rasterizing PDF.", e);
        }
    }

    /**
     * Convert a PDF to an image-based PDF with customized options.
     */
    public static void rasterize(Path inputPath, Path outputPath, RasterizeOptions options) {
        Objects.requireNonNull(options, "options must not be null");
        try {
            RASTERIZER.rasterize(inputPath, outputPath, options);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new PdfException("Unexpected error while rasterizing PDF.", e);
        }
    }

    /**
     * Convert a PDF stream to an image-based PDF with default DPI (150).
     */
    public static void rasterize(InputStream inputStream, OutputStream outputStream) {
        try {
            RASTERIZER.rasterize(inputStream, outputStream, RasterizeOptions.ofDpi(RasterizeOptions.DEFAULT_DPI));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new PdfException("Unexpected error while rasterizing PDF.", e);
        }
    }

    /**
     * Convert a PDF stream to an image-based PDF with customized options.
     */
    public static void rasterize(InputStream inputStream, OutputStream outputStream, RasterizeOptions options) {
        Objects.requireNonNull(options, "options must not be null");
        try {
            RASTERIZER.rasterize(inputStream, outputStream, options);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new PdfException("Unexpected error while rasterizing PDF.", e);
        }
    }
}
