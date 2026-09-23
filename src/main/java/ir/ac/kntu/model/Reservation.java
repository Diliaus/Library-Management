package ir.ac.kntu.model;

import ir.ac.kntu.model.item.LibraryItem;
import ir.ac.kntu.exception.InvalidInputException;
import java.time.LocalDateTime;

public class Reservation {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    private LibraryItem item;
    private LocalDateTime reservationDate;
    private LocalDateTime expiryDate;
    private String status;

    public Reservation(LibraryItem item, int expiryDays) {
        if (item == null) {
            throw new InvalidInputException("Library item cannot be null");
        }
        this.item = item;
        this.reservationDate = LocalDateTime.now();
        this.expiryDate = reservationDate.plusDays(expiryDays);
        this.status = STATUS_PENDING;
    }

    public LibraryItem getItem() {
        return item;
    }

    public void setItem(LibraryItem item) {
        this.item = item;
    }

    public LocalDateTime getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(LocalDateTime reservationDate) {
        this.reservationDate = reservationDate;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getStatus() {
        return status;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate) && STATUS_ACTIVE.equals(status);
    }

    public void setStatus(String status) {
        if (status == null) {
            throw new InvalidInputException("Status cannot be null");
        }
        String upperStatus = status.toUpperCase().trim();
        if (!upperStatus.equals(STATUS_PENDING) &&
                !upperStatus.equals(STATUS_ACTIVE) &&
                !upperStatus.equals(STATUS_COMPLETED) &&
                !upperStatus.equals(STATUS_CANCELLED)) {
            throw new InvalidInputException("Invalid reservation status");
        }
        this.status = upperStatus;
    }
}