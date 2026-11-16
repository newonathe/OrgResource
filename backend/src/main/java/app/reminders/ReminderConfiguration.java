package app.reminders;

import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReminderConfiguration {
    @Bean
    public ReminderMode reminderMode(@Value("${orgresource.reminders.mode:DISABLED}") String configuredMode) {
        try {
            return ReminderMode.valueOf(configuredMode.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException invalidMode) {
            throw new IllegalStateException("orgresource.reminders.mode must be DISABLED, FAKE_TWILIO, or TWILIO_VIRTUAL.");
        }
    }

    @Bean
    public ReminderTransport reminderTransport(
            ReminderMode mode,
            @Value("${orgresource.reminders.fake-twilio.base-url:http://127.0.0.1:9997/}") String fakeBaseUrl,
            @Value("${orgresource.reminders.fake-twilio.message-service-sid:MG00000000000000000000000000000000}") String fakeServiceSid,
            @Value("${ORGRESOURCE_TWILIO_BASE_URL:}") String twilioBaseUrl,
            @Value("${ORGRESOURCE_TWILIO_ACCOUNT_SID:}") String accountSid,
            @Value("${ORGRESOURCE_TWILIO_AUTH_TOKEN:}") String authToken,
            @Value("${ORGRESOURCE_TWILIO_FROM_NUMBER:}") String fromNumber) {
        if (mode == ReminderMode.DISABLED) return new DisabledReminderTransport();
        if (mode == ReminderMode.FAKE_TWILIO) return new FakeTwilioTransport(fakeBaseUrl, fakeServiceSid);
        if (mode == ReminderMode.TWILIO_VIRTUAL) {
            return new TwilioRestTransport(twilioBaseUrl, accountSid, authToken, fromNumber);
        }
        throw new IllegalStateException("No supported reminder transport is configured.");
    }
}
