package com.learning.android.popular_movies.responses;

import com.learning.android.popular_movies.models.Trailer;

import java.util.List;

public class TrailerResponse {
    int id;

    List<Trailer> results;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Trailer> getResults() {
        return results;
    }

    public void setResults(List<Trailer> results) {
        this.results = results;
    }
}
