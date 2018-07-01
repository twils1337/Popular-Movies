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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.learning.android.popular_movies.database.AppDataBase;
import com.learning.android.popular_movies.interfaces.ClientService;
import com.learning.android.popular_movies.database.Movie;
import com.learning.android.popular_movies.responses.MovieResponse;
import com.learning.android.popular_movies.interfaces.MovieAdapterOnClickHandler;
import com.learning.android.popular_movies.utilities.AppExecutors;
import com.learning.android.popular_movies.utilities.ServiceGenerator;
import com.learning.android.popular_movies.viewModels.MainViewModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity
                          implements SharedPreferences.OnSharedPreferenceChangeListener,
                                     MovieAdapterOnClickHandler{

    private MovieAdapter mAdapter;
    private RecyclerView mMoviesRV;
    private TextView mConnectionFailureTV;
    private Button mRetryButton;
    private boolean sortByPopular;
    private boolean showFavorites = false;
    private MovieAdapterOnClickHandler mHandler = this;
    private Movie selectedMovie;
    private Context selectedContext;
    private AppDataBase mDB;

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
        if (showFavorites){
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
        if (sortByPopular){
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
                if(mAdapter == null){
                    createAndSetAdapter(result.getResults());
                }
                else{
                    mAdapter.setMovies(result.getResults());
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
                if (showFavorites){
                    mAdapter.setMovies(favoriteMovies);
                }
            }
        });
    }

    private void getMoviesFromDBAndDisplay(){
        AppExecutors.getsInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                final List<Movie> movies = mDB.movieDao().fetchAllFavoritedMovies();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.setMovies(movies);
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
            case R.id.action_favorites:
                showFavorites = !showFavorites;
                GetMoviesAndLoadUI();
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
        sortByPopular = prefSortValue.equals(defaultKey);
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
    public void onClick(Movie movie) {
        if (movie.getRunTime() == -1) {
            ClientService client = ServiceGenerator.createService(ClientService.class);
            Call<JsonObject> call = client.getSelectedMovie(movie.getId(), BuildConfig.API_KEY);
            selectedMovie = movie;
            selectedContext = this;
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    try {
                        JsonObject obj = response.body().getAsJsonObject();
                        int runTime = obj.get("runtime").getAsInt();
                        selectedMovie.setRunTime(runTime);
                        translateMovieToJsonAndStartIntent(selectedMovie, selectedContext);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    ErrorHandleRequests(t);
                }
            });
        }
        else{
            selectedMovie = movie;
            translateMovieToJsonAndStartIntent(selectedMovie, this);
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

