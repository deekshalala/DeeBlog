package com.example.deeblog;

import java.util.Date;

public class Notifications {
    String fromUser, toUser, type;
    String post;

    public Notifications() {
    }

    public Notifications(String fromUser, String toUser, String type, String post) {
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.type = type;
        this.post= post;

    }

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }

    public String getFromUser() {
        return fromUser;
    }

    public void setFromUser(String fromUser) {
        this.fromUser = fromUser;
    }

    public String getToUser() {
        return toUser;
    }

    public void setToUser(String toUser) {
        this.toUser = toUser;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

