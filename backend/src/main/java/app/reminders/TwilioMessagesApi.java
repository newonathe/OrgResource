package app.reminders;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Path;

/** Retrofit contract for Twilio's Account Messages REST resource. */
public interface TwilioMessagesApi {
    @FormUrlEncoded
    @POST("2010-04-01/Accounts/{accountSid}/Messages.json")
    Call<ResponseBody> createMessage(@Path("accountSid") String accountSid,
            @Field("From") String from,
            @Field("To") String to,
            @Field("Body") String body,
            @retrofit2.http.Header("Authorization") String authorization);
}
