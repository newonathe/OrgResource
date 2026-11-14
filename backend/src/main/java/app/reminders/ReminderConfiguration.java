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
            @Value("${orgresource.reminders.fake-twilio.message-service-sid:MG00000000000000000000000000000000}") String fakeServiceSid) {
        if (mode == ReminderMode.DISABLED) return new DisabledReminderTransport();
        if (mode == ReminderMode.FAKE_TWILIO) return new FakeTwilioTransport(fakeBaseUrl, fakeServiceSid);
        throw new IllegalStateException("TWILIO_VIRTUAL is not enabled: no real-provider adapter or live send is authorized in this phase.");
    }
}
