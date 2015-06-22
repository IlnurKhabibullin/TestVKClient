package com.example.testvkclient;

/**
 * Created by Ильнур on 17.06.2015.
 */
public class Post {

    private final String author_name;
    private final String photo_50;
    private final Long date;
    private final String text;
    private final String[] post_photos;
    private final int likesCount;

    public Post(String author_name, Long date, String text, String photo_50, String[] post_photos, int likesCount) {
        this.author_name = author_name;
        this.date = date;
        this.text = text;
        this.photo_50 = photo_50;
        this.post_photos = post_photos;
        this.likesCount = likesCount;
    }

    public String getAuthor_name() {
        return author_name;
    }

    public Long getDate() {
        return date;
    }

    public String getText() {
        return text;
    }

    public String getPhoto_50() {
        return photo_50;
    }

    public String[] getPost_photos() {
        return post_photos;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public String toString() {
        return text;
    }
}
