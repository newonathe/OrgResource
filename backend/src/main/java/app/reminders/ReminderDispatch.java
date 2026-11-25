package app.reminders;

/** Private, short-lived payload created only after the durable claim and final eligibility check. */
public final class ReminderDispatch {
    private final Long allocationId;
    private final String destination;
    private final String body;

    public ReminderDispatch(Long allocationId, String destination, String body) {
        this.allocationId = allocationId;
        this.destination = destination;
        this.body = body;
    }

    public Long getAllocationId() { return allocationId; }
    public String getDestination() { return destination; }
    public String getBody() { return body; }
}
