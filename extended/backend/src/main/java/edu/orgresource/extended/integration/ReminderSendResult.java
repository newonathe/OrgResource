package edu.orgresource.extended.integration;

import edu.orgresource.extended.domain.ReminderOutcome;

public record ReminderSendResult(ReminderOutcome outcome, Integer httpStatus, String providerMessageSid) {
    public static ReminderSendResult simulated(int status) { return new ReminderSendResult(ReminderOutcome.SIMULATED, status, null); }
    public static ReminderSendResult providerAccepted(int status, String sid) { return new ReminderSendResult(ReminderOutcome.PROVIDER_ACCEPTED, status, sid); }
    public static ReminderSendResult rejected(int status) { return new ReminderSendResult(ReminderOutcome.REJECTED, status, null); }
    public static ReminderSendResult unknown(Integer status) { return new ReminderSendResult(ReminderOutcome.UNKNOWN, status, null); }
}
