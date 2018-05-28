package com.example.android.popular_movies;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.example.android.popular_movies.model.Movie;
import com.example.android.popular_movies.utilities.NetworkMovieUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private MovieAdapter mAdapter;
    private RecyclerView mMoviesRV;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MovieQueryTask mqt = new MovieQueryTask(this);
        mqt.execute();
        mMoviesRV = (RecyclerView) findViewById(R.id.rvMovies);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,2);
        mMoviesRV.setLayoutManager(gridLayoutManager);
        mMoviesRV.setHasFixedSize(true);
    }

    private List<Movie> createMovieList(Context context, String jsonResult) {
        List<Movie> movies = new ArrayList<>();
        try {
            JSONObject json = new JSONObject(jsonResult);
            String resultsKey = "results";
            JSONArray resultsJson = json.getJSONArray(resultsKey);
            for (int i = 0; i < resultsJson.length(); ++i){
                JSONObject movieJson = resultsJson.getJSONObject(i);
                Movie movie = new Movie(
                        movieJson.getBoolean("adult"),
                        movieJson.getString("backdrop_path"),
                        getListFromJsonArr(movieJson.getJSONArray("genre_ids")),
                        movieJson.getInt("id"),
                        movieJson.getString("original_language"),
                        movieJson.getString("original_title"),
                        movieJson.getString("overview"),
                        movieJson.getDouble("popularity"),
                        movieJson.getString("poster_path"),
                        movieJson.getString("release_date"),
                        movieJson.getString("title"),
                        movieJson.getBoolean("video"),
                        movieJson.getDouble("vote_average"),
                        movieJson.getInt("vote_count")
                );
                movie.setAPI_KEY(context);
                movies.add(movie);
            }
        } catch (JSONException e) {
            Log.e("Json Error", "doInBackground: " + e.getMessage());
        }
        return movies;
    }

    private List<Integer> getListFromJsonArr(JSONArray arr)
            throws JSONException{
        List<Integer> genreIDs = new ArrayList<>();
        for (int i = 0; i < arr.length(); ++i){
            genreIDs.add(arr.getInt(i));
        }
        return genreIDs;
    }

    public class MovieQueryTask extends AsyncTask<URL, Void, String> {
        private Context context;

        public MovieQueryTask(Context context){
            this.context = context;
        }

        @Override
        protected String doInBackground(URL... urls) {
            return NetworkMovieUtils.getResponseFromURL(context,true);
        }

        @Override
        protected void onPostExecute(String jsonResult) {
            List<Movie> movies = createMovieList(context, jsonResult);
            mAdapter = new MovieAdapter(movies);
            mMoviesRV.setAdapter(mAdapter);
        }

    }
}
