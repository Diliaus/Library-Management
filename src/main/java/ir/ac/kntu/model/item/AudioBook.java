package ir.ac.kntu.model.item;

public class AudioBook extends DigitalResource {

    private int duration;

    public AudioBook(String title, int publishYear, String category,
                     String format, double fileSize, String downloadUrl, int duration) {
        super(title, publishYear, category, format, fileSize, downloadUrl);
        setDuration(duration);
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        if (duration <= 0) {
            throw new IllegalArgumentException("Duration must be positive");
        }
        this.duration = duration;
    }

    @Override
    public String getItemType() {
        return "AUDIO_BOOK";
    }

    @Override
    protected String getItemTypePrefix() {
        return "AUD";
    }

    @Override
    public String toString() {
        return "Type: AUDIO_BOOK | ID: " + getId() + " | Title: " + getTitle() +
                " | Duration: " + duration + " mins | Format: " + getFormat() +
                " | Size: " + getFileSize() + "MB | Year: " + getPublishYear() +
                " | Category: " + getCategory() + " | URL: " + getDownloadUrl();
    }
}