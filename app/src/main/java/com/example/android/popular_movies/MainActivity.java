package com.example.android.popular_movies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.android.popular_movies.model.Movie;
import com.example.android.popular_movies.utilities.NetworkMovieUtils;
import com.example.android.popular_movies.interfaces.MovieAdapterOnClickHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
                          implements SharedPreferences.OnSharedPreferenceChangeListener,
                                     MovieAdapterOnClickHandler{

    private MovieAdapter mAdapter;
    private RecyclerView mMoviesRV;
    private boolean sortByPopular;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpSharedPreferences();
        MovieQueryTask mqt = new MovieQueryTask(this, getString(R.string.api_key), sortByPopular);
        mqt.execute(-1);
        mMoviesRV = findViewById(R.id.rvMovies);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,2);
        mMoviesRV.setLayoutManager(gridLayoutManager);
        mMoviesRV.setHasFixedSize(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int selectedID = item.getItemId();
        if (selectedID == R.id.action_settings){
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpSharedPreferences(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        loadSortPreference(sp);
        sp.registerOnSharedPreferenceChangeListener(this);
    }

    private void loadSortPreference(SharedPreferences sp){
        String defaultKey = getString(R.string.pref_sort_by_popularity_key);
        String prefSortValue = sp.getString(getString(R.string.sort_by_key), defaultKey);
        sortByPopular = prefSortValue.equals(defaultKey);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    private List<Movie> createMovieList(String jsonResult) {
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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(getString(R.string.sort_by_key))){
            loadSortPreference(sharedPreferences);
            MovieQueryTask mqt = new MovieQueryTask(this, getString(R.string.api_key), sortByPopular);
            mqt.execute(-1);
        }
    }

    @Override
    public void onClick(Movie movie) {
        try {
            if (movie.getRunTime() == -1) {
                MovieQueryTask mqt = new MovieQueryTask(movie, getString(R.string.api_key), this);
                mqt.execute(movie.getID());
            }
            else{
                translateMovieToJsonAndStartIntent(movie,this);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private int getMovieRunTime(String json){
        int runTime = -1;
        try {
            JSONObject jsonObject = new JSONObject(json);
            runTime = jsonObject.getInt("runtime");
        } catch(JSONException e){
            e.printStackTrace();
        }
        return runTime;
    }

    public class MovieQueryTask extends AsyncTask<Integer, Void, String> {
        private String mApiKey;
        private MovieAdapterOnClickHandler mHandler;
        private boolean mSortByPopularity;
        private boolean grabMovieList;
        private Movie mMovie;
        private Context mContext;

        public MovieQueryTask(MovieAdapterOnClickHandler handler, String apiKey, boolean sortByPopularity){
            mHandler = handler;
            mApiKey = apiKey;
            mSortByPopularity = sortByPopularity;
            grabMovieList = true;
            mMovie = null;
        }

        public MovieQueryTask(Movie movie, String apiKey, Context context){
            mMovie = movie;
            mApiKey = apiKey;
            mContext = context;
        }

        @Override
        protected String doInBackground(Integer... IDs) {
            String result = NetworkMovieUtils.getResponseFromURL(mApiKey, mSortByPopularity, IDs[0]);
            if (IDs.length > 0 && IDs[0] != -1){
                grabMovieList = false;
            }
            return result;
        }

        @Override
        protected void onPostExecute(String jsonResult) {
            if (grabMovieList){
                List<Movie> movies = createMovieList(jsonResult);
                mAdapter = new MovieAdapter(movies, mHandler);
                mMoviesRV.setAdapter(mAdapter);
            }
            else{
                int runTime = getMovieRunTime(jsonResult);
                mMovie.setRunTime(runTime);
                try{
                    translateMovieToJsonAndStartIntent(mMovie, mContext);
                } catch (Exception e){
                    e.printStackTrace();
                }

            }
        }
    }
    private void translateMovieToJsonAndStartIntent(Movie movie, Context context) throws JsonProcessingException {
        Intent startMovieDetailActivity = new Intent(context, MovieDetailActivity.class);
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JodaModule());
        om.configure(com.fasterxml.jackson.databind.SerializationFeature.
                WRITE_DATES_AS_TIMESTAMPS , false);
        String movieJson = om.writeValueAsString(movie);
        startMovieDetailActivity.putExtra("MovieJSON", movieJson);
        startActivity(startMovieDetailActivity);
    }
}

