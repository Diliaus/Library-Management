package ir.ac.kntu.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import ir.ac.kntu.model.SystemConfiguration;
import ir.ac.kntu.model.item.LibraryItem;
import ir.ac.kntu.services.Catalog;
import ir.ac.kntu.services.UserManager;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static ir.ac.kntu.persistence.DataConstants.*;

public final class DataManager {

    private static final String DATA_FILE = "library_data.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private DataManager() {
    }

    public static void saveData(Catalog catalog, UserManager userManager) {
        JsonObject root = new JsonObject();
        root.add(KEY_SYSTEM_CONFIG, saveSystemConfig());
        root.add(KEY_CATALOG_ITEMS, ItemSerializer.saveCatalogItems(catalog));
        root.add(KEY_REGULAR_USERS, UserSerializer.saveRegularUsers(userManager));
        root.add(KEY_SUPPORTERS, UserAdminSerializer.saveSupporters(userManager));
        root.add(KEY_ADMINS, UserAdminSerializer.saveAdmins(userManager));
        root.add(KEY_FINES, UserAdminSerializer.saveFines(userManager));
        root.add(KEY_SUPPORT_REQUESTS, UserAdminSerializer.saveSupportRequests(userManager));
        root.add(KEY_ID_GENERATOR_COUNTERS, saveIdGeneratorCounters());
        try (FileWriter writer = new FileWriter(DATA_FILE)) {
            GSON.toJson(root, writer);
        } catch (IOException e) {
            System.err.println("Failed to save data: " + e.getMessage());
        }
    }

    public static boolean loadData(Catalog catalog, UserManager userManager) {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            return false;
        }
        try (FileReader reader = new FileReader(file)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            loadSystemConfig(root);
            Map<String, LibraryItem> itemsMap = ItemSerializer.loadCatalogItems(root);
            for (LibraryItem item : itemsMap.values()) {
                catalog.addItem(item);
            }
            List<UserSerializer.RegularUserRef> userRefs = UserSerializer.loadRegularUsers(root, itemsMap);
            for (UserSerializer.RegularUserRef ref : userRefs) {
                userManager.registerRegularUser(ref.getUser());
                UserSerializer.resolveItemRefs(ref.getUser(), ref.getBorrowItemIds(), itemsMap);
            }
            UserAdminSerializer.loadSupporters(root, userManager);
            UserAdminSerializer.loadAdmins(root, userManager);
            UserAdminSerializer.loadFines(root, userManager);
            UserAdminSerializer.loadSupportRequests(root, userManager);
            loadIdGeneratorCounters(root);
            return true;
        } catch (JsonSyntaxException e) {
            System.err.println("Failed to load data: " + e.getMessage());
            return false;
        } catch (IOException e) {
            System.err.println("Failed to load data: " + e.getMessage());
            return false;
        }
    }

    private static JsonObject saveSystemConfig() {
        JsonObject obj = new JsonObject();
        SystemConfiguration config = SystemConfiguration.getInstance();
        obj.addProperty(KEY_MAX_BORROW_DAYS, config.getMaxBorrowDays());
        obj.addProperty(KEY_FINE_RATE_PER_DAY, config.getFineRatePerDay());
        obj.addProperty(KEY_RESERVATION_EXPIRY_DAYS, config.getReservationExpiryDays());
        obj.addProperty(KEY_MAX_SIMUL_RESERVATIONS, config.getMaxSimultaneousReservations());
        return obj;
    }

    private static void loadSystemConfig(JsonObject root) {
        if (!root.has(KEY_SYSTEM_CONFIG)) {
            return;
        }
        JsonObject obj = root.getAsJsonObject(KEY_SYSTEM_CONFIG);
        SystemConfiguration config = SystemConfiguration.getInstance();
        config.setMaxBorrowDays(obj.get(KEY_MAX_BORROW_DAYS).getAsInt());
        config.setFineRatePerDay(obj.get(KEY_FINE_RATE_PER_DAY).getAsDouble());
        config.setReservationExpiryDays(obj.get(KEY_RESERVATION_EXPIRY_DAYS).getAsInt());
        config.setMaxSimultaneousReservations(obj.get(KEY_MAX_SIMUL_RESERVATIONS).getAsInt());
    }

    private static JsonObject saveIdGeneratorCounters() {
        JsonObject obj = new JsonObject();
        try {
            Field countersField = ir.ac.kntu.util.IdGenerator.class.getDeclaredField("COUNTERS");
            countersField.setAccessible(true);
            @SuppressWarnings(UNCHECKED)
            Map<String, Integer> counters = (Map<String, Integer>) countersField.get(null);
            for (Map.Entry<String, Integer> entry : counters.entrySet()) {
                obj.addProperty(entry.getKey(), entry.getValue());
            }
        } catch (ReflectiveOperationException e) {
            System.err.println("Failed to save ID counters: " + e.getMessage());
        }
        return obj;
    }

    private static void loadIdGeneratorCounters(JsonObject root) {
        if (!root.has(KEY_ID_GENERATOR_COUNTERS)) {
            return;
        }
        JsonObject countersObj = root.getAsJsonObject(KEY_ID_GENERATOR_COUNTERS);
        try {
            Field countersField = ir.ac.kntu.util.IdGenerator.class.getDeclaredField("COUNTERS");
            countersField.setAccessible(true);
            @SuppressWarnings(UNCHECKED)
            Map<String, Integer> counters = (Map<String, Integer>) countersField.get(null);
            counters.clear();
            for (String key : countersObj.keySet()) {
                counters.put(key, countersObj.get(key).getAsInt());
            }
        } catch (ReflectiveOperationException e) {
            System.err.println("Failed to load ID counters: " + e.getMessage());
        }
    }
}
