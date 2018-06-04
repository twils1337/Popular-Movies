package com.learning.android.popular_movies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.learning.android.popular_movies.model.Movie;
import com.learning.android.popular_movies.utilities.NetworkMovieUtils;
import com.learning.android.popular_movies.interfaces.MovieAdapterOnClickHandler;
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
    private TextView mConnectionFailureTV;
    private Button mRetryButton;
    private boolean sortByPopular;
    boolean mIsConnected = true;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mConnectionFailureTV = findViewById(R.id.tv_connection_Failure);
        mRetryButton = findViewById(R.id.retry_button);
        setUpSharedPreferences();
        GetMoviesAndLoadUI();
    }

    private void GetMoviesAndLoadUI() {
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

    public void setErrorMessage(){
        mConnectionFailureTV.setText(R.string.connection_error_text);
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
                Movie movie = createMovieFromJson(movieJson);
                movies.add(movie);
            }
        } catch (JSONException e) {
            Log.e("Json Error", "doInBackground: " + e.getMessage());
        }
        return movies;
    }

    private Movie createMovieFromJson(JSONObject jsonObject) throws JSONException{
        Movie movie = new Movie(
                jsonObject.getBoolean("adult"),
                jsonObject.getString("backdrop_path"),
                getListFromJsonArr(jsonObject.getJSONArray("genre_ids")),
                jsonObject.getInt("id"),
                jsonObject.getString("original_language"),
                jsonObject.getString("original_title"),
                jsonObject.getString("overview"),
                jsonObject.getDouble("popularity"),
                jsonObject.getString("poster_path"),
                jsonObject.getString("release_date"),
                jsonObject.getString("title"),
                jsonObject.getBoolean("video"),
                jsonObject.getDouble("vote_average"),
                jsonObject.getInt("vote_count")
        );
        return movie;
    }

    private List<Integer> getListFromJsonArr(JSONArray arr)
            throws JSONException{
        List<Integer> genreIDs = new ArrayList<>();
        for (int i = 0; i < arr.length(); ++i){
            genreIDs.add(arr.getInt(i));
        }
        return genreIDs;
    }

    public void retryOnClick(View v){
        GetMoviesAndLoadUI();
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
            if (isOnline()){
                mIsConnected = true;
                String result = NetworkMovieUtils.getResponseFromURL(mApiKey, mSortByPopularity, IDs[0]);
                if (IDs.length > 0 && IDs[0] != -1){
                    grabMovieList = false;
                }
                return result;
            }
            else{
                mIsConnected = false;
                return "";
            }
        }

        @Override
        protected void onPostExecute(String jsonResult) {
            setMainView(mIsConnected);
            if (mIsConnected){
                if (grabMovieList){
                    mMoviesRV.setVisibility(View.VISIBLE);
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
            else{
                setErrorMessage();
            }
        }
    }

    public void setMainView(boolean isConnected) {
        int recyclcerVisibility = isConnected ? View.VISIBLE : View.GONE;
        int errorVisibility = isConnected ? View.GONE : View.VISIBLE;
        mMoviesRV.setVisibility(recyclcerVisibility);
        mConnectionFailureTV.setVisibility(errorVisibility);
        mRetryButton.setVisibility(errorVisibility);
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

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}

