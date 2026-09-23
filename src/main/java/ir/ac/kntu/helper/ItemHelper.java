package ir.ac.kntu.helper;

import ir.ac.kntu.model.item.AudioBook;
import ir.ac.kntu.model.item.Book;
import ir.ac.kntu.model.item.Ebook;
import ir.ac.kntu.model.item.LibraryItem;
import ir.ac.kntu.model.item.Magazine;
import ir.ac.kntu.services.Catalog;
import ir.ac.kntu.util.Validator;

import java.util.Scanner;

public final class ItemHelper {

    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String MSG_INVALID_INPUT = "Invalid input data. Expected positive numbers.";
    private static final String MSG_INVALID_OPTION = "Invalid option!";

    private static Scanner scanner;

    private ItemHelper() {
    }

    public static void handleAddItem(Catalog catalog, Scanner sc) {
        scanner = sc;
        System.out.println("""
                \nSelect Item Type:
                1. Book
                2. Magazine
                3. Ebook
                4. Audio Book""");
        System.out.print("Choice: ");
        String typeChoice = scanner.nextLine().trim();
        if (!typeChoice.matches("[1234]")) {
            System.out.println(RED + MSG_INVALID_OPTION + RESET);
            return;
        }

        System.out.print("Title: ");
        String title = scanner.nextLine();
        if (title.trim().isEmpty()) {
            System.out.println(RED + "Title cannot be empty." + RESET);
            return;
        }
        int year = readYear();
        if (year <= 0) {
            return;
        }
        System.out.print("Category: ");
        String category = scanner.nextLine();
        if (category.trim().isEmpty()) {
            System.out.println(RED + "Category cannot be empty." + RESET);
            return;
        }

        LibraryItem item = createItemByType(typeChoice, title, year, category);
        if (item != null) {
            catalog.addItem(item);
            System.out.println(GREEN + "Item created successfully with ID: " + item.getId() + RESET);
        } else {
            System.out.println(RED + "Failed to create item." + RESET);
        }
    }

    private static int readYear() {
        System.out.print("Year: ");
        try {
            int year = Integer.parseInt(scanner.nextLine().trim());
            if (Validator.isValidPublishYear(year) && year > 0) {
                return year;
            }
            System.out.println(RED + "Invalid publish year." + RESET);
        } catch (NumberFormatException e) {
            System.out.println(RED + "Invalid year format." + RESET);
        }
        return -1;
    }

    private static LibraryItem createItemByType(String type, String title, int year, String category) {
        switch (type) {
            case "1":
                return createBook(title, year, category);
            case "2":
                return createMag(title, year, category);
            case "3":
                return createEbook(title, year, category);
            case "4":
                return createAudio(title, year, category);
            default:
                return null;
        }
    }

    private static LibraryItem createBook(String title, int year, String category) {
        try {
            System.out.print("Available Copies: ");
            int copies = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Pages: ");
            int pages = Integer.parseInt(scanner.nextLine().trim());

            if (copies < 0 || pages <= 0) {
                System.out.println(RED + MSG_INVALID_INPUT + RESET);
                return null;
            }

            System.out.print("ISBN: ");
            String isbn = scanner.nextLine();
            if (!Validator.isValidISBN(isbn)) {
                System.out.println(RED + "Invalid ISBN format." + RESET);
                return null;
            }
            System.out.print("Author: ");
            String author = scanner.nextLine();
            if (author.trim().isEmpty()) {
                System.out.println(RED + "Author cannot be empty." + RESET);
                return null;
            }
            return new Book(title, year, category, copies, pages, isbn, author);
        } catch (NumberFormatException e) {
            System.out.println(RED + MSG_INVALID_INPUT + RESET);
            return null;
        }
    }

    private static LibraryItem createMag(String title, int year, String category) {
        try {
            System.out.print("Available Copies: ");
            int copies = Integer.parseInt(scanner.nextLine().trim());

            if (copies < 0) {
                System.out.println(RED + MSG_INVALID_INPUT + RESET);
                return null;
            }

            System.out.print("ISSN: ");
            String issn = scanner.nextLine();
            if (!Validator.isValidISSN(issn)) {
                System.out.println(RED + "Invalid ISSN format." + RESET);
                return null;
            }
            System.out.print("Cycle (e.g. Monthly, Weekly): ");
            String cycle = scanner.nextLine();
            if (cycle.trim().isEmpty()) {
                System.out.println(RED + "Cycle cannot be empty." + RESET);
                return null;
            }
            return new Magazine(title, year, category, copies, issn, cycle);
        } catch (NumberFormatException e) {
            System.out.println(RED + MSG_INVALID_INPUT + RESET);
            return null;
        }
    }

    private static LibraryItem createEbook(String title, int year, String category) {
        try {
            System.out.print("Format (e.g. PDF, EPUB): ");
            String format = scanner.nextLine();
            if (format.trim().isEmpty()) {
                System.out.println(RED + "Format cannot be empty." + RESET);
                return null;
            }
            System.out.print("File Size (MB): ");
            double size = Double.parseDouble(scanner.nextLine().trim());
            System.out.print("Download URL: ");
            String url = scanner.nextLine();
            System.out.print("Pages: ");
            int pages = Integer.parseInt(scanner.nextLine().trim());

            if (size <= 0 || pages <= 0 || url.trim().isEmpty()) {
                System.out.println(RED + MSG_INVALID_INPUT + RESET);
                return null;
            }

            return new Ebook(title, year, category, format, size, url, pages);
        } catch (NumberFormatException e) {
            System.out.println(RED + MSG_INVALID_INPUT + RESET);
            return null;
        }
    }

    private static LibraryItem createAudio(String title, int year, String category) {
        try {
            System.out.print("Format (e.g. MP3): ");
            String format = scanner.nextLine();
            if (format.trim().isEmpty()) {
                System.out.println(RED + "Format cannot be empty." + RESET);
                return null;
            }
            System.out.print("File Size (MB): ");
            double size = Double.parseDouble(scanner.nextLine().trim());
            System.out.print("Download URL: ");
            String url = scanner.nextLine();
            System.out.print("Duration (Minutes): ");
            int duration = Integer.parseInt(scanner.nextLine().trim());

            if (size <= 0 || duration <= 0 || url.trim().isEmpty()) {
                System.out.println(RED + MSG_INVALID_INPUT + RESET);
                return null;
            }

            return new AudioBook(title, year, category, format, size, url, duration);
        } catch (NumberFormatException e) {
            System.out.println(RED + MSG_INVALID_INPUT + RESET);
            return null;
        }
    }
}