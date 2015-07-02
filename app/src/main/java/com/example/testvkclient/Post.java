package com.example.testvkclient;

import android.os.Parcel;
import android.os.Parcelable;

public class Post implements Parcelable {
    private String author_name;
    private String author_avatar;
    private Long date;
    private int likesCount;
    private Boolean post_type; //false - post, true - repost
    private String text;
    private String repost_text;
    private String[] post_photos;
    private String repost_source_name;
    private String repost_source_avatar;

//author name
    public String getAuthor_name() {
        return author_name;
    }

    public void setAuthor_name(String author_name) {
        this.author_name = author_name;
    }
//author avatar
    public String getAuthor_avatar() {
        return author_avatar;
    }

    public void setAuthor_avatar(String author_avatar) {
        this.author_avatar = author_avatar;
    }
//post date
    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date * 1000l;
    }
//post text
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
//post type
    public Boolean getPost_type() {
        return post_type;
    }

    public void setPost_type(Boolean post_type) {
        this.post_type = post_type;
    }
//
    public String[] getPost_photos() {
        return post_photos;
    }

    public void setPost_photos(String[] post_photos) {
        this.post_photos = post_photos;
    }
//post likes
    public int getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }
//if post is repost, then name of repost source else it's null and shouldn't be used, as the fields below
    public String getRepost_source_name() {
        return repost_source_name;
    }

    public void setRepost_source_name(String repost_source_name) {
        this.repost_source_name = repost_source_name;
    }
//repost source avatar
    public String getRepost_source_avatar() {
        return repost_source_avatar;
    }

    public void setRepost_source_avatar(String repost_source_avatar) {
        this.repost_source_avatar = repost_source_avatar;
    }
//repost text
    public String getRepost_text() {
        return repost_text;
    }

    public void setRepost_text(String repost_text) {
        this.repost_text = repost_text;
    }
//toString
    public String toString() {
        return text;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(author_name);
        dest.writeString(author_avatar);
        dest.writeString(text);
        dest.writeString(repost_source_name);
        dest.writeString(repost_source_avatar);
        dest.writeString(repost_text);
        dest.writeStringArray(post_photos);
        dest.writeValue(post_type);
        dest.writeInt(likesCount);
        dest.writeLong(date);
    }
}
