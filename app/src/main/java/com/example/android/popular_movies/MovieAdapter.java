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
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder>{

    private List<Movie> mMovies;

    @Override
    public int getItemCount() {
        return mMovies == null ? 0 : mMovies.size();
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.movie_list_item, parent, false);

        return new MovieViewHolder(parent.getContext(), view);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        Movie movie = mMovies.get(position);
        holder.bind(movie);
    }

    public void fetchMovieData(){
        new MovieQueryTask().execute();
    }

    private void createMovieList(String jsonResult) {
        try {
            JSONObject json = new JSONObject(jsonResult);
            mMovies = new ArrayList<>();
            Iterator<String> keys = json.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                JSONObject value = json.getJSONObject(key);
                Movie movie = new Movie(
                        value.getBoolean("adult"),
                        value.getString("backdrop_path"),
                        getListFromJsonArr(value.getJSONArray("genre_ids")),
                        value.getInt("id"),
                        value.getString("original_language"),
                        value.getString("original_title"),
                        value.getString("overview"),
                        value.getDouble("popularity"),
                        value.getString("poster_path"),
                        value.getString("release_date"),
                        value.getString("title"),
                        value.getBoolean("video"),
                        value.getDouble("vote_average"),
                        value.getInt("vote_count")
                );
                mMovies.add(movie);
            }
        } catch (JSONException e) {
            Log.e("Json Error", "doInBackground: " + e.getMessage());
        }
    }
    private List<Integer> getListFromJsonArr(JSONArray arr)
            throws JSONException{
        List<Integer> genreIDs = new ArrayList<>();
        for (int i = 0; i < arr.length(); ++i){
            genreIDs.add(arr.getInt(i));
        }
        return genreIDs;
    }



    public class MovieViewHolder extends RecyclerView.ViewHolder{
        private ImageView movieImageView;
        private Context context;
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

    class MovieQueryTask extends AsyncTask<URL, Void, String> {

        @Override
        protected String doInBackground(URL... urls) {
            return NetworkMovieUtils.getResponseFromURL(true);
        }

        @Override
        protected void onPostExecute(String jsonResult) {
            createMovieList(jsonResult);
        }

    }
}
