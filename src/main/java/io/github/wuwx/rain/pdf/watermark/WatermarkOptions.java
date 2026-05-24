package io.github.wuwx.rain.pdf.watermark;

import java.awt.Color;

/**
 * Immutable parameters for text watermark rendering.
 */
public final class WatermarkOptions {
    public static final int DEFAULT_FONT_SIZE = 48;
    public static final float DEFAULT_OPACITY = 0.2f;
    public static final float DEFAULT_ROTATION = -30.0f;
    public static final Color DEFAULT_COLOR = new Color(160, 160, 160);
    public static final String DEFAULT_FONT_RESOURCE_PATH = "/fonts/LXGWWenKai-Regular.ttf";
    public static final float DEFAULT_HORIZONTAL_SPACING = 150.0f;
    public static final float DEFAULT_VERTICAL_SPACING = 150.0f;

    private final String text;
    private final int fontSize;
    private final float opacity;
    private final float rotation;
    private final Color color;
    private final String fontResourcePath;
    private final float horizontalSpacing;
    private final float verticalSpacing;

    private WatermarkOptions(Builder builder) {
        this.text = builder.text;
        this.fontSize = builder.fontSize;
        this.opacity = builder.opacity;
        this.rotation = builder.rotation;
        this.color = builder.color;
        this.fontResourcePath = builder.fontResourcePath;
        this.horizontalSpacing = builder.horizontalSpacing;
        this.verticalSpacing = builder.verticalSpacing;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static WatermarkOptions ofText(String text) {
        return builder().text(text).build();
    }

    public String getText() {
        return text;
    }

    public int getFontSize() {
        return fontSize;
    }

    public float getOpacity() {
        return opacity;
    }

    public float getRotation() {
        return rotation;
    }

    public Color getColor() {
        return color;
    }

    public String getFontResourcePath() {
        return fontResourcePath;
    }

    public float getHorizontalSpacing() {
        return horizontalSpacing;
    }

    public float getVerticalSpacing() {
        return verticalSpacing;
    }

    public static final class Builder {
        private String text;
        private int fontSize = DEFAULT_FONT_SIZE;
        private float opacity = DEFAULT_OPACITY;
        private float rotation = DEFAULT_ROTATION;
        private Color color = DEFAULT_COLOR;
        private String fontResourcePath = DEFAULT_FONT_RESOURCE_PATH;
        private float horizontalSpacing = DEFAULT_HORIZONTAL_SPACING;
        private float verticalSpacing = DEFAULT_VERTICAL_SPACING;

        private Builder() {
        }

        public Builder text(String text) {
            this.text = text;
            return this;
        }

        public Builder fontSize(int fontSize) {
            this.fontSize = fontSize;
            return this;
        }

        public Builder opacity(float opacity) {
            this.opacity = opacity;
            return this;
        }

        public Builder rotation(float rotation) {
            this.rotation = rotation;
            return this;
        }

        public Builder color(Color color) {
            this.color = color;
            return this;
        }

        public Builder fontResourcePath(String fontResourcePath) {
            this.fontResourcePath = fontResourcePath;
            return this;
        }

        public Builder horizontalSpacing(float horizontalSpacing) {
            this.horizontalSpacing = horizontalSpacing;
            return this;
        }

        public Builder verticalSpacing(float verticalSpacing) {
            this.verticalSpacing = verticalSpacing;
            return this;
        }

        public WatermarkOptions build() {
            if (text == null || text.trim().isEmpty()) {
                throw new IllegalArgumentException("Watermark text must not be blank.");
            }
            if (fontSize <= 0) {
                throw new IllegalArgumentException("fontSize must be greater than 0.");
            }
            if (opacity < 0.0f || opacity > 1.0f) {
                throw new IllegalArgumentException("opacity must be in range [0.0, 1.0].");
            }
            if (color == null) {
                throw new IllegalArgumentException("color must not be null.");
            }
            if (fontResourcePath != null && fontResourcePath.trim().isEmpty()) {
                throw new IllegalArgumentException("fontResourcePath must not be blank.");
            }
            if (horizontalSpacing <= 0) {
                throw new IllegalArgumentException("horizontalSpacing must be greater than 0.");
            }
            if (verticalSpacing <= 0) {
                throw new IllegalArgumentException("verticalSpacing must be greater than 0.");
            }
            return new WatermarkOptions(this);
        }
    }
}
