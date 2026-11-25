package app.reminders;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import app.entities.ReminderOutcome;

/** Official Messages API protocol transport; no logging or automatic request retry is enabled. */
public final class TwilioRestTransport implements ReminderTransport {
    private final TwilioMessagesApi api;
    private final Gson gson;
    private final String accountSid;
    private final String authorization;
    private final String fromNumber;

    public TwilioRestTransport(String baseUrl, String accountSid, String authToken, String fromNumber) {
        this(baseUrl, accountSid, authToken, fromNumber, 5000L, 10000L, 10000L, 15000L);
    }

    TwilioRestTransport(String baseUrl, String accountSid, String authToken, String fromNumber,
            long connectTimeoutMs, long readTimeoutMs, long writeTimeoutMs, long callTimeoutMs) {
        validateBaseUrl(baseUrl);
        if (accountSid == null || !accountSid.matches("^AC[0-9a-fA-F]{32}$")) {
            throw new IllegalArgumentException("A valid Twilio Account SID is required.");
        }
        if (authToken == null || authToken.trim().isEmpty()) {
            throw new IllegalArgumentException("A Twilio Auth Token is required.");
        }
        if (fromNumber == null || !fromNumber.matches("^\\+[1-9][0-9]{7,14}$")) {
            throw new IllegalArgumentException("A valid fictional or authorized Twilio From number is required.");
        }
        this.accountSid = accountSid;
        this.fromNumber = fromNumber;
        this.authorization = Credentials.basic(accountSid, authToken);
        this.gson = new Gson();
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(connectTimeoutMs, TimeUnit.MILLISECONDS)
                .readTimeout(readTimeoutMs, TimeUnit.MILLISECONDS)
                .writeTimeout(writeTimeoutMs, TimeUnit.MILLISECONDS)
                .callTimeout(callTimeoutMs, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(false)
                .followRedirects(false)
                .followSslRedirects(false)
                .build();
        this.api = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(TwilioMessagesApi.class);
    }

    @Override
    public ReminderSendResult send(ReminderDispatch dispatch) {
        final Response<ResponseBody> response;
        try {
            response = api.createMessage(accountSid, fromNumber, dispatch.getDestination(),
                    dispatch.getBody(), authorization).execute();
        } catch (IOException | RuntimeException uncertain) {
            return ReminderSendResult.unknown(null);
        }

        int status = response.code();
        if (!response.isSuccessful()) {
            parseError(response.errorBody());
            return status >= 400 && status < 500
                    ? ReminderSendResult.rejected(status)
                    : ReminderSendResult.unknown(status);
        }

        String rawBody;
        try {
            ResponseBody body = response.body();
            if (body == null) return ReminderSendResult.unknown(status);
            rawBody = body.string();
        } catch (IOException unreadableBody) {
            return ReminderSendResult.unknown(status);
        }

        try {
            TwilioMessageReply reply = gson.fromJson(rawBody, TwilioMessageReply.class);
            if (reply == null || reply.sid == null || !reply.sid.matches("^SM[0-9a-fA-F]{32}$")) {
                return ReminderSendResult.unknown(status);
            }
            return ReminderSendResult.providerAccepted(status, reply.sid);
        } catch (JsonParseException malformedResponse) {
            return ReminderSendResult.unknown(status);
        }
    }

    private void parseError(ResponseBody body) {
        if (body == null) return;
        try {
            String rawBody = body.string();
            if (rawBody != null && !rawBody.trim().isEmpty()) {
                gson.fromJson(rawBody, TwilioErrorReply.class);
            }
        } catch (IOException | JsonParseException ignored) {
            // HTTP status determines rejection; provider text is never logged or persisted.
        } finally {
            body.close();
        }
    }

    private void validateBaseUrl(String baseUrl) {
        if (baseUrl == null || !baseUrl.endsWith("/")) {
            throw new IllegalArgumentException("Twilio base URL must be absolute and end with '/'.");
        }
        try {
            URI uri = URI.create(baseUrl);
            String host = uri.getHost();
            boolean loopback = "http".equals(uri.getScheme())
                    && ("127.0.0.1".equals(host) || "localhost".equalsIgnoreCase(host));
            boolean official = "https".equals(uri.getScheme())
                    && "api.twilio.com".equalsIgnoreCase(host)
                    && (uri.getPort() == -1 || uri.getPort() == 443);
            if (uri.getUserInfo() != null || uri.getQuery() != null || uri.getFragment() != null
                    || !(loopback || official)) {
                throw new IllegalArgumentException("Twilio base URL must use the official HTTPS host or a loopback test server.");
            }
        } catch (IllegalArgumentException invalidUrl) {
            throw new IllegalArgumentException("Twilio base URL must use the official HTTPS host or a loopback test server.");
        }
    }

    private static final class TwilioMessageReply {
        private String sid;
    }

    private static final class TwilioErrorReply {
        @SuppressWarnings("unused") private Integer code;
        @SuppressWarnings("unused") private String message;
        @SuppressWarnings("unused") private String more_info;
    }
}
