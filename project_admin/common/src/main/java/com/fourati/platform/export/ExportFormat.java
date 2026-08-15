package com.fourati.platform.export;

public enum ExportFormat {

    CSV("text/csv", "csv"),
    PDF("application/pdf", "pdf"),
    JSON("application/json", "json");

    private final String mediaType;
    private final String extension;

    ExportFormat(String mediaType, String extension) {
        this.mediaType = mediaType;
        this.extension = extension;
    }

    public String getMediaType() { return mediaType; }
    public String getExtension() { return extension; }
}
