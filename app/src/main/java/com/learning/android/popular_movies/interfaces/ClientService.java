package com.learning.android.popular_movies.interfaces;

import com.google.gson.JsonObject;
import com.learning.android.popular_movies.responses.MovieResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ClientService {
    @GET("/3/movie/popular")
    Call<MovieResponse> getPopularMovies(@Query("api_key") String api_key);

    @GET("/3/movie/top_rated")
    Call<MovieResponse> getTopRatedMovies(@Query("api_key") String api_key);

    @GET("/3/movie/{id}")
    Call<JsonObject> getSelectedMovie(@Path("id") int id, @Query("api_key") String api_key);
}
