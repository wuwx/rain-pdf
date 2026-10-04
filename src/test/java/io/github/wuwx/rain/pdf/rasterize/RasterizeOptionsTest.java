package io.github.wuwx.rain.pdf.rasterize;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class RasterizeOptionsTest {

    @Test
    public void shouldUseDefaultValues() {
        RasterizeOptions options = RasterizeOptions.ofDpi(RasterizeOptions.DEFAULT_DPI);

        assertEquals(RasterizeOptions.DEFAULT_DPI, options.getDpi(), 0.0001f);
        assertEquals(RasterizeOptions.DEFAULT_IMAGE_FORMAT, options.getImageFormat());
    }

    @Test
    public void shouldAcceptFormatSupportedByImageIo() {
        RasterizeOptions options = RasterizeOptions.builder().imageFormat("jpg").build();

        assertEquals("jpg", options.getImageFormat());
    }

    @Test
    public void shouldRejectFormatWithoutImageWriter() {
        try {
            RasterizeOptions.builder().imageFormat("webp").build();
            fail("Expected IllegalArgumentException for format without ImageIO writer");
        } catch (IllegalArgumentException expected) {
            assertEquals(
                    "imageFormat must be supported by ImageIO, but no image writer was found for: webp",
                    expected.getMessage());
        }
    }

    @Test
    public void shouldRejectNonPositiveDpi() {
        try {
            RasterizeOptions.builder().dpi(0).build();
            fail("Expected IllegalArgumentException for dpi <= 0");
        } catch (IllegalArgumentException expected) {
            assertEquals("dpi must be greater than 0.", expected.getMessage());
        }
    }

    @Test
    public void shouldRejectBlankImageFormat() {
        try {
            RasterizeOptions.builder().imageFormat(" ").build();
            fail("Expected IllegalArgumentException for blank image format");
        } catch (IllegalArgumentException expected) {
            assertEquals("imageFormat must not be blank.", expected.getMessage());
        }
    }
}
