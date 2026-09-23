package ir.ac.kntu;

import ir.ac.kntu.helper.DataInitializer;
import ir.ac.kntu.helper.RegisterHelper;
import ir.ac.kntu.model.BorrowRecord;
import ir.ac.kntu.model.Fine;
import ir.ac.kntu.model.item.LibraryItem;
import ir.ac.kntu.model.user.RegularUser;
import ir.ac.kntu.model.user.User;
import ir.ac.kntu.persistence.DataManager;
import ir.ac.kntu.services.Catalog;
import ir.ac.kntu.services.UserManager;
import ir.ac.kntu.exception.InvalidInputException;
import ir.ac.kntu.exception.LibrarySystemException;

import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Main {

    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";

    private static final String MENU_MAIN = "\n=== MAIN MENU ===";
    private static final String MSG_INVALID = "Invalid option!";
    private static final String CHOOSE_OPTION = "Choose an option: ";
    private static final String PROMPT_PASSWORD = "Password: ";
    private static final String MSG_PASSWORD_EMPTY = "Password cannot be empty.";

    private static final String STR_BACK = "back";

    private static final Catalog CATALOG = new Catalog();
    private static final UserManager USER_MANAGER = new UserManager();
    private static final Scanner SCANNER = new Scanner(System.in);

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            DataManager.saveData(CATALOG, USER_MANAGER);
        }));

        boolean loaded = DataManager.loadData(CATALOG, USER_MANAGER);
        if (!loaded) {
            DataInitializer.initializeData(CATALOG);

            RegularUser defaultUser = new RegularUser(
                    "Ali", "Alavi", "PassAli1234!",
                    "aliahmadi@kntu.ac.ir", "09121111111", "STU000001"
            );
            USER_MANAGER.registerRegularUser(defaultUser);

            List<LibraryItem> foundItems = CATALOG.searchByTitle("The Great Gatsby");
            if (!foundItems.isEmpty()) {
                LibraryItem gatsby = foundItems.get(0);
                BorrowRecord record = new BorrowRecord(gatsby, 14);
                record.setReturned(true);
                defaultUser.addBorrowRecord(record);

                Fine defaultFine = new Fine(25_000.0);
                USER_MANAGER.addFineToUser(defaultUser.getEmail(), defaultFine);
            }
        }

        boolean running = true;
        while (running) {
            try {
                User currentUser = USER_MANAGER.getCurrentUser();
                if (currentUser == null) {
                    running = handleMainMenu();
                } else {
                    currentUser.displayMenu(CATALOG, USER_MANAGER, SCANNER);
                }
            } catch (LibrarySystemException e) {
                System.out.println(RED + "System Error: " + e.getMessage() + RESET);
            }
        }
    }

    private static void printMenuOptions() {
        System.out.println(CYAN + MENU_MAIN + RESET);
        System.out.println("1. Register Account");
        System.out.println("2. User Login (Regular Users)");
        System.out.println("3. Admin Login");
        System.out.println("4. Supporter Login");
        System.out.println("5. Exit");
        System.out.print(CHOOSE_OPTION);
    }

    private static boolean handleMainMenu() {
        printMenuOptions();
        String choice = SCANNER.nextLine().trim();
        try {
            switch (choice) {
                case "1" -> RegisterHelper.handleRegister(USER_MANAGER, SCANNER);
                case "2" -> handleUserLogin();
                case "3" -> handleAdminLogin();
                case "4" -> handleSupporterLogin();
                case "5" -> {
                    System.out.println(YELLOW + "Exiting program..." + RESET);
                    return false;
                }
                default -> System.out.println(RED + MSG_INVALID + RESET);
            }
        } catch (InvalidInputException e) {
            System.out.println(RED + "Login/Input Error: " + e.getMessage() + RESET);
        }
        return true;
    }

    private static void handleAdminLogin() {
        System.out.print("Admin Username/Email (or '0' to back): ");
        String username = SCANNER.nextLine();
        if ("0".equals(username) || STR_BACK.equalsIgnoreCase(username.trim())) {
            return;
        }
        if (username.trim().isEmpty()) {
            throw new InvalidInputException("Username cannot be empty.");
        }
        System.out.print(PROMPT_PASSWORD);
        String password = SCANNER.nextLine();
        if ("0".equals(password) || STR_BACK.equalsIgnoreCase(password.trim())) {
            return;
        }
        if (password.trim().isEmpty()) {
            throw new InvalidInputException(MSG_PASSWORD_EMPTY);
        }
        executeLogin(username.trim(), password, "ADMIN");
    }

    private static void handleSupporterLogin() {
        System.out.print("Supporter Username/Email (or '0' to back): ");
        String username = SCANNER.nextLine();
        if ("0".equals(username) || STR_BACK.equalsIgnoreCase(username.trim())) {
            return;
        }
        if (username.trim().isEmpty()) {
            throw new InvalidInputException("Username cannot be empty.");
        }
        System.out.print(PROMPT_PASSWORD);
        String password = SCANNER.nextLine();
        if ("0".equals(password) || STR_BACK.equalsIgnoreCase(password.trim())) {
            return;
        }
        if (password.trim().isEmpty()) {
            throw new InvalidInputException(MSG_PASSWORD_EMPTY);
        }
        executeLogin(username.trim(), password, "SUPPORTER");
    }

    private static void handleUserLogin() {
        System.out.print("Username/Email (or '0' to back): ");
        String email = SCANNER.nextLine();
        if ("0".equals(email) || STR_BACK.equalsIgnoreCase(email.trim())) {
            return;
        }
        if (email.trim().isEmpty()) {
            throw new InvalidInputException("Identifier cannot be empty.");
        }
        System.out.print(PROMPT_PASSWORD);
        String password = SCANNER.nextLine();
        if ("0".equals(password) || STR_BACK.equalsIgnoreCase(password.trim())) {
            return;
        }
        if (password.trim().isEmpty()) {
            throw new InvalidInputException(MSG_PASSWORD_EMPTY);
        }
        executeLogin(email.trim(), password, "REGULAR");
    }

    private static void executeLogin(String identifier, String password, String expectedRole) {
        boolean loginSuccess = false;
        if ("ADMIN".equals(expectedRole)) {
            loginSuccess = USER_MANAGER.loginAdmin(identifier, password);
        } else if ("SUPPORTER".equals(expectedRole)) {
            loginSuccess = USER_MANAGER.loginSupporter(identifier, password);
        } else {
            loginSuccess = USER_MANAGER.loginUser(identifier, password);
        }

        if (loginSuccess) {
            User user = USER_MANAGER.getCurrentUser();
            if ("REGULAR".equals(expectedRole) && user instanceof RegularUser) {
                handleTwoStepVerification(user.getEmail());
                System.out.println(GREEN + "Login successful!" + RESET);
            } else {
                System.out.println(GREEN + "Login successful!" + RESET);
            }
        } else {
            throw new InvalidInputException("Invalid credentials or unauthorized role.");
        }
    }

    private static void handleTwoStepVerification(String email) {
        Random random = new Random();
        int code = 100_000 + random.nextInt(900_000);
        System.out.println(YELLOW + "Email sent to: " + email + RESET);
        System.out.println("Code: " + code);
        System.out.print("Enter code (or '0' to back): ");
        String input = SCANNER.nextLine().trim();
        if ("0".equals(input) || STR_BACK.equalsIgnoreCase(input)) {
            USER_MANAGER.logout();
            throw new InvalidInputException("Verification canceled by user.");
        }
        if (!input.equals(String.valueOf(code))) {
            USER_MANAGER.logout();
            throw new InvalidInputException("Verification code mismatch. Access denied.");
        }
    }
}