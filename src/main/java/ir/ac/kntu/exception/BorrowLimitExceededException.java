package ir.ac.kntu.exception;

public class BorrowLimitExceededException extends LibrarySystemException {
    public BorrowLimitExceededException(String message) {
        super(message);
    }
}