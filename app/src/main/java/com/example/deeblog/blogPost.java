package com.example.deeblog;

import androidx.fragment.app.FragmentManager;

import java.sql.Timestamp;
import java.util.Date;

public class blogPost extends BlogPostID {
    public String user, image, title, desc, thumb;
    Date timestamp;

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }



    public blogPost() {
    }

    public blogPost(String user, String image, String title, String desc, String thumb, Date timestamp) {
        this.user = user;
        this.image = image;
        this.title = title;
        this.desc = desc;
        this.thumb = thumb;
        this.timestamp = timestamp;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

}
