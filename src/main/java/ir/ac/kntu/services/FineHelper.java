package ir.ac.kntu.services;

import ir.ac.kntu.exception.InvalidInputException;
import ir.ac.kntu.exception.UserNotFoundException;
import ir.ac.kntu.model.Fine;
import ir.ac.kntu.model.user.User;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FineHelper {
    private final UserManager userManager;
    private final Map<String, List<Fine>> userFines;

    public FineHelper(UserManager userManager, Map<String, List<Fine>> userFines) {
        this.userManager = userManager;
        this.userFines = userFines;
    }

    public void addFine(String email, Fine fine) {
        if (email == null) {
            throw new UserNotFoundException("User not found for the specified fine");
        }
        User user = userManager.findAnyUser(email);
        if (user == null) {
            throw new UserNotFoundException("User not found for the specified fine");
        }
        if (fine == null) {
            throw new InvalidInputException("Fine cannot be null");
        }
        userFines.computeIfAbsent(user.getEmail().toLowerCase().trim(), k -> new ArrayList<>()).add(fine);
    }

    public List<Fine> getFines(String email) {
        if (email == null) {
            return Collections.emptyList();
        }
        User user = userManager.findAnyUser(email);
        if (user == null) {
            return Collections.emptyList();
        }
        return userFines.getOrDefault(user.getEmail().toLowerCase().trim(), new ArrayList<>());
    }

    public double getUnpaidFines(String email) {
        if (email == null) {
            return 0;
        }
        User user = userManager.findAnyUser(email);
        if (user == null) {
            return 0;
        }
        String emailKey = user.getEmail().toLowerCase().trim();
        double total = 0;
        List<Fine> fines = userFines.get(emailKey);
        if (fines != null) {
            for (Fine f : fines) {
                if (!f.isPaid()) {
                    total += f.getAmount();
                }
            }
        }
        return total;
    }
}