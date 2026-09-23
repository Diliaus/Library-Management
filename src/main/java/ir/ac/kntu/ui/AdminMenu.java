package ir.ac.kntu.ui;

import ir.ac.kntu.model.SystemConfiguration;
import ir.ac.kntu.model.RequestSection;
import ir.ac.kntu.model.user.Supporter;
import ir.ac.kntu.model.user.User;
import ir.ac.kntu.model.user.Admin;
import ir.ac.kntu.persistence.DataManager;
import ir.ac.kntu.services.UserManager;
import ir.ac.kntu.services.Catalog;
import ir.ac.kntu.services.ReportService;
import ir.ac.kntu.exception.LibrarySystemException;
import ir.ac.kntu.exception.InvalidInputException;
import ir.ac.kntu.util.Validator;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

@SuppressWarnings("PMD.CyclomaticComplexity")
public class AdminMenu {
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String CYAN = "\u001B[36m";
    private static final String BLUE = "\u001B[34m";
    private static final String MSG_INVALID = "Invalid option!";
    private static final String CHOOSE_OPTION = "Choose an option: ";
    private static final String STR_BACK = "0. Back";
    private static final String ROLE_SUPPORTER = "SUPPORTER";
    private static final String PROMPT_SUPPORTER_USERNAME = "Enter Supporter Username: ";

    private static final Map<String, RequestSection> SECTION_MAP = Map.of(
            "1", RequestSection.BOOKS,
            "2", RequestSection.TECHNICAL,
            "3", RequestSection.FINANCIAL,
            "4", RequestSection.RESERVATION
    );

    private final UserManager userManager;
    private final Catalog catalog;
    private final Scanner scanner;

    public AdminMenu(UserManager userManager, Catalog catalog, Scanner scanner) {
        this.userManager = userManager;
        this.catalog = catalog;
        this.scanner = scanner;
    }

    private void runMenu(String title, Runnable[] actions, Runnable onBack, String... options) {
        while (true) {
            System.out.println(title);
            for (int i = 0; i < options.length; i++) {
                System.out.println((i + 1) + ". " + options[i]);
            }
            System.out.println(STR_BACK);
            System.out.print(CHOOSE_OPTION);
            String choice = scanner.nextLine().trim();
            if ("0".equals(choice)) {
                onBack.run();
                return;
            }
            int idx = choice.matches("\\d+") ? Integer.parseInt(choice) - 1 : -1;
            if (idx >= 0 && idx < actions.length) {
                try {
                    actions[idx].run();
                } catch (InvalidInputException e) {
                    System.out.println(RED + "Admin Control Error: " + e.getMessage() + RESET);
                } catch (LibrarySystemException e) {
                    System.out.println(RED + "Admin Control Error: " + e.getMessage() + RESET);
                }
            } else {
                System.out.println(RED + MSG_INVALID + RESET);
            }
        }
    }

    public void show() {
        Runnable[] adminActions = {
            this::manageConfigurations, this::manageSupportersMenu,
            this::registerSubAdmin, this::manageUsersMenu, this::handleHtmlReportGeneration
        };
        runMenu(CYAN + "\n=== ADMIN MAIN MENU ===" + RESET, adminActions, () -> {
            userManager.logout();
            System.out.println(GREEN + "Logged out successfully." + RESET);
        }, "Manage System Configurations", "Manage Supporters (CRUD & Sections)",
            "Register New Sub-Admin", "Manage Users (Search, Block, Reset Password)",
            "Generate HTML Financial Report");
    }

    private void handleHtmlReportGeneration() {
        System.out.println(BLUE + "\n--- GENERATING HTML REPORT ---" + RESET);
        ReportService reportService = new ReportService(this.userManager, this.catalog);
        String fileName = "Library_Financial_Report.html";
        reportService.generateHtmlReport(fileName);
        System.out.println(GREEN + "Done! Open '" + fileName + "' in your browser." + RESET);
    }

    private void manageConfigurations() {
        SystemConfiguration config = SystemConfiguration.getInstance();
        while (true) {
            System.out.println(BLUE + "\n--- SYSTEM CONFIGURATIONS ---" + RESET);
            System.out.println("1. Change Max Borrow Days (Current: " + config.getMaxBorrowDays() + ")");
            System.out.println("2. Change Fine Rate Per Day (Current: " + config.getFineRatePerDay() + ")");
            System.out.println("3. Change Reservation Expiry Days (Current: " + config.getReservationExpiryDays() + ")");
            System.out.println("4. Change Max Simultaneous Reservations (Current: " + config.getMaxSimultaneousReservations() + ")");
            System.out.println(STR_BACK);
            System.out.print(CHOOSE_OPTION);
            String choice = scanner.nextLine().trim();
            if ("0".equals(choice)) {
                return;
            }

            try {
                switch (choice) {
                    case "1" -> updateConfig("Enter Max Borrow Days: ", val -> config.setMaxBorrowDays(Integer.parseInt(val)), true);
                    case "2" -> updateConfig("Enter Fine Rate: ", val -> config.setFineRatePerDay(Double.parseDouble(val)), false);
                    case "3" -> updateConfig("Enter Expiry Days: ", val -> config.setReservationExpiryDays(Integer.parseInt(val)), true);
                    case "4" -> updateConfig("Enter Max Reservations: ", val -> config.setMaxSimultaneousReservations(Integer.parseInt(val)), true);
                    default -> System.out.println(RED + MSG_INVALID + RESET);
                }
            } catch (InvalidInputException e) {
                System.out.println(RED + e.getMessage() + RESET);
            }
        }
    }

