package com.learning.android.popular_movies;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
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

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.learning.android.popular_movies.database.AppDataBase;
import com.learning.android.popular_movies.interfaces.ClientService;
import com.learning.android.popular_movies.database.Movie;
import com.learning.android.popular_movies.responses.MovieResponse;
import com.learning.android.popular_movies.interfaces.MovieAdapterOnClickHandler;
import com.learning.android.popular_movies.utilities.AppExecutors;
import com.learning.android.popular_movies.utilities.ServiceGenerator;
import com.learning.android.popular_movies.viewModels.MainViewModel;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import javax.security.auth.login.LoginException;

import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function3;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import io.reactivex.Observable;

public class MainActivity extends AppCompatActivity
                          implements SharedPreferences.OnSharedPreferenceChangeListener,
                                     MovieAdapterOnClickHandler{

    private MovieAdapter mAdapter;
    private RecyclerView mMoviesRV;
    private TextView mConnectionFailureTV;
    private Button mRetryButton;
    private Sort selectedSort = Sort.popular;
    private MovieAdapterOnClickHandler mHandler = this;
    private Movie selectedMovie;
    private AppDataBase mDB;
    private List<Movie> displayMovies;

    public enum Sort{
        popular,
        top_rated,
        favorites
    }
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mDB = AppDataBase.getsInstance(getApplicationContext());
        setContentView(R.layout.activity_main);
        mConnectionFailureTV = findViewById(R.id.tv_connection_Failure);
        mRetryButton = findViewById(R.id.retry_button);
        setUpSharedPreferences();
        GetMoviesAndLoadUI();
        setUpViewModelForFavoritesChanges();
    }

    private void GetMoviesAndLoadUI() throws JsonIOException{
        if (selectedSort == Sort.favorites){
            getMoviesFromDBAndDisplay();
        }
        else{
            ClientService client = ServiceGenerator.createService(ClientService.class);
            getMoviesFromInternetAndDisplay(client);
        }
        mMoviesRV = findViewById(R.id.rvMovies);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,2);
        mMoviesRV.setLayoutManager(gridLayoutManager);
        mMoviesRV.setHasFixedSize(true);
    }

    private void getMoviesFromInternetAndDisplay(ClientService client) {
        Call<MovieResponse> call;
        if (selectedSort == Sort.popular){
            call = client.getPopularMovies(BuildConfig.API_KEY);
        }
        else{
            call = client.getTopRatedMovies(BuildConfig.API_KEY);
        }
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                setMainView(true);
                MovieResponse result = response.body();
                if(mAdapter == null && result != null){
                    createAndSetAdapter(result.getResults());
                }
                else if (result != null){
                    mAdapter.setMovies(result.getResults());
                }
                else{//Server down or some other error
                    setMainView(false);
                }
            }
            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                ErrorHandleRequests(t);
            }
        });
    }

    private void setUpViewModelForFavoritesChanges(){
        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getMovies().observe(this, new Observer<List<Movie>>() {
            @Override
            public void onChanged(@Nullable List<Movie> favoriteMovies) {
                if (selectedSort == Sort.favorites){
                    if (mAdapter == null) {
                        createAndSetAdapter(favoriteMovies);
                    }
                    else{
                        mAdapter.setMovies(favoriteMovies);
                    }

                }
            }
        });
    }

    private void getMoviesFromDBAndDisplay(){
        AppExecutors.getsInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                final List<Movie> movies = mDB.movieDao().fetchAllFavoritedMovies();
                displayMovies = movies;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setMainView(true);
                        if (selectedSort == Sort.favorites){
                            if (mAdapter != null ){
                                mAdapter.setMovies(movies);
                            }
                            else{
                                createAndSetAdapter(movies);
                            }
                        }
                    }
                });
            }
        });

    }

    private void createAndSetAdapter(List<Movie> movies) {
        mAdapter = new MovieAdapter(movies, mHandler);
        mMoviesRV.setAdapter(mAdapter);
    }


    private void ErrorHandleRequests(Throwable t) {
        if (!isOnline()){
            setMainView(false);
            mConnectionFailureTV.setText(R.string.connection_error_text);
        }
        else{
            t.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_settings:
                Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
                startActivity(startSettingsActivity);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setUpSharedPreferences(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        loadSortPreference(sp);
        sp.registerOnSharedPreferenceChangeListener(this);
    }

    private void loadSortPreference(SharedPreferences sp){
        String defaultKey = getString(R.string.pref_sort_by_popularity_key);
        String prefSortValue = sp.getString(getString(R.string.sort_by_key), defaultKey);
        switch (prefSortValue){
            case "popularity":
                selectedSort = Sort.popular;
                break;
            case "topRated":
                selectedSort = Sort.top_rated;
                break;
            case "favorites":
                selectedSort = Sort.favorites;
                break;
            default:
                selectedSort = Sort.popular;
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    public void retryOnClick(View view){
        GetMoviesAndLoadUI();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(getString(R.string.sort_by_key))){
            loadSortPreference(sharedPreferences);
            GetMoviesAndLoadUI();
        }
    }

    @Override
    public void onClick(final Movie movie) {
            selectedMovie = movie;
            ClientService client = ServiceGenerator.createService(ClientService.class);
            Observable.zip(client.getSelectedMovie(movie.getId(), BuildConfig.API_KEY),
                    client.getMovieTrailers(movie.getId(), BuildConfig.API_KEY),
                    client.getMovieReviews(movie.getId(), BuildConfig.API_KEY),
                    new Function3<JsonObject, JsonObject, JsonObject, List<JsonObject>>() {
                        @Override
                        public List<JsonObject> apply(JsonObject movieDetails, JsonObject trailers, JsonObject reviews) throws Exception {
                            List<JsonObject> results = Arrays.asList(movieDetails,trailers,reviews);
                            return results;
                        }
                    })
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<List<JsonObject>>() {
                @Override
                public void accept(List<JsonObject> jsonObjects) throws Exception {
                    JsonObject movieDetails = jsonObjects.get(0);
                    JsonObject trailers = jsonObjects.get(1);
                    JsonObject reviews = jsonObjects.get(2);
                    selectedMovie.setRunTime(movieDetails.get("runtime").getAsInt());
                    Gson gson = new Gson();
                    List<String> JsonResults = Arrays.asList(gson.toJson(selectedMovie),
                            gson.toJson(trailers), gson.toJson(reviews));
                    translateMovieToJsonAndStartIntent(JsonResults);
                }
            });
    }

    private void setMainView(boolean isConnected) {
        int recyclerVisibility = isConnected ? View.VISIBLE : View.GONE;
        int errorVisibility = isConnected ? View.GONE : View.VISIBLE;
        mMoviesRV.setVisibility(recyclerVisibility);
        mConnectionFailureTV.setVisibility(errorVisibility);
        mRetryButton.setVisibility(errorVisibility);
    }

    private void translateMovieToJsonAndStartIntent(List<String> info){
        Intent startMovieDetailActivity = new Intent(this, MovieDetailActivity.class);
        startMovieDetailActivity.putExtra("MovieJSON",info.get(0));
        startMovieDetailActivity.putExtra("TrailersJSON", info.get(1));
        startMovieDetailActivity.putExtra("ReviewsJSON", info.get(2));
        startActivity(startMovieDetailActivity);
    }

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Gson gson = new Gson();
        outState.putString("Movies", gson.toJson(displayMovies));
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Gson gson = new Gson();
        if (savedInstanceState != null && savedInstanceState.containsKey("Movies")){
            Type listType = new TypeToken<List<Movie>>(){}.getType();
            displayMovies = gson.fromJson(savedInstanceState.getString("Movies"), listType);
            createAndSetAdapter(displayMovies);
        }
        super.onRestoreInstanceState(savedInstanceState);
    }
}

