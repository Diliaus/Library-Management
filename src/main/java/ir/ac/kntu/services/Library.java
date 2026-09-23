package ir.ac.kntu.services;

public class Library {

    private final Catalog catalog;
    private final UserManager userManager;

    public Library() {
        this.catalog = new Catalog();
        this.userManager = new UserManager();
    }

    public Catalog getCatalog() {
        return catalog;
    }

    public UserManager getUserManager() {
        return userManager;
    }
}