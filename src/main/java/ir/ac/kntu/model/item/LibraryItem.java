package ir.ac.kntu.model.item;

import ir.ac.kntu.util.IdGenerator;
import ir.ac.kntu.util.Validator;
import ir.ac.kntu.exception.InvalidInputException;
import java.util.LinkedList;
import java.util.Queue;

public abstract class LibraryItem implements Displayable {

    private String id;
    private String title;
    private int publishYear;
    private String category;
    private int copies;
    private final Queue<String> reservationQueue;

    public LibraryItem(String title, int publishYear, String category) {
        setTitle(title);
        setCategory(category);
        setPublishYear(publishYear);

        this.id = IdGenerator.generateItemId(getItemTypePrefix());
        this.copies = 1;
        this.reservationQueue = new LinkedList<>();
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new InvalidInputException("Title cannot be empty");
        }
        this.title = title.trim();
    }

    @Override
    public int getPublishYear() {
        return publishYear;
    }

    public void setPublishYear(int publishYear) {
        if (!Validator.isValidPublishYear(publishYear)) {
            throw new InvalidInputException("Invalid Publish Year");
        }
        this.publishYear = publishYear;
    }

    @Override
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new InvalidInputException("Category cannot be empty");
        }
        this.category = category.trim();
    }

    public int getCopies() {
        return copies;
    }

    public void setCopies(int copies) {
        if (copies < 0) {
            throw new InvalidInputException("Copies cannot be negative");
        }
        this.copies = copies;
    }

    public Queue<String> getReservationQueue() {
        return reservationQueue;
    }

    public boolean isReservable() {
        return !"EBOOK".equalsIgnoreCase(getItemType()) && !"AUDIOBOOK".equalsIgnoreCase(getItemType());
    }

    @Override
    public abstract boolean isAvailable();

    @Override
    public abstract String getItemType();

    protected abstract String getItemTypePrefix();
}