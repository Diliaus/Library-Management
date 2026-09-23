package ir.ac.kntu.ui;

import ir.ac.kntu.helper.NavigationHelper;
import ir.ac.kntu.model.BorrowRecord;
import ir.ac.kntu.model.Fine;
import ir.ac.kntu.model.Reservation;
import ir.ac.kntu.model.SupportRequest;
import ir.ac.kntu.model.RequestSection;
import ir.ac.kntu.model.SystemConfiguration;
import ir.ac.kntu.model.item.LibraryItem;
import ir.ac.kntu.model.item.PhysicalItem;
import ir.ac.kntu.model.user.RegularUser;
import ir.ac.kntu.model.user.User;
import ir.ac.kntu.services.Catalog;
import ir.ac.kntu.services.UserManager;
import ir.ac.kntu.util.Validator;
import ir.ac.kntu.exception.InvalidInputException;
import ir.ac.kntu.exception.LibrarySystemException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

@SuppressWarnings("PMD.CyclomaticComplexity")
public class RegularUserMenu {
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String CYAN = "\u001B[36m";
    private static final String BLUE = "\u001B[34m";
    private static final String MENU_USER = "\n=== USER MENU (%s) ===";
    private static final String MSG_INVALID = "Invalid option!";
    private static final String CHOOSE_OPTION = "Choose an option: ";
    private static final String PROMPT_ITEM_ID = "Item ID: ";
    private static final String STR_LOGOUT_SUCCESS = "Logged out successfully.";
    private static final String STR_BACK_PROMPT = " (or '0' to back): ";
    private static final String STR_YES = "yes";
    private static final String STR_ALL = "all";
    private static final String DASH_BRACKET = "- [";
    private final Catalog catalog;
    private final UserManager userManager;
    private final Scanner scanner;

    public RegularUserMenu(Catalog catalog, UserManager userManager, Scanner scanner) {
        this.catalog = catalog;
        this.userManager = userManager;
        this.scanner = scanner;
    }

    public void show() {
        while (true) {
            RegularUser user = (RegularUser) userManager.getCurrentUser();
            if (user == null) {
                return;
            }
            System.out.println(CYAN + String.format(MENU_USER, user.getFirstName()) + RESET);
            System.out.println("1. View Entire Catalog");
            System.out.println("2. Search Items by Title");
            System.out.println("3. Advanced Item Filtering");
            System.out.println("4. Borrow Item");
            System.out.println("5. Return or Extend Item");
            System.out.println("6. Reserve Item");
            System.out.println("7. Financial Management (Wallet & Fines)");
            System.out.println("8. Support Center (Requests)");
            System.out.println("9. View My Borrowed & Reserved Items");
            System.out.println("10. Edit Personal Profile");
            System.out.println("0. Logout");
            System.out.print(CHOOSE_OPTION);
            String choice = scanner.nextLine().trim();
            if ("0".equals(choice)) {
                userManager.logout();
                System.out.println(GREEN + STR_LOGOUT_SUCCESS + RESET);
                return;
            }
            try {
                processChoice(choice, user);
            } catch (LibrarySystemException e) {
                System.out.println(RED + "Operation Failed: " + e.getMessage() + RESET);
            }
        }
    }

    private void processChoice(String choice, RegularUser user) {
        Map<String, Runnable> actions = Map.of(
                "1", this::showCatalog,
                "2", this::searchByTitle,
                "3", this::handleAdvancedFilter,
                "4", () -> handleBorrow(user),
                "5", () -> handleReturnOrExtend(user),
                "6", () -> handleReserve(user),
                "7", () -> handleFinancialMenu(user),
                "8", () -> handleSupportRequests(user),
                "9", () -> showUserBorrowedItems(user),
                "10", () -> handleEditProfile(user)
        );
        Runnable action = actions.get(choice);
        if (action != null) {
            action.run();
        } else {
            System.out.println(RED + MSG_INVALID + RESET);
        }
    }

    private void handleFinancialMenu(RegularUser user) {
        while (true) {
            System.out.println(BLUE + "\n--- FINANCIAL MANAGEMENT ---" + RESET);
            System.out.println("Current Wallet Balance: " + user.getWallet().getBalance() + " Tomans");
            System.out.println("Total Unpaid Fines: " + userManager.getTotalUnpaidFines(user.getEmail()) + " Tomans");
            System.out.println("1. Charge Wallet");
            System.out.println("2. View Transaction History");
            System.out.println("3. View & Pay Fines");
            System.out.println("0. Back");
            System.out.print(CHOOSE_OPTION);
            String choice = scanner.nextLine().trim();
            if ("0".equals(choice)) {
                return;
            }
            try {
                if ("1".equals(choice)) {
                    chargeWallet(user);
                } else if ("2".equals(choice)) {
                    viewWalletTransactions(user);
                } else if ("3".equals(choice)) {
                    handleFines(user);
                } else {
                    System.out.println(RED + MSG_INVALID + RESET);
                }
            } catch (LibrarySystemException e) {
                System.out.println(RED + "Financial Error: " + e.getMessage() + RESET);
            }
        }
    }

