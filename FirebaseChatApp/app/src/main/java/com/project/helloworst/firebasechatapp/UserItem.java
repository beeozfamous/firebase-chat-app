package com.project.helloworst.firebasechatapp;

import android.support.annotation.Keep;

import java.io.Serializable;

@Keep
public class UserItem implements Serializable {
    private String name;
    private String status;
    private String image;
    private String thumb_image;

    public UserItem() {

    }

    public UserItem(String name, String status, String image) {
        this.name = name;
        this.status = status;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }
}
