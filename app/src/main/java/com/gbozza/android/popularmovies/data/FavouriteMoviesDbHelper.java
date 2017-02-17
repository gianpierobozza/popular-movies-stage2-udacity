package com.gbozza.android.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.gbozza.android.popularmovies.data.FavouriteMoviesContract.FavouriteMovieEntry;

public class FavouriteMoviesDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "favouriteMoviesDb.db";
    private static final int VERSION = 1;

    FavouriteMoviesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_TABLE = "CREATE TABLE "      + FavouriteMovieEntry.TABLE_NAME + " (" +
                FavouriteMovieEntry._ID                  + " INTEGER PRIMARY KEY, " +
                FavouriteMovieEntry.COLUMN_MOVIE_ID      + " INTEGER NOT NULL, " +
                FavouriteMovieEntry.COLUMN_BACKDROP_PATH + " TEXT NOT NULL, " +
                FavouriteMovieEntry.COLUMN_POSTER_PATH   + " TEXT NOT NULL, " +
                FavouriteMovieEntry.COLUMN_OVERVIEW      + " TEXT NOT NULL, " +
                FavouriteMovieEntry.COLUMN_TITLE         + " TEXT NOT NULL, " +
                FavouriteMovieEntry.COLUMN_RELEASE_DATE  + " TEXT NOT NULL, " +
                FavouriteMovieEntry.COLUMN_VOTE_AVERAGE  + " TEXT NOT NULL);";

        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FavouriteMovieEntry.TABLE_NAME);
        onCreate(db);
    }

}
