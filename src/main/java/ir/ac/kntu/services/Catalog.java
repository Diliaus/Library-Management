package ir.ac.kntu.services;

import ir.ac.kntu.model.SystemConfiguration;
import ir.ac.kntu.model.item.Book;
import ir.ac.kntu.model.item.Ebook;
import ir.ac.kntu.model.item.LibraryItem;
import ir.ac.kntu.model.user.RegularUser;
import ir.ac.kntu.exception.InvalidInputException;
import ir.ac.kntu.exception.ItemNotAvailableException;
import ir.ac.kntu.exception.BorrowLimitExceededException;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class Catalog implements CatalogService, Pageable<LibraryItem> {

    private final Map<String, LibraryItem> items;

    public Catalog() {
        this.items = new HashMap<>();
    }

    @Override
    public void addItem(LibraryItem item) {
        if (item == null) {
            throw new InvalidInputException("Item cannot be null");
        }
        if (items.containsKey(item.getId())) {
            throw new InvalidInputException("Item with this ID already exists: " + item.getId());
        }
        items.put(item.getId(), item);
    }

    @Override
    public void removeItem(String id) {
        if (id == null || !items.containsKey(id)) {
            throw new ItemNotAvailableException("Item not found in catalog for deletion.");
        }
        items.remove(id);
    }

    @Override
    public LibraryItem getItemById(String id) {
        if (id == null || !items.containsKey(id)) {
            throw new ItemNotAvailableException("The requested item with ID " + id + " does not exist.");
        }
        return items.get(id);
    }

    @Override
    public List<LibraryItem> searchByTitle(String title) {
        List<LibraryItem> results = new ArrayList<>();
        if (title == null || title.trim().isEmpty()) {
            return results;
        }
        String searchTitle = title.toLowerCase().trim();
        for (LibraryItem item : items.values()) {
            if (item.getTitle().toLowerCase().contains(searchTitle)) {
                results.add(item);
            }
        }
        return results;
    }

    @Override
    public List<LibraryItem> filterItems(String type, String category, Integer publishYear, Integer maxPages) {
        List<LibraryItem> results = new ArrayList<>();
        for (LibraryItem item : items.values()) {
            if (type != null && !item.getItemType().equalsIgnoreCase(type.trim())) {
                continue;
            }
            if (category != null && !item.getCategory().equalsIgnoreCase(category.trim())) {
                continue;
            }
            if (publishYear != null && item.getPublishYear() != publishYear.intValue()) {
                continue;
            }
            if (isExceedingMaxPages(item, maxPages)) {
                continue;
            }
            results.add(item);
        }
        return results;
    }

    private boolean isExceedingMaxPages(LibraryItem item, Integer maxPages) {
        return maxPages != null && isOverMaxPages(item, maxPages.intValue());
    }

    private boolean isOverMaxPages(LibraryItem item, int maxPages) {
        if (item instanceof Book) {
            return ((Book) item).getPages() > maxPages;
        }
        if (item instanceof Ebook) {
            return ((Ebook) item).getPages() > maxPages;
        }
        return false;
    }

    @Override
    public Map<String, LibraryItem> getAllItems() {
        return new HashMap<>(items);
    }

    @Override
    public void reserveItem(RegularUser user, String itemId) {
        LibraryItem item = validateReservationRequest(user, itemId);
        int maxAllowed = SystemConfiguration.getInstance().getMaxSimultaneousReservations();
        long currentActiveReservations = countUserActiveReservations(user.getEmail());
        if (currentActiveReservations >= maxAllowed) {
            throw new BorrowLimitExceededException("Reservation Denied: You have reached the maximum allowed simultaneous reservations (" + maxAllowed + ").");
        }
        if (item.getReservationQueue().contains(user.getEmail())) {
            throw new InvalidInputException("You are already in the reservation queue for this item.");
        }
        item.getReservationQueue().add(user.getEmail());
    }

    private LibraryItem validateReservationRequest(RegularUser user, String itemId) {
        if (user == null || itemId == null) {
            throw new InvalidInputException("User or Item ID cannot be null");
        }
        LibraryItem item = items.get(itemId);
        if (item == null) {
            throw new ItemNotAvailableException("Item not found in catalog");
        }
        if (!item.isReservable()) {
            throw new InvalidInputException("Reservation Denied: Digital resources cannot be reserved.");
        }
        return item;
    }

    private long countUserActiveReservations(String userEmail) {
        return items.values().stream()
                .filter(i -> i.getReservationQueue().contains(userEmail))
                .count();
    }

    @Override
    public List<LibraryItem> getPage(List<LibraryItem> itemList, int pageNumber, int pageSize) {
        if (itemList == null || itemList.isEmpty()) {
            return Collections.emptyList();
        }
        int fromIndex = (pageNumber - 1) * pageSize;
        if (fromIndex >= itemList.size() || fromIndex < 0) {
            return Collections.emptyList();
        }
        int toIndex = Math.min(fromIndex + pageSize, itemList.size());
        return itemList.subList(fromIndex, toIndex);
    }
}