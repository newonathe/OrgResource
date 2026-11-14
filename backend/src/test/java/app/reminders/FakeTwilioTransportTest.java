package app.reminders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import app.entities.ReminderOutcome;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FakeTwilioTransportTest {
    private static final String SERVICE_SID = "MG00000000000000000000000000000000";
    private static final String DESTINATION = "+15550001111";
    private static final String MESSAGE = "Fictional reminder message";
    private HttpServer server;
    private AtomicInteger requests;
    private AtomicReference<Integer> responseCode;
    private AtomicReference<String> responseBody;
    private AtomicReference<Map<String, String>> form;
    private AtomicReference<String> authorization;

    @Before
    public void startLoopbackFake() throws IOException {
        requests = new AtomicInteger();
        responseCode = new AtomicReference<Integer>(200);
        responseBody = new AtomicReference<String>();
        form = new AtomicReference<Map<String, String>>();
        authorization = new AtomicReference<String>();
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/fake_twilio/send", this::handle);
        server.start();
    }

    @After
    public void stopLoopbackFake() {
        if (server != null) server.stop(0);
    }

    @Test
    public void postsExpectedFormWithDummyAuthorizationAndParsesAcceptedReply() throws Exception {
        Map<String, String> echo = new HashMap<String, String>();
        echo.put("status", "accepted");
        echo.put("body", MESSAGE);
        echo.put("to", DESTINATION);
        echo.put("messaging_service_sid", SERVICE_SID);
        responseBody.set(new Gson().toJson(echo));

        ReminderSendResult result = transport().send(dispatch());

        assertEquals(ReminderOutcome.SIMULATED, result.getOutcome());
        assertEquals(Integer.valueOf(200), result.getHttpStatus());
        assertNull(result.getProviderMessageSid());
        assertEquals(1, requests.get());
        assertEquals("Basic ZHVtbXk6ZHVtbXk=", authorization.get());
        assertEquals(DESTINATION, form.get().get("To"));
        assertEquals(SERVICE_SID, form.get().get("MessagingServiceSid"));
        assertEquals(MESSAGE, form.get().get("Body"));
    }

    @Test
    public void providerRejectionIsRecordedAndAmbiguousServerFailureIsNotRetried() {
        responseCode.set(400);
        responseBody.set("rejected");
        ReminderSendResult rejected = transport().send(dispatch());
        assertEquals(ReminderOutcome.REJECTED, rejected.getOutcome());
        assertEquals(Integer.valueOf(400), rejected.getHttpStatus());
        assertEquals(1, requests.get());

        responseCode.set(503);
        ReminderSendResult unknown = transport().send(dispatch());
        assertEquals(ReminderOutcome.UNKNOWN, unknown.getOutcome());
        assertEquals(Integer.valueOf(503), unknown.getHttpStatus());
        assertEquals(2, requests.get());
    }

    @Test
    public void malformedSuccessResponseIsUnknownAndTransportRejectsNonLoopbackHost() {
        responseBody.set("not json");
        ReminderSendResult malformed = transport().send(dispatch());
        assertEquals(ReminderOutcome.UNKNOWN, malformed.getOutcome());
        assertEquals(Integer.valueOf(200), malformed.getHttpStatus());
        assertEquals(1, requests.get());

        try {
            new FakeTwilioTransport("http://example.com/", SERVICE_SID);
            fail("External host must be rejected before any network call.");
        } catch (IllegalArgumentException expected) {
            assertNotNull(expected.getMessage());
            assertTrue(expected.getMessage().contains("loopback"));
        }
    }

    private FakeTwilioTransport transport() {
        return new FakeTwilioTransport("http://127.0.0.1:" + server.getAddress().getPort() + "/", SERVICE_SID);
    }

    private ReminderDispatch dispatch() {
        return new ReminderDispatch(1L, DESTINATION, MESSAGE);
    }

    private void handle(HttpExchange exchange) throws IOException {
        requests.incrementAndGet();
        authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int count;
        while ((count = exchange.getRequestBody().read(buffer)) >= 0) bytes.write(buffer, 0, count);
        form.set(decodeForm(new String(bytes.toByteArray(), StandardCharsets.UTF_8)));
        byte[] response = responseBody.get() == null ? new byte[0]
                : responseBody.get().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(responseCode.get(), response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    private Map<String, String> decodeForm(String body) throws IOException {
        Map<String, String> values = new HashMap<String, String>();
        for (String pair : body.split("&")) {
            String[] parts = pair.split("=", 2);
            String key = URLDecoder.decode(parts[0], "UTF-8");
            String value = parts.length == 2 ? URLDecoder.decode(parts[1], "UTF-8") : "";
            values.put(key, value);
        }
        return values;
    }
}
