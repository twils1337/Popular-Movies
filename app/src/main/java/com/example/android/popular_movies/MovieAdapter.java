package com.example.android.popular_movies;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.android.popular_movies.model.Movie;
import com.example.android.popular_movies.utilities.NetworkMovieUtils;
import com.squareup.picasso.Picasso;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder>{

    private List<Movie> mMovies;

    public MovieAdapter(List<Movie> movies){
        mMovies = movies;
    }

    @Override
    public int getItemCount() {
        return mMovies == null ? 0 : mMovies.size();
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.movie_list_item, parent, false);

        return new MovieViewHolder(context, view);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        Movie movie = mMovies.get(position);
        if(android.os.Debug.isDebuggerConnected())
            android.os.Debug.waitForDebugger();
        holder.bind(movie);
    }

//    public void fetchMovieData(Context context){
//        new MovieQueryTask(context).execute();
//    }



    public class MovieViewHolder extends RecyclerView.ViewHolder{
        public ImageView movieImageView;
        public Context context;
        MovieViewHolder(Context context, View itemView)
        {
            super(itemView);
            this.context = context;
            movieImageView = itemView.findViewById(R.id.ivMovie);
        }

        void bind(Movie movie){
            Picasso.with(context)
                    .load(movie.getFullPosterURL())
                    .into(movieImageView);
        }
    }
}
