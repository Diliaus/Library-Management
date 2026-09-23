package ir.ac.kntu.model.user;

import ir.ac.kntu.util.Validator;
import ir.ac.kntu.exception.InvalidInputException;

public abstract class User implements Authenticatable {

    private final String username;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private boolean active;
    private User creator;

    public User(String username, String password, String firstName, String lastName, String email, String phoneNumber) {
        if (username == null || username.trim().isEmpty()) {
            throw new InvalidInputException("Username cannot be empty");
        }
        this.username = username;
        setPassword(password);
        setFirstName(firstName);
        setLastName(lastName);
        setEmail(email);
        setPhoneNumber(phoneNumber);
        this.active = true;
        this.creator = null;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        if (!Validator.isStrongPassword(password)) {
            throw new InvalidInputException("Password is too weak");
        }
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new InvalidInputException("First name cannot be empty");
        }
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new InvalidInputException("Last name cannot be empty");
        }
        this.lastName = lastName;
    }

    @Override
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (!Validator.isValidEmail(email)) {
            throw new InvalidInputException("Invalid email format");
        }
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        if (!Validator.isValidPhone(phoneNumber)) {
            throw new InvalidInputException("Invalid phone number format");
        }
        this.phoneNumber = phoneNumber;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    @Override
    public abstract String getRole();

    public abstract void displayMenu(ir.ac.kntu.services.Catalog catalog, ir.ac.kntu.services.UserManager userManager, java.util.Scanner scanner);
}