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

    public void fetchMovieData(Context context){
        new MovieQueryTask(context).execute();
    }

    private void createMovieList(Context context, String jsonResult) {
        try {
                JSONObject json = new JSONObject(jsonResult);
                mMovies = new ArrayList<>();
                String resultsKey = "results";
                JSONArray resultsJson = json.getJSONArray(resultsKey);
    //            Iterator<String> keys = resultsJson.keys();
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

    class MovieQueryTask extends AsyncTask<URL, Void, String> {
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
            createMovieList(context, jsonResult);
        }

    }
}
