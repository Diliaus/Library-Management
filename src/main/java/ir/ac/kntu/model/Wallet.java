package ir.ac.kntu.model;

import ir.ac.kntu.exception.InsufficientBalanceException;
import ir.ac.kntu.exception.InvalidInputException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Wallet {

    private double balance;
    private final List<Transaction> transactions;

    public Wallet() {
        this.balance = 0.0;
        this.transactions = new ArrayList<>();
    }

    public double getBalance() {
        return balance;
    }

    public void charge(double amount) {
        if (amount <= 0) {
            throw new InvalidInputException("Charge amount must be positive.");
        }
        this.balance += amount;
        transactions.add(new Transaction("CHARGE", amount, "Wallet charged"));
    }

    public void deduct(double amount, String description) {
        if (amount <= 0) {
            throw new InvalidInputException("Deduction amount must be positive.");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new InvalidInputException("Description cannot be empty.");
        }
        if (this.balance < amount) {
            throw new InsufficientBalanceException("Insufficient wallet balance.");
        }
        this.balance -= amount;
        transactions.add(new Transaction("PAYMENT", amount, description.trim()));
    }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public List<Transaction> getTransactionsInRecordRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new InvalidInputException("Start and End dates cannot be null.");
        }
        List<Transaction> filtered = new ArrayList<>();
        for (Transaction t : transactions) {
            if (!t.getDate().isBefore(start) && !t.getDate().isAfter(end)) {
                filtered.add(t);
            }
        }
        return filtered;
    }
}