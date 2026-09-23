package ir.ac.kntu.model.user;

import ir.ac.kntu.model.BorrowRecord;
import ir.ac.kntu.model.Reservation;
import ir.ac.kntu.model.SupportTicket;
import ir.ac.kntu.model.Wallet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RegularUser extends User {

    private static final int LIMIT_GUEST = 2;
    private static final int LIMIT_STUDENT = 10;
    private static final int LIMIT_FACULTY = 15;

    private static final String ROLE_GUEST = "GUEST";
    private static final String ROLE_STUDENT = "STUDENT";
    private static final String ROLE_FACULTY = "FACULTY";

    private static final String PREFIX_STUDENT = "STU";
    private static final String PREFIX_FACULTY = "FAC";

    private final String memberId;
    private final Wallet wallet;
    private final List<BorrowRecord> borrowRecords;
    private final List<SupportTicket> tickets;
    private final List<Reservation> reservations;

    public RegularUser(String firstName, String lastName, String password,
                       String email, String phone, String memberId) {
        super(email, password, firstName, lastName, email, phone);

        this.wallet = new Wallet();
        this.borrowRecords = new ArrayList<>();
        this.tickets = new ArrayList<>();
        this.reservations = new ArrayList<>();
        this.memberId = memberId;
    }

    public String getId() {
        return memberId;
    }

    public String getMemberId() {
        return memberId;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public List<BorrowRecord> getBorrowRecords() {
        return Collections.unmodifiableList(borrowRecords);
    }

    public void addBorrowRecord(BorrowRecord record) {
        if (record != null) {
            borrowRecords.add(record);
        }
    }

    public List<SupportTicket> getTickets() {
        return Collections.unmodifiableList(tickets);
    }

    public void addTicket(SupportTicket ticket) {
        if (ticket != null) {
            tickets.add(ticket);
        }
    }

    public List<Reservation> getReservations() {
        return Collections.unmodifiableList(reservations);
    }

    public void addReservation(Reservation reservation) {
        if (reservation != null) {
            reservations.add(reservation);
        }
    }

    public int getActiveBorrowedCount() {
        int count = 0;
        for (BorrowRecord record : borrowRecords) {
            if (!record.isReturned()) {
                count++;
            }
        }
        return count;
    }

    public boolean hasOverdueItems() {
        for (BorrowRecord record : borrowRecords) {
            if (record.isOverdue()) {
                return true;
            }
        }
        return false;
    }

    public double getTotalUnpaidFines() {
        double total = 0;
        for (BorrowRecord record : borrowRecords) {
            total += record.calculateFine();
        }
        return total;
    }

    public Reservation getPendingReservationForItem(String itemId) {
        for (Reservation res : reservations) {
            if (res.getItem().getId().equalsIgnoreCase(itemId) && Reservation.STATUS_PENDING.equals(res.getStatus())) {
                return res;
            }
        }
        return null;
    }

    public int getMaxBorrowedItems() {
        if (memberId == null) {
            return LIMIT_GUEST;
        }
        if (memberId.startsWith(PREFIX_STUDENT)) {
            return LIMIT_STUDENT;
        }
        if (memberId.startsWith(PREFIX_FACULTY)) {
            return LIMIT_FACULTY;
        }
        return LIMIT_GUEST;
    }

    @Override
    public String getRole() {
        if (memberId == null) {
            return ROLE_GUEST;
        }
        if (memberId.startsWith(PREFIX_STUDENT)) {
            return ROLE_STUDENT;
        }
        if (memberId.startsWith(PREFIX_FACULTY)) {
            return ROLE_FACULTY;
        }
        return ROLE_GUEST;
    }

    @Override
    public void displayMenu(ir.ac.kntu.services.Catalog catalog, ir.ac.kntu.services.UserManager userManager, java.util.Scanner scanner) {
        new ir.ac.kntu.ui.RegularUserMenu(catalog, userManager, scanner).show();
    }
}