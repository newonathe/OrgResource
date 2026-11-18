package edu.orgresource.extended.integration;

import java.io.IOException;
import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import edu.orgresource.extended.domain.ReminderMode;
import edu.orgresource.extended.domain.ReminderOutcome;

/** Retrofit/Gson reminder transport that can reach only a loopback test server. */
@Component
public final class C3ReminderTransport {
    private final String baseUrl;
    private final String accountSid;
    private final String authToken;
    private final String fromNumber;
    private volatile LoopbackRetrofitFakeTwilioClient fakeClient;
    private volatile LoopbackRetrofitTwilioClient twilioClient;

    public C3ReminderTransport(@Value("${c1.reminder.base-url:http://127.0.0.1:9099/}") String baseUrl,
                               @Value("${c1.reminder.twilio-account-sid:}") String accountSid,
                               @Value("${c1.reminder.twilio-auth-token:}") String authToken,
                               @Value("${c1.reminder.twilio-from:+15555550123}") String fromNumber) {
        this.baseUrl = baseUrl;
        this.accountSid = accountSid;
        this.authToken = authToken;
        this.fromNumber = fromNumber;
    }

    public ReminderSendResult send(ReminderMode mode, ReminderDispatch dispatch) {
        if (mode == ReminderMode.DISABLED) return ReminderSendResult.unknown(null);
        try {
            if (mode == ReminderMode.FAKE_TWILIO) {
                FakeTwilioMessage response = fakeClient().send(new FakeTwilioMessage(
                        dispatch.destination(), fromNumber, dispatch.body()));
                if (response == null || response.status == null || response.status.isBlank())
                    return ReminderSendResult.unknown(200);
                if ("accepted".equalsIgnoreCase(response.status)) return ReminderSendResult.simulated(200);
                if ("rejected".equalsIgnoreCase(response.status)) return ReminderSendResult.rejected(200);
                return ReminderSendResult.unknown(200);
            }
            LoopbackRetrofitTwilioClient.TwilioProtocolResult response = twilioClient()
                    .sendResult(dispatch.destination(), fromNumber, dispatch.body());
            if (!response.successful()) {
                int status = response.httpStatus();
                return status >= 400 && status < 500 ? ReminderSendResult.rejected(status)
                        : ReminderSendResult.unknown(status);
            }
            TwilioMessageResponse message = response.message();
            if (message == null || message.sid == null || !message.sid.matches("^SM[0-9a-fA-F]{32}$"))
                return ReminderSendResult.unknown(response.httpStatus());
            return ReminderSendResult.providerAccepted(response.httpStatus(), message.sid);
        } catch (IOException | IllegalArgumentException uncertainOrInvalid) {
            return ReminderSendResult.unknown(null);
        }
    }

    private LoopbackRetrofitFakeTwilioClient fakeClient() {
        if (fakeClient == null) synchronized (this) {
            if (fakeClient == null) fakeClient = new LoopbackRetrofitFakeTwilioClient(URI.create(baseUrl));
        }
        return fakeClient;
    }

    private LoopbackRetrofitTwilioClient twilioClient() {
        if (twilioClient == null) synchronized (this) {
            if (twilioClient == null) {
                if (accountSid == null || !accountSid.matches("^AC[0-9a-fA-F]{32}$")
                        || authToken == null || authToken.isBlank()
                        || fromNumber == null || !fromNumber.matches("^\\+[1-9][0-9]{7,14}$"))
                    throw new IllegalArgumentException("Loopback Twilio protocol configuration is incomplete.");
                twilioClient = new LoopbackRetrofitTwilioClient(URI.create(baseUrl), accountSid, authToken);
            }
        }
        return twilioClient;
    }
}
