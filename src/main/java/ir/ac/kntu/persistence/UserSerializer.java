package ir.ac.kntu.persistence;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import ir.ac.kntu.model.BorrowRecord;
import ir.ac.kntu.model.Reservation;
import ir.ac.kntu.model.RequestSection;
import ir.ac.kntu.model.SupportTicket;
import ir.ac.kntu.model.Transaction;
import ir.ac.kntu.model.Wallet;
import ir.ac.kntu.model.item.LibraryItem;
import ir.ac.kntu.model.user.RegularUser;
import ir.ac.kntu.services.UserManager;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ir.ac.kntu.persistence.DataConstants.*;

public final class UserSerializer {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private UserSerializer() {
    }

    public static JsonArray saveRegularUsers(UserManager userManager) {
        JsonArray arr = new JsonArray();
        for (RegularUser user : userManager.getRegularUsers().values()) {
            arr.add(regularUserToJson(user));
        }
        return arr;
    }

    private static JsonObject regularUserToJson(RegularUser user) {
        JsonObject obj = new JsonObject();
        obj.addProperty(KEY_MEMBER_ID, user.getMemberId());
        obj.addProperty(KEY_USERNAME, user.getUsername());
        obj.addProperty(KEY_PASSWORD, user.getPassword());
        obj.addProperty(KEY_FIRST_NAME, user.getFirstName());
        obj.addProperty(KEY_LAST_NAME, user.getLastName());
        obj.addProperty(KEY_EMAIL, user.getEmail());
        obj.addProperty(KEY_PHONE_NUMBER, user.getPhoneNumber());
        obj.addProperty(KEY_ACTIVE, user.isActive());
        obj.addProperty(KEY_ROLE, user.getRole());
        obj.addProperty(KEY_WALLET_BALANCE, user.getWallet().getBalance());
        obj.add(KEY_TRANSACTIONS, transactionsToJson(user.getWallet().getTransactions()));
        obj.add(KEY_BORROW_RECORDS, borrowRecordsToJson(user.getBorrowRecords()));
        obj.add(KEY_TICKETS, ticketsToJson(user.getTickets()));
        obj.add(KEY_RESERVATIONS, reservationsToJson(user.getReservations()));
        return obj;
    }

    private static JsonArray transactionsToJson(List<Transaction> txns) {
        JsonArray arr = new JsonArray();
        for (Transaction transaction : txns) {
            JsonObject obj = new JsonObject();
            obj.addProperty(KEY_TYPE, transaction.getType());
            obj.addProperty(KEY_AMOUNT, transaction.getAmount());
            obj.addProperty(KEY_DATE, transaction.getDate().format(DTF));
            obj.addProperty(KEY_DESCRIPTION, transaction.getDescription());
            arr.add(obj);
        }
        return arr;
    }

    private static JsonArray borrowRecordsToJson(List<BorrowRecord> records) {
        JsonArray arr = new JsonArray();
        for (BorrowRecord record : records) {
            JsonObject obj = new JsonObject();
            obj.addProperty(KEY_ITEM_ID, record.getItem().getId());
            obj.addProperty(KEY_BORROW_DATE, record.getBorrowDate().format(DTF));
            obj.addProperty(KEY_DUE_DATE, record.getDueDate().format(DTF));
            obj.addProperty(KEY_RETURNED, record.isReturned());
            arr.add(obj);
        }
        return arr;
    }

    private static JsonArray ticketsToJson(List<SupportTicket> tickets) {
        JsonArray arr = new JsonArray();
        if (tickets == null) {
            return arr;
        }
        for (SupportTicket ticket : tickets) {
            JsonObject obj = new JsonObject();
            obj.addProperty(KEY_SECTION, ticket.getSection().name());
            obj.addProperty(KEY_DESCRIPTION, ticket.getDescription());
            obj.addProperty(KEY_STATUS, ticket.getStatus());
            obj.addProperty(KEY_RESPONSE, ticket.getResponse());
            arr.add(obj);
        }
        return arr;
    }

    private static JsonArray reservationsToJson(List<Reservation> reservations) {
        JsonArray arr = new JsonArray();
        for (Reservation reservation : reservations) {
            JsonObject obj = new JsonObject();
            obj.addProperty(KEY_ITEM_ID, reservation.getItem().getId());
            obj.addProperty(KEY_RESERVATION_DATE, reservation.getReservationDate().format(DTF));
            obj.addProperty(KEY_EXPIRY_DATE, reservation.getExpiryDate().format(DTF));
            obj.addProperty(KEY_STATUS, reservation.getStatus());
            arr.add(obj);
        }
        return arr;
    }

