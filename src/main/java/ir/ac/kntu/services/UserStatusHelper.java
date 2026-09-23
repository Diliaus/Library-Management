package ir.ac.kntu.services;

import ir.ac.kntu.exception.InvalidInputException;
import ir.ac.kntu.exception.UserNotFoundException;
import ir.ac.kntu.model.user.Admin;
import ir.ac.kntu.model.user.Supporter;
import ir.ac.kntu.model.user.User;

public class UserStatusHelper {
    private final UserManager userManager;

    public UserStatusHelper(UserManager userManager) {
        this.userManager = userManager;
    }

    public void toggleStatus(String identifier) {
        User user = userManager.findAnyUser(identifier);
        if (user == null) {
            throw new UserNotFoundException("User not found with identifier: " + identifier);
        }
        User currentUser = userManager.getCurrentUser();
        if (currentUser != null) {
            checkToggleAccess(user, currentUser);
        }
        user.setActive(!user.isActive());
    }

    public boolean isActive(String identifier) {
        User user = userManager.findAnyUser(identifier);
        if (user == null) {
            throw new UserNotFoundException("User not found with identifier: " + identifier);
        }
        return user.isActive();
    }

    private void checkToggleAccess(User user, User currentUser) {
        if ("ADMIN".equals(user.getRole())) {
            checkAdminToggleAccess((Admin) user, currentUser);
        } else if ("SUPPORTER".equals(user.getRole())) {
            checkSupporterToggleAccess((Supporter) user, currentUser);
        }
    }

    private void checkAdminToggleAccess(Admin targetAdmin, User currentUser) {
        if (targetAdmin.getUsername().equalsIgnoreCase(currentUser.getUsername())) {
            return;
        }
        boolean canManage = false;
        User currentCreator = targetAdmin.getCreator();
        while (currentCreator != null) {
            if (currentCreator.getUsername().equalsIgnoreCase(currentUser.getUsername())) {
                canManage = true;
                break;
            }
            currentCreator = currentCreator.getCreator();
        }
        if (!canManage) {
            throw new InvalidInputException("Access Denied: You can only block admins created directly or indirectly by you.");
        }
    }

    private void checkSupporterToggleAccess(Supporter supporter, User currentUser) {
        if (supporter.getCreatedByAdminEmail() != null && !supporter.getCreatedByAdminEmail().equalsIgnoreCase(currentUser.getEmail())) {
            throw new InvalidInputException("Access Denied: You can only block supporters created by you.");
        }
    }
}