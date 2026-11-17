package edu.orgresource.extended.identity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public final class C1Secrets {
    private static final SecureRandom RANDOM = new SecureRandom();
    private C1Secrets() { }

    public static String bearerToken() { return randomToken(32); }

    public static List<String> recoveryCodes(int count) {
        List<String> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) result.add(randomToken(32));
        return List.copyOf(result);
    }

    private static String randomToken(int size) {
        byte[] bytes = new byte[size];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static byte[] sha256(String secret) {
        try { return MessageDigest.getInstance("SHA-256").digest(secret.getBytes(StandardCharsets.UTF_8)); }
        catch (NoSuchAlgorithmException impossible) { throw new IllegalStateException("SHA-256 unavailable", impossible); }
    }

    public static UUID uuid() { return UUID.randomUUID(); }
}
