package io.github.wuwx.rain.pdf;

import io.github.wuwx.rain.pdf.rasterize.RasterizeOptions;
import io.github.wuwx.rain.pdf.rasterize.RasterizeProcessor;
import io.github.wuwx.rain.pdf.watermark.WatermarkProcessor;
import io.github.wuwx.rain.pdf.watermark.WatermarkOptions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PdfProcess {
    private static final WatermarkProcessor WATERMARKER = new WatermarkProcessor();
    private static final RasterizeProcessor RASTERIZER = new RasterizeProcessor();

    private byte[] data;

    PdfProcess(byte[] data) {
        this.data = data;
    }

    public PdfProcess watermark(String text) {
        WatermarkOptions options = WatermarkOptions.ofText(text);
        return watermark(options);
    }

    public PdfProcess watermark(WatermarkOptions options) {
        try (InputStream in = new java.io.ByteArrayInputStream(data);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            WATERMARKER.addTextWatermark(in, out, options);
            this.data = out.toByteArray();
            return this;
        } catch (IOException e) {
            throw new PdfException("Failed to add watermark.", e);
        }
    }

    public PdfProcess rasterize() {
        return rasterize(RasterizeOptions.ofDpi(RasterizeOptions.DEFAULT_DPI));
    }

    public PdfProcess rasterize(RasterizeOptions options) {
        try (InputStream in = new java.io.ByteArrayInputStream(data);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            RASTERIZER.rasterize(in, out, options);
            this.data = out.toByteArray();
            return this;
        } catch (IOException e) {
            throw new PdfException("Failed to rasterize PDF.", e);
        }
    }

    public void write(Path target) {
        try {
            Path parent = target.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(target, data);
        } catch (IOException e) {
            throw new PdfException("Failed to write PDF.", e);
        }
    }

    public void write(OutputStream target) {
        try {
            target.write(data);
            target.flush();
        } catch (IOException e) {
            throw new PdfException("Failed to write PDF.", e);
        }
    }

    public byte[] toByteArray() {
        return data.clone();
    }
}
