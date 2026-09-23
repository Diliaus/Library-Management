package ir.ac.kntu.model;

import ir.ac.kntu.exception.InvalidInputException;
import ir.ac.kntu.exception.BorrowLimitExceededException;
import ir.ac.kntu.model.item.LibraryItem;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class BorrowRecord implements Extendable {

    private LibraryItem item;
    private LocalDateTime borrowDate;
    private LocalDateTime dueDate;
    private boolean returned;

    public BorrowRecord(LibraryItem item, int durationDays) {
        if (item == null) {
            throw new InvalidInputException("Library item cannot be null");
        }
        if (durationDays <= 0) {
            throw new InvalidInputException("Duration days must be positive.");
        }
        this.item = item;
        this.borrowDate = LocalDateTime.now();
        this.dueDate = borrowDate.plusDays(durationDays);
        this.returned = false;
    }

    public LibraryItem getItem() {
        return item;
    }

    public void setItem(LibraryItem item) {
        if (item == null) {
            throw new InvalidInputException("Library item cannot be null");
        }
        this.item = item;
    }

    public LocalDateTime getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDateTime borrowDate) {
        if (borrowDate == null) {
            throw new InvalidInputException("Borrow date cannot be null");
        }
        this.borrowDate = borrowDate;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        if (dueDate == null || dueDate.isBefore(borrowDate)) {
            throw new InvalidInputException("Invalid due date");
        }
        this.dueDate = dueDate;
    }

    public boolean isReturned() {
        return returned;
    }

    public void setReturned(boolean returned) {
        this.returned = returned;
    }

    public boolean isOverdue() {
        return !returned && LocalDateTime.now().isAfter(dueDate);
    }

    @Override
    public void extendDuration(int extraDays) {
        if (extraDays <= 0) {
            throw new InvalidInputException("Extension days must be positive.");
        }
        if (!canBeExtended()) {
            if (returned) {
                throw new BorrowLimitExceededException("Cannot extend a resource that has already been returned.");
            }
            if (isOverdue()) {
                throw new BorrowLimitExceededException("Cannot extend duration because the item is already overdue.");
            }
        }
        this.dueDate = this.dueDate.plusDays(extraDays);
    }

    @Override
    public boolean canBeExtended() {
        return !returned && !isOverdue();
    }

    public double calculateFine() {
        double currentFineRate = SystemConfiguration.getInstance().getFineRatePerDay();
        return calculateFine(currentFineRate);
    }

    public double calculateFine(double finePerDay) {
        if (finePerDay < 0) {
            throw new InvalidInputException("Fine per day cannot be negative");
        }
        if (!isOverdue()) {
            return 0.0;
        }
        long days = ChronoUnit.DAYS.between(dueDate, LocalDateTime.now());
        if (days <= 0) {
            return 0.0;
        }
        return days * finePerDay;
    }
}