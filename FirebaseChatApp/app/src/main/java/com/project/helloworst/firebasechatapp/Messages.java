package com.project.helloworst.firebasechatapp;

public class Messages {

    private String  message,type,from;
    private long time;
    private boolean seen;


    public Messages(String message, long time, boolean seen, String type, String from) {
        this.message = message;
        this.time = time;
        this.seen = seen;
        this.type = type;
    }

    public Messages() {
    }

    public String getMessage() { return message; }

    public void setMessage(String message) { this.message = message; }

    public long getTime() { return time; }

    public void setTime(long time) { this.time = time; }

    public boolean getSeen() { return seen; }

    public void setSeen(boolean seen) { this.seen = seen; }

    public String getType() { return type; }

    public void setType(String type) { this.type = type; }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
