package ir.ac.kntu.model.item;

public interface Displayable {
    String getId();

    String getTitle();

    String getCategory();

    int getPublishYear();

    String getItemType();

    boolean isAvailable();
}