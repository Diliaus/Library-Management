package ir.ac.kntu.services;

import ir.ac.kntu.model.BorrowRecord;
import ir.ac.kntu.model.Fine;
import ir.ac.kntu.model.SupportRequest;
import ir.ac.kntu.model.SystemConfiguration;
import ir.ac.kntu.model.item.LibraryItem;
import ir.ac.kntu.model.user.RegularUser;
import ir.ac.kntu.model.user.Supporter;
import ir.ac.kntu.model.user.User;
import ir.ac.kntu.model.user.Admin;
import ir.ac.kntu.exception.InvalidInputException;
import ir.ac.kntu.exception.DuplicateUsernameException;
import ir.ac.kntu.exception.UserNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ir.ac.kntu.exception.LibrarySystemException;

@SuppressWarnings("PMD.CyclomaticComplexity")
public class UserManager {
    private final Map<String, RegularUser> regularUsers;
    private final Map<String, Supporter> supporters;
    private final Map<String, Admin> admins;
    private final Map<String, List<Fine>> userFines;
    private final List<SupportRequest> supportRequests;
    private User currentUser;

    private final FineHelper fineHelper;
    private final SupportRequestHelper supportRequestHelper;
    private final UserStatusHelper userStatusHelper;

    private static final String MSG_CREDENTIALS_NULL = "Credentials cannot be null";
    private static final String MSG_ACCOUNT_BLOCKED = "Account is blocked. Access denied.";
    private static final String MSG_INCORRECT_PASSWORD = "Incorrect password";
    private static final String MSG_USER_NOT_FOUND = "User not found.";

    public UserManager() {
        this.regularUsers = new HashMap<>();
        this.supporters = new HashMap<>();
        this.admins = new HashMap<>();
        this.userFines = new HashMap<>();
        this.supportRequests = new ArrayList<>();
        this.currentUser = null;

        this.fineHelper = new FineHelper(this, this.userFines);
        this.supportRequestHelper = new SupportRequestHelper(this.supportRequests);
        this.userStatusHelper = new UserStatusHelper(this);

        initializeDefaultUsers();
    }

    private void initializeDefaultUsers() {
        Admin mainAdmin = new Admin(
                "admin",
                "Admin@123",
                "System",
                "Administrator",
                "admin@kntu.ac.ir",
                "09123456789"
        );
        admins.put(mainAdmin.getUsername().toLowerCase().trim(), mainAdmin);

        String supporterKey = "supporter1";
        if (!supporters.containsKey(supporterKey)) {
            Supporter defaultSupporter = new Supporter(
                    supporterKey,
                    "Support@123",
                    "Support",
                    "One",
                    "supporter1@kntu.ac.ir",
                    "09123456788"
            );
            defaultSupporter.setCreatedByAdminEmail("admin@kntu.ac.ir");
            supporters.put(supporterKey, defaultSupporter);
        }
    }

    private void requireNonNullFields(User user) {
        if (user == null || user.getUsername() == null || user.getFirstName() == null || user.getLastName() == null) {
            throw new InvalidInputException("User details cannot be empty or null");
        }
    }

    private boolean isUsernameOrEmailDuplicate(String username, String email) {
        String uKey = username.toLowerCase().trim();
        String eKey = email.toLowerCase().trim();
        return regularUsers.containsKey(eKey) || regularUsers.containsKey(uKey) ||
                supporters.containsKey(eKey) || supporters.containsKey(uKey) ||
                admins.containsKey(eKey) || admins.containsKey(uKey);
    }

    private void checkDuplicate(String username, String email, String type) {
        // اگر موقع لود شدن دیتابیس نام کاربری supporter1 بود، اجازه جایگزینی اطلاعات کامل را بدهد
        if ("supporter1".equalsIgnoreCase(username)) {
            return;
        }
        if (isUsernameOrEmailDuplicate(username, email)) {
            throw new DuplicateUsernameException(type + " with this username or email already exists");
        }
    }

    public boolean registerRegularUser(RegularUser user) {
        requireNonNullFields(user);
        checkDuplicate(user.getUsername(), user.getEmail(), "User");
        String emailKey = user.getEmail().toLowerCase().trim();
        regularUsers.put(emailKey, user);
        userFines.put(emailKey, new ArrayList<>());
        return true;
    }

    public boolean registerSupporter(Supporter supporter) {
        requireNonNullFields(supporter);
        checkDuplicate(supporter.getUsername(), supporter.getEmail(), "Supporter");
        if (supporter.getCreator() == null) {
            supporter.setCreator(currentUser);
        }
        if (currentUser != null && supporter.getCreatedByAdminEmail() == null) {
            supporter.setCreatedByAdminEmail(currentUser.getEmail());
        }
        supporters.put(supporter.getUsername().toLowerCase().trim(), supporter);
        return true;
    }

