package app.reminders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import app.entities.ReminderOutcome;
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
import okhttp3.Credentials;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TwilioRestTransportTest {
    private static final String ACCOUNT_SID = "AC00000000000000000000000000000000";
    private static final String AUTH_TOKEN = "fictional-test-auth-token";
    private static final String FROM = "+15550001111";
    private static final String TO = "+15550002222";
    private static final String BODY = "Fictional protocol test message";
    private static final String MESSAGE_SID = "SM00000000000000000000000000000000";

    private HttpServer server;
    private AtomicInteger requestCount;
    private AtomicReference<Integer> responseCode;
    private AtomicReference<String> responseBody;
    private AtomicReference<String> requestMethod;
    private AtomicReference<String> requestPath;
    private AtomicReference<String> authorization;
    private AtomicReference<String> contentType;
    private AtomicReference<Map<String, String>> form;
    private AtomicReference<Long> responseDelayMs;
    private AtomicReference<Boolean> dropResponse;

    @Before
    public void startLoopbackProtocolServer() throws IOException {
        requestCount = new AtomicInteger();
        responseCode = new AtomicReference<Integer>(201);
        responseBody = new AtomicReference<String>("{\"sid\":\"" + MESSAGE_SID + "\",\"status\":\"queued\"}");
        requestMethod = new AtomicReference<String>();
        requestPath = new AtomicReference<String>();
        authorization = new AtomicReference<String>();
        contentType = new AtomicReference<String>();
        form = new AtomicReference<Map<String, String>>();
        responseDelayMs = new AtomicReference<Long>(0L);
        dropResponse = new AtomicReference<Boolean>(false);
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", this::handle);
        server.start();
    }

    @After
    public void stopLoopbackProtocolServer() {
        if (server != null) server.stop(0);
    }

    @Test
    public void sendsOfficialFormAndBasicAuthAndReportsOnlyProviderAcceptance() {
        ReminderSendResult result = transport().send(dispatch());

        assertEquals(ReminderOutcome.PROVIDER_ACCEPTED, result.getOutcome());
        assertEquals(Integer.valueOf(201), result.getHttpStatus());
        assertEquals(MESSAGE_SID, result.getProviderMessageSid());
        assertEquals("POST", requestMethod.get());
        assertEquals("/2010-04-01/Accounts/" + ACCOUNT_SID + "/Messages.json", requestPath.get());
        assertEquals(Credentials.basic(ACCOUNT_SID, AUTH_TOKEN), authorization.get());
        assertTrue(contentType.get().startsWith("application/x-www-form-urlencoded"));
        assertEquals(FROM, form.get().get("From"));
        assertEquals(TO, form.get().get("To"));
        assertEquals(BODY, form.get().get("Body"));
        assertEquals(1, requestCount.get());
    }

    @Test
    public void parsesDefinitiveHttpRejectionAndDoesNotReplayAmbiguousServerFailures() {
        responseCode.set(400);
        responseBody.set("{\"code\":21211,\"message\":\"Fictional invalid recipient\"}");
        ReminderSendResult rejected = transport().send(dispatch());
        assertEquals(ReminderOutcome.REJECTED, rejected.getOutcome());
        assertEquals(Integer.valueOf(400), rejected.getHttpStatus());
        assertEquals(1, requestCount.get());

        responseCode.set(503);
        responseBody.set("{\"code\":20429,\"message\":\"Fictional temporary failure\"}");
        ReminderSendResult unknown = transport().send(dispatch());
        assertEquals(ReminderOutcome.UNKNOWN, unknown.getOutcome());
        assertEquals(Integer.valueOf(503), unknown.getHttpStatus());
        assertEquals(2, requestCount.get());
    }

    @Test
    public void malformedAcceptedResponseAndLostResponseRemainUnknownWithoutRetry() {
        responseBody.set("not json");
        ReminderSendResult malformed = transport().send(dispatch());
        assertEquals(ReminderOutcome.UNKNOWN, malformed.getOutcome());
        assertEquals(Integer.valueOf(201), malformed.getHttpStatus());
        assertEquals(1, requestCount.get());

        dropResponse.set(true);
        ReminderSendResult lost = transport().send(dispatch());
        assertEquals(ReminderOutcome.UNKNOWN, lost.getOutcome());
        assertEquals(2, requestCount.get());
    }

    @Test
    public void timeoutIsBoundedAndUnsupportedHostsAreRejectedBeforeSending() {
        responseDelayMs.set(700L);
        ReminderSendResult timedOut = transport(1000L, 150L, 1000L, 300L).send(dispatch());
        assertEquals(ReminderOutcome.UNKNOWN, timedOut.getOutcome());
        assertEquals(1, requestCount.get());

        try {
            new TwilioRestTransport("https://example.invalid/", ACCOUNT_SID, AUTH_TOKEN, FROM);
            fail("Non-Twilio external hosts must be rejected.");
        } catch (IllegalArgumentException expected) {
            assertNotNull(expected.getMessage());
            assertTrue(expected.getMessage().contains("official HTTPS host"));
        }
    }

    private TwilioRestTransport transport() {
        return transport(1000L, 1000L, 1000L, 2000L);
    }

    private TwilioRestTransport transport(long connectMs, long readMs, long writeMs, long callMs) {
        return new TwilioRestTransport("http://127.0.0.1:" + server.getAddress().getPort() + "/",
                ACCOUNT_SID, AUTH_TOKEN, FROM, connectMs, readMs, writeMs, callMs);
    }

    private ReminderDispatch dispatch() {
        return new ReminderDispatch(1L, TO, BODY);
    }

    private void handle(HttpExchange exchange) throws IOException {
        requestCount.incrementAndGet();
        requestMethod.set(exchange.getRequestMethod());
        requestPath.set(exchange.getRequestURI().getRawPath());
        authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
        contentType.set(exchange.getRequestHeaders().getFirst("Content-Type"));
        ByteArrayOutputStream request = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int count;
        while ((count = exchange.getRequestBody().read(buffer)) != -1) request.write(buffer, 0, count);
        form.set(decodeForm(new String(request.toByteArray(), StandardCharsets.UTF_8)));

        if (Boolean.TRUE.equals(dropResponse.get())) {
            exchange.close();
            return;
        }
        if (responseDelayMs.get() > 0) {
            try { Thread.sleep(responseDelayMs.get()); }
            catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); }
        }
        byte[] response = responseBody.get().getBytes(StandardCharsets.UTF_8);
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
