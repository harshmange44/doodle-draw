package com.hrsh.doodledraw;

public class DrawingModel {
    String uid;
    String email;
    String url;

    public DrawingModel(String uid, String email, String url) {
        this.uid = uid;
        this.email = email;
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String name) {
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}

