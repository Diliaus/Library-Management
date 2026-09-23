package ir.ac.kntu.model.item;

import ir.ac.kntu.util.Validator;
import ir.ac.kntu.exception.InvalidInputException;

public class Book extends PhysicalItem {

    private int pages;
    private String isbn;
    private String author;

    public Book(String title, int publishYear, String category,
                int availableCopies, int pages, String isbn, String author) {
        super(title, publishYear, category, availableCopies);
        setPages(pages);
        setIsbn(isbn);
        setAuthor(author);
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        if (pages <= 0) {
            throw new InvalidInputException("Pages must be positive");
        }
        this.pages = pages;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        if (!Validator.isValidISBN(isbn)) {
            throw new InvalidInputException("Invalid ISBN format");
        }
        this.isbn = isbn.trim();
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        if (author == null || author.trim().isEmpty()) {
            throw new InvalidInputException("Author cannot be empty");
        }
        this.author = author.trim();
    }

    @Override
    public boolean isReservable() {
        return true;
    }

    @Override
    public String getItemType() {
        return "BOOK";
    }

    @Override
    protected String getItemTypePrefix() {
        return "BOK";
    }

    @Override
    public String toString() {
        return "Type: BOOK | ID: " + getId() + " | Title: " + getTitle() +
                " | Author: " + author + " | Pages: " + pages +
                " | ISBN: " + isbn + " | Year: " + getPublishYear() +
                " | Category: " + getCategory() + " | Copies: " + getAvailableCopies();
    }
}