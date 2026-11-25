package app.reminders;

import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

/** Retrofit transport for the separately running course FakeTwilio app. */
public final class FakeTwilioTransport implements ReminderTransport {
    private static final String DUMMY_AUTHORIZATION = "Basic ZHVtbXk6ZHVtbXk=";

    private final FakeTwilioApi api;
    private final Gson gson;
    private final String messagingServiceSid;

    public FakeTwilioTransport(String baseUrl, String messagingServiceSid) {
        validateLoopbackBaseUrl(baseUrl);
        if (messagingServiceSid == null || messagingServiceSid.trim().isEmpty()) {
            throw new IllegalArgumentException("FakeTwilio MessagingServiceSid must be configured.");
        }
        this.messagingServiceSid = messagingServiceSid;
        this.gson = new Gson();
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .callTimeout(15, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false)
                .build();
        this.api = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(FakeTwilioApi.class);
    }

    @Override
    public ReminderSendResult send(ReminderDispatch dispatch) {
        final Response<ResponseBody> response;
        try {
            response = api.send(dispatch.getDestination(), messagingServiceSid,
                    dispatch.getBody(), DUMMY_AUTHORIZATION).execute();
        } catch (IOException | RuntimeException uncertain) {
            return ReminderSendResult.unknown(null);
        }

        int status = response.code();
        if (!response.isSuccessful()) {
            close(response.errorBody());
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

        final FakeTwilioReply reply;
        try {
            reply = gson.fromJson(rawBody, FakeTwilioReply.class);
        } catch (JsonParseException malformedResponse) {
            return ReminderSendResult.unknown(status);
        }
        if (reply == null) return ReminderSendResult.unknown(status);

        String replyStatus = reply.getStatus() == null ? "" : reply.getStatus().trim().toLowerCase(Locale.ROOT);
        if ("rejected".equals(replyStatus)) return ReminderSendResult.rejected(status);
        boolean validAcceptance = "accepted".equals(replyStatus)
                && dispatch.getDestination().equals(reply.getTo())
                && dispatch.getBody().equals(reply.getBody())
                && messagingServiceSid.equals(reply.getMessagingServiceSid())
                && (reply.getSid() == null || reply.getSid().length() <= 64);
        if (!validAcceptance) return ReminderSendResult.unknown(status);
        return ReminderSendResult.simulated(status, reply.getSid());
    }

    private void validateLoopbackBaseUrl(String baseUrl) {
        if (baseUrl == null || !baseUrl.endsWith("/")) {
            throw new IllegalArgumentException("FakeTwilio base URL must be absolute and end with '/'.");
        }
        try {
            URI uri = URI.create(baseUrl);
            String host = uri.getHost();
            if (!"http".equals(uri.getScheme()) || uri.getUserInfo() != null
                    || !("127.0.0.1".equals(host) || "localhost".equalsIgnoreCase(host))) {
                throw new IllegalArgumentException("FakeTwilio mode is restricted to a loopback HTTP endpoint.");
            }
        } catch (IllegalArgumentException invalidUrl) {
            throw new IllegalArgumentException("FakeTwilio mode requires a valid loopback HTTP base URL.");
        }
    }

    private void close(ResponseBody body) {
        if (body != null) {
            try { body.close(); } catch (RuntimeException ignored) { }
        }
    }
}
