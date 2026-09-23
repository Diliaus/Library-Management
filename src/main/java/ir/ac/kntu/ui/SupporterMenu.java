package ir.ac.kntu.ui;

import ir.ac.kntu.model.BorrowRecord;
import ir.ac.kntu.model.SupportRequest;
import ir.ac.kntu.model.item.AudioBook;
import ir.ac.kntu.model.item.Book;
import ir.ac.kntu.model.item.Ebook;
import ir.ac.kntu.model.item.LibraryItem;
import ir.ac.kntu.model.item.Magazine;
import ir.ac.kntu.model.item.PhysicalItem;
import ir.ac.kntu.model.user.RegularUser;
import ir.ac.kntu.model.user.Supporter;
import ir.ac.kntu.services.Catalog;
import ir.ac.kntu.services.UserManager;
import ir.ac.kntu.exception.LibrarySystemException;
import ir.ac.kntu.exception.InvalidInputException;
import ir.ac.kntu.util.Validator;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.function.Supplier;

@SuppressWarnings("PMD.CyclomaticComplexity")
public class SupporterMenu {
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String BLUE = "\u001B[34m";
    private static final String CYAN = "\u001B[36m";
    private static final String MENU_SUPPORTER = "\n=== SUPPORTER MENU ===";
    private static final String MSG_INVALID = "Invalid option!";
    private static final String STATUS_CLOSED = "CLOSED";

    private final Catalog catalog;
    private final UserManager userManager;
    private final Scanner scanner;
    private final Supporter currentSupporter;

    public SupporterMenu(Catalog catalog, UserManager userManager, Scanner scanner, Supporter currentSupporter) {
        this.catalog = catalog;
        this.userManager = userManager;
        this.scanner = scanner;
        this.currentSupporter = currentSupporter;
    }

    public void showSupporterMenu() {
        while (true) {
            System.out.println(CYAN + MENU_SUPPORTER + " (Sections: " + currentSupporter.getAssignedSections() + ")" + RESET);
            System.out.println("1. Add Library Item");
            System.out.println("2. Remove Library Item");
            System.out.println("3. View Catalog");
            System.out.println("4. View Support Requests");
            System.out.println("5. View Fines (Sorted)");
            System.out.println("6. View Loan History (Recent 10)");
            System.out.println("7. View Item Reservation Queue (Specific)");
            System.out.println("8. View All Reserved Items & Queues (Global)");
            System.out.println("9. View User Transactions");
            System.out.println("10. Logout");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();
            if (executeMenuChoice(choice)) {
                return;
            }
        }
    }

    private boolean executeMenuChoice(String choice) {
        if ("10".equals(choice)) {
            userManager.logout();
            System.out.println(GREEN + "Logged out successfully." + RESET);
            return true;
        }
        Map<String, Runnable> actions = Map.of(
                "1", this::handleAddItem,
                "2", this::removeItem,
                "3", this::showCatalog,
                "4", this::handleRequests,
                "5", this::showFines,
                "6", this::showLoanHistory,
                "7", this::viewItemReservationQueue,
                "8", this::viewAllReservationsAndQueues,
                "9", this::viewUserTransactions
        );
        Runnable action = actions.get(choice);
        if (action != null) {
            try {
                action.run();
            } catch (LibrarySystemException e) {
                System.out.println(RED + "Supporter Operation Error: " + e.getMessage() + RESET);
            }
        } else {
            System.out.println(RED + MSG_INVALID + RESET);
        }
        return false;
    }

    private void viewAllReservationsAndQueues() {
        System.out.println(BLUE + "\n--- ALL RESERVED ITEMS & QUEUES ---" + RESET);
        boolean hasAnyReservation = false;
        for (LibraryItem item : catalog.getAllItems().values()) {
            if (item instanceof PhysicalItem physicalItem) {
                Queue<String> queue = physicalItem.getReservationQueue();
                if (queue != null && !queue.isEmpty()) {
                    hasAnyReservation = true;
                    System.out.println(CYAN + "\n>> Item: [" + item.getId() + "] " + item.getTitle() + " (" + item.getCategory() + ")" + RESET);
                    System.out.println("   Queue Length: " + queue.size() + " users");
                    int position = 1;
                    for (String userEmail : queue) {
                        System.out.println("   " + position + ". User Email: " + userEmail + " | Item Title: " + item.getTitle());
                        position++;
                    }
                    System.out.println("-----------------------------------");
                }
            }
        }
        if (!hasAnyReservation) {
            System.out.println("There are no active item reservations in the entire system.");
        }
    }

