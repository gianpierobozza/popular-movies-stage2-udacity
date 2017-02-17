package com.gbozza.android.popularmovies.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gbozza.android.popularmovies.MovieDetailActivity;
import com.gbozza.android.popularmovies.R;
import com.gbozza.android.popularmovies.models.Movie;
import com.gbozza.android.popularmovies.utilities.MoviePosterCallback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter to manage the RecyclerView in the Main Activity
 */
public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MoviesAdapterViewHolder> {

    private List<Movie> mMovieList;

    private static final String INTENT_MOVIE_KEY = "movieObject";
    private static final int INDEX_MOVIE_ID = 0;
    private static final int INDEX_BACKDROP_PATH = 1;
    private static final int INDEX_POSTER_PATH = 2;
    private static final int INDEX_OVERVIEW = 3;
    private static final int INDEX_TITLE = 4;
    private static final int INDEX_RELEASE_DATE = 5;
    private static final int INDEX_VOTE_AVERAGE = 6;

    /**
     * Inner class to represent the ViewHolder for the Adapter
     */
    public class MoviesAdapterViewHolder extends RecyclerView.ViewHolder {
        final CardView mPopularMovieCardView;
        final ImageView mMoviePosterImageView;
        public ProgressBar mMoviePosterProgressBar;
        public TextView mMoviePosterErrorTextView;
        final TextView mMovieTitleTextView;
        Context mContext;

        /**
         * Constructor to the ViewHolder class
         *
         * @param view the we are going to inflate
         */
        MoviesAdapterViewHolder(View view) {
            super(view);
            mPopularMovieCardView = (CardView) view.findViewById(R.id.cv_popular_movie);
            mMoviePosterImageView = (ImageView) view.findViewById(R.id.iv_movie_poster);
            mMoviePosterProgressBar = (ProgressBar) view.findViewById(R.id.pb_movie_poster);
            mMoviePosterErrorTextView = (TextView) view.findViewById(R.id.tv_movie_poster_error);
            mMovieTitleTextView = (TextView) view.findViewById(R.id.tv_movie_title);
            mContext = view.getContext();
        }
    }

    @Override
    public MoviesAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.popular_movies_card;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        return new MoviesAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MoviesAdapterViewHolder moviesAdapterViewHolder, int position) {
        Movie movie = mMovieList.get(position);

        Picasso.with(moviesAdapterViewHolder.mContext)
                .load(movie.buildPosterPath(moviesAdapterViewHolder.mContext))
                .into(moviesAdapterViewHolder.mMoviePosterImageView, new MoviePosterCallback(moviesAdapterViewHolder));
        moviesAdapterViewHolder.mMovieTitleTextView.setText(movie.getOriginalTitle());

        moviesAdapterViewHolder.mPopularMovieCardView.setTag(R.id.card_view_item, position);

        moviesAdapterViewHolder.mPopularMovieCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Class destinationClass = MovieDetailActivity.class;
                Intent movieDetailIntent = new Intent(view.getContext(), destinationClass);
                int position = (int) view.getTag(R.id.card_view_item);
                movieDetailIntent.putExtra(INTENT_MOVIE_KEY, mMovieList.get(position));
                view.getContext().startActivity(movieDetailIntent);
            }
        });
    }

    public void loadCursorIntoAdapter(Cursor newCursor) {
        if (null != newCursor) {
            newCursor.moveToFirst();
            List<Movie> movieList = new ArrayList<>();
            try {
                do {
                    movieList.add(createMovieFromCursor(newCursor));
                } while (newCursor.moveToNext());
            } finally {
                newCursor.close();
            }
            mMovieList = new ArrayList<>(movieList);
        } else {
            clearMovieList();
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (null != mMovieList) {
            return mMovieList.size();
        } else {
            return 0;
        }
    }

    /**
     * Reset the Movie List, new search, for example
     */
    public void clearMovieList() {
        if (mMovieList != null) {
            mMovieList.clear();
            notifyDataSetChanged();
        }
    }

    /**
     * Setter method for the Movie List Object
     *
     * @param movieList the List containing Movie Objects
     */
    public void setMoviesData(List<Movie> movieList) {
        if (null == mMovieList) {
            mMovieList = movieList;
        } else {
            mMovieList.addAll(movieList);
        }
        notifyDataSetChanged();
    }

    /**
     * Getter method for the Movie List Object
     *
     * @return the Movie List
     */
    public List<Movie> getMoviesData() {
        return mMovieList;
    }

    public Movie createMovieFromCursor(Cursor cursor) {
        return new Movie(
                cursor.getInt(INDEX_MOVIE_ID),
                cursor.getString(INDEX_BACKDROP_PATH),
                cursor.getString(INDEX_POSTER_PATH),
                cursor.getString(INDEX_OVERVIEW),
                cursor.getString(INDEX_TITLE),
                cursor.getString(INDEX_RELEASE_DATE),
                cursor.getString(INDEX_VOTE_AVERAGE)
        );
    }
}
