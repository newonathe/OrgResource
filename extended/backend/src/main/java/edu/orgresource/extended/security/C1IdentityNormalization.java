package edu.orgresource.extended.security;

import java.text.Normalizer;
import java.util.Locale;

public final class C1IdentityNormalization {
    private C1IdentityNormalization() { }
    public static String username(String input) {
        if (input == null) return "";
        return Normalizer.normalize(input.trim(), Normalizer.Form.NFKC).toLowerCase(Locale.ROOT);
    }
    public static String slug(String input) {
        if (input == null) return "";
        return Normalizer.normalize(input.trim(), Normalizer.Form.NFKC).toLowerCase(Locale.ROOT);
    }
}
