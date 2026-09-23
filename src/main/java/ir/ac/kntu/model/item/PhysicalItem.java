package ir.ac.kntu.model.item;

import java.util.LinkedList;
import java.util.Queue;

public abstract class PhysicalItem extends LibraryItem implements Reservable {

    private int availableCopies;
    private final Queue<String> reservationQueue;

    public PhysicalItem(String title, int publishYear, String category, int availableCopies) {
        super(title, publishYear, category);
        setAvailableCopies(availableCopies);
        this.reservationQueue = new LinkedList<>();
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(int availableCopies) {
        if (availableCopies < 0) {
            throw new IllegalArgumentException("Available copies cannot be negative");
        }
        this.availableCopies = availableCopies;
    }

    @Override
    public Queue<String> getReservationQueue() {
        return reservationQueue;
    }

    @Override
    public boolean isAvailable() {
        return availableCopies > 0;
    }

    @Override
    public void addToWaitlist(String userId) {
        if (!reservationQueue.contains(userId)) {
            reservationQueue.add(userId);
        }
    }

    @Override
    public void removeFromWaitlist(String userId) {
        reservationQueue.remove(userId);
    }

    @Override
    public boolean isAvailableForReservation() {
        return !isAvailable();
    }
}