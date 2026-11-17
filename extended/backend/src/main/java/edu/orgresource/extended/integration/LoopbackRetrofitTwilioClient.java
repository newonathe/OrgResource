package edu.orgresource.extended.integration;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.time.Duration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/** Loopback-only protocol client used by the bounded C1 HTTP experiment. */
public final class LoopbackRetrofitTwilioClient {
    private final TwilioMessagesApi api;

    public LoopbackRetrofitTwilioClient(URI baseUri, String accountSid, String authToken) {
        requireLoopbackHttp(baseUri);
        if (isBlank(accountSid) || isBlank(authToken)) {
            throw new IllegalArgumentException("Local protocol credentials must be provided in process memory.");
        }
        String baseUrl = baseUri.toString();
        if (!baseUrl.endsWith("/")) baseUrl += "/";
        OkHttpClient http = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(2))
                .readTimeout(Duration.ofSeconds(3))
                .writeTimeout(Duration.ofSeconds(3))
                .callTimeout(Duration.ofSeconds(5))
                .retryOnConnectionFailure(false)
                .addInterceptor(chain -> {
                    Request request = chain.request().newBuilder()
                            .header("Authorization", Credentials.basic(accountSid, authToken))
                            .build();
                    return chain.proceed(request);
                })
                .build();
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        this.api = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(http)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(TwilioMessagesApi.class);
        this.accountSid = accountSid;
    }

    private final String accountSid;

    public TwilioMessageResponse send(String to, String from, String body) throws IOException {
        TwilioProtocolResult result = sendResult(to, from, body);
        if (!result.successful() || result.message() == null)
            throw new IOException("Loopback provider protocol returned HTTP " + result.httpStatus() + ".");
        return result.message();
    }

    public TwilioProtocolResult sendResult(String to, String from, String body) throws IOException {
        Response<TwilioMessageResponse> response = api.createMessage(accountSid, to, from, body).execute();
        return new TwilioProtocolResult(response.code(), response.isSuccessful(), response.body());
    }

    public record TwilioProtocolResult(int httpStatus, boolean successful, TwilioMessageResponse message) { }

    private static void requireLoopbackHttp(URI uri) {
        if (uri == null || !"http".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                || uri.getUserInfo() != null || uri.getQuery() != null || uri.getFragment() != null) {
            throw new IllegalArgumentException("C1 provider stubs must use a plain loopback HTTP base URI.");
        }
        try {
            InetAddress address = InetAddress.getByName(uri.getHost());
            if (!address.isLoopbackAddress()) {
                throw new IllegalArgumentException("C1 provider stubs are restricted to loopback addresses.");
            }
        } catch (UnknownHostException exception) {
            throw new IllegalArgumentException("C1 provider stub host could not be resolved.");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
