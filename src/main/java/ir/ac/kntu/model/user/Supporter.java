package ir.ac.kntu.model.user;

import ir.ac.kntu.model.RequestSection;
import java.util.ArrayList;
import java.util.List;

public class Supporter extends User {

    private final List<RequestSection> assignedSections;
    private String createdByAdminEmail;
    private boolean isBlocked = false;

    public Supporter(String username, String password, String firstName, String lastName,
                     String email, String phoneNumber) {
        super(username, password, firstName, lastName, email, phoneNumber);
        this.assignedSections = new ArrayList<>();
    }

    public List<RequestSection> getAssignedSections() {
        return assignedSections;
    }

    public void addSection(RequestSection section) {
        if (section != null && !assignedSections.contains(section)) {
            assignedSections.add(section);
        }
    }

    public void removeSection(RequestSection section) {
        assignedSections.remove(section);
    }

    public boolean hasAccessTo(RequestSection section) {
        return assignedSections.contains(section);
    }

    public String getCreatedByAdminEmail() {
        return createdByAdminEmail;
    }

    public void setCreatedByAdminEmail(String createdByAdminEmail) {
        this.createdByAdminEmail = createdByAdminEmail;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        this.isBlocked = blocked;
    }

    @Override
    public String getRole() {
        return "SUPPORTER";
    }

    @Override
    public void displayMenu(ir.ac.kntu.services.Catalog catalog, ir.ac.kntu.services.UserManager userManager, java.util.Scanner scanner) {
        ir.ac.kntu.ui.SupporterMenu supporterMenu = new ir.ac.kntu.ui.SupporterMenu(catalog, userManager, scanner, this);
        supporterMenu.showSupporterMenu();
    }
}