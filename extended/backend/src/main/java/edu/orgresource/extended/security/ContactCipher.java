package edu.orgresource.extended.security;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import edu.orgresource.extended.api.C1ApiException;

/** AES-256-GCM contact storage. The key is supplied only through protected runtime configuration. */
@Component
public final class ContactCipher {
    private static final int NONCE_BYTES = 12;
    private static final int TAG_BITS = 128;
    private final byte[] key;
    private final String keyId;
    private final SecureRandom random = new SecureRandom();

    public ContactCipher(@Value("${c1.contact.encryption-key:}") String encodedKey,
                         @Value("${c1.contact.encryption-key-id:local-v1}") String keyId) {
        this.keyId = keyId;
        if (encodedKey == null || encodedKey.isBlank()) {
            this.key = null;
            return;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(encodedKey);
            if (decoded.length != 32 || keyId == null || !keyId.matches("[A-Za-z0-9._-]{1,64}"))
                throw new IllegalArgumentException("Contact encryption configuration is invalid.");
            this.key = decoded;
        } catch (IllegalArgumentException invalid) {
            throw new IllegalStateException("Contact encryption configuration is invalid.");
        }
    }

    public EncryptedContact encrypt(long organizationId, UUID allocationPublicId, String plaintext) {
        requireKey();
        byte[] nonce = new byte[NONCE_BYTES];
        random.nextBytes(nonce);
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(TAG_BITS, nonce));
            cipher.updateAAD(aad(organizationId, allocationPublicId));
            return new EncryptedContact(cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8)), nonce, keyId);
        } catch (GeneralSecurityException failure) {
            throw encryptionFailure();
        }
    }

    public String decrypt(long organizationId, UUID allocationPublicId, byte[] ciphertext,
                          byte[] nonce, String storedKeyId) {
        requireKey();
        if (ciphertext == null || nonce == null || !keyId.equals(storedKeyId))
            throw new C1ApiException(500, "CONTACT_INTEGRITY_FAILURE", "Stored contact data could not be verified.");
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(TAG_BITS, nonce));
            cipher.updateAAD(aad(organizationId, allocationPublicId));
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException failure) {
            throw new C1ApiException(500, "CONTACT_INTEGRITY_FAILURE", "Stored contact data could not be verified.");
        }
    }

    private void requireKey() {
        if (key == null) throw new C1ApiException(503, "CONTACT_ENCRYPTION_UNAVAILABLE",
                "Consent contact storage is unavailable until protected encryption is configured.");
    }

    private static byte[] aad(long organizationId, UUID allocationPublicId) {
        return ("orgresource-contact-v1|" + organizationId + "|" + allocationPublicId)
                .getBytes(StandardCharsets.UTF_8);
    }

    private static C1ApiException encryptionFailure() {
        return new C1ApiException(503, "CONTACT_ENCRYPTION_UNAVAILABLE", "Consent contact storage is unavailable.");
    }

    public record EncryptedContact(byte[] ciphertext, byte[] nonce, String keyId) { }
}
