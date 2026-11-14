package app.reminders;

import app.entities.ReminderOutcome;

public final class ReminderSendResult {
    private final ReminderOutcome outcome;
    private final Integer httpStatus;
    private final String providerMessageSid;

    private ReminderSendResult(ReminderOutcome outcome, Integer httpStatus, String providerMessageSid) {
        this.outcome = outcome;
        this.httpStatus = httpStatus;
        this.providerMessageSid = providerMessageSid;
    }

    public static ReminderSendResult simulated(int status, String sid) {
        return new ReminderSendResult(ReminderOutcome.SIMULATED, status, sid);
    }

    public static ReminderSendResult rejected(Integer status) {
        return new ReminderSendResult(ReminderOutcome.REJECTED, status, null);
    }

    public static ReminderSendResult unknown(Integer status) {
        return new ReminderSendResult(ReminderOutcome.UNKNOWN, status, null);
    }

    public ReminderOutcome getOutcome() { return outcome; }
    public Integer getHttpStatus() { return httpStatus; }
    public String getProviderMessageSid() { return providerMessageSid; }
}