    private void handleEditProfile(RegularUser user) {
        System.out.println(BLUE + "\n--- EDIT PERSONAL PROFILE ---" + RESET);
        String firstName = promptForField("First Name", user.getFirstName());
        String lastName = promptForField("Last Name", user.getLastName());
        String phone = promptForField("Phone Number", user.getPhoneNumber());
        String password = promptForField("Password", user.getPassword());
        String email = promptForField("Email", user.getEmail());
        String oldEmail = user.getEmail();
        userManager.updateUserProfile(oldEmail, firstName, lastName, phone);
        if (!email.equalsIgnoreCase(oldEmail)) {
            userManager.updateUserEmail(oldEmail, email);
        }
        if (!password.equals(user.getPassword())) {
            userManager.updateUserPassword(oldEmail, password);
        }
        System.out.println(GREEN + "Profile updated successfully." + RESET);
    }

    private String promptForField(String fieldName, String currentValue) {
        System.out.print("Enter New " + fieldName + " (Current: " + currentValue + "): ");
        String value = scanner.nextLine().trim();
        if (value.isEmpty()) {
            return currentValue;
        }
        return value;
    }

    private void showCatalog() {
        List<LibraryItem> allItems = new ArrayList<>(catalog.getAllItems().values());
        NavigationHelper.printItemsWithPagination(allItems, scanner);
    }

    private void searchByTitle() {
        System.out.print("Enter title keywords (or '0' to back): ");
        String title = scanner.nextLine();
        if (title.trim().isEmpty()) {
            throw new InvalidInputException("Search title cannot be empty.");
        }
        List<LibraryItem> results = catalog.searchByTitle(title);
        NavigationHelper.printItemsWithPagination(results, scanner);
    }

    private void handleAdvancedFilter() {
        System.out.print("Enter item type (BOOK, MAGAZINE, EBOOK, AUDIO_BOOK) or 'all': ");
        String typeInput = scanner.nextLine().trim();
        String type = STR_ALL.equalsIgnoreCase(typeInput) ? null : typeInput;
        System.out.print("Enter category or 'all': ");
        String catInput = scanner.nextLine().trim();
        String category = STR_ALL.equalsIgnoreCase(catInput) ? null : catInput;
        System.out.print("Enter publish year or '0' for all: ");
        Integer publishYear = readFilterValue("Invalid year format. Proceeding with 'all'.");
        System.out.print("Enter max pages or '0' for all: ");
        Integer maxPages = readFilterValue("Invalid pages format. Proceeding with 'all'.");
        List<LibraryItem> results = catalog.filterItems(type, category, publishYear, maxPages);
        NavigationHelper.printItemsWithPagination(results, scanner);
    }

    private Integer readFilterValue(String errorMsg) {
        try {
            int value = Integer.parseInt(scanner.nextLine());
            if (value != 0) {
                return Integer.valueOf(value);
            }
        } catch (NumberFormatException e) {
            System.out.println(RED + errorMsg + RESET);
        }
        return null;
    }

    private void handleBorrow(RegularUser user) {
        System.out.print(PROMPT_ITEM_ID + STR_BACK_PROMPT);
        String itemId = scanner.nextLine().trim();
        if ("0".equals(itemId)) {
            return;
        }
        if (!Validator.isValidItemId(itemId)) {
            throw new InvalidInputException("Invalid Item ID format.");
        }
        LibraryItem item = catalog.getItemById(itemId);
        int defaultMaxBorrowDays = SystemConfiguration.getInstance().getMaxBorrowDays();
        if (item instanceof PhysicalItem physicalItem) {
            physicalItem.setAvailableCopies(physicalItem.getAvailableCopies() - 1);
            user.addBorrowRecord(new BorrowRecord(item, defaultMaxBorrowDays));
        } else {
            user.addBorrowRecord(new BorrowRecord(item, 365));
        }
        System.out.println(GREEN + "Item borrowed successfully." + RESET);
    }

