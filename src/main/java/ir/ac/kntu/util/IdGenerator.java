package ir.ac.kntu.util;

import java.util.HashMap;
import java.util.Map;

public final class IdGenerator {

    private static final Map<String, Integer> COUNTERS = new HashMap<>();

    private IdGenerator() {
    }

    public static String generateMemberId(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            throw new IllegalArgumentException("Prefix cannot be null or empty");
        }
        String cleanPrefix = prefix.toUpperCase().trim();
        int nextId = COUNTERS.getOrDefault(cleanPrefix, 1);
        COUNTERS.put(cleanPrefix, nextId + 1);
        return String.format("%s%06d", cleanPrefix, nextId);
    }

    public static String generateItemId(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            throw new IllegalArgumentException("Prefix cannot be null or empty");
        }
        String cleanPrefix = prefix.toUpperCase().trim();
        int nextId = COUNTERS.getOrDefault(cleanPrefix, 1);
        COUNTERS.put(cleanPrefix, nextId + 1);
        return String.format("%s%08d", cleanPrefix, nextId);
    }
}