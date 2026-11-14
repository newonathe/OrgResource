package app.reminders;

import com.google.gson.annotations.SerializedName;

public final class FakeTwilioReply {
    @SerializedName("messaging_service_sid")
    private String messagingServiceSid;
    private String status;
    private String sid;
    private String body;
    private String to;

    public String getMessagingServiceSid() { return messagingServiceSid; }
    public String getStatus() { return status; }
    public String getSid() { return sid; }
    public String getBody() { return body; }
    public String getTo() { return to; }
}