    @FunctionalInterface
    private interface ConfigSetter {
        void set(String value);
    }

    private void updateConfig(String prompt, ConfigSetter setter, boolean isInt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        try {
            double numericCheck = Double.parseDouble(input);
            if (numericCheck < 0 || (isInt && input.contains("."))) {
                throw new InvalidInputException("Invalid data. Expected positive values.");
            }
            setter.set(input);
            DataManager.saveData(catalog, userManager);
            System.out.println(GREEN + "Updated successfully." + RESET);
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Invalid numeric format or rule violation.");
        }
    }

    private void manageSupportersMenu() {
        Runnable[] suppActions = {
            this::registerSupporter, this::viewSupporters, this::editSupporter,
            this::deleteSupporter, () -> modifySupporterSection(true), () -> modifySupporterSection(false)
        };
        runMenu(BLUE + "\n--- MANAGE SUPPORTERS (CRUD) ---" + RESET, suppActions, () -> {},
                "Register New Supporter", "View Supporter Details / List", "Edit Supporter Information",
                "Delete Supporter", "Assign Section to Supporter", "Remove Section from Supporter");
    }

    private String readInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private String readValidated(String prompt, Predicate<String> validator, String errorMsg, boolean isOptional) {
        while (true) {
            String value = readInput(prompt);
            if (value.isEmpty()) {
                if (isOptional) {
                    return value;
                }
                System.out.println(RED + "Input cannot be empty. Try again." + RESET);
                continue;
            }
            if (validator.test(value)) {
                return value;
            }
            System.out.println(RED + errorMsg + RESET);
        }
    }

    private String[] readUserCredentials() {
        System.out.println(CYAN + "(Enter '0' at any step to cancel and go back)" + RESET);

        String user = readValidated("Username: ", s -> !s.contains(" "), "Username cannot contain spaces.", false);
        if ("0".equals(user)) {
            return new String[0];
        }

        String pass = readValidated("Password: ", Validator::isStrongPassword, "Password is too weak.", false);
        if ("0".equals(pass)) {
            return new String[0];
        }

        String first = readValidated("First Name: ", s -> true, "", false);
        if ("0".equals(first)) {
            return new String[0];
        }

        String last = readValidated("Last Name: ", s -> true, "", false);
        if ("0".equals(last)) {
            return new String[0];
        }

        String email = readValidated("Email: ", Validator::isValidEmail, "Invalid email format.", false);
        if ("0".equals(email)) {
            return new String[0];
        }

        String phone = readValidated("Phone Number: ", Validator::isValidPhone, "Invalid phone format.", false);
        if ("0".equals(phone)) {
            return new String[0];
        }

        return new String[]{user, pass, first, last, email, phone};
    }

    private void registerSupporter() {
        System.out.println(BLUE + "\n--- REGISTER NEW SUPPORTER ---" + RESET);
        String[] creds = readUserCredentials();
        if (creds.length == 0) {
            System.out.println(RED + "Registration cancelled. Returning to menu." + RESET);
            return;
        }
        Supporter supporter = new Supporter(creds[0], creds[1], creds[2], creds[3], creds[4], creds[5]);
        User activeAdmin = userManager.getCurrentUser();
        if (activeAdmin != null) {
            supporter.setCreatedByAdminEmail(activeAdmin.getEmail());
        }
        userManager.registerSupporter(supporter);
        DataManager.saveData(catalog, userManager);
        System.out.println(GREEN + "Supporter registered successfully." + RESET);
    }

    private void registerSubAdmin() {
        System.out.println(BLUE + "\n--- REGISTER NEW SUB-ADMIN ---" + RESET);
        String[] creds = readUserCredentials();
        if (creds.length == 0) {
            System.out.println(RED + "Registration cancelled. Returning to menu." + RESET);
            return;
        }
        Admin newAdmin = new Admin(creds[0], creds[1], creds[2], creds[3], creds[4], creds[5]);
        User activeAdmin = userManager.getCurrentUser();
        if (activeAdmin instanceof Admin) {
            newAdmin.setCreator(activeAdmin);
        }
        userManager.registerAdmin(newAdmin);
        DataManager.saveData(catalog, userManager);
        System.out.println(GREEN + "Sub-Admin registered successfully." + RESET);
    }

