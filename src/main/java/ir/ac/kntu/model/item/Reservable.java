package ir.ac.kntu.model.item;

public interface Reservable {
    void addToWaitlist(String userId);

    void removeFromWaitlist(String userId);

    boolean isAvailableForReservation();
}