    private void processReturn(BorrowRecord record, RegularUser user) {
        if (record.isOverdue()) {
            double customFineRate = SystemConfiguration.getInstance().getFineRatePerDay();
            double fineAmount = record.calculateFine(customFineRate);
            userManager.addFineToUser(user.getEmail(), new Fine(fineAmount));
            System.out.println(RED + "Item is overdue! Fine generated: " + fineAmount + " Tomans." + RESET);
        }
        record.setReturned(true);
        if (record.getItem() instanceof PhysicalItem physicalItem) {
            if (!physicalItem.getReservationQueue().isEmpty()) {
                String nextUserEmail = physicalItem.getReservationQueue().poll();
                User nextBaseUser = userManager.findAnyUser(nextUserEmail);
                if (nextBaseUser instanceof RegularUser nextUser) {
                    Reservation pendingRes = nextUser.getPendingReservationForItem(physicalItem.getId());
                    if (pendingRes != null) {
                        pendingRes.setStatus(Reservation.STATUS_ACTIVE);
                        int expiryDays = SystemConfiguration.getInstance().getReservationExpiryDays();
                        pendingRes.setExpiryDate(java.time.LocalDateTime.now().plusDays(expiryDays));
                    }
                }
                userManager.assignItemToNextInQueue(nextUserEmail, physicalItem);
                System.out.println(GREEN + "Item returned and assigned to next reserved user (" + nextUserEmail + ")." + RESET);
            } else {
                physicalItem.setAvailableCopies(physicalItem.getAvailableCopies() + 1);
                System.out.println(GREEN + "Item returned successfully." + RESET);
            }
        } else {
            System.out.println(GREEN + "Item returned successfully." + RESET);
        }
    }

    private void handleReturnOrExtend(RegularUser user) {
        System.out.print(PROMPT_ITEM_ID + STR_BACK_PROMPT);
        String itemId = scanner.nextLine().trim();
        if ("0".equals(itemId) || itemId.isEmpty()) {
            return;
        }
        BorrowRecord targetRecord = null;
        for (BorrowRecord record : user.getBorrowRecords()) {
            if (record.getItem().getId().equalsIgnoreCase(itemId) && !record.isReturned()) {
                targetRecord = record;
                break;
            }
        }
        if (targetRecord == null) {
            throw new InvalidInputException("No active borrow record found for this item.");
        }
        System.out.println("1. Return Item");
        System.out.println("2. Extend Borrow Duration");
        System.out.print("Choose action (or '0' to back): ");
        String action = scanner.nextLine().trim();
        if ("1".equals(action)) {
            processReturn(targetRecord, user);
        } else if ("2".equals(action)) {
            System.out.print("Enter extra days to add (or '0' to back): ");
            int extraDays = Integer.parseInt(scanner.nextLine().trim());
            if (extraDays == 0) {
                return;
            }
            targetRecord.extendDuration(extraDays);
            System.out.println(GREEN + "Duration extended successfully." + RESET);
        } else {
            System.out.println(RED + MSG_INVALID + RESET);
        }
    }

    private void handleReserve(RegularUser user) {
        System.out.print(PROMPT_ITEM_ID + STR_BACK_PROMPT);
        String itemId = scanner.nextLine().trim();
        if ("0".equals(itemId) || itemId.isEmpty()) {
            return;
        }
        LibraryItem item = catalog.getItemById(itemId);
        catalog.reserveItem(user, itemId);
        PhysicalItem physicalItem = (PhysicalItem) item;
        int defaultExpiryDays = SystemConfiguration.getInstance().getReservationExpiryDays();
        Reservation reservation = new Reservation(physicalItem, defaultExpiryDays);
        user.addReservation(reservation);
        System.out.println(GREEN + "Item reserved successfully. Queue position: " + physicalItem.getReservationQueue().size() + RESET);
    }

    private void chargeWallet(RegularUser user) {
        System.out.print("Amount (or '0' to back): ");
        double amount = Double.parseDouble(scanner.nextLine());
        if (amount == 0) {
            return;
        }
        user.getWallet().charge(amount);
        System.out.println(GREEN + "Wallet charged successfully." + RESET);
    }

    private void viewWalletTransactions(RegularUser user) {
        System.out.println(CYAN + "\n>> Your Transactions:" + RESET);
        var txList = user.getWallet().getTransactions();
        if (txList.isEmpty()) {
            System.out.println("No transactions found.");
        } else {
            txList.forEach(t -> System.out.println("- [" + t.getType() + "] " + t.getAmount() + " Tomans | " + t.getDescription() + " | Date: " + t.getDate()));
        }
    }

