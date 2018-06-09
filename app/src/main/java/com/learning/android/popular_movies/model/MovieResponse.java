package com.learning.android.popular_movies.model;

import java.util.List;

public class MovieResponse {
    int page;
    List<Movie> results;
    int total_pages;
    int total_results;

    public MovieResponse(){}

    public void setResults(List<Movie> movies){
        results = movies;
    }

    public List<Movie> getResults(){
        return results;
    }
}