    public boolean registerAdmin(Admin admin) {
        requireNonNullFields(admin);
        checkDuplicate(admin.getUsername(), admin.getEmail(), "Admin");
        if (admin.getCreator() == null) {
            admin.setCreator(currentUser);
        }
        admins.put(admin.getUsername().toLowerCase().trim(), admin);
        return true;
    }

    public void removeSupporter(String username) {
        if (username == null) {
            throw new InvalidInputException("Username cannot be null");
        }
        String key = username.toLowerCase().trim();
        if (!supporters.containsKey(key)) {
            for (Supporter sup : supporters.values()) {
                if (sup.getUsername().equalsIgnoreCase(username)) {
                    supporters.remove(sup.getUsername().toLowerCase().trim());
                    return;
                }
            }
            throw new UserNotFoundException("Supporter not found to remove.");
        }
        supporters.remove(key);
    }

    private User authenticateUser(String identifier, String password, String expectedRole) {
        if (identifier == null || password == null) {
            throw new InvalidInputException(MSG_CREDENTIALS_NULL);
        }
        User user = findAnyUser(identifier.toLowerCase().trim());
        if (user == null) {
            throw new UserNotFoundException(buildNotFoundMsg(expectedRole));
        }
        if (expectedRole != null && !expectedRole.equalsIgnoreCase(user.getRole())) {
            throw new InvalidInputException("Access denied: Unauthorized role");
        }
        checkUserActive(user);
        checkUserPassword(user, password);
        return user;
    }

    private String buildNotFoundMsg(String expectedRole) {
        if (expectedRole == null) {
            return "No user found with the provided username or email";
        }
        return "No " + expectedRole.toLowerCase() + " found with the provided username or email";
    }

    private void checkUserActive(User user) {
        if (!user.isActive()) {
            throw new InvalidInputException(MSG_ACCOUNT_BLOCKED);
        }
    }

    private void checkUserPassword(User user, String password) {
        if (user.getPassword() == null || !user.getPassword().equals(password)) {
            throw new InvalidInputException(MSG_INCORRECT_PASSWORD);
        }
    }

    public boolean loginUser(String identifier, String password) {
        currentUser = authenticateUser(identifier, password, null);
        return true;
    }

    public boolean loginAdmin(String identifier, String password) {
        currentUser = authenticateUser(identifier, password, "ADMIN");
        return true;
    }

    public boolean loginSupporter(String identifier, String password) {
        currentUser = authenticateUser(identifier, password, "SUPPORTER");
        return true;
    }

    public void logout() {
        currentUser = null;
    }

    public User findAnyUser(String identifier) {
        if (identifier == null) {
            return null;
        }
        String key = identifier.toLowerCase().trim();
        User user = admins.get(key);
        if (user != null) {
            return user;
        }
        user = supporters.get(key);
        if (user != null) {
            return user;
        }
        user = regularUsers.get(key);
        if (user != null) {
            return user;
        }
        user = findUserByMatch(admins.values(), identifier);
        if (user != null) {
            return user;
        }
        user = findUserByMatch(supporters.values(), identifier);
        if (user != null) {
            return user;
        }
        return findRegularUserByMatch(regularUsers.values(), identifier);
    }

