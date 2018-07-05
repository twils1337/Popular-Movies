package com.learning.android.popular_movies.viewModels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import com.learning.android.popular_movies.database.AppDataBase;
import com.learning.android.popular_movies.database.Movie;
import android.support.annotation.NonNull;

import java.util.List;

public class MainViewModel extends AndroidViewModel{

    private LiveData<List<Movie>> movies;

    public MainViewModel(@NonNull Application application) {
        super(application);
        AppDataBase dataBase = AppDataBase.getsInstance(this.getApplication());
        movies = dataBase.movieDao().fetchAllFavoriteMovies();
    }

    public LiveData<List<Movie>> getMovies(){
        return movies;
    }
}