    private void paginate(int totalItems, String title, BiConsumer<Integer, Integer> renderer, String backText) {
        int pageSize = 10;
        int currentPage = 0;
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / pageSize));
        while (true) {
            int start = currentPage * pageSize;
            int end = Math.min(start + pageSize, totalItems);
            System.out.println(CYAN + String.format("\n--- %s (Page %d of %d) ---", title, currentPage + 1, totalPages) + RESET);
            renderer.accept(start, end);
            System.out.println("\n[N] Next Page | [P] Previous Page | " + backText);
            System.out.print("Choice: ");
            String action = scanner.nextLine().trim().toLowerCase();
            if ("n".equals(action) && currentPage < totalPages - 1) {
                currentPage++;
            } else if ("p".equals(action) && currentPage > 0) {
                currentPage--;
            } else if ("0".equals(action)) {
                return;
            } else if (!"n".equals(action) && !"p".equals(action)) {
                System.out.println(RED + MSG_INVALID + RESET);
            }
        }
    }

    private void viewSupporters() {
        User activeAdmin = userManager.getCurrentUser();
        String adminEmail = activeAdmin != null ? activeAdmin.getEmail() : "";
        List<Supporter> supporters = userManager.getAllUsersInSystem().stream()
                .filter(u -> ROLE_SUPPORTER.equals(u.getRole()))
                .map(u -> (Supporter) u)
                .filter(supp -> adminEmail.equalsIgnoreCase(supp.getCreatedByAdminEmail()))
                .toList();
        if (supporters.isEmpty()) {
            throw new InvalidInputException("No supporters registered under your management.");
        }
        paginate(supporters.size(), "Supporter List", (start, end) -> {
            System.out.println(CYAN + String.format("%-15s %-15s %-15s %-20s", "Username", "First Name", "Last Name", "Assigned Sections") + RESET);
            System.out.println("---------------------------------------------------------------------------");
            for (int i = start; i < end; i++) {
                Supporter supp = supporters.get(i);
                System.out.printf("%-15s %-15s %-15s %-20s\n", supp.getUsername(), supp.getFirstName(), supp.getLastName(), supp.getAssignedSections());
            }
        }, "0. Back");
    }

    private void checkSupporterAccessible(Supporter supporter, User activeAdmin, String action) {
        if (supporter == null || (activeAdmin != null && supporter.getCreatedByAdminEmail() != null
                && !supporter.getCreatedByAdminEmail().equalsIgnoreCase(activeAdmin.getEmail()))) {
            throw new InvalidInputException("Access Denied or Not Found: Cannot " + action + " this supporter.");
        }
    }

    private Supporter getSupporterIfAccessible(String username, String action) {
        Supporter supporter = userManager.getSupporter(username);
        checkSupporterAccessible(supporter, userManager.getCurrentUser(), action);
        return supporter;
    }

    private void editSupporter() {
        System.out.println(BLUE + "\n--- EDIT SUPPORTER INFORMATION ---" + RESET);
        Supporter supporter = getSupporterIfAccessible(readInput(PROMPT_SUPPORTER_USERNAME), "edit");
        String first = readInput("New First Name (Enter to skip): ");
        if (!first.isEmpty()) {
            supporter.setFirstName(first);
        }
        String last = readInput("New Last Name (Enter to skip): ");
        if (!last.isEmpty()) {
            supporter.setLastName(last);
        }
        String email = readValidated("New Email (Enter to skip): ", Validator::isValidEmail, "Invalid format.", true);
        if (!email.isEmpty()) {
            userManager.updateUserEmail(supporter.getUsername(), email);
        }
        String phone = readValidated("New Phone (Enter to skip): ", Validator::isValidPhone, "Invalid format.", true);
        if (!phone.isEmpty()) {
            supporter.setPhoneNumber(phone);
        }

        DataManager.saveData(catalog, userManager);
        System.out.println(GREEN + "Supporter updated successfully." + RESET);
    }

    private void deleteSupporter() {
        System.out.println(BLUE + "\n--- DELETE SUPPORTER ---" + RESET);
        Supporter supporter = getSupporterIfAccessible(readInput("Enter Supporter Username to delete: "), "delete");
        userManager.removeSupporter(supporter.getUsername());
        DataManager.saveData(catalog, userManager);
        System.out.println(GREEN + "Supporter deleted successfully." + RESET);
    }

    private void modifySupporterSection(boolean assign) {
        Supporter supporter = getSupporterIfAccessible(readInput(PROMPT_SUPPORTER_USERNAME), assign ? "assign" : "remove");
        RequestSection section = chooseSection();
        if (assign == supporter.getAssignedSections().contains(section)) {
            throw new InvalidInputException("Supporter already in requested state for this section.");
        }
        if (assign) {
            supporter.addSection(section);
        } else {
            supporter.removeSection(section);
        }

        DataManager.saveData(catalog, userManager);
        System.out.println(GREEN + "Section modified successfully for " + supporter.getUsername() + "." + RESET);
    }

    private void manageUsersMenu() {
        Runnable[] userActions = { this::filterAndSearchUsers, this::toggleUserActiveStatus, this::resetUserPassword };
        runMenu(BLUE + "\n--- MANAGE USERS ---" + RESET, userActions, () -> {},
                "Filter and Search Users", "Toggle User Active Status", "Reset User Password");
    }

    private void filterAndSearchUsers() {
        System.out.println(BLUE + "\n--- FILTER AND SEARCH USERS ---" + RESET);
        String keyword = readInput("Enter search keyword: ").toLowerCase();
        if (keyword.isEmpty()) {
            throw new InvalidInputException("Keyword cannot be empty.");
        }
        List<User> filteredUsers = userManager.getAllUsersInSystem().stream()
                .filter(systemUser -> (systemUser.getUsername() != null && systemUser.getUsername().toLowerCase().contains(keyword))
                        || (systemUser.getFirstName() != null && systemUser.getFirstName().toLowerCase().contains(keyword))
                        || (systemUser.getLastName() != null && systemUser.getLastName().toLowerCase().contains(keyword))
                        || (systemUser.getRole() != null && systemUser.getRole().toLowerCase().contains(keyword)))
                .toList();
        if (filteredUsers.isEmpty()) {
            throw new InvalidInputException("No matches found.");
        }
        paginate(filteredUsers.size(), "Results", (start, end) -> {
            System.out.println(CYAN + String.format("%-15s %-15s %-15s %-12s %-8s", "Username", "First Name", "Last Name", "Role", "Status") + RESET);
            System.out.println("----------------------------------------------------------------------");
            for (int i = start; i < end; i++) {
                User userObj = filteredUsers.get(i);
                System.out.printf("%-15s %-15s %-15s %-12s %-8s\n", userObj.getUsername(), userObj.getFirstName(), userObj.getLastName(), userObj.getRole(), userObj.isActive() ? "Active" : "Blocked");
            }
        }, "0. Back to Menu");
    }

    private void verifyUserAccess(User targetUser, User activeAdmin, String action) {
        if ("ADMIN".equals(targetUser.getRole())) {
            boolean canManage = false;
            User currentCreator = targetUser.getCreator();
            while (currentCreator != null) {
                if (currentCreator.getUsername().equalsIgnoreCase(activeAdmin.getUsername())) {
                    canManage = true;
                    break;
                }
                currentCreator = currentCreator.getCreator();
            }
            if (!canManage && !targetUser.getUsername().equalsIgnoreCase(activeAdmin.getUsername())) {
                throw new InvalidInputException("Access Denied: You cannot manage this admin.");
            }
        } else if (ROLE_SUPPORTER.equals(targetUser.getRole())) {
            checkSupporterAccessible((Supporter) targetUser, activeAdmin, action);
        }
    }

    private void toggleUserActiveStatus() {
        System.out.println(BLUE + "\n--- TOGGLE USER STATUS ---" + RESET);
        User targetUser = findAndVerifyUser("Enter target User Email/Username: ", "block");
        userManager.toggleUserStatus(targetUser.getEmail());
        DataManager.saveData(catalog, userManager);
        System.out.println(GREEN + "User status updated to: " + (targetUser.isActive() ? "Active" : "Blocked") + RESET);
    }

    private void resetUserPassword() {
        System.out.println(BLUE + "\n--- RESET USER PASSWORD ---" + RESET);
        User targetUser = findAndVerifyUser("Enter User Email/Username: ", "reset");
        String newPassword = readValidated("Enter New Password: ", Validator::isStrongPassword, "Password is too weak.", false);
        targetUser.setPassword(newPassword);
        DataManager.saveData(catalog, userManager);
        System.out.println(GREEN + "Password reset successfully." + RESET);
    }

    private User findAndVerifyUser(String prompt, String action) {
        User targetUser = userManager.findAnyUser(readInput(prompt));
        if (targetUser == null) {
            throw new InvalidInputException("User not found.");
        }
        verifyUserAccess(targetUser, userManager.getCurrentUser(), action);
        return targetUser;
    }

    private RequestSection chooseSection() {
        System.out.println("Select Section:\n1. BOOKS\n2. TECHNICAL\n3. FINANCIAL\n4. RESERVATION");
        RequestSection section = SECTION_MAP.get(readInput("Choice: "));
        if (section == null) {
            throw new InvalidInputException("Invalid section choice.");
        }
        return section;
    }
}