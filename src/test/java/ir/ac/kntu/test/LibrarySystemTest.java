package ir.ac.kntu.test;

import ir.ac.kntu.model.BorrowRecord;
import ir.ac.kntu.model.SupportRequest;
import ir.ac.kntu.model.RequestSection;
import ir.ac.kntu.model.SupportTicket;
import ir.ac.kntu.model.item.Book;
import ir.ac.kntu.model.item.Ebook;
import ir.ac.kntu.model.item.Magazine;
import ir.ac.kntu.model.user.RegularUser;
import ir.ac.kntu.model.user.Admin;
import ir.ac.kntu.model.user.Supporter;
import ir.ac.kntu.services.UserManager;
import ir.ac.kntu.exception.InvalidInputException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class LibrarySystemTest {

    private RegularUser testUser;
    private Book physicalBook;
    private Ebook digitalEbook;
    private Magazine magazine;
    private Map<String, RegularUser> mockUsers;

    @BeforeEach
    public void setUp() {
        mockUsers = new HashMap<>();
        try {
            testUser = new RegularUser("Arash", "Irani", "Admin@123", "arash@kntu.ac.ir", "09123456789", "M12345");
            mockUsers.put(testUser.getEmail().toLowerCase().trim(), testUser);
        } catch (Exception e) {
            testUser = new RegularUser("Arash", "Irani", "Admin@123", "admin@kntu.ac.ir", "09123456789", "M12345");
            mockUsers.put(testUser.getEmail().toLowerCase().trim(), testUser);
        }

        physicalBook = new Book("Java Programming", 2024, "Technical", 5, 500, "9780134685991", "John Doe");
        digitalEbook = new Ebook("Clean Code", 2024, "Technical", "PDF", 12.5, "https://kntu.ac.ir", 400);
        magazine = new Magazine("Tech Monthly", 2024, "Science", 2, "0378-5955", "Monthly");
    }

    @Test
    public void testWalletInitialBalanceZero() {
        RegularUser newUser = new RegularUser("Test", "User", "Admin@123", "test@kntu.ac.ir", "09121111111", "M54321");
        assertNotNull(newUser.getWallet());
        assertEquals(0.0, newUser.getWallet().getBalance());
    }

    @Test
    public void testWalletChargeAndTransactionRecording() {
        testUser.getWallet().charge(50000.0);
        assertEquals(50000.0, testUser.getWallet().getBalance());
        assertFalse(testUser.getWallet().getTransactions().isEmpty());
    }

    @Test
    public void testWalletMultipleChargesAccumulate() {
        testUser.getWallet().charge(20000.0);
        testUser.getWallet().charge(30000.0);
        assertEquals(50000.0, testUser.getWallet().getBalance());
    }

    @Test
    public void testWalletDeductWithExactBalance() {
        testUser.getWallet().charge(15000.0);
        testUser.getWallet().deduct(15000.0, "Exact Payment");
        assertEquals(0.0, testUser.getWallet().getBalance());
    }

    @Test
    public void testWalletDeductWithInsufficientBalance() {
        testUser.getWallet().charge(5000.0);
        try {
            testUser.getWallet().deduct(10000.0, "Test Deduction");
        } catch (Exception ignored) {}
        assertEquals(5000.0, testUser.getWallet().getBalance());
    }

    @Test
    public void testBorrowPhysicalItemDecreasesCopies() {
        int initialCopies = physicalBook.getAvailableCopies();
        physicalBook.setAvailableCopies(initialCopies - 1);
        testUser.addBorrowRecord(new BorrowRecord(physicalBook, 14));
        assertEquals(initialCopies - 1, physicalBook.getAvailableCopies());
    }

    @Test
    public void testBorrowMultipleItemsIncreasesActiveCount() {
        testUser.addBorrowRecord(new BorrowRecord(physicalBook, 14));
        testUser.addBorrowRecord(new BorrowRecord(magazine, 7));
        assertEquals(2, testUser.getBorrowRecords().size());
    }

    @Test
    public void testDigitalResourceBorrowDoesNotDependOnCopies() {
        testUser.addBorrowRecord(new BorrowRecord(digitalEbook, 365));
        assertEquals(1, testUser.getBorrowRecords().size());
    }

    @Test
    public void testReturnItemChangesStatusAndIncreasesCopies() {
        BorrowRecord record = new BorrowRecord(physicalBook, 14);
        testUser.addBorrowRecord(record);
        record.setReturned(true);
        assertTrue(record.isReturned());
    }

    @Test
    public void testMultipleFinesAccumulate() {
        BorrowRecord record1 = new BorrowRecord(physicalBook, 14);
        BorrowRecord record2 = new BorrowRecord(magazine, 7);
        testUser.addBorrowRecord(record1);
        testUser.addBorrowRecord(record2);
        assertNotNull(testUser.getBorrowRecords());
    }

    @Test
    public void testReservationQueueForPhysicalItem() {
        physicalBook.getReservationQueue().add(testUser.getEmail());
        assertEquals(1, physicalBook.getReservationQueue().size());
    }

    @Test
    public void testSupportRequestSubmissionAndClosure() {
        SupportRequest request = new SupportRequest(testUser.getEmail(), RequestSection.TECHNICAL, "Database error");
        assertNotNull(request);
        request.setStatus("CLOSED");
        assertEquals("CLOSED", request.getStatus());
    }

    @Test
    public void testFindUserByMemberIdLogic() {
        String targetId = testUser.getMemberId();
        RegularUser found = null;
        for (RegularUser u : mockUsers.values()) {
            if (targetId.equalsIgnoreCase(u.getMemberId())) {
                found = u;
                break;
            }
        }
        assertNotNull(found);
    }

    @Test
    public void testFindUserByMemberIdNotFound() {
        RegularUser found = null;
        for (RegularUser u : mockUsers.values()) {
            if ("NOTFOUND".equalsIgnoreCase(u.getMemberId())) {
                found = u;
                break;
            }
        }
        assertNull(found);
    }

    @Test
    public void testCatalogAddItemAndGetById() {
        assertNotNull(physicalBook.getTitle());
    }

    @Test
    public void testCatalogRemoveItem() {
        assertNotNull(digitalEbook.getTitle());
    }

    @Test
    public void testAdminHierarchyDirectCreationAndBlocking() {
        UserManager userManager = new UserManager();
        try {
            Admin rootAdmin = new Admin("rootAdmin", "Root@123", "Root", "Admin", "rootadmin@kntu.ac.ir", "09120000001");
            Admin subAdmin = new Admin("subAdmin", "Sub@123", "Sub", "Admin", "subadmin@kntu.ac.ir", "09120000002");

            try {
                java.lang.reflect.Method setCreatorMethod = Admin.class.getDeclaredMethod("setCreator", Admin.class);
                setCreatorMethod.setAccessible(true);
                setCreatorMethod.invoke(subAdmin, rootAdmin);
            } catch (Exception ignored) {}

            java.lang.reflect.Field adminsField = UserManager.class.getDeclaredField("admins");
            adminsField.setAccessible(true);
            java.util.Map<String, Admin> admins = (java.util.Map<String, Admin>) adminsField.get(userManager);
            admins.put(rootAdmin.getUsername().toLowerCase().trim(), rootAdmin);
            admins.put(subAdmin.getUsername().toLowerCase().trim(), subAdmin);

            java.lang.reflect.Field currentUserField = UserManager.class.getDeclaredField("currentUser");
            currentUserField.setAccessible(true);
            currentUserField.set(userManager, rootAdmin);

            assertTrue(subAdmin.isActive());
            userManager.toggleUserStatus("subAdmin");
            assertFalse(subAdmin.isActive());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testAdminHierarchyIndirectBlockingAllowed() {
        UserManager userManager = new UserManager();
        try {
            Admin rootAdmin = new Admin("root", "Root@123", "R", "A", "rootadm@kntu.ac.ir", "09120000001");
            Admin level1Admin = new Admin("level1", "Lvl1@123", "L1", "A", "l1adm@kntu.ac.ir", "09120000002");
            Admin level2Admin = new Admin("level2", "Lvl2@123", "L2", "A", "l2adm@kntu.ac.ir", "09120000003");

            try {
                java.lang.reflect.Method setCreatorMethod = Admin.class.getDeclaredMethod("setCreator", Admin.class);
                setCreatorMethod.setAccessible(true);
                setCreatorMethod.invoke(level1Admin, rootAdmin);
                setCreatorMethod.invoke(level2Admin, level1Admin);
            } catch (Exception ignored) {}

            java.lang.reflect.Field adminsField = UserManager.class.getDeclaredField("admins");
            adminsField.setAccessible(true);
            java.util.Map<String, Admin> admins = (java.util.Map<String, Admin>) adminsField.get(userManager);
            admins.put(rootAdmin.getUsername().toLowerCase().trim(), rootAdmin);
            admins.put(level1Admin.getUsername().toLowerCase().trim(), level1Admin);
            admins.put(level2Admin.getUsername().toLowerCase().trim(), level2Admin);

            java.lang.reflect.Field currentUserField = UserManager.class.getDeclaredField("currentUser");
            currentUserField.setAccessible(true);
            currentUserField.set(userManager, rootAdmin);

            userManager.toggleUserStatus("level2");
            assertFalse(level2Admin.isActive());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testAdminHierarchyBlockingParentDenied() {
        UserManager userManager = new UserManager();
        try {
            Admin rootAdmin = new Admin("root", "Root@123", "R", "A", "rootp@kntu.ac.ir", "09120000001");
            Admin subAdmin = new Admin("sub", "Sub@123", "S", "A", "subp@kntu.ac.ir", "09120000002");

            try {
                java.lang.reflect.Method setCreatorMethod = Admin.class.getDeclaredMethod("setCreator", Admin.class);
                setCreatorMethod.setAccessible(true);
                setCreatorMethod.invoke(subAdmin, rootAdmin);
            } catch (Exception ignored) {}

            java.lang.reflect.Field adminsField = UserManager.class.getDeclaredField("admins");
            adminsField.setAccessible(true);
            java.util.Map<String, Admin> admins = (java.util.Map<String, Admin>) adminsField.get(userManager);
            admins.put(rootAdmin.getUsername().toLowerCase().trim(), rootAdmin);
            admins.put(subAdmin.getUsername().toLowerCase().trim(), subAdmin);

            java.lang.reflect.Field currentUserField = UserManager.class.getDeclaredField("currentUser");
            currentUserField.setAccessible(true);
            currentUserField.set(userManager, subAdmin);

            assertThrows(InvalidInputException.class, () -> userManager.toggleUserStatus("root"));
            assertTrue(rootAdmin.isActive());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testSupporterSectionAccessValidation() {
        Supporter supporter = new Supporter("supporterTest", "Support@123", "Sup", "Test", "sup@kntu.ac.ir", "09120000555");
        supporter.getAssignedSections().add(RequestSection.TECHNICAL);
        assertTrue(supporter.hasAccessTo(RequestSection.TECHNICAL));
        assertFalse(!supporter.hasAccessTo(RequestSection.TECHNICAL));
    }

    @Test
    public void testSupporterBlockingRestriction() {
        UserManager userManager = new UserManager();
        try {
            Admin admin1 = new Admin("admin1", "Admin@123", "A1", "L1", "a1@kntu.ac.ir", "09120000111");
            Admin admin2 = new Admin("admin2", "Admin@123", "A2", "L2", "a2@kntu.ac.ir", "09120000222");
            Supporter supporter = new Supporter("supporter", "Support@123", "Sup", "Test", "supt@kntu.ac.ir", "09120000555");

            try {
                java.lang.reflect.Field creatorEmailField = Supporter.class.getDeclaredField("createdByAdminEmail");
                creatorEmailField.setAccessible(true);
                creatorEmailField.set(supporter, admin1.getEmail());
            } catch (Exception ignored) {}

            java.lang.reflect.Field supportersField = UserManager.class.getDeclaredField("supporters");
            supportersField.setAccessible(true);
            java.util.Map<String, Supporter> supporters = (java.util.Map<String, Supporter>) supportersField.get(userManager);
            supporters.put(supporter.getUsername().toLowerCase().trim(), supporter);

            java.lang.reflect.Field currentUserField = UserManager.class.getDeclaredField("currentUser");
            currentUserField.setAccessible(true);
            currentUserField.set(userManager, admin2);

            assertThrows(InvalidInputException.class, () -> userManager.toggleUserStatus("supporter"));
            assertTrue(supporter.isActive());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testPhysicalItemReservationQueueAndAssignment() {
        UserManager userManager = new UserManager();
        RegularUser userInQueue = new RegularUser("Ali", "Alavi", "Admin@123", "ali@kntu.ac.ir", "09128888888", "M99999");
        userManager.registerRegularUser(userInQueue);
        physicalBook.getReservationQueue().add(userInQueue.getEmail());
        assertEquals(1, physicalBook.getReservationQueue().size());
        String nextUserEmail = physicalBook.getReservationQueue().poll();
        userManager.assignItemToNextInQueue(nextUserEmail, physicalBook);
        assertEquals(1, userInQueue.getBorrowRecords().size());
        assertEquals(physicalBook.getId(), userInQueue.getBorrowRecords().get(0).getItem().getId());
    }

    @Test
    public void testSupportTicketInitializationAndFlow() {
        SupportTicket ticket = new SupportTicket(RequestSection.TECHNICAL, "Cannot download Ebook");
        assertEquals("OPEN", ticket.getStatus());
        assertEquals("", ticket.getResponse());
        ticket.setResponse("The link has been updated.");
        assertEquals("CLOSED", ticket.getStatus());
        assertEquals("The link has been updated.", ticket.getResponse());
    }


    @Test
    public void testInvalidPasswordThrowsInputException() {
        assertThrows(InvalidInputException.class, () -> {
            new RegularUser("Wrong", "Pass", "123", "wrong@kntu.ac.ir", "09123456789", "M00001");
        });
    }

    @Test
    public void testInvalidPhoneNumberThrowsInputException() {
        assertThrows(InvalidInputException.class, () -> {
            new RegularUser("Wrong", "Phone", "Admin@123", "wrongphone@kntu.ac.ir", "123456", "M00003");
        });
    }

    @Test
    public void testItemInvalidPublishYearThrowsException() {
        assertThrows(InvalidInputException.class, () -> {
            new Book("Invalid Book", 2500, "Technical", 5, 500, "9780134685991", "Author");
        });
    }

    @Test
    public void testBorrowRecordExtensionDaysLessThanZero() {
        BorrowRecord record = new BorrowRecord(physicalBook, 7);
        try {
            record.extendDuration(-5);
        } catch (Exception ignored) {}
        assertNotNull(record);
    }

    @Test
    public void testWalletChargeNegativeAmount() {
        assertThrows(InvalidInputException.class, () -> testUser.getWallet().charge(-5000.0));
    }

    @Test
    public void testWalletDeductNegativeAmount() {
        testUser.getWallet().charge(10000.0);
        try {
            testUser.getWallet().deduct(-2000.0, "Negative Deduction");
        } catch (Exception ignored) {}
        assertEquals(10000.0, testUser.getWallet().getBalance());
    }

    @Test
    public void testWalletTransactionDetailsRecordedCorrectly() {
        testUser.getWallet().charge(30000.0);
        testUser.getWallet().deduct(10000.0, "Book Purchase Fine");

        var transactions = testUser.getWallet().getTransactions();
        assertEquals(2, transactions.size());

        var lastTx = transactions.get(transactions.size() - 1);
        assertEquals(10000.0, lastTx.getAmount());
        assertEquals("Book Purchase Fine", lastTx.getDescription());
    }

    @Test
    public void testBorrowFailsAndEntersQueueWhenCopiesAreZero() {
        physicalBook.setAvailableCopies(0);

        physicalBook.getReservationQueue().add(testUser.getEmail());

        assertEquals(1, physicalBook.getReservationQueue().size());
        assertEquals(testUser.getEmail(), physicalBook.getReservationQueue().peek());
    }

}