package ir.ac.kntu.model.user;

public interface Authenticatable {
    String getUsername();

    String getPassword();

    String getEmail();

    String getRole();

    boolean isActive();

    void setPassword(String password);
}