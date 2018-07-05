package com.learning.android.popular_movies.viewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.learning.android.popular_movies.database.AppDataBase;
import com.learning.android.popular_movies.database.Movie;

public class MovieViewModel extends ViewModel{
    private LiveData<Movie> movie;

    public MovieViewModel(AppDataBase dataBase, int movieID){
        movie = dataBase.movieDao().getMovieByID(movieID);
    }

    public LiveData<Movie> getMovie(){
        return movie;
    }
}
