package com.example.androidcode;

public class Movie {
    private String id;
    private String title;
    private String description;
    private String imageUrl;
    private double rating;

    public Movie() {} // Required for Firebase

    public Movie(String id, String title, String description, String imageUrl, double rating) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.rating = rating;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public double getRating() { return rating; }
}