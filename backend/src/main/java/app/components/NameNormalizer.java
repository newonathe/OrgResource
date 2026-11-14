package app.components;

import java.util.Locale;
import app.components.ApiException;

public final class NameNormalizer {
    private NameNormalizer() { }

    public static String display(String value, String field) {
        if (value == null) {
            throw new ApiException(400, "VALIDATION_FAILED", field + " is required.");
        }
        String normalized = value.trim().replaceAll("\\s+", " ");
        if (normalized.isEmpty() || normalized.length() > 100) {
            throw new ApiException(400, "VALIDATION_FAILED", field + " must contain 1 to 100 characters.");
        }
        return normalized;
    }

    public static String key(String display) {
        return display.toLowerCase(Locale.ROOT);
    }
}
