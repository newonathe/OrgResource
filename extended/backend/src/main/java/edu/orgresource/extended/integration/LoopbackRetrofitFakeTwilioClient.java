package edu.orgresource.extended.integration;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;

import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/** No-provider simulation client; it accepts only loopback HTTP URLs. */
public final class LoopbackRetrofitFakeTwilioClient {
    private final FakeTwilioApi api;

    public LoopbackRetrofitFakeTwilioClient(URI baseUri) {
        requireLoopback(baseUri);
        String baseUrl = baseUri.toString();
        if (!baseUrl.endsWith("/")) baseUrl += "/";
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(2))
                .readTimeout(Duration.ofSeconds(3))
                .writeTimeout(Duration.ofSeconds(3))
                .callTimeout(Duration.ofSeconds(5))
                .retryOnConnectionFailure(false)
                .followRedirects(false)
                .followSslRedirects(false)
                .build();
        this.api = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().disableHtmlEscaping().create()))
                .build()
                .create(FakeTwilioApi.class);
    }

    public FakeTwilioMessage send(FakeTwilioMessage request) throws IOException {
        Response<FakeTwilioMessage> response = api.send(request).execute();
        if (!response.isSuccessful() || response.body() == null) {
            throw new IOException("Loopback FakeTwilio returned HTTP " + response.code() + ".");
        }
        return response.body();
    }

    private static void requireLoopback(URI uri) {
        if (uri == null || !"http".equalsIgnoreCase(uri.getScheme()) || !"127.0.0.1".equals(uri.getHost())
                || uri.getUserInfo() != null || uri.getQuery() != null || uri.getFragment() != null) {
            throw new IllegalArgumentException("FakeTwilio compatibility tests must use a plain HTTP URL at 127.0.0.1.");
        }
    }
}
