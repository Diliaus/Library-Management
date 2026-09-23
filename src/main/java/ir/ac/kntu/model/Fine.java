package ir.ac.kntu.model;

import java.time.LocalDateTime;

public class Fine {

    private double amount;
    private boolean paid;
    private LocalDateTime dateIssued;

    public Fine(double amount) {
        setAmount(amount);
        this.paid = false;
        this.dateIssued = LocalDateTime.now();
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Fine amount cannot be negative");
        }
        this.amount = amount;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public void pay() {
        this.paid = true;
    }

    public LocalDateTime getDateIssued() {
        return dateIssued;
    }

    public void setDateIssued(LocalDateTime dateIssued) {
        this.dateIssued = dateIssued;
    }
}