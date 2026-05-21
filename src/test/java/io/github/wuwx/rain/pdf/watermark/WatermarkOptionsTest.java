package io.github.wuwx.rain.pdf.watermark;

import org.junit.Test;

import java.awt.Color;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class WatermarkOptionsTest {

    @Test
    public void shouldUseDefaultValuesWhenOnlyTextProvided() {
        WatermarkOptions options = WatermarkOptions.ofText("DRAFT");

        assertEquals("DRAFT", options.getText());
        assertEquals(WatermarkOptions.DEFAULT_FONT_SIZE, options.getFontSize());
        assertEquals(WatermarkOptions.DEFAULT_OPACITY, options.getOpacity(), 0.0001f);
        assertEquals(WatermarkOptions.DEFAULT_ROTATION, options.getRotation(), 0.0001f);
        assertEquals(WatermarkOptions.DEFAULT_COLOR, options.getColor());
    }

    @Test
    public void shouldUseCustomValues() {
        WatermarkOptions options = WatermarkOptions.builder()
                .text("CONFIDENTIAL")
                .fontSize(24)
                .opacity(0.5f)
                .rotation(15.0f)
                .color(Color.RED)
                .build();

        assertEquals("CONFIDENTIAL", options.getText());
        assertEquals(24, options.getFontSize());
        assertEquals(0.5f, options.getOpacity(), 0.0001f);
        assertEquals(15.0f, options.getRotation(), 0.0001f);
        assertEquals(Color.RED, options.getColor());
    }

    @Test
    public void shouldRejectBlankText() {
        try {
            WatermarkOptions.builder().text("  ").build();
            fail("Expected IllegalArgumentException for blank text");
        } catch (IllegalArgumentException expected) {
            assertEquals("Watermark text must not be blank.", expected.getMessage());
        }
    }

    @Test
    public void shouldRejectInvalidOpacity() {
        try {
            WatermarkOptions.builder().text("DRAFT").opacity(1.1f).build();
            fail("Expected IllegalArgumentException for opacity out of range");
        } catch (IllegalArgumentException expected) {
            assertEquals("opacity must be in range [0.0, 1.0].", expected.getMessage());
        }
    }
}
