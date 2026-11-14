package app.reminders;

public final class DisabledReminderTransport implements ReminderTransport {
    @Override
    public ReminderSendResult send(ReminderDispatch dispatch) {
        return ReminderSendResult.unknown(null);
    }
}
