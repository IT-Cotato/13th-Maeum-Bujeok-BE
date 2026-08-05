package com.maumbujeok.backend.domain.burn.application;

import java.util.Map;
import java.util.Map.Entry;

public final class BurningTalismanCatalog {
    private static final Map<Integer, String> KEYWORDS = Map.ofEntries(
            Map.entry(1, "\uAE30\uC5B5\uC815\uB9AC"),
            Map.entry(2, "\uD3C9\uC628"),
            Map.entry(3, "\uC790\uCC45\uD574\uC18C"),
            Map.entry(4, "\uC6A9\uAE30"),
            Map.entry(5, "\uC2AC\uD514\uD574\uC18C"),
            Map.entry(6, "\uBBF8\uB828\uC815\uB9AC"),
            Map.entry(7, "\uC720\uB300\uAC10"),
            Map.entry(8, "\uC790\uAE30\uD655\uC2E0"),
            Map.entry(9, "\uBCF4\uD638"),
            Map.entry(10, "\uBD84\uB178\uC9C4\uC815"),
            Map.entry(11, "\uD65C\uB825"),
            Map.entry(12, "\uD76C\uB9DD"),
            Map.entry(13, "\uCE68\uCC29")
    );

    private BurningTalismanCatalog() {
    }

    public static boolean contains(Integer type) {
        return type != null && KEYWORDS.containsKey(type);
    }

    public static Integer number(String storedType) {
        if (storedType == null) {
            return null;
        }
        try {
            int parsed = Integer.parseInt(storedType);
            return contains(parsed) ? parsed : null;
        } catch (NumberFormatException ignored) {
            return KEYWORDS.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(storedType))
                    .map(Entry::getKey)
                    .findFirst()
                    .orElseGet(() -> legacyNumber(storedType));
        }
    }

    private static Integer legacyNumber(String storedType) {
        return switch (storedType) {
            case "\uB9C8\uC74C\uC815\uD654", "MIND_CLEAR" -> 1;
            case "CALM" -> 2;
            default -> null;
        };
    }

    public static String keyword(Integer type) {
        if (!contains(type)) {
            throw new IllegalArgumentException("Unsupported burning talisman type: " + type);
        }
        return KEYWORDS.get(type);
    }
}