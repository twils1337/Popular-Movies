package com.example.android.popular_movies.model;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.List;

public class Movie {
    private final String moviePosterURLPrefix =  "http://image.tmdb.org/t/p/";
    // image sizes: "w92", "w154", "w185", "w342", "w500", "w780", or "original"
    private final String defaultThumbnailSize = "w185/";

    private boolean isAdult;
    private String backDropPath;
    private List<Integer> genreIDs;
    private Integer ID;
    private String originalLang;
    private String originalTitle;
    private String overview;
    private double popularity;
    private String posterPath ;
    private DateTime releaseDate ;
    private String title;
    private boolean isVideo;
    private double voteAvg;
    private Integer voteCount;
    private String API_KEY =
    

    public Movie(boolean isAdult, String backDropPath, List<Integer> genreIDs, Integer ID,
             String originalLang, String originalTitle, String overview, double popularity,
             String posterPath, String releaseDate, String title, boolean isVideo, double voteAvg,
             Integer voteCount) {
        this.isAdult = isAdult;
        this.backDropPath = backDropPath;
        this.genreIDs = genreIDs;
        this.ID = ID;
        this.originalLang = originalLang;
        this.originalTitle = originalTitle;
        this.overview = overview;
        this.popularity = popularity;
        this.posterPath = posterPath;
        this.releaseDate = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(releaseDate);
        this.title = title;
        this.isVideo = isVideo;
        this.voteAvg = voteAvg;
    }

    public boolean getIsAdult(){
        return isAdult;
    }

    public void setIsAdult(boolean isAdult){
        this.isAdult = isAdult;
    }


    public String getBackDropPath(){
        return this.backDropPath;
    }

    public void setBackDropPath(String path){
        this.backDropPath = path;
    }


    public List<Integer> getGenreIDs(){
        return genreIDs;
    }

    public void setGenreIDs(List<Integer> genreIDs){
        this.genreIDs = genreIDs;
    }

    public int getID(){
        return ID;
    }

    public void setID(int id){
        ID = id;
    }

    public String getOriginalLang(){
        return originalLang;
    }

    public void setOriginalLang(String lang){
        originalLang = lang;
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

    public String getReleaseDate(){
        return releaseDate.toString();
    }

    public void setReleaseDate(String date){
        this.releaseDate =  DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(date);
    }

    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public double getVoteAvg(){
        return voteAvg;
    }

    public void setVoteAvg(double voteAvg){
        this.voteAvg = voteAvg;
    }

    public Integer getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(Integer voteCount) {
        this.voteCount = voteCount;
    }

    public String getFullPosterURL(){
        if (posterPath != null){
            return moviePosterURLPrefix + defaultThumbnailSize + posterPath;
        }
        else{
            return null;
        }
    }
}
