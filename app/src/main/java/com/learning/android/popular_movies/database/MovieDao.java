package com.learning.android.popular_movies.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface MovieDao {
    @Query("Select * From movies Order By releaseDate")
    LiveData<List<Movie>> fetchAllFavoriteMovies();

    @Query("Select * From movies Order By releaseDate")
    List<Movie> fetchAllFavoritedMovies();

    @Query("Select * From movies Where id = :id")
    LiveData<Movie> getMovieByID(int id);

    @Query("Select * From movies Where id = :id")
    Movie getFavoriteMovieByID(int id);

    @Insert
    void insertMovie(Movie movie);

    @Delete
    void deleteMovie(Movie movie);
}
