package com.learning.android.popular_movies;

import android.arch.lifecycle.ViewModelProviders;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.learning.android.popular_movies.database.AppDataBase;
import com.learning.android.popular_movies.database.Movie;

import com.learning.android.popular_movies.models.Review;
import com.learning.android.popular_movies.models.Trailer;
import com.learning.android.popular_movies.responses.ReviewReponse;
import com.learning.android.popular_movies.responses.TrailerResponse;
import com.learning.android.popular_movies.utilities.AppExecutors;
import com.learning.android.popular_movies.viewModels.MovieModelFactory;
import com.learning.android.popular_movies.viewModels.MovieViewModel;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;
import java.util.Calendar;

import android.arch.lifecycle.Observer;

import org.w3c.dom.Text;

import java.util.List;
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
    private Movie mSelectedMovie;
    private List<Trailer> mTrailers;
    private List<Review> mReviews;
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
        Bundle extras = parentIntent.getExtras();
        if (extras.containsKey("MovieJSON")){
            String movieJson = parentIntent.getStringExtra("MovieJSON");
            Gson gson = new Gson();
            mSelectedMovie = gson.fromJson(movieJson, Movie.class);
            initFavoriteStatus();
        }
        if (extras.containsKey("TrailersJSON")){
            generateTrailersSectionForView(extras.getString("TrailersJSON"));
        }
        if(extras.containsKey("ReviewsJSON")){
            generateReviewsSectionForView(extras.getString("ReviewsJSON"));
        }
        loadDataIntoView(mSelectedMovie);
     }

    private void initFavoriteStatus() {
        ExecutorService es = Executors.newSingleThreadExecutor();
        es.submit(new Runnable() {
            @Override
            public void run() {
                isFavorite = mDB.movieDao().getFavoriteMovieByID(mSelectedMovie.getId()) != null;
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

    private void generateTrailersSectionForView(String trailersJSON){
        Gson gson = new Gson();
        TrailerResponse response = gson.fromJson(trailersJSON, TrailerResponse.class);
        ViewGroup viewGroup = (ViewGroup) findViewById(android.R.id.content);
        LinearLayout trailerSection = (LinearLayout) findViewById(R.id.trailer_ll);
        int trailerCount = response.getResults().size();
        mTrailers = response.getResults();
        for (int i = 0; i < trailerCount; ++i){
            LinearLayout row = (LinearLayout) getLayoutInflater()
                    .inflate(R.layout.trailer_row, viewGroup, false);
            row.setClickable(true);
            row.setTag(i);
            TextView trailerDescription = (TextView) row.findViewById(R.id.trailer_description);
            trailerDescription.setText("Trailer #"+new Integer(i+1).toString());
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int itemID = (int) v.getTag();
                    String videoID = mTrailers.get(itemID).getKey();
                    Intent youTubeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:"+videoID));
                    Context context = getApplicationContext();
                    try{
                        context.startActivity(youTubeIntent);
                    }
                    catch(ActivityNotFoundException ex){
                        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://youtube.com/watch?v="+videoID));
                        context.startActivity(webIntent);
                    }
                }
            });
            trailerSection.addView(row);
            if (i < trailerCount-1) {
                trailerSection.addView(getLineSeperator());
            }
        }

    }

    private void generateReviewsSectionForView(String reviewsJSON){
        Gson gson = new Gson();
        ReviewReponse response = gson.fromJson(reviewsJSON, ReviewReponse.class);
        ViewGroup viewGroup = (ViewGroup) findViewById(android.R.id.content);
        LinearLayout reviewSection = (LinearLayout) findViewById(R.id.review_ll);
        int reviewCount = response.getTotal_results();
        mReviews = response.getResults();
        for (int i = 0; i < reviewCount; ++i) {
            LinearLayout row = (LinearLayout) getLayoutInflater()
                    .inflate(R.layout.review_row, viewGroup, false);
            TextView reviewBody = row.findViewById(R.id.review_body);
            TextView authorTV = row.findViewById(R.id.author_name);
            reviewBody.setText(mReviews.get(i).getContent());
            authorTV.setText("-"+mReviews.get(i).getAuthor());
            reviewSection.addView(row);
            if (i < reviewCount-1){
                reviewSection.addView(getLineSeperator());
            }
        }
    }

    private View getLineSeperator(){
        View view = new View(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,2);
        params.setMargins(0,32,0,32);
        view.setLayoutParams(params);
        view.setBackgroundColor(getResources().getColor(R.color.grey));
        return view;
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
            SetUpObservableForFavoriteChanges();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        favoriteItem = item;
        if(item.getItemId() == R.id.action_favorite)
        {
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
        MovieModelFactory factory = new MovieModelFactory(mDB, mSelectedMovie.getId());
        MovieViewModel viewModel = ViewModelProviders.of(this,factory).get(MovieViewModel.class);
        viewModel.getMovie().observe(this, new Observer<Movie>() {
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
                Movie movie = mDB.movieDao().getFavoriteMovieByID(mSelectedMovie.getId());
                if (movie==null){
                    mDB.movieDao().insertMovie(mSelectedMovie);
                }
            }
        });
    }

    private void RemoveMovieFromFavoritesDB() {
        AppExecutors.getsInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                Movie movie = mDB.movieDao().getFavoriteMovieByID(mSelectedMovie.getId());
                if (movie!=null){
                    mDB.movieDao().deleteMovie(movie);
                }
            }
        });
    }
}
