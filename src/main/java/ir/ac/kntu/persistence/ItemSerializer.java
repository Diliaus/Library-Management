package ir.ac.kntu.persistence;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ir.ac.kntu.model.item.AudioBook;
import ir.ac.kntu.model.item.Book;
import ir.ac.kntu.model.item.Ebook;
import ir.ac.kntu.model.item.LibraryItem;
import ir.ac.kntu.model.item.Magazine;
import ir.ac.kntu.services.Catalog;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;

import static ir.ac.kntu.persistence.DataConstants.*;

public final class ItemSerializer {

    private ItemSerializer() {
    }

    public static JsonArray saveCatalogItems(Catalog catalog) {
        JsonArray arr = new JsonArray();
        for (LibraryItem item : catalog.getAllItems().values()) {
            arr.add(itemToJson(item));
        }
        return arr;
    }

    public static Map<String, LibraryItem> loadCatalogItems(JsonObject root) {
        Map<String, LibraryItem> map = new LinkedHashMap<>();
        if (!root.has(KEY_CATALOG_ITEMS)) {
            return map;
        }
        JsonArray arr = root.getAsJsonArray(KEY_CATALOG_ITEMS);
        for (JsonElement elem : arr) {
            JsonObject obj = elem.getAsJsonObject();
            LibraryItem item = jsonToItem(obj);
            if (item != null) {
                map.put(item.getId(), item);
            }
        }
        return map;
    }

    public static JsonObject itemToJson(LibraryItem item) {
        JsonObject obj = new JsonObject();
        obj.addProperty(KEY_ID, item.getId());
        obj.addProperty(KEY_TYPE, item.getItemType());
        obj.addProperty(KEY_TITLE, item.getTitle());
        obj.addProperty(KEY_PUBLISH_YEAR, item.getPublishYear());
        obj.addProperty(KEY_CATEGORY, item.getCategory());
        obj.addProperty(KEY_COPIES, item.getCopies());
        if (item instanceof Book book) {
            addBookFields(obj, book);
        } else if (item instanceof Magazine magazine) {
            addMagazineFields(obj, magazine);
        } else if (item instanceof Ebook ebook) {
            addEbookFields(obj, ebook);
        } else if (item instanceof AudioBook audioBook) {
            addAudioBookFields(obj, audioBook);
        }
        return obj;
    }

    private static void addBookFields(JsonObject obj, Book book) {
        obj.addProperty(KEY_PAGES, book.getPages());
        obj.addProperty(KEY_ISBN, book.getIsbn());
        obj.addProperty(KEY_AUTHOR, book.getAuthor());
        obj.addProperty(KEY_AVAILABLE_COPIES, book.getAvailableCopies());
        obj.add(KEY_RESERVATION_QUEUE, queueToJson(book.getReservationQueue()));
    }

    private static void addMagazineFields(JsonObject obj, Magazine magazine) {
        obj.addProperty(KEY_ISSN, magazine.getIssn());
        obj.addProperty(KEY_CYCLE, magazine.getCycle());
        obj.addProperty(KEY_AVAILABLE_COPIES, magazine.getAvailableCopies());
        obj.add(KEY_RESERVATION_QUEUE, queueToJson(magazine.getReservationQueue()));
    }

    private static void addEbookFields(JsonObject obj, Ebook ebook) {
        obj.addProperty(KEY_PAGES, ebook.getPages());
        obj.addProperty(KEY_FORMAT, ebook.getFormat());
        obj.addProperty(KEY_FILE_SIZE, ebook.getFileSize());
        obj.addProperty(KEY_DOWNLOAD_URL, ebook.getDownloadUrl());
    }

    private static void addAudioBookFields(JsonObject obj, AudioBook audioBook) {
        obj.addProperty(KEY_DURATION, audioBook.getDuration());
        obj.addProperty(KEY_FORMAT, audioBook.getFormat());
        obj.addProperty(KEY_FILE_SIZE, audioBook.getFileSize());
        obj.addProperty(KEY_DOWNLOAD_URL, audioBook.getDownloadUrl());
    }

    private static JsonArray queueToJson(Queue<String> queue) {
        JsonArray arr = new JsonArray();
        if (queue != null) {
            for (String s : queue) {
                arr.add(s);
            }
        }
        return arr;
    }

