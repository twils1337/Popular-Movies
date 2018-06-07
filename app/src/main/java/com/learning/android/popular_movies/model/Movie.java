package com.learning.android.popular_movies.model;

import com.google.gson.annotations.SerializedName;

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
    @SerializedName("adult")
    private boolean isAdult;

    @SerializedName("backdrop_path")
    private String backdropPath;

    @SerializedName("genre_ids")
    private List<Integer> genreIds;

    private Integer id;

    @SerializedName("original_title")
    private String originalTitle;

    @SerializedName("original_language")
    private String originalLanguage;

    private String overview;

    private double popularity;

    @SerializedName("poster_path")
    private String posterPath;

    @SerializedName("release_date")
    private Date releaseFate;

    @SerializedName("vote_count")
    private Integer vote_count;

    private int runTime = -1;

    private String title;

    @SerializedName("video")
    private boolean isOnVideo;

    @SerializedName("vote_average")
    private double vote_average;


    public Movie(){
        this.releaseFate = new Date();
        this.genreIds = new ArrayList<>();
    }

    public boolean getIsAdult(){
        return isAdult;
    }

    public void setIsAdult(boolean isAdult){
        this.isAdult = isAdult;
    }


    public String getBackdropPath(){
        return this.backdropPath;
    }

    public void setBackdropPath(String path){
        this.backdropPath = path;
    }


    public List<Integer> getGenreIds(){
        return genreIds;
    }

    public void setGenreIds(List<Integer> genreIds){
        this.genreIds = genreIds;
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public String getOriginalLanguage(){
        return originalLanguage;
    }

    public void setOriginalLanguage(String lang){
        originalLanguage = lang;
    }

    public String getOriginalTitle(){
        return originalTitle;
    }

    public void setOriginalTitle(String title){
        originalTitle = title;
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

    public String getPosterPath(){
        return posterPath;
    }

    public void setPosterPath(String path){
        posterPath = path;
    }

    public Date getReleaseDate(){
        return releaseFate;
    }

    public void setReleaseDate(String date) throws ParseException{
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        releaseFate = df.parse(date);
    }
    public boolean isVideo(){
        return isOnVideo;
    }
    public void setisVideo(boolean isVideo){
        isOnVideo = isVideo;
    }

    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public double getVoteAverage(){
        return vote_average;
    }

    public void setVoteAverage(double vote_average){
        this.vote_average = vote_average;
    }

    public Integer getVoteCount() {
        return vote_count;
    }

    public void setVoteCount(Integer vote_count) {
        this.vote_count = vote_count;
    }

    public String getFullPosterURL(){
        if (posterPath != null || !posterPath .equals("")){
            return moviePosterURLPrefix + defaultThumbnailSize + posterPath;
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
