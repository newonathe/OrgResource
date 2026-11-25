package app.reminders;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface FakeTwilioApi {
    @FormUrlEncoded
    @POST("fake_twilio/send")
    Call<ResponseBody> send(@Field("To") String to,
                            @Field("MessagingServiceSid") String messagingServiceSid,
                            @Field("Body") String body,
                            @Header("Authorization") String authorization);
}
