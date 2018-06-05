package com.learning.android.popular_movies.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Movie {
    private final String moviePosterURLPrefix =  "http://image.tmdb.org/t/p/";
    // image sizes: "w92", "w154", "w185", "w342", "w500", "w780", or "original"
    private final String defaultThumbnailSize = "w185/";
    private Integer vote_count;
    private Integer id;
    private boolean video;
    private double vote_average;
    private String title;
    private double popularity;
    private String poster_path;
    private String original_language;
    private String original_title;
    private List<Integer> genre_ids;
    private String backdrop_path;
    private boolean adult;
    private String overview;
    private Date release_date;
    private int runTime = -1;

    public Movie(){
        this.release_date = new Date();
        this.genre_ids = new ArrayList<>();
    }

    public boolean getIsAdult(){
        return adult;
    }

    public void setIsAdult(boolean isAdult){
        this.adult = isAdult;
    }


    public String getBackdrop_path(){
        return this.backdrop_path;
    }

    public void setBackdrop_path(String path){
        this.backdrop_path = path;
    }


    public List<Integer> getGenre_ids(){
        return genre_ids;
    }

    public void setGenre_ids(List<Integer> genre_ids){
        this.genre_ids = genre_ids;
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public String getOriginal_language(){
        return original_language;
    }

    public void setOriginal_language(String lang){
        original_language = lang;
    }

    public String getOriginal_title(){
        return original_title;
    }

    public void setOriginal_title(String title){
        original_title = title;
    }

    public String getOverview(){
        return overview;
    }

    public void setOverview(String overview){
        this.overview = overview;
    }

    public double getPopularity(){
        return popularity;
    }

    public void setPopularity(double popularity){
        this.popularity = popularity;
    }

    public String getPoster_path(){
        return poster_path;
    }

    public void setPoster_path(String path){
        poster_path = path;
    }

    public Date getRelease_date(){
        return release_date;
    }

    public void setRelease_date(String date) throws ParseException{
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        release_date = df.parse(date);
    }
    public boolean isVideo(){
        return video;
    }
    public void setVideo(boolean isVideo){
        video = isVideo;
    }

    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public double getVote_average(){
        return vote_average;
    }

    public void setVote_average(double vote_average){
        this.vote_average = vote_average;
    }

    public Integer getVote_count() {
        return vote_count;
    }

    public void setVote_count(Integer vote_count) {
        this.vote_count = vote_count;
    }

    public String getFullPosterURL(){
        if (poster_path != null){
            return moviePosterURLPrefix + defaultThumbnailSize + poster_path;
        }
        else{
            return null;
        }
    }

    public int getRunTime(){
        return runTime;
    }

    public void setRunTime(int runTime) {
        this.runTime = runTime;
    }
}
