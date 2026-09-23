package ir.ac.kntu.helper;

import ir.ac.kntu.model.user.RegularUser;
import ir.ac.kntu.model.user.Student;
import ir.ac.kntu.model.user.Professor;
import ir.ac.kntu.model.user.Guest;
import ir.ac.kntu.services.UserManager;
import ir.ac.kntu.util.Validator;
import ir.ac.kntu.exception.InvalidInputException;

import java.util.Scanner;
import java.util.function.Predicate;

public final class RegisterHelper {

    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String PROMPT_PASSWORD = "Password: ";

    private RegisterHelper() {
    }

    private static String readValidatedField(String prompt, Predicate<String> validator, String errorMsg, Scanner scanner) {
        System.out.print(prompt);
        String value = scanner.nextLine();
        if (validator.test(value)) {
            return value;
        }
        throw new InvalidInputException(errorMsg);
    }

    private static String selectAccountType(Scanner scanner) {
        System.out.println("\nSelect Account Type:");
        System.out.println("1. Student (STU)");
        System.out.println("2. Professor / Faculty (FAC)");
        System.out.println("3. Guest (GST)");
        System.out.print("Choice: ");
        String typeChoice = scanner.nextLine().trim();
        switch (typeChoice) {
            case "1" -> {
                return "STUDENT";
            }
            case "2" -> {
                return "FACULTY";
            }
            case "3" -> {
                return "GUEST";
            }
            default -> throw new InvalidInputException("Invalid account type choice!");
        }
    }

    public static void handleRegister(UserManager userManager, Scanner scanner) {
        String userType = selectAccountType(scanner);
        String[] fields = readRegistrationFields(scanner);
        RegularUser user = createUserByType(userType, fields);

        if (userManager.registerRegularUser(user)) {
            System.out.println(GREEN + "Account created successfully! Your generated Member ID is: " + user.getMemberId() + RESET);
        } else {
            throw new InvalidInputException("Registration failed: User could not be saved.");
        }
    }

    private static String[] readRegistrationFields(Scanner scanner) {
        Predicate<String> notEmpty = s -> s != null && !s.trim().isEmpty();
        String firstName = readValidatedField("First Name: ", notEmpty, "First name cannot be empty.", scanner);
        String lastName = readValidatedField("Last Name: ", notEmpty, "Last name cannot be empty.", scanner);
        String password = readValidatedField(PROMPT_PASSWORD, Validator::isStrongPassword, "Weak password.", scanner);
        String email = readValidatedField("Email: ", Validator::isValidEmail, "Invalid email format.", scanner);
        String phone = readValidatedField("Phone: ", Validator::isValidPhone, "Invalid phone format.", scanner);

        return new String[]{password, firstName, lastName, email, phone};
    }

    private static RegularUser createUserByType(String userType, String[] fields) {
        if ("STUDENT".equals(userType)) {
            return new Student(fields[0], fields[1], fields[2], fields[3], fields[4]);
        }
        if ("FACULTY".equals(userType)) {
            return new Professor(fields[0], fields[1], fields[2], fields[3], fields[4]);
        }
        return new Guest(fields[0], fields[1], fields[2], fields[3], fields[4]);
    }
}