    public static List<RegularUserRef> loadRegularUsers(JsonObject root, Map<String, LibraryItem> itemsMap) {
        List<RegularUserRef> refs = new ArrayList<>();
        if (!root.has(KEY_REGULAR_USERS)) {
            return refs;
        }
        JsonArray arr = root.getAsJsonArray(KEY_REGULAR_USERS);
        for (JsonElement elem : arr) {
            JsonObject obj = elem.getAsJsonObject();
            RegularUserRef ref = jsonToRegularUser(obj, itemsMap);
            if (ref != null) {
                refs.add(ref);
            }
        }
        return refs;
    }

    public static class RegularUserRef {
        private RegularUser user;
        private List<BorrowItemRef> borrowItemIds;
        private List<String> reservationItemIds;

        public RegularUser getUser() {
            return user;
        }

        public List<BorrowItemRef> getBorrowItemIds() {
            return borrowItemIds;
        }

        public List<String> getReservationItemIds() {
            return reservationItemIds;
        }
    }

    public static class BorrowItemRef {
        private String itemId;
        private String borrowDate;
        private String dueDate;
        private boolean returned;

        public String getItemId() {
            return itemId;
        }

        public String getBorrowDate() {
            return borrowDate;
        }

        public String getDueDate() {
            return dueDate;
        }

        public boolean isReturned() {
            return returned;
        }
    }

    private static RegularUserRef jsonToRegularUser(JsonObject obj, Map<String, LibraryItem> itemsMap) {
        try {
            String memberId = obj.get(KEY_MEMBER_ID).getAsString();
            String password = obj.get(KEY_PASSWORD).getAsString();
            String firstName = obj.get(KEY_FIRST_NAME).getAsString();
            String lastName = obj.get(KEY_LAST_NAME).getAsString();
            String email = obj.get(KEY_EMAIL).getAsString();
            String phoneNumber = obj.get(KEY_PHONE_NUMBER).getAsString();
            RegularUser user = new RegularUser(firstName, lastName, password, email, phoneNumber, memberId);
            if (obj.has(KEY_ACTIVE)) {
                user.setActive(obj.get(KEY_ACTIVE).getAsBoolean());
            }
            RegularUserRef ref = new RegularUserRef();
            ref.user = user;
            ref.borrowItemIds = new ArrayList<>();
            ref.reservationItemIds = new ArrayList<>();
            restoreWalletBalance(obj, user);
            restoreTransactions(obj, user);
            restoreBorrowItemRefs(obj, ref);
            restoreTickets(obj, user);
            restoreReservations(obj, ref, itemsMap);
            return ref;
        } catch (JsonSyntaxException e) {
            System.err.println("Failed to load regular user: " + e.getMessage());
            return null;
        }
    }

    private static void restoreWalletBalance(JsonObject obj, RegularUser user) {
        if (obj.has(KEY_WALLET_BALANCE)) {
            double balance = obj.get(KEY_WALLET_BALANCE).getAsDouble();
            if (balance > 0) {
                user.getWallet().charge(balance);
            }
        }
    }

    private static void restoreTransactions(JsonObject obj, RegularUser user) {
        if (obj.has(KEY_TRANSACTIONS)) {
            JsonArray txArr = obj.getAsJsonArray(KEY_TRANSACTIONS);
            List<Transaction> txns = jsonToTransactions(txArr);
            try {
                Field txnsField = Wallet.class.getDeclaredField(KEY_TRANSACTIONS);
                txnsField.setAccessible(true);
                @SuppressWarnings(UNCHECKED)
                List<Transaction> existingTxns = (List<Transaction>) txnsField.get(user.getWallet());
                existingTxns.clear();
                existingTxns.addAll(txns);
            } catch (ReflectiveOperationException e) {
                System.err.println("Failed to restore transactions: " + e.getMessage());
            }
        }
    }

