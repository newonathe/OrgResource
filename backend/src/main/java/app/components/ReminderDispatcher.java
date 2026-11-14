package app.components;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import app.reminders.ReminderDispatch;
import app.reminders.ReminderMode;
import app.reminders.ReminderSendResult;
import app.reminders.ReminderTransport;

/** Runs claim, pre-send validation, external HTTP and outcome persistence as separate short steps. */
@Component
public class ReminderDispatcher {
    @Autowired private ReminderComponent reminders;
    @Autowired private ReminderTransport transport;
    @Autowired private ReminderMode mode;

    public void dispatchOne(Long allocationId) {
        if (mode == ReminderMode.DISABLED || !reminders.claimOne(allocationId)) return;
        ReminderDispatch dispatch = reminders.prepareForDispatch(allocationId);
        if (dispatch == null) return;

        ReminderSendResult result;
        try {
            result = transport.send(dispatch);
        } catch (RuntimeException uncertainFailure) {
            result = ReminderSendResult.unknown(null);
        }
        reminders.recordOutcome(allocationId, result);
    }
}