    private User findUserByMatch(Iterable<? extends User> users, String identifier) {
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(identifier) || u.getEmail().equalsIgnoreCase(identifier)) {
                return u;
            }
        }
        return null;
    }

    private User findRegularUserByMatch(Iterable<? extends RegularUser> users, String identifier) {
        for (RegularUser r : users) {
            if (r.getUsername().equalsIgnoreCase(identifier) || r.getMemberId().equalsIgnoreCase(identifier) || r.getEmail().equalsIgnoreCase(identifier)) {
                return r;
            }
        }
        return null;
    }

    public void throwUserNotFound(String identifier) {
        throw new UserNotFoundException("User not found with identifier: " + identifier);
    }

    public User getRegularUser(String identifier) {
        User user = findAnyUser(identifier);
        if (user == null) {
            throwUserNotFound(identifier);
        }
        return user;
    }

    public Supporter getSupporter(String username) {
        if (username == null) {
            throw new InvalidInputException("Username cannot be null");
        }
        Supporter supporter = supporters.get(username.toLowerCase().trim());
        if (supporter == null) {
            for (Supporter sup : supporters.values()) {
                if (sup.getUsername().equalsIgnoreCase(username) || sup.getEmail().equalsIgnoreCase(username)) {
                    return sup;
                }
            }
            throw new UserNotFoundException("Supporter not found with username: " + username);
        }
        return supporter;
    }

    public Supporter findSupporterByUsername(String username) {
        try {
            return getSupporter(username);
        } catch (LibrarySystemException e) {
            return null;
        }
    }

    public List<User> getAllUsersInSystem() {
        List<User> all = new ArrayList<>();
        all.addAll(admins.values());
        all.addAll(supporters.values());
        all.addAll(regularUsers.values());
        return all;
    }

    public void addFineToUser(String email, Fine fine) {
        fineHelper.addFine(email, fine);
    }

    public List<Fine> getUserFines(String email) {
        return fineHelper.getFines(email);
    }

    public double getTotalUnpaidFines(String email) {
        return fineHelper.getUnpaidFines(email);
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public Map<String, RegularUser> getRegularUsers() {
        return Collections.unmodifiableMap(regularUsers);
    }

    public Map<String, List<Fine>> getUserFinesByEmail() {
        return userFines;
    }

    public void addSupportRequest(SupportRequest request) {
        supportRequestHelper.addRequest(request);
    }

    public List<SupportRequest> getSupportRequests() {
        return supportRequestHelper.getRequests();
    }

    public void assignItemToNextInQueue(String userEmail, LibraryItem item) {
        if (userEmail == null || item == null) {
            throw new InvalidInputException("Email or Item cannot be null");
        }
        User user = findAnyUser(userEmail);
        if (user instanceof RegularUser nextUser) {
            int maxBorrowDays = SystemConfiguration.getInstance().getMaxBorrowDays();
            nextUser.addBorrowRecord(new BorrowRecord(item, maxBorrowDays));
            return;
        }
        throw new UserNotFoundException("Next user in queue not found");
    }

    public void toggleUserStatus(String identifier) {
        userStatusHelper.toggleStatus(identifier);
    }

    public boolean isUserActive(String identifier) {
        return userStatusHelper.isActive(identifier);
    }

    public List<SupportRequest> getSupportRequestsBySection(ir.ac.kntu.model.RequestSection section) {
        return supportRequestHelper.getRequestsBySection(section);
    }

    public void updateUserProfile(String oldIdentifier, String newFirstName, String newLastName, String newPhone) {
        User user = findAnyUser(oldIdentifier);
        if (user == null) {
            throw new UserNotFoundException(MSG_USER_NOT_FOUND);
        }
        user.setFirstName(newFirstName);
        user.setLastName(newLastName);
        user.setPhoneNumber(newPhone);
    }

    public void updateUserEmail(String oldIdentifier, String newEmail) {
        User user = findAnyUser(oldIdentifier);
        if (user == null) {
            throw new UserNotFoundException(MSG_USER_NOT_FOUND);
        }
        String currentEmail = user.getEmail().toLowerCase().trim();
        String targetEmail = newEmail.toLowerCase().trim();
        if (!currentEmail.equalsIgnoreCase(targetEmail) && isEmailTaken(targetEmail)) {
            throw new DuplicateUsernameException("The new email already exists in the system.");
        }
        if (user instanceof RegularUser regularUser) {
            updateRegularUserEmail(regularUser, currentEmail, targetEmail, newEmail);
        } else if (user instanceof Supporter supporter) {
            updateSupporterEmail(supporter, currentEmail, targetEmail, newEmail);
        }
    }

    public void updateUserPassword(String oldIdentifier, String newPassword) {
        User user = findAnyUser(oldIdentifier);
        if (user == null) {
            throw new UserNotFoundException(MSG_USER_NOT_FOUND);
        }
        user.setPassword(newPassword);
    }

    private boolean isEmailTaken(String email) {
        return regularUsers.containsKey(email) || supporters.containsKey(email) || admins.containsKey(email);
    }

    private void updateRegularUserEmail(RegularUser regularUser, String currentEmail, String targetEmail, String newEmail) {
        if (!currentEmail.equalsIgnoreCase(targetEmail)) {
            regularUsers.remove(currentEmail);
            regularUser.setEmail(newEmail);
            regularUsers.put(targetEmail, regularUser);
            if (userFines.containsKey(currentEmail)) {
                userFines.put(targetEmail, userFines.remove(currentEmail));
            }
        }
    }

    private void updateSupporterEmail(Supporter supporter, String currentEmail, String targetEmail, String newEmail) {
        supporter.setEmail(newEmail);
        if (!currentEmail.equalsIgnoreCase(targetEmail) && userFines.containsKey(currentEmail)) {
            userFines.put(targetEmail, userFines.remove(currentEmail));
        }
    }

    public void changeSupporterUsername(String oldUsername, String newUsername) {
        String oldKey = oldUsername.toLowerCase().trim();
        String newKey = newUsername.toLowerCase().trim();
        if (!supporters.containsKey(oldKey)) {
            throw new UserNotFoundException("Supporter not found.");
        }
        if (isUsernameOrEmailDuplicate(newUsername, "dummy_check_" + newKey + "@kntu.ac.ir")) {
            throw new DuplicateUsernameException("The new username already exists in the system.");
        }
        Supporter supporter = supporters.remove(oldKey);
        try {
            java.lang.reflect.Field usernameField = User.class.getDeclaredField("username");
            usernameField.setAccessible(true);
            usernameField.set(supporter, newUsername);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            supporters.put(oldKey, supporter);
            throw new InvalidInputException("Failed to modify username field safely.");
        }
        supporters.put(newKey, supporter);
    }
}