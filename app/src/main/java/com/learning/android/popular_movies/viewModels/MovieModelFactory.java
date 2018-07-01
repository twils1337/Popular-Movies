package com.learning.android.popular_movies.viewModels;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.learning.android.popular_movies.database.AppDataBase;
import com.learning.android.popular_movies.viewModels.MovieViewModel;

public class MovieModelFactory extends ViewModelProvider.NewInstanceFactory {
    private final AppDataBase mDB;
    private final int mMovieID;


    public MovieModelFactory(AppDataBase mDB, int mMovieID) {
        this.mDB = mDB;
        this.mMovieID = mMovieID;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T) new MovieViewModel(mDB, mMovieID);
    }
}