    private void handleFines(RegularUser user) {
        double total = userManager.getTotalUnpaidFines(user.getEmail());
        System.out.println("Total Unpaid Fines: " + total);
        if (total > 0) {
            System.out.print("Pay? (yes/no) [or '0' to back]: ");
            if (STR_YES.equalsIgnoreCase(scanner.nextLine().trim())) {
                user.getWallet().deduct(total, "Fines payment");
                userManager.getUserFines(user.getEmail()).forEach(fine -> fine.setPaid(true));
                System.out.println(GREEN + "All fines paid successfully." + RESET);
            }
        }
    }

    private void sendNewRequest(RegularUser user) {
        System.out.println("Select Section:\n1. BOOKS\n2. TECHNICAL\n3. FINANCIAL\n4. RESERVATION");
        System.out.print("Choice: ");
        String sectionChoice = scanner.nextLine().trim();
        RequestSection section = switch (sectionChoice) {
            case "1" -> RequestSection.BOOKS;
            case "2" -> RequestSection.TECHNICAL;
            case "3" -> RequestSection.FINANCIAL;
            case "4" -> RequestSection.RESERVATION;
            default -> throw new InvalidInputException("Invalid section choice.");
        };
        System.out.print("Enter your message: ");
        String msg = scanner.nextLine().trim();
        if (msg.isEmpty()) {
            throw new InvalidInputException("Message cannot be empty.");
        }
        userManager.addSupportRequest(new SupportRequest(user.getEmail(), section, msg));
        System.out.println(GREEN + "Support request submitted successfully." + RESET);
    }

    private void viewRequestHistory(RegularUser user) {
        System.out.println(BLUE + "\n--- YOUR REQUESTS HISTORY ---" + RESET);
        List<SupportRequest> userRequests = userManager.getSupportRequests().stream()
                .filter(req -> req.getUserEmail().equalsIgnoreCase(user.getEmail()))
                .toList();
        if (userRequests.isEmpty()) {
            System.out.println("You have not submitted any requests yet.");
            return;
        }
        userRequests.forEach(req -> System.out.println(req + "\n-----------------------------------"));
    }

    private void handleSupportRequests(RegularUser user) {
        System.out.println(BLUE + "\n--- SUPPORT CENTER ---" + RESET);
        System.out.println("1. Send New Request");
        System.out.println("2. View Request History");
        System.out.print("Choice (or '0' to back): ");
        Map<String, Runnable> actions = Map.of(
                "1", () -> sendNewRequest(user),
                "2", () -> viewRequestHistory(user)
        );
        Runnable action = actions.get(scanner.nextLine().trim());
        if (action != null) {
            action.run();
        }
    }

    private void showUserBorrowedItems(RegularUser user) {
        System.out.println(BLUE + "\n--- MY ITEMS (Member ID: " + user.getMemberId() + ") ---" + RESET);
        List<BorrowRecord> records = user.getBorrowRecords();
        System.out.println(CYAN + ">> Active Loans (Not Returned yet):" + RESET);
        List<BorrowRecord> activeRecords = records == null ? List.of() : records.stream().filter(r -> !r.isReturned()).toList();
        activeRecords.forEach(r -> System.out.println(DASH_BRACKET + r.getItem().getId() + "] " + r.getItem().getTitle()));
        if (activeRecords.isEmpty()) {
            System.out.println("No active loans.");
        }
        System.out.println(CYAN + "\n>> Reservation History (Active Queue):" + RESET);
        List<Reservation> reservations = user.getReservations();
        if (reservations == null || reservations.isEmpty()) {
            System.out.println("No items reserved.");
        } else {
            reservations.forEach(res -> {
                LibraryItem item = res.getItem();
                int position = -1;
                if (item instanceof PhysicalItem physicalItem) {
                    List<String> queueList = new ArrayList<>(physicalItem.getReservationQueue());
                    position = queueList.indexOf(user.getEmail()) + 1;
                }
                System.out.println(DASH_BRACKET + item.getId() + "] " + item.getTitle() +
                        " | Status: " + res.getStatus() +
                        (position > 0 && "PENDING".equals(res.getStatus()) ? " | Queue Position: " + position : ""));
            });
        }
        System.out.println(CYAN + "\n>> Past History (Returned Items):" + RESET);
        List<BorrowRecord> pastRecords = records == null ? List.of() : records.stream().filter(BorrowRecord::isReturned).toList();
        pastRecords.forEach(r -> System.out.println(DASH_BRACKET + r.getItem().getId() + "] " + r.getItem().getTitle()));
        if (pastRecords.isEmpty()) {
            System.out.println("No past borrowing history.");
        }
    }
}