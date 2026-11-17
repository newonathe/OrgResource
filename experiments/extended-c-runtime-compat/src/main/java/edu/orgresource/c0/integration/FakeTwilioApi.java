package edu.orgresource.c0.integration;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface FakeTwilioApi {
    @POST("fake/messages")
    Call<FakeTwilioMessage> send(@Body FakeTwilioMessage request);
}