    private void viewUserTransactions() {
        System.out.println(BLUE + "\n--- VIEW USER TRANSACTIONS ---" + RESET);
        System.out.print("Enter User Member ID: ");
        String memberId = scanner.nextLine().trim();
        if (memberId.isEmpty()) {
            throw new InvalidInputException("Member ID cannot be empty.");
        }
        RegularUser targetUser = null;
        for (RegularUser u : userManager.getRegularUsers().values()) {
            if (memberId.equalsIgnoreCase(u.getMemberId())) {
                targetUser = u;
                break;
            }
        }
        if (targetUser == null) {
            throw new InvalidInputException("User not found with this Member ID.");
        }
        System.out.println(CYAN + "\n>> Transaction History for " + targetUser.getFirstName() + " " + targetUser.getLastName() + ":" + RESET);
        var txList = targetUser.getWallet().getTransactions();
        if (txList.isEmpty()) {
            System.out.println("This user has no transactions.");
        } else {
            txList.forEach(t -> System.out.println("- [" + t.getType() + "] " + t.getAmount() + " Tomans | " + t.getDescription() + " | Date: " + t.getDate()));
        }
    }

    private void removeItem() {
        System.out.print("Enter Item ID: ");
        String itemId = scanner.nextLine();
        if (itemId.trim().isEmpty()) {
            throw new InvalidInputException("Item ID cannot be empty.");
        }

        catalog.removeItem(itemId);
        System.out.println(GREEN + "Item removed successfully." + RESET);
    }

    private void showCatalog() {
        System.out.println(BLUE + "\n--- ALL ITEMS ---" + RESET);
        for (LibraryItem item : catalog.getAllItems().values()) {
            System.out.println(formatItemDetails(item));
        }
    }

    private String formatItemDetails(LibraryItem item) {
        String base = "[" + item.getId() + "] " + item.getTitle() + " | Category: " + item.getCategory() + " | Year: " + item.getPublishYear();
        if (item instanceof Book book) {
            return base + " | Type: Book | Author: " + book.getAuthor() + " | ISBN: " + book.getIsbn() + " | Pages: " + book.getPages() + " | Copies: " + book.getAvailableCopies();
        }
        if (item instanceof Magazine mag) {
            return base + " | Type: Magazine | ISSN: " + mag.getIssn() + " | Cycle: " + mag.getCycle() + " | Copies: " + mag.getAvailableCopies();
        }
        if (item instanceof Ebook ebook) {
            return base + " | Type: Ebook | Format: " + ebook.getFormat() + " | Size: " + ebook.getFileSize() + "MB | Pages: " + ebook.getPages() + " | URL: " + ebook.getDownloadUrl();
        }
        if (item instanceof AudioBook audio) {
            return base + " | Type: Audio Book | Format: " + audio.getFormat() + " | Size: " + audio.getFileSize() + "MB | Duration: " + audio.getDuration() + " mins | URL: " + audio.getDownloadUrl();
        }
        return base;
    }

    private void handleRequests() {
        System.out.println(BLUE + "\n--- SUPPORT REQUESTS MANAGEMENT ---" + RESET);
        System.out.println("Filter by: 1. All  2. Open  3. Closed");
        System.out.print("Choice: ");
        String filterChoice = scanner.nextLine().trim();
        printFilteredRequests(filterChoice);
        System.out.print("Enter Request ID to handle (or press Enter to return): ");
        String reqId = scanner.nextLine().trim();
        if (reqId.isEmpty()) {
            return;
        }
        SupportRequest targetRequest = findRequestById(reqId);
        if (targetRequest == null) {
            throw new InvalidInputException("Request ID not found or you do not have access to its section.");
        }
        if (STATUS_CLOSED.equals(targetRequest.getStatus())) {
            throw new InvalidInputException("This request is already closed.");
        }
        closeRequest(targetRequest);
    }

