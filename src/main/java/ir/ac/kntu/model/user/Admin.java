package ir.ac.kntu.model.user;

public class Admin extends User {

    public Admin(String username, String password, String firstName, String lastName, String email, String phoneNumber) {
        super(username, password, firstName, lastName, email, phoneNumber);
    }

    public Admin(String username, String password, String firstName, String lastName, String email, String phoneNumber, User creator) {
        super(username, password, firstName, lastName, email, phoneNumber);
        this.setCreator(creator);
    }

    @SuppressWarnings("PMD.UselessOverridingMethod")
    public void setCreator(Admin creator) {
        super.setCreator(creator);
    }

    @Override
    public String getRole() {
        return "ADMIN";
    }

    @Override
    public void displayMenu(ir.ac.kntu.services.Catalog catalog, ir.ac.kntu.services.UserManager userManager, java.util.Scanner scanner) {
        ir.ac.kntu.ui.AdminMenu adminMenu = new ir.ac.kntu.ui.AdminMenu(userManager, catalog, scanner);
        adminMenu.show();
    }
}