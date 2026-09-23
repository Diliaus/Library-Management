package ir.ac.kntu.exception;

public class InsufficientBalanceException extends LibrarySystemException {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}