    private void printFilteredRequests(String filterChoice) {
        for (SupportRequest req : userManager.getSupportRequests()) {
            if (!currentSupporter.hasAccessTo(req.getSection())) {
                continue;
            }
            if ("2".equals(filterChoice) && !"OPEN".equals(req.getStatus())) {
                continue;
            }
            if ("3".equals(filterChoice) && !STATUS_CLOSED.equals(req.getStatus())) {
                continue;
            }
            System.out.println(req);
            System.out.println("-----------------------------------");
        }
    }

    private SupportRequest findRequestById(String reqId) {
        for (SupportRequest req : userManager.getSupportRequests()) {
            if (req.getId().equalsIgnoreCase(reqId) && currentSupporter.hasAccessTo(req.getSection())) {
                return req;
            }
        }
        return null;
    }

    private void closeRequest(SupportRequest targetRequest) {
        String responseText = readNonEmpty("Enter Response Text: ", "Response");
        targetRequest.setResponse(responseText);
        targetRequest.setStatus(STATUS_CLOSED);
        System.out.println(GREEN + "Request updated and closed successfully." + RESET);
    }

    private void showFines() {
        System.out.println(BLUE + "\n--- USERS FINES LIST ---" + RESET);
        List<RegularUser> sortedUsers = userManager.getRegularUsers().values().stream()
                .sorted((u1, u2) -> Double.compare(userManager.getTotalUnpaidFines(u2.getEmail()), userManager.getTotalUnpaidFines(u1.getEmail())))
                .toList();
        for (RegularUser user : sortedUsers) {
            double unpaidFine = userManager.getTotalUnpaidFines(user.getEmail());
            System.out.println("User: " + user.getFirstName() + " " + user.getLastName() + " (" + user.getEmail() + ") | Unpaid Fines: " + unpaidFine + " Tomans");
        }
    }

    private String formatLoanLine(RegularUser user, BorrowRecord record) {
        String status = record.isReturned() ? (GREEN + "Returned" + RESET) :
                (record.isOverdue() ? (RED + "Active (OVERDUE)" + RESET) : (CYAN + "Active" + RESET));
        return String.format("User: %s %s (%s) | Item: [%s] %s | Status: %s",
                user.getFirstName(), user.getLastName(), user.getEmail(),
                record.getItem().getId(), record.getItem().getTitle(), status);
    }

    private void showLoanHistory() {
        System.out.println(BLUE + "\n--- LOANS HISTORY (Recent 10) ---" + RESET);
        List<String> allLoanLines = userManager.getRegularUsers().values().stream()
                .filter(usr -> usr.getBorrowRecords() != null)
                .flatMap(usr -> usr.getBorrowRecords().stream().map(rec -> formatLoanLine(usr, rec)))
                .toList();
        if (allLoanLines.isEmpty()) {
            System.out.println("No borrow history found in the entire system.");
            return;
        }
        int totalLoans = allLoanLines.size();
        int startIndex = Math.max(0, totalLoans - 10);
        for (int i = totalLoans - 1; i >= startIndex; i--) {
            System.out.println(allLoanLines.get(i));
        }
    }

    private void viewItemReservationQueue() {
        System.out.print("Enter Item ID: ");
        String itemId = scanner.nextLine().trim();
        if (itemId.isEmpty()) {
            throw new InvalidInputException("Item ID cannot be empty.");
        }
        LibraryItem item = catalog.getItemById(itemId);
        if (item == null) {
            throw new InvalidInputException("Item not found.");
        }
        if (!(item instanceof PhysicalItem)) {
            throw new InvalidInputException("Digital resources do not have a reservation queue.");
        }
        PhysicalItem physicalItem = (PhysicalItem) item;
        Queue<String> queue = physicalItem.getReservationQueue();
        System.out.println(BLUE + "\n--- RESERVATION QUEUE FOR: " + item.getTitle() + " [" + item.getId() + "] ---" + RESET);
        if (queue == null || queue.isEmpty()) {
            System.out.println("No users in the reservation queue for this item.");
            return;
        }
        int position = 1;
        for (String userEmail : queue) {
            System.out.println(position + ". User Email: " + userEmail);
            position++;
        }
    }

    private String readNonEmpty(String prompt, String fieldName) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println(RED + fieldName + " cannot be empty. Try again." + RESET);
                continue;
            }
            return input;
        }
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value < 0) {
                    System.out.println(RED + "Value cannot be negative. Please enter a valid positive integer." + RESET);
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println(RED + "Invalid number format. Please enter an integer." + RESET);
            }
        }
    }

    private int readValidYear(String prompt) {
        while (true) {
            int year = readInt(prompt);
            if (Validator.isValidPublishYear(year)) {
                return year;
            }
            System.out.println(RED + "Invalid publish year. Year must be between 1450 and current year." + RESET);
        }
    }

    private double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                double value = Double.parseDouble(scanner.nextLine().trim());
                if (value < 0) {
                    System.out.println(RED + "Value cannot be negative. Please enter a valid positive number." + RESET);
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println(RED + "Invalid decimal format. Please enter a valid number." + RESET);
            }
        }
    }

    private String readValidISBN(String prompt) {
        while (true) {
            String isbn = readNonEmpty(prompt, "ISBN");
            if (Validator.isValidISBN(isbn)) {
                return isbn;
            }
            System.out.println(RED + "Invalid ISBN format. (Must start with 978 or 979 and have 13 digits total)." + RESET);
        }
    }

    private String readValidISSN(String prompt) {
        while (true) {
            String issn = readNonEmpty(prompt, "ISSN");
            if (Validator.isValidISSN(issn)) {
                return issn;
            }
            System.out.println(RED + "Invalid ISSN format. (Pattern: XXXX-XXXX or XXXX-XXXI)." + RESET);
        }
    }

    private String readValidURL(String prompt) {
        while (true) {
            String url = readNonEmpty(prompt, "URL");
            if (Validator.isValidURL(url)) {
                return url;
            }
            System.out.println(RED + "Invalid URL format. (Must start with https://)." + RESET);
        }
    }

    private void handleAddItem() {
        System.out.println("\nSelect Item Type:");
        System.out.println("1. Book");
        System.out.println("2. Magazine");
        System.out.println("3. Ebook");
        System.out.println("4. Audio Book");
        System.out.print("Choice: ");
        String typeChoice = scanner.nextLine().trim();
        if (!typeChoice.matches("[1234]")) {
            throw new InvalidInputException(MSG_INVALID);
        }
        String title = readNonEmpty("Title: ", "Title");
        int year = readValidYear("Year: ");
        String category = readNonEmpty("Category: ", "Category");
        LibraryItem item = createItemByType(typeChoice, title, year, category);
        if (item != null) {
            catalog.addItem(item);
            System.out.println(GREEN + "Item created with ID: " + item.getId() + RESET);
        }
    }

    private LibraryItem createItemByType(String typeChoice, String title, int year, String category) {
        Map<String, Supplier<LibraryItem>> creators = Map.of(
                "1", () -> createBook(title, year, category),
                "2", () -> createMag(title, year, category),
                "3", () -> createEbook(title, year, category),
                "4", () -> createAudio(title, year, category)
        );
        Supplier<LibraryItem> creator = creators.get(typeChoice);
        return creator != null ? creator.get() : null;
    }

    private LibraryItem createBook(String title, int year, String category) {
        int copies = readInt("Available Copies: ");
        int pages = readInt("Pages: ");
        String isbn = readValidISBN("ISBN: ");
        String author = readNonEmpty("Author: ", "Author");
        return new Book(title, year, category, copies, pages, isbn, author);
    }

    private LibraryItem createMag(String title, int year, String category) {
        int copies = readInt("Available Copies: ");
        String issn = readValidISSN("ISSN: ");
        String cycle = readNonEmpty("Cycle (Monthly/Weekly/Seasonal): ", "Cycle");
        return new Magazine(title, year, category, copies, issn, cycle);
    }

    private LibraryItem createEbook(String title, int year, String category) {
        String format = readNonEmpty("Format (e.g. PDF): ", "Format");
        double size = readDouble("Size (MB): ");
        String url = readValidURL("URL: ");
        int pages = readInt("Pages: ");
        return new Ebook(title, year, category, format, size, url, pages);
    }

    private LibraryItem createAudio(String title, int year, String category) {
        String format = readNonEmpty("Format (e.g. MP3): ", "Format");
        double size = readDouble("Size (MB): ");
        String url = readValidURL("URL: ");
        int duration = readInt("Duration (Minutes): ");
        return new AudioBook(title, year, category, format, size, url, duration);
    }
}