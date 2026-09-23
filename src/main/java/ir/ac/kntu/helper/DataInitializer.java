package ir.ac.kntu.helper;

import ir.ac.kntu.model.item.AudioBook;
import ir.ac.kntu.model.item.Book;
import ir.ac.kntu.model.item.Ebook;
import ir.ac.kntu.model.item.Magazine;
import ir.ac.kntu.services.Catalog;

public final class DataInitializer {

    private static final String CAT_FICTION = "Fiction";
    private static final String CAT_SCIENCE = "Science";
    private static final String CAT_TECH = "Technology";
    private static final String CAT_HISTORY = "History";
    private static final String CAT_NEWS = "News";
    private static final String FMT_MP3 = "MP3";

    private DataInitializer() {
    }

    public static void initializeData(Catalog catalog) {
        if (catalog == null) {
            throw new IllegalArgumentException("Catalog cannot be null");
        }

        catalog.addItem(new Book("The Great Gatsby", 1925,
                CAT_FICTION, 5, 180, "9780743273565", "F. Scott Fitzgerald"));
        catalog.addItem(new Book("To Kill a Mockingbird", 1960,
                CAT_FICTION, 0, 281, "9780446310789", "Harper Lee"));
        catalog.addItem(new Book("A Brief History of Time", 1988,
                CAT_SCIENCE, 4, 212, "9780553380163", "Stephen Hawking"));
        catalog.addItem(new Book("Sapiens A Brief History", 2011,
                CAT_HISTORY, 6, 443, "9780062316097", "Yuval Noah Harari"));
        catalog.addItem(new Book("1984 George Orwell", 1949,
                CAT_FICTION, 8, 328, "9780451524935", "George Orwell"));

        catalog.addItem(new Magazine("National Geographic", 2020,
                CAT_SCIENCE, 10, "0027-9358", "Monthly"));
        catalog.addItem(new Magazine("Time Magazine", 2024,
                CAT_NEWS, 7, "0040-781X", "Weekly"));
        catalog.addItem(new Magazine("The Economist", 2023,
                CAT_NEWS, 5, "0013-0613", "Weekly"));
        catalog.addItem(new Magazine("Scientific American", 2021,
                CAT_SCIENCE, 4, "0036-8733", "Monthly"));

        catalog.addItem(new Ebook("Introduction to Algorithms", 2009,
                CAT_TECH, "PDF", 45.2, "https://download.resource/algo.pdf", 1292));
        catalog.addItem(new Ebook("Clean Code", 2008,
                CAT_TECH, "EPUB", 12.5, "https://download.resource/cleancode.epub", 464));
        catalog.addItem(new Ebook("Effective Java", 2018,
                CAT_TECH, "PDF", 8.4, "https://download.resource/effjava.pdf", 412));
        catalog.addItem(new Ebook("The Pragmatic Programmer", 1999,
                CAT_TECH, "EPUB", 5.1, "https://download.resource/pragmatic.epub", 352));

        catalog.addItem(new AudioBook("The Hobbit", 1937,
                CAT_FICTION, FMT_MP3, 320.0, "https://audio.resource/hobbit.mp3", 310));
        catalog.addItem(new AudioBook("Atomic Habits", 2018,
                "Self-Help", "M4B", 185.5, "https://audio.resource/habits.m4b", 320));
        catalog.addItem(new AudioBook("The Alchemist", 1988,
                CAT_FICTION, FMT_MP3, 110.2, "https://audio.resource/alchemist.mp3", 163));
        catalog.addItem(new AudioBook("Thinking Fast and Slow", 2011,
                CAT_SCIENCE, FMT_MP3, 412.0, "https://audio.resource/thinking.mp3", 499));
    }
}