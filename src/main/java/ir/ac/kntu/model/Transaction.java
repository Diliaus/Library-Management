package ir.ac.kntu.model;

import java.time.LocalDateTime;

public class Transaction {

    private final String type;
    private final double amount;
    private final LocalDateTime date;
    private final String description;

    public Transaction(String type, double amount, String description) {
        this.type = type;
        this.amount = amount;
        this.date = LocalDateTime.now();
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }
}