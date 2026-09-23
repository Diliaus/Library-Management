package ir.ac.kntu.persistence;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ir.ac.kntu.exception.DuplicateUsernameException;
import ir.ac.kntu.exception.InvalidInputException;
import ir.ac.kntu.exception.UserNotFoundException;
import ir.ac.kntu.model.Fine;
import ir.ac.kntu.model.RequestSection;
import ir.ac.kntu.model.SupportRequest;
import ir.ac.kntu.model.user.Admin;
import ir.ac.kntu.model.user.Supporter;
import ir.ac.kntu.model.user.User;
import ir.ac.kntu.services.UserManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static ir.ac.kntu.persistence.DataConstants.*;

public final class UserAdminSerializer {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private UserAdminSerializer() {
    }

    public static JsonArray saveSupporters(UserManager userManager) {
        JsonArray arr = new JsonArray();
        for (User user : userManager.getAllUsersInSystem()) {
            if ("SUPPORTER".equals(user.getRole())) {
                arr.add(supporterToJson((Supporter) user));
            }
        }
        return arr;
    }

    private static JsonObject supporterToJson(Supporter supporter) {
        JsonObject obj = new JsonObject();
        obj.addProperty(KEY_USERNAME, supporter.getUsername());
        obj.addProperty(KEY_PASSWORD, supporter.getPassword());
        obj.addProperty(KEY_FIRST_NAME, supporter.getFirstName());
        obj.addProperty(KEY_LAST_NAME, supporter.getLastName());
        obj.addProperty(KEY_EMAIL, supporter.getEmail());
        obj.addProperty(KEY_PHONE_NUMBER, supporter.getPhoneNumber());
        obj.addProperty(KEY_ACTIVE, supporter.isActive());
        obj.addProperty(KEY_CREATED_BY_ADMIN_EMAIL, supporter.getCreatedByAdminEmail());
        obj.addProperty(KEY_BLOCKED, supporter.isBlocked());
        JsonArray sectionsArr = new JsonArray();
        for (RequestSection sec : supporter.getAssignedSections()) {
            sectionsArr.add(sec.name());
        }
        obj.add(KEY_ASSIGNED_SECTIONS, sectionsArr);
        return obj;
    }

    public static void loadSupporters(JsonObject root, UserManager userManager) {
        if (!root.has(KEY_SUPPORTERS)) {
            return;
        }
        JsonArray arr = root.getAsJsonArray(KEY_SUPPORTERS);
        for (JsonElement elem : arr) {
            loadSingleSupporter(elem.getAsJsonObject(), userManager);
        }
    }

    private static void loadSingleSupporter(JsonObject obj, UserManager userManager) {
        String username = obj.get(KEY_USERNAME).getAsString();
        if (userManager.findAnyUser(username) != null) {
            return;
        }
        String password = obj.get(KEY_PASSWORD).getAsString();
        String firstName = obj.get(KEY_FIRST_NAME).getAsString();
        String lastName = obj.get(KEY_LAST_NAME).getAsString();
        String email = obj.get(KEY_EMAIL).getAsString();
        String phoneNumber = obj.get(KEY_PHONE_NUMBER).getAsString();
        Supporter supporter = new Supporter(username, password, firstName, lastName, email, phoneNumber);
        supporter.setActive(obj.get(KEY_ACTIVE).getAsBoolean());
        if (obj.has(KEY_CREATED_BY_ADMIN_EMAIL) && !obj.get(KEY_CREATED_BY_ADMIN_EMAIL).isJsonNull()) {
            supporter.setCreatedByAdminEmail(obj.get(KEY_CREATED_BY_ADMIN_EMAIL).getAsString());
        }
        if (obj.has(KEY_BLOCKED)) {
            supporter.setBlocked(obj.get(KEY_BLOCKED).getAsBoolean());
        }
        if (obj.has(KEY_ASSIGNED_SECTIONS)) {
            for (JsonElement se : obj.getAsJsonArray(KEY_ASSIGNED_SECTIONS)) {
                supporter.addSection(RequestSection.valueOf(se.getAsString()));
            }
        }
        try {
            userManager.registerSupporter(supporter);
        } catch (DuplicateUsernameException | InvalidInputException e) {
            System.err.println("Failed to load supporter: " + e.getMessage());
        }
    }

    public static JsonArray saveAdmins(UserManager userManager) {
        JsonArray arr = new JsonArray();
        for (User user : userManager.getAllUsersInSystem()) {
            if ("ADMIN".equals(user.getRole())) {
                arr.add(adminToJson((Admin) user));
            }
        }
        return arr;
    }

    private static JsonObject adminToJson(Admin admin) {
        JsonObject obj = new JsonObject();
        obj.addProperty(KEY_USERNAME, admin.getUsername());
        obj.addProperty(KEY_PASSWORD, admin.getPassword());
        obj.addProperty(KEY_FIRST_NAME, admin.getFirstName());
        obj.addProperty(KEY_LAST_NAME, admin.getLastName());
        obj.addProperty(KEY_EMAIL, admin.getEmail());
        obj.addProperty(KEY_PHONE_NUMBER, admin.getPhoneNumber());
        obj.addProperty(KEY_ACTIVE, admin.isActive());
        return obj;
    }

    public static void loadAdmins(JsonObject root, UserManager userManager) {
        if (!root.has(KEY_ADMINS)) {
            return;
        }
        JsonArray arr = root.getAsJsonArray(KEY_ADMINS);
        for (JsonElement elem : arr) {
            JsonObject obj = elem.getAsJsonObject();
            String username = obj.get(KEY_USERNAME).getAsString();
            if (userManager.findAnyUser(username) != null) {
                continue;
            }
            String password = obj.get(KEY_PASSWORD).getAsString();
            String firstName = obj.get(KEY_FIRST_NAME).getAsString();
            String lastName = obj.get(KEY_LAST_NAME).getAsString();
            String email = obj.get(KEY_EMAIL).getAsString();
            String phoneNumber = obj.get(KEY_PHONE_NUMBER).getAsString();
            Admin admin = new Admin(username, password, firstName, lastName, email, phoneNumber);
            admin.setActive(obj.get(KEY_ACTIVE).getAsBoolean());
            try {
                userManager.registerAdmin(admin);
            } catch (DuplicateUsernameException | InvalidInputException e) {
                System.err.println("Failed to load admin: " + e.getMessage());
            }
        }
    }

    public static JsonArray saveFines(UserManager userManager) {
        JsonArray arr = new JsonArray();
        Map<String, List<Fine>> finesMap = userManager.getUserFinesByEmail();
        for (Map.Entry<String, List<Fine>> entry : finesMap.entrySet()) {
            for (Fine fine : entry.getValue()) {
                JsonObject obj = new JsonObject();
                obj.addProperty(KEY_USER_EMAIL, entry.getKey());
                obj.addProperty(KEY_AMOUNT, fine.getAmount());
                obj.addProperty(KEY_PAID, fine.isPaid());
                obj.addProperty(KEY_DATE_ISSUED, fine.getDateIssued().format(DTF));
                arr.add(obj);
            }
        }
        return arr;
    }

    public static void loadFines(JsonObject root, UserManager userManager) {
        if (!root.has(KEY_FINES)) {
            return;
        }
        JsonArray arr = root.getAsJsonArray(KEY_FINES);
        for (JsonElement elem : arr) {
            JsonObject obj = elem.getAsJsonObject();
            String userEmail = obj.get(KEY_USER_EMAIL).getAsString();
            double amount = obj.get(KEY_AMOUNT).getAsDouble();
            boolean paid = obj.get(KEY_PAID).getAsBoolean();
            String dateIssuedStr = obj.get(KEY_DATE_ISSUED).getAsString();
            Fine fine = new Fine(amount);
            fine.setPaid(paid);
            fine.setDateIssued(LocalDateTime.parse(dateIssuedStr, DTF));
            try {
                userManager.addFineToUser(userEmail, fine);
            } catch (UserNotFoundException | InvalidInputException e) {
                System.err.println("Failed to load fine: " + e.getMessage());
            }
        }
    }

    public static JsonArray saveSupportRequests(UserManager userManager) {
        JsonArray arr = new JsonArray();
        for (SupportRequest req : userManager.getSupportRequests()) {
            JsonObject obj = new JsonObject();
            obj.addProperty(KEY_ID, req.getId());
            obj.addProperty(KEY_USER_EMAIL, req.getUserEmail());
            obj.addProperty(KEY_SECTION, req.getSection().name());
            obj.addProperty(KEY_MESSAGE, req.getMessage());
            obj.addProperty(KEY_RESPONSE, req.getResponse());
            obj.addProperty(KEY_STATUS, req.getStatus());
            arr.add(obj);
        }
        return arr;
    }

    public static void loadSupportRequests(JsonObject root, UserManager userManager) {
        if (!root.has(KEY_SUPPORT_REQUESTS)) {
            return;
        }
        JsonArray arr = root.getAsJsonArray(KEY_SUPPORT_REQUESTS);
        for (JsonElement elem : arr) {
            JsonObject obj = elem.getAsJsonObject();
            String id = obj.get(KEY_ID).getAsString();
            String userEmail = obj.get(KEY_USER_EMAIL).getAsString();
            RequestSection section = RequestSection.valueOf(obj.get(KEY_SECTION).getAsString());
            String message = obj.get(KEY_MESSAGE).getAsString();
            String response = obj.has(KEY_RESPONSE) ? obj.get(KEY_RESPONSE).getAsString() : "";
            String status = obj.has(KEY_STATUS) ? obj.get(KEY_STATUS).getAsString() : "OPEN";
            SupportRequest req = new SupportRequest(userEmail, section, message);
            req.setId(id);
            if (!response.isEmpty()) {
                req.setResponse(response);
            }
            req.setStatus(status);
            userManager.addSupportRequest(req);
        }
    }
}
