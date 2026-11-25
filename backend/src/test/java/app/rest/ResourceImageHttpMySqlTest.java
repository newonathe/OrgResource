package app.rest;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import app.SpringBootApp;
import app.components.ResourceImageSupport;
import app.entities.Resource;
import app.entities.ResourceType;
import app.repositories.ResourceRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.zip.CRC32;
import javax.imageio.ImageIO;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.LockModeType;
import org.hibernate.Hibernate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringBootApp.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ResourceImageHttpMySqlTest {
    private static final int MAX_IMAGE_BYTES = 2_097_152;

    @LocalServerPort private int port;
    @Autowired private TestRestTemplate http;
    @Autowired private ResourceRepository resources;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private EntityManagerFactory entityManagerFactory;

    private Long resourceId;
    private String baseUrl;

    @Before
    public void createOnlyThisTestResource() {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        Resource saved = resources.saveAndFlush(new Resource("Image test " + suffix,
                ("image test " + suffix).toLowerCase(Locale.ROOT), ResourceType.REUSABLE, 0));
        resourceId = saved.id;
        baseUrl = "http://127.0.0.1:" + port + "/api/resources/";
    }

    @After
    public void removeOnlyThisTestResource() {
        if (resourceId != null && resources.existsById(resourceId)) resources.deleteById(resourceId);
    }

    @Test
    public void realHttpAndMySqlEnforceImageContractAndKeepResourceJsonStable() throws Exception {
        assertEquals(5, entityManagerFactory.getMetamodel().getEntities().size());
        List<String> tables = jdbc.queryForList("SHOW TABLES", String.class);
        Collections.sort(tables);
        assertEquals(Arrays.asList("allocation", "department", "project", "resource", "resource_requirement"), tables);
        assertEquals(5, jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_type = 'BASE TABLE'",
                Integer.class).intValue());
        assertEquals("mediumblob", jdbc.queryForObject(
                "SELECT LOWER(column_type) FROM information_schema.columns "
                        + "WHERE table_schema = DATABASE() AND table_name = 'resource' AND column_name = 'image_bytes'",
                String.class));
        assertEquals(404, http.getForEntity(baseUrl + resourceId + "/image", byte[].class).getStatusCodeValue());
        assertEquals(404, http.getForEntity(baseUrl + "999999999/image", byte[].class).getStatusCodeValue());

        byte[] largePng = pngWithNoise(512, 512);
        assertTrue("The valid PNG fixture must exceed the MySQL 64KiB BLOB boundary.", largePng.length > 65536);
        ResponseEntity<String> pngUpload = upload(resourceId, "file", "image/png", largePng);
        assertEquals(200, pngUpload.getStatusCodeValue());
        JsonNode uploadResult = objectMapper.readTree(pngUpload.getBody());
        assertEquals(resourceId.longValue(), uploadResult.get("resourceId").asLong());
        assertEquals("image/png", uploadResult.get("contentType").asText());
        assertEquals(largePng.length, uploadResult.get("sizeBytes").asInt());
        String pngHash = ResourceImageSupport.sha256(largePng);
        assertEquals(pngHash, uploadResult.get("sha256").asText());

        Map<String, Object> stored = jdbc.queryForMap(
                "SELECT OCTET_LENGTH(image_bytes) AS byte_count, LOWER(SHA2(image_bytes, 256)) AS digest, image_content_type "
                        + "FROM resource WHERE id = ?", resourceId);
        assertEquals(largePng.length, ((Number) stored.get("byte_count")).intValue());
        assertEquals(pngHash, stored.get("digest"));
        assertEquals("image/png", stored.get("image_content_type"));

        EntityManager readContext = entityManagerFactory.createEntityManager();
        try {
            readContext.getTransaction().begin();
            Resource withoutImage = readContext.createQuery(
                    "SELECT r FROM Resource r WHERE r.id = :id", Resource.class)
                    .setParameter("id", resourceId)
                    .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                    .getSingleResult();
            assertFalse(Hibernate.isPropertyInitialized(withoutImage, "imageBytes"));
            readContext.refresh(withoutImage);
            assertFalse(Hibernate.isPropertyInitialized(withoutImage, "imageBytes"));
            assertArrayEquals(largePng, withoutImage.getImageBytes());
            assertTrue(Hibernate.isPropertyInitialized(withoutImage, "imageBytes"));
            readContext.getTransaction().rollback();
        } finally {
            readContext.close();
        }

        Map<String, Object> update = new HashMap<String, Object>();
        update.put("name", "Image updated " + resourceId);
        update.put("totalQuantity", 0);
        update.put("expectedTotalQuantity", 0);
        ResponseEntity<String> ordinaryResourceUpdate = http.postForEntity(
                baseUrl + resourceId + "/update", update, String.class);
        assertEquals(200, ordinaryResourceUpdate.getStatusCodeValue());

        ResponseEntity<byte[]> pngDownload = http.getForEntity(baseUrl + resourceId + "/image", byte[].class);
        assertEquals(200, pngDownload.getStatusCodeValue());
        assertEquals("image/png", pngDownload.getHeaders().getContentType().toString());
        assertTrue(pngDownload.getHeaders().getCacheControl().contains("no-store"));
        assertEquals("nosniff", pngDownload.getHeaders().getFirst("X-Content-Type-Options"));
        assertArrayEquals(largePng, pngDownload.getBody());

        byte[] jpeg = jpegImage(48, 32);
        ResponseEntity<String> jpegUpload = upload(resourceId, "file", "image/jpeg", jpeg);
        assertEquals(200, jpegUpload.getStatusCodeValue());
        ResponseEntity<byte[]> jpegDownload = http.getForEntity(baseUrl + resourceId + "/image", byte[].class);
        assertEquals("image/jpeg", jpegDownload.getHeaders().getContentType().toString());
        assertArrayEquals(jpeg, jpegDownload.getBody());

        byte[] exactLimitPng = pngExactly(MAX_IMAGE_BYTES);
        assertEquals(MAX_IMAGE_BYTES, exactLimitPng.length);
        ResponseEntity<String> maxUpload = upload(resourceId, "file", "image/png", exactLimitPng);
        assertEquals(200, maxUpload.getStatusCodeValue());
        ResponseEntity<byte[]> maxDownload = http.getForEntity(baseUrl + resourceId + "/image", byte[].class);
        assertArrayEquals(exactLimitPng, maxDownload.getBody());
        assertEquals(ResourceImageSupport.sha256(exactLimitPng),
                jdbc.queryForObject("SELECT LOWER(SHA2(image_bytes, 256)) FROM resource WHERE id = ?",
                        String.class, resourceId));

        assertApiError(upload(resourceId, "file", "image/png", new byte[MAX_IMAGE_BYTES + 1]), 413, "IMAGE_TOO_LARGE");
        assertApiError(upload(resourceId, "file", "image/png", new byte[0]), 400, "INVALID_IMAGE");
        assertApiError(upload(resourceId, "file", "image/jpeg", pngImage(8, 8)), 400, "INVALID_IMAGE");
        assertApiError(upload(resourceId, "file", "image/svg+xml",
                "<svg xmlns=\"http://www.w3.org/2000/svg\"></svg>".getBytes(StandardCharsets.UTF_8)),
                400, "INVALID_IMAGE");
        assertApiError(upload(resourceId, "file", "image/png", Arrays.copyOf(pngImage(8, 8), 15)),
                400, "INVALID_IMAGE");
        assertApiError(upload(resourceId, "file", "image/jpeg", Arrays.copyOf(jpeg, jpeg.length - 2)),
                400, "INVALID_IMAGE");
        assertApiError(upload(resourceId, "other", "image/png", pngImage(8, 8)), 400, "INVALID_IMAGE");
        assertApiError(upload(999999999L, "file", "image/png", pngImage(8, 8)), 404, "NOT_FOUND");

        ResponseEntity<String> resourceList = http.getForEntity("http://127.0.0.1:" + port + "/api/resources", String.class);
        assertEquals(200, resourceList.getStatusCodeValue());
        assertFalse(resourceList.getBody().contains("image_bytes"));
        assertFalse(resourceList.getBody().contains("imageContentType"));
    }

    private ResponseEntity<String> upload(Long id, String field, String contentType, byte[] bytes) {
        MultiValueMap<String, Object> multipart = new LinkedMultiValueMap<String, Object>();
        HttpHeaders partHeaders = new HttpHeaders();
        partHeaders.setContentType(MediaType.parseMediaType(contentType));
        partHeaders.setContentDispositionFormData(field, "synthetic-image.bin");
        ByteArrayResource file = new ByteArrayResource(bytes) {
            @Override public String getFilename() { return "synthetic-image.bin"; }
        };
        multipart.add(field, new HttpEntity<ByteArrayResource>(file, partHeaders));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        return http.postForEntity(baseUrl + id + "/image", new HttpEntity<MultiValueMap<String, Object>>(multipart, headers), String.class);
    }

    private void assertApiError(ResponseEntity<String> response, int status, String code) throws Exception {
        assertEquals(status, response.getStatusCodeValue());
        assertEquals(code, objectMapper.readTree(response.getBody()).get("code").asText());
    }

    private byte[] pngImage(int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        assertTrue(ImageIO.write(image, "png", bytes));
        image.flush();
        return bytes.toByteArray();
    }

    private byte[] pngWithNoise(int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Random random = new Random(18005L);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) image.setRGB(x, y, random.nextInt(0x1000000));
        }
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        assertTrue(ImageIO.write(image, "png", bytes));
        image.flush();
        return bytes.toByteArray();
    }

    private byte[] jpegImage(int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        assertTrue(ImageIO.write(image, "jpeg", bytes));
        image.flush();
        return bytes.toByteArray();
    }

    private byte[] pngExactly(int targetSize) throws IOException {
        byte[] png = pngWithNoise(64, 64);
        int dataLength = targetSize - png.length - 12;
        if (dataLength < 8) throw new AssertionError("PNG fixture is too large to pad to the exact boundary.");
        byte[] chunkType = "tEXt".getBytes(StandardCharsets.US_ASCII);
        byte[] data = new byte[dataLength];
        byte[] keyword = "Comment\0".getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(keyword, 0, data, 0, keyword.length);
        Arrays.fill(data, keyword.length, data.length, (byte) 'A');
        CRC32 crc = new CRC32();
        crc.update(chunkType);
        crc.update(data);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream(targetSize);
        bytes.write(png, 0, png.length - 12);
        DataOutputStream output = new DataOutputStream(bytes);
        output.writeInt(dataLength);
        output.write(chunkType);
        output.write(data);
        output.writeInt((int) crc.getValue());
        bytes.write(png, png.length - 12, 12);
        return bytes.toByteArray();
    }
}
