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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.learning.android.popular_movies.model.Movie;
import com.learning.android.popular_movies.utilities.NetworkMovieUtils;
import com.learning.android.popular_movies.interfaces.MovieAdapterOnClickHandler;
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;

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
    private boolean mIsConnected = true;

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

    private void setErrorMessage(){
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
        JsonElement jsonElement = new JsonParser().parse(jsonResult);
        String results = jsonElement.getAsJsonObject().getAsJsonArray("results").toString();
        Type listType = new TypeToken<ArrayList<Movie>>(){}.getType();
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd")
                .create();
        List<Movie> movies = gson.fromJson(results, listType);
        return movies;
    }

    public void retryOnClick(View view){
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
                mqt.execute(movie.getId());
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

    class MovieQueryTask extends AsyncTask<Integer, Void, String> {
        private String mApiKey;
        private MovieAdapterOnClickHandler mHandler;
        private boolean mSortByPopularity;
        private boolean grabMovieList;
        private Movie mMovie;
        private Context mContext;

        MovieQueryTask(MovieAdapterOnClickHandler handler, String apiKey, boolean sortByPopularity){
            mHandler = handler;
            mApiKey = apiKey;
            mSortByPopularity = sortByPopularity;
            grabMovieList = true;
            mMovie = null;
        }

        MovieQueryTask(Movie movie, String apiKey, Context context){
            mMovie = movie;
            mApiKey = apiKey;
            mContext = context;
        }

        @Override
        protected String doInBackground(Integer... IDs) {
            if (isOnline()){
                mIsConnected = true;
                String result = NetworkMovieUtils.getResponseFromURL(mApiKey, mSortByPopularity, IDs[0]);

                if (IDs[0] != -1){
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

    private void setMainView(boolean isConnected) {
        int recyclerVisibility = isConnected ? View.VISIBLE : View.GONE;
        int errorVisibility = isConnected ? View.GONE : View.VISIBLE;
        mMoviesRV.setVisibility(recyclerVisibility);
        mConnectionFailureTV.setVisibility(errorVisibility);
        mRetryButton.setVisibility(errorVisibility);
    }

    private void translateMovieToJsonAndStartIntent(Movie movie, Context context){
        Intent startMovieDetailActivity = new Intent(context, MovieDetailActivity.class);
        Gson gson = new Gson();
        startMovieDetailActivity.putExtra("MovieJSON", gson.toJson(movie));
        startActivity(startMovieDetailActivity);
    }

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}

