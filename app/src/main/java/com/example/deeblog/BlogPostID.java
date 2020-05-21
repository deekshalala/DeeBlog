package com.example.deeblog;

import androidx.annotation.NonNull;

import com.google.firebase.database.Exclude;

public class BlogPostID {

    @Exclude
    public String BlogPostId;

    public <T extends BlogPostID> T withId(@NonNull final String id){
        this.BlogPostId=id;
        return  (T)this;
    }
}
