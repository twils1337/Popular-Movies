package com.learning.android.popular_movies.responses;

import com.learning.android.popular_movies.database.Movie;

import java.util.List;

public class MovieResponse {
    int page;
    private List<Movie> results;
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
