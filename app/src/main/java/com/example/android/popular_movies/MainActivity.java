package com.example.android.popular_movies;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.android.popular_movies.utilities.NetworkMovieUtils;

public class MainActivity extends AppCompatActivity {

    private MovieAdapter mAdapter;
    private RecyclerView mMoviesRV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMoviesRV = (RecyclerView) findViewById(R.id.rvMovies);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,2);
        mMoviesRV.setLayoutManager(gridLayoutManager);
        mMoviesRV.setHasFixedSize(true);
        mAdapter = new MovieAdapter();
        mAdapter.fetchMovieData(this);
        mMoviesRV.setAdapter(mAdapter);
    }
}
