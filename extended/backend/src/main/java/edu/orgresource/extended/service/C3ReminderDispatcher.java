package edu.orgresource.extended.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import edu.orgresource.extended.domain.ReminderMode;
import edu.orgresource.extended.integration.C3ReminderTransport;
import edu.orgresource.extended.integration.ReminderDispatch;
import edu.orgresource.extended.integration.ReminderSendResult;

@Component
public class C3ReminderDispatcher {
    private final C3ReminderService reminders;
    private final C3ReminderTransport transport;

    public C3ReminderDispatcher(C3ReminderService reminders, C3ReminderTransport transport) {
        this.reminders = reminders;
        this.transport = transport;
    }

    public void dispatchOne(long allocationId, LocalDateTime sampledNow, ReminderMode mode) {
        dispatchOne(allocationId, sampledNow, mode, transport);
    }

    public void dispatchOne(long allocationId, LocalDateTime sampledNow, ReminderMode mode, C3ReminderTransport selectedTransport) {
        if (mode == ReminderMode.DISABLED || !reminders.claimOne(allocationId, sampledNow)) return;
        ReminderDispatch dispatch = reminders.prepareForDispatch(allocationId, sampledNow);
        if (dispatch == null) return;
        ReminderSendResult result;
        try { result = selectedTransport.send(mode, dispatch); }
        catch (RuntimeException uncertainFailure) { result = ReminderSendResult.unknown(null); }
        reminders.recordOutcome(allocationId, result);
    }
}
