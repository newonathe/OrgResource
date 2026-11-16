package app.components;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/** Bounded, local image validation and content hashing for Resource image requests. */
public final class ResourceImageSupport {
    public static final int MAX_BYTES = 2_097_152;
    private static final long MAX_PIXELS = 16_777_216L;
    private static final byte[] PNG_SIGNATURE = new byte[] {
            (byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a
    };
    private static final byte[] PNG_END = new byte[] {
            0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4e, 0x44, (byte) 0xae, 0x42, 0x60, (byte) 0x82
    };

    private ResourceImageSupport() { }

    public static byte[] readBounded(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream(8192);
        byte[] buffer = new byte[8192];
        int total = 0;
        int count;
        while ((count = input.read(buffer)) != -1) {
            if (count == 0) continue;
            if (count > MAX_BYTES - total) {
                throw new ApiException(413, "IMAGE_TOO_LARGE", "Image content must not exceed 2097152 bytes.");
            }
            output.write(buffer, 0, count);
            total += count;
        }
        return output.toByteArray();
    }

    public static String validate(byte[] bytes, String declaredContentType) {
        if (bytes == null || bytes.length == 0) invalid("The image file must not be empty.");
        if (bytes.length > MAX_BYTES) {
            throw new ApiException(413, "IMAGE_TOO_LARGE", "Image content must not exceed 2097152 bytes.");
        }
        String contentType = normalizeContentType(declaredContentType);
        boolean png = startsWith(bytes, PNG_SIGNATURE);
        boolean jpeg = bytes.length >= 3 && (bytes[0] & 0xff) == 0xff
                && (bytes[1] & 0xff) == 0xd8 && (bytes[2] & 0xff) == 0xff;
        if ((!png && !jpeg) || (png && !"image/png".equals(contentType))
                || (jpeg && !"image/jpeg".equals(contentType))) {
            invalid("Only matching PNG and JPEG image content is accepted.");
        }
        if ((png && !endsWith(bytes, PNG_END)) || (jpeg && !endsWith(bytes, new byte[] {(byte) 0xff, (byte) 0xd9}))) {
            invalid("The image content is incomplete or unreadable.");
        }

        ImageReader reader = null;
        try (ImageInputStream imageInput = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            if (imageInput == null) invalid("The image content is incomplete or unreadable.");
            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInput);
            if (!readers.hasNext()) invalid("The image content is incomplete or unreadable.");
            reader = readers.next();
            reader.setInput(imageInput, true, true);
            String format = reader.getFormatName().toLowerCase(Locale.ROOT);
            if ((png && !"png".equals(format)) || (jpeg && !"jpeg".equals(format))) {
                invalid("The declared image format does not match its content.");
            }
            int width = reader.getWidth(0);
            int height = reader.getHeight(0);
            if (width <= 0 || height <= 0 || (long) width * (long) height > MAX_PIXELS) {
                invalid("Image dimensions must be positive and no larger than 16777216 pixels.");
            }
            BufferedImage decoded = reader.read(0);
            if (decoded == null) invalid("The image content is incomplete or unreadable.");
            decoded.flush();
            return png ? "image/png" : "image/jpeg";
        } catch (IOException | RuntimeException invalidImage) {
            if (invalidImage instanceof ApiException) throw (ApiException) invalidImage;
            throw new ApiException(400, "INVALID_IMAGE", "The image content is incomplete or unreadable.");
        } finally {
            if (reader != null) reader.dispose();
        }
    }

    public static String sha256(byte[] bytes) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte value : digest) hex.append(String.format(Locale.ROOT, "%02x", value & 0xff));
            return hex.toString();
        } catch (NoSuchAlgorithmException unavailable) {
            throw new IllegalStateException("SHA-256 is unavailable in this Java runtime.", unavailable);
        }
    }

    private static String normalizeContentType(String declaredContentType) {
        if (declaredContentType == null) return "";
        int parameters = declaredContentType.indexOf(';');
        String mediaType = parameters < 0 ? declaredContentType : declaredContentType.substring(0, parameters);
        return mediaType.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean startsWith(byte[] value, byte[] prefix) {
        if (value.length < prefix.length) return false;
        for (int i = 0; i < prefix.length; i++) if (value[i] != prefix[i]) return false;
        return true;
    }

    private static boolean endsWith(byte[] value, byte[] suffix) {
        if (value.length < suffix.length) return false;
        int offset = value.length - suffix.length;
        for (int i = 0; i < suffix.length; i++) if (value[offset + i] != suffix[i]) return false;
        return true;
    }

    private static void invalid(String message) {
        throw new ApiException(400, "INVALID_IMAGE", message);
    }
}