    public static LibraryItem jsonToItem(JsonObject obj) {
        String type = obj.get(KEY_TYPE).getAsString();
        String id = obj.get(KEY_ID).getAsString();
        LibraryItem item;
        switch (type) {
            case "BOOK":
                item = jsonToBook(obj, id);
                break;
            case "MAGAZINE":
                item = jsonToMagazine(obj, id);
                break;
            case "EBOOK":
                item = jsonToEbook(obj, id);
                break;
            case "AUDIO_BOOK":
                item = jsonToAudioBook(obj, id);
                break;
            default:
                return null;
        }
        if (obj.has(KEY_COPIES)) {
            item.setCopies(obj.get(KEY_COPIES).getAsInt());
        }
        return item;
    }

    private static Book jsonToBook(JsonObject obj, String id) {
        String title = obj.get(KEY_TITLE).getAsString();
        int publishYear = obj.get(KEY_PUBLISH_YEAR).getAsInt();
        String category = obj.get(KEY_CATEGORY).getAsString();
        int pages = obj.get(KEY_PAGES).getAsInt();
        String isbn = obj.get(KEY_ISBN).getAsString();
        String author = obj.get(KEY_AUTHOR).getAsString();
        int availableCopies = obj.get(KEY_AVAILABLE_COPIES).getAsInt();
        Book book = new Book(title, publishYear, category, availableCopies, pages, isbn, author);
        book.setId(id);
        if (obj.has(KEY_RESERVATION_QUEUE)) {
            for (JsonElement qe : obj.getAsJsonArray(KEY_RESERVATION_QUEUE)) {
                book.getReservationQueue().add(qe.getAsString());
            }
        }
        return book;
    }

    private static Magazine jsonToMagazine(JsonObject obj, String id) {
        String title = obj.get(KEY_TITLE).getAsString();
        int publishYear = obj.get(KEY_PUBLISH_YEAR).getAsInt();
        String category = obj.get(KEY_CATEGORY).getAsString();
        String issn = obj.get(KEY_ISSN).getAsString();
        String cycle = obj.get(KEY_CYCLE).getAsString();
        int availableCopies = obj.get(KEY_AVAILABLE_COPIES).getAsInt();
        Magazine mag = new Magazine(title, publishYear, category, availableCopies, issn, cycle);
        mag.setId(id);
        if (obj.has(KEY_RESERVATION_QUEUE)) {
            for (JsonElement qe : obj.getAsJsonArray(KEY_RESERVATION_QUEUE)) {
                mag.getReservationQueue().add(qe.getAsString());
            }
        }
        return mag;
    }

    private static Ebook jsonToEbook(JsonObject obj, String id) {
        String title = obj.get(KEY_TITLE).getAsString();
        int publishYear = obj.get(KEY_PUBLISH_YEAR).getAsInt();
        String category = obj.get(KEY_CATEGORY).getAsString();
        int pages = obj.get(KEY_PAGES).getAsInt();
        String format = obj.get(KEY_FORMAT).getAsString();
        double fileSize = obj.get(KEY_FILE_SIZE).getAsDouble();
        String downloadUrl = obj.get(KEY_DOWNLOAD_URL).getAsString();
        Ebook ebook = new Ebook(title, publishYear, category, format, fileSize, downloadUrl, pages);
        ebook.setId(id);
        return ebook;
    }

    private static AudioBook jsonToAudioBook(JsonObject obj, String id) {
        String title = obj.get(KEY_TITLE).getAsString();
        int publishYear = obj.get(KEY_PUBLISH_YEAR).getAsInt();
        String category = obj.get(KEY_CATEGORY).getAsString();
        int duration = obj.get(KEY_DURATION).getAsInt();
        String format = obj.get(KEY_FORMAT).getAsString();
        double fileSize = obj.get(KEY_FILE_SIZE).getAsDouble();
        String downloadUrl = obj.get(KEY_DOWNLOAD_URL).getAsString();
        AudioBook audio = new AudioBook(title, publishYear, category, format, fileSize, downloadUrl, duration);
        audio.setId(id);
        return audio;
    }
}
