package ir.ac.kntu.services;

import ir.ac.kntu.model.item.LibraryItem;
import ir.ac.kntu.model.user.RegularUser;
import java.util.List;
import java.util.Map;

public interface CatalogService {
    void addItem(LibraryItem item);

    void removeItem(String id);

    LibraryItem getItemById(String id);

    List<LibraryItem> searchByTitle(String title);

    List<LibraryItem> filterItems(String type, String category, Integer publishYear, Integer maxPages);

    Map<String, LibraryItem> getAllItems();

    void reserveItem(RegularUser user, String itemId);
}