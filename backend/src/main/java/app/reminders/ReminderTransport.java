package app.reminders;

public interface ReminderTransport {
    ReminderSendResult send(ReminderDispatch dispatch);
}
