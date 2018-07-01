package com.learning.android.popular_movies;

import android.arch.lifecycle.LiveData;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.learning.android.popular_movies.database.AppDataBase;
import com.learning.android.popular_movies.database.Movie;

import com.learning.android.popular_movies.utilities.AppExecutors;
import com.squareup.picasso.Picasso;

import java.util.Calendar;

import android.arch.lifecycle.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MovieDetailActivity extends AppCompatActivity {
    private TextView mMovieTitle;
    private ImageView mMoviePoster;
    private TextView mDate;
    private TextView mRunTime;
    private TextView mRating;
    private TextView mSynopsis;
    private Movie selectedMovie;
    private AppDataBase mDB;
    private MenuItem favoriteItem;
    private boolean isFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        setUpView();
        mDB = AppDataBase.getsInstance(getApplicationContext());
        Intent parentIntent = getIntent();
        if (parentIntent.hasExtra("MovieJSON")){
            String movieJson = parentIntent.getStringExtra("MovieJSON");
            Gson gson = new Gson();
            selectedMovie = gson.fromJson(movieJson, Movie.class);
            loadDataIntoView(selectedMovie);
            initFavoriteStatus();
        }
     }

    private void initFavoriteStatus() {
        ExecutorService es = Executors.newSingleThreadExecutor();
        es.submit(new Runnable() {
            @Override
            public void run() {
                isFavorite = mDB.movieDao().getFavoriteMovieByID(selectedMovie.getId()) != null;
            }
        });
        es.shutdown();
        try{
            es.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        }
        catch (Exception e){
            Log.e("Error", "onCreate: "+e.getMessage() );
        }
    }

    private void loadDataIntoView(Movie movie) {
        mMovieTitle.setText(movie.getTitle());
        Picasso.with(this)
                .load(movie.getFullPosterURL())
                .placeholder(R.drawable.movie_placeholder)
                .error(R.drawable.robot_msg_error)
                .into(mMoviePoster);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(movie.getReleaseDate());
        mDate.setText(String.valueOf(calendar.get(Calendar.YEAR)));
        mRunTime.setText(String.valueOf(movie.getRunTime()) + " min");
        mRating.setText(String.valueOf(movie.getVoteAverage())+"/10");
        mSynopsis.setText(movie.getOverview());
    }

    private void setUpView() {
        mMovieTitle = findViewById(R.id.tv_movie_title);

        mMoviePoster = findViewById(R.id.iv_movie_poster);
        mMoviePoster.getLayoutParams().width = this.getResources().getDisplayMetrics().heightPixels/4;
        mMoviePoster.getLayoutParams().height = this.getResources().getDisplayMetrics().heightPixels/4;

        mDate = findViewById(R.id.tv_release_date);
        mRunTime = findViewById(R.id.tv_run_time);
        mRating = findViewById(R.id.tv_rating);

        mSynopsis = findViewById(R.id.tv_synopsis);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.detail_menu, menu);
        if(menu != null) {
            favoriteItem = menu.findItem(R.id.action_favorite);
            favoriteItem.setIcon(isFavorite ? R.drawable.full_star : R.drawable.empty_star);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        favoriteItem = item;
        if(item.getItemId() == R.id.action_favorite)
        {
            SetUpObservableForFavoriteChanges();

            if (!isFavorite){
                AddMovieToFavoritesDB();
            }
            else{
                RemoveMovieFromFavoritesDB();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void SetUpObservableForFavoriteChanges() {
        LiveData<Movie> movie = mDB.movieDao().getMovieByID(selectedMovie.getId());
        movie.observe(this, new Observer<Movie>() {
            @Override
            public void onChanged(@Nullable Movie movie) {
                if (movie == null){
                    isFavorite = false;
                    favoriteItem.setIcon(R.drawable.empty_star);
                }
                else {
                    isFavorite = true;
                    favoriteItem.setIcon(R.drawable.full_star);
                }
            }
        });
    }

    private void AddMovieToFavoritesDB() {
        AppExecutors.getsInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                Movie movie = mDB.movieDao().getFavoriteMovieByID(selectedMovie.getId());
                if (movie==null){
                    mDB.movieDao().insertMovie(selectedMovie);
                }
            }
        });
    }

    private void RemoveMovieFromFavoritesDB() {
        AppExecutors.getsInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                Movie movie = mDB.movieDao().getFavoriteMovieByID(selectedMovie.getId());
                if (movie!=null){
                    mDB.movieDao().deleteMovie(movie);
                }
            }
        });
    }
}
