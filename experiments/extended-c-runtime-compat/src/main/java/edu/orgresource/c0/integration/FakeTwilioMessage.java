package edu.orgresource.c0.integration;

public class FakeTwilioMessage {
    public String to;
    public String from;
    public String body;
    public String id;
    public String status;

    public FakeTwilioMessage() { }

    public FakeTwilioMessage(String to, String from, String body) {
        this.to = to;
        this.from = from;
        this.body = body;
    }
}
