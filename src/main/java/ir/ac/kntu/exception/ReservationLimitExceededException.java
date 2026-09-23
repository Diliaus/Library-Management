package ir.ac.kntu.exception;
public class ReservationLimitExceededException extends RuntimeException {
    public ReservationLimitExceededException(String message) {
        super(message);
    }
}