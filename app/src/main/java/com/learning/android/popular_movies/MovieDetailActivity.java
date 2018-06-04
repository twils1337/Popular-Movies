package com.learning.android.popular_movies;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import com.learning.android.popular_movies.model.Movie;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.squareup.picasso.Picasso;

import java.io.IOException;

public class MovieDetailActivity extends AppCompatActivity {
    private TextView mMovieTitle;
    private ImageView mMoviePoster;
    private TextView mDate;
    private TextView mRunTime;
    private TextView mRating;
    private TextView mSynopsis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        setUpView();

        Intent parentIntent = getIntent();
        if (parentIntent.hasExtra("MovieJSON")){
            String movieJson = parentIntent.getStringExtra("MovieJSON");
            ObjectMapper om = new ObjectMapper();
            om.registerModule(new JodaModule());
            try{
                Movie movie = om.readValue(movieJson, Movie.class);
                loadDataIntoView(movie);

            } catch (IOException e){
                e.printStackTrace();
            }

        }
     }

    private void loadDataIntoView(Movie movie) {
        mMovieTitle.setText(movie.getTitle());
        Picasso.with(this)
                .load(movie.getFullPosterURL())
                .into(mMoviePoster);
        mDate.setText(String.valueOf(movie.getReleaseDate().getYear()));
        mRunTime.setText(String.valueOf(movie.getRunTime()) + " min");
        mRating.setText(String.valueOf(movie.getVoteAvg())+"/10");
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
}
