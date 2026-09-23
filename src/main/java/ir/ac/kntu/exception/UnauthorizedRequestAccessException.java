package ir.ac.kntu.exception;

public class UnauthorizedRequestAccessException extends RuntimeException {
    public UnauthorizedRequestAccessException(String message) {
        super(message);
    }
}
