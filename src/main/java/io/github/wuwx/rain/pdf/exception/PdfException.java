package io.github.wuwx.rain.pdf.exception;

public class PdfException extends RuntimeException {
    public PdfException(String message) {
        super(message);
    }

    public PdfException(String message, Throwable cause) {
        super(message, cause);
    }
}