    private static void restoreBorrowItemRefs(JsonObject obj, RegularUserRef ref) {
        if (obj.has(KEY_BORROW_RECORDS)) {
            JsonArray brArr = obj.getAsJsonArray(KEY_BORROW_RECORDS);
            for (JsonElement be : brArr) {
                JsonObject bro = be.getAsJsonObject();
                BorrowItemRef bir = new BorrowItemRef();
                bir.itemId = bro.get(KEY_ITEM_ID).getAsString();
                bir.borrowDate = bro.get(KEY_BORROW_DATE).getAsString();
                bir.dueDate = bro.get(KEY_DUE_DATE).getAsString();
                bir.returned = bro.get(KEY_RETURNED).getAsBoolean();
                ref.borrowItemIds.add(bir);
            }
        }
    }

    private static void restoreTickets(JsonObject obj, RegularUser user) {
        if (obj.has(KEY_TICKETS)) {
            JsonArray tiArr = obj.getAsJsonArray(KEY_TICKETS);
            for (JsonElement te : tiArr) {
                JsonObject to = te.getAsJsonObject();
                RequestSection section = RequestSection.valueOf(to.get(KEY_SECTION).getAsString());
                String description = to.get(KEY_DESCRIPTION).getAsString();
                String status = to.get(KEY_STATUS).getAsString();
                String response = to.has(KEY_RESPONSE) ? to.get(KEY_RESPONSE).getAsString() : "";
                SupportTicket ticket = new SupportTicket(section, description);
                ticket.setStatus(status);
                if (!response.isEmpty()) {
                    ticket.setResponse(response);
                }
                user.addTicket(ticket);
            }
        }
    }

    private static void restoreReservations(JsonObject obj, RegularUserRef ref, Map<String, LibraryItem> itemsMap) {
        if (obj.has(KEY_RESERVATIONS)) {
            RegularUser user = ref.user;
            JsonArray rArr = obj.getAsJsonArray(KEY_RESERVATIONS);
            for (JsonElement re : rArr) {
                JsonObject ro = re.getAsJsonObject();
                ref.reservationItemIds.add(ro.get(KEY_ITEM_ID).getAsString());
                String reservationDateStr = ro.get(KEY_RESERVATION_DATE).getAsString();
                String expiryDateStr = ro.get(KEY_EXPIRY_DATE).getAsString();
                String status = ro.get(KEY_STATUS).getAsString();
                LibraryItem dummyItem = itemsMap != null
                        ? itemsMap.get(ro.get(KEY_ITEM_ID).getAsString()) : null;
                if (dummyItem == null) {
                    continue;
                }
                Reservation res = new Reservation(dummyItem, 1);
                res.setReservationDate(LocalDateTime.parse(reservationDateStr, DTF));
                res.setExpiryDate(LocalDateTime.parse(expiryDateStr, DTF));
                res.setStatus(status);
                user.addReservation(res);
            }
        }
    }

    public static void resolveItemRefs(RegularUser user, List<BorrowItemRef> borrowRefs,
                                        Map<String, LibraryItem> itemsMap) {
        for (BorrowItemRef bir : borrowRefs) {
            LibraryItem item = itemsMap.get(bir.itemId);
            if (item == null) {
                System.err.println("Item not found: " + bir.itemId);
                continue;
            }
            BorrowRecord record = new BorrowRecord(item, 1);
            record.setBorrowDate(LocalDateTime.parse(bir.borrowDate, DTF));
            record.setDueDate(LocalDateTime.parse(bir.dueDate, DTF));
            record.setReturned(bir.returned);
            user.addBorrowRecord(record);
        }
    }

    public static List<Transaction> jsonToTransactions(JsonArray arr) {
        List<Transaction> list = new ArrayList<>();
        for (JsonElement elem : arr) {
            JsonObject obj = elem.getAsJsonObject();
            String type = obj.get(KEY_TYPE).getAsString();
            double amount = obj.get(KEY_AMOUNT).getAsDouble();
            String description = obj.get(KEY_DESCRIPTION).getAsString();
            Transaction transaction = new Transaction(type, amount, description);
            if (obj.has(KEY_DATE)) {
                try {
                    Field dateField = Transaction.class.getDeclaredField(KEY_DATE);
                    dateField.setAccessible(true);
                    dateField.set(transaction, LocalDateTime.parse(obj.get(KEY_DATE).getAsString(), DTF));
                } catch (ReflectiveOperationException e) {
                    System.err.println("Failed to set transaction date: " + e.getMessage());
                }
            }
            list.add(transaction);
        }
        return list;
    }
}
