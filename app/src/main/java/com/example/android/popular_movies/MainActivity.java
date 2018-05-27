package com.example.android.popular_movies;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {

    private MovieAdapter mAdapter;
    private RecyclerView mMoviesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        mMoviesList = findViewById(R.id.rvMovies);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,2);
        mMoviesList.setLayoutManager(gridLayoutManager);
        mMoviesList.setHasFixedSize(true);
        mAdapter = new MovieAdapter();
        mAdapter.fetchMovieData();
        mMoviesList.setAdapter(mAdapter);
    }
}
