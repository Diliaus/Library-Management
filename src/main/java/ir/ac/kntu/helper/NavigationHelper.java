package ir.ac.kntu.helper;

import ir.ac.kntu.model.item.LibraryItem;

import java.util.List;
import java.util.Scanner;

public final class NavigationHelper {

    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String BLUE = "\u001B[34m";
    private static final String MSG_INVALID = "Invalid option!";

    private NavigationHelper() {
    }

    private static int getNextPage(String action, int currentPage, int totalPages) {
        if (action.equals("n") && currentPage < totalPages) {
            return currentPage + 1;
        }
        if (action.equals("p") && currentPage > 1) {
            return currentPage - 1;
        }
        return currentPage;
    }

    private static boolean isExitAction(String action) {
        return "b".equals(action) || "quit".equals(action) || "0".equals(action);
    }

    public static void printItemsWithPagination(List<LibraryItem> items, Scanner scanner) {
        if (items == null || items.isEmpty()) {
            System.out.println(RED + "No items found." + RESET);
            return;
        }
        if (scanner == null) {
            throw new IllegalArgumentException("Scanner cannot be null");
        }

        int pageSize = 10;
        int currentPage = 1;
        int totalPages = (int) Math.ceil((double) items.size() / pageSize);

        while (true) {
            printPage(items, currentPage, pageSize);
            String action = readPageAction(scanner);
            if (isExitAction(action)) {
                break;
            }
            int nextPage = getNextPage(action, currentPage, totalPages);
            if (nextPage == currentPage) {
                System.out.println(RED + MSG_INVALID + RESET);
            } else {
                currentPage = nextPage;
            }
        }
    }

    private static void printPage(List<LibraryItem> items, int page, int pageSize) {
        int totalPages = (int) Math.ceil((double) items.size() / pageSize);
        System.out.println(BLUE + "\n--- Results (Page " + page + " of " + totalPages + ") ---" + RESET);
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, items.size());
        for (int i = start; i < end; i++) {
            LibraryItem item = items.get(i);
            System.out.println(item);
        }
    }

    private static String readPageAction(Scanner scanner) {
        System.out.println("\nOptions: [n] Next Page | [p] Previous Page | [b/0] Back to Menu");
        System.out.print("Choose action: ");
        return scanner.nextLine().trim().toLowerCase();
    }
}