package io.github.wuwx.rain.pdf.rasterize;

public final class RasterizeOptions {
    public static final float DEFAULT_DPI = 150.0f;
    public static final String DEFAULT_IMAGE_FORMAT = "png";

    private final float dpi;
    private final String imageFormat;

    private RasterizeOptions(Builder builder) {
        this.dpi = builder.dpi;
        this.imageFormat = builder.imageFormat;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static RasterizeOptions ofDpi(float dpi) {
        return builder().dpi(dpi).build();
    }

    public float getDpi() {
        return dpi;
    }

    public String getImageFormat() {
        return imageFormat;
    }

    public static final class Builder {
        private float dpi = DEFAULT_DPI;
        private String imageFormat = DEFAULT_IMAGE_FORMAT;

        private Builder() {
        }

        public Builder dpi(float dpi) {
            this.dpi = dpi;
            return this;
        }

        public Builder imageFormat(String imageFormat) {
            this.imageFormat = imageFormat;
            return this;
        }

        public RasterizeOptions build() {
            if (dpi <= 0) {
                throw new IllegalArgumentException("dpi must be greater than 0.");
            }
            if (imageFormat == null || imageFormat.trim().isEmpty()) {
                throw new IllegalArgumentException("imageFormat must not be blank.");
            }
            return new RasterizeOptions(this);
        }
    }
}
