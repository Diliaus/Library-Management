package ir.ac.kntu.model.item;

import ir.ac.kntu.util.Validator;

public abstract class DigitalResource extends LibraryItem {

    private String format;
    private double fileSize;
    private String downloadUrl;

    public DigitalResource(String title, int publishYear, String category,
                           String format, double fileSize, String downloadUrl) {
        super(title, publishYear, category);
        setFormat(format);
        setFileSize(fileSize);
        setDownloadUrl(downloadUrl);
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        if (format == null || format.trim().isEmpty()) {
            throw new IllegalArgumentException("Format cannot be empty");
        }
        this.format = format.trim();
    }

    public double getFileSize() {
        return fileSize;
    }

    public void setFileSize(double fileSize) {
        if (fileSize <= 0) {
            throw new IllegalArgumentException("File size must be positive");
        }
        this.fileSize = fileSize;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        if (!Validator.isValidURL(downloadUrl)) {
            throw new IllegalArgumentException("Invalid Download URL format");
        }
        this.downloadUrl = downloadUrl.trim();
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}