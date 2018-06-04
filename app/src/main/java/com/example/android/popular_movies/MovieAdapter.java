package com.example.android.popular_movies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.view.View.OnClickListener;

import com.example.android.popular_movies.interfaces.MovieAdapterOnClickHandler;
import com.example.android.popular_movies.model.Movie;
import com.squareup.picasso.Picasso;

import java.util.List;


public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder>{

    private List<Movie> mMovies;
    private final MovieAdapterOnClickHandler mParentClickListener;

    public MovieAdapter(List<Movie> movies, MovieAdapterOnClickHandler handler){
        mMovies = movies;
        mParentClickListener = handler;
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
        view.getLayoutParams().height = context.getResources().getDisplayMetrics().heightPixels/2;
        return new MovieViewHolder(context, view);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        Movie movie = mMovies.get(position);
        holder.bind(movie);
    }


    public class MovieViewHolder extends RecyclerView.ViewHolder
                                 implements OnClickListener {
        public ImageView movieImageView;
        public Context context;
        MovieViewHolder(Context context, View itemView)
        {
            super(itemView);
            this.context = context;
            movieImageView = itemView.findViewById(R.id.ivMovie);
            itemView.setOnClickListener(this);
        }

        void bind(Movie movie){
            Picasso.with(context)
                    .load(movie.getFullPosterURL())
                    .into(movieImageView);
        }

        @Override
        public void onClick(View v) {
            int movieIndex = getAdapterPosition();
            Movie movie = mMovies.get(movieIndex);
            mParentClickListener.onClick(movie);
        }
    }
}
