package com.learning.android.popular_movies.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Entity(tableName = "movies")
public class Movie {
    @Ignore
    private final String moviePosterURLPrefix =  "http://image.tmdb.org/t/p/";
    // image sizes: "w92", "w154", "w185", "w342", "w500", "w780", or "original"

    @Ignore
    private final String defaultThumbnailSize = "w185/";
    
    @SerializedName("adult")
    private boolean isAdult;

    @SerializedName("backdrop_path")
    private String backdropPath;

    @SerializedName("genre_ids")
    private List<Integer> genreIds;

    @PrimaryKey
    private int id;

    @SerializedName("original_title")
    private String originalTitle;

    @SerializedName("original_language")
    private String originalLanguage;

    private String overview;

    private double popularity;

    @SerializedName("poster_path")
    private String posterPath;

    @SerializedName("release_date")
    private Date releaseDate;

    @SerializedName("vote_count")
    private Integer voteCount;

    private int runTime = -1;

    private String title;

    @SerializedName("video")
    private boolean isOnVideo;

    @SerializedName("vote_average")
    private double voteAverage;

    @Ignore
    public Movie(){
        this.releaseDate = new Date();
        this.genreIds = new ArrayList<>();
    }

    public Movie(Integer id, boolean isAdult, String backdropPath, List<Integer> genreIds,
                 String originalTitle, String originalLanguage, String overview, double popularity,
                 String posterPath, Date releaseDate, Integer voteCount, int runTime, String title,
                 boolean isOnVideo, double voteAverage) {
        this.isAdult = isAdult;
        this.backdropPath = backdropPath;
        this.genreIds = genreIds;
        this.id = id;
        this.originalTitle = originalTitle;
        this.originalLanguage = originalLanguage;
        this.overview = overview;
        this.popularity = popularity;
        this.posterPath = posterPath;
        this.releaseDate = releaseDate;
        this.voteCount = voteCount;
        this.runTime = runTime;
        this.title = title;
        this.isOnVideo = isOnVideo;
        this.voteAverage = voteAverage;
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
        return releaseDate;
    }

    public void setReleaseDate(String date) throws ParseException{
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        releaseDate = df.parse(date);
    }

    public boolean isOnVideo() {
        return isOnVideo;
    }

    public void setIsVideo(boolean isVideo){
        isOnVideo = isVideo;
    }

    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public double getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(double vote_average){
        this.voteAverage = vote_average;
    }

    public Integer getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(Integer vote_count) {
        this.voteCount = vote_count;
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
