package ir.ac.kntu.exception;
public class UserNotFoundException extends LibrarySystemException {
    public UserNotFoundException(String message) {
        super(message);
    }
}