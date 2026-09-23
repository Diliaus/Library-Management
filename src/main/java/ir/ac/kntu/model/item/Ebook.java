package ir.ac.kntu.model.item;

public class Ebook extends DigitalResource {

    private int pages;

    public Ebook(String title, int publishYear, String category,
                 String format, double fileSize, String downloadUrl, int pages) {
        super(title, publishYear, category, format, fileSize, downloadUrl);
        setPages(pages);
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        if (pages <= 0) {
            throw new IllegalArgumentException("Pages must be positive");
        }
        this.pages = pages;
    }

    @Override
    public String getItemType() {
        return "EBOOK";
    }

    @Override
    protected String getItemTypePrefix() {
        return "EBK";
    }

    @Override
    public String toString() {
        return "Type: EBOOK | ID: " + getId() + " | Title: " + getTitle() +
                " | Pages: " + pages + " | Format: " + getFormat() +
                " | Size: " + getFileSize() + "MB | Year: " + getPublishYear() +
                " | Category: " + getCategory() + " | URL: " + getDownloadUrl();
    }
}