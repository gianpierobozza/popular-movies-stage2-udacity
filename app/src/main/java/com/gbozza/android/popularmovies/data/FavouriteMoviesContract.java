package com.gbozza.android.popularmovies.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class FavouriteMoviesContract {

    public static final String AUTHORITY = "com.gbozza.android.popularmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_FAVOURITES = "favourites";

    public static final class FavouriteMovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVOURITES).build();

        public static final String TABLE_NAME = "favourites";
        public static final String COLUMN_MOVIE_ID = "movieId";
        public static final String COLUMN_MOVIE_TITLE = "title";

        public static Uri buildFavouriteUriWithMovieId(int movieId) {
            return CONTENT_URI.buildUpon()
                    .appendPath(Integer.toString(movieId))
                    .build();
        }
    }

}
