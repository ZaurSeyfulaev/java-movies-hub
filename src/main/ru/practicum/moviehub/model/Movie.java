package ru.practicum.moviehub.model;

public class Movie {

    private int year;
    private String title;
    private int id;

    public Movie(int year, String title) {
        this.year = year;
        this.title = title;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}