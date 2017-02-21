package com.gbozza.android.popularmovies.activities;

/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gbozza.android.popularmovies.R;
import com.gbozza.android.popularmovies.adapters.ReviewsAdapter;
import com.gbozza.android.popularmovies.adapters.VideosAdapter;
import com.gbozza.android.popularmovies.data.FavouriteMoviesContract.FavouriteMovieEntry;
import com.gbozza.android.popularmovies.fragments.MovieListFragment;
import com.gbozza.android.popularmovies.models.Movie;
import com.gbozza.android.popularmovies.models.Review;
import com.gbozza.android.popularmovies.models.Video;
import com.gbozza.android.popularmovies.utilities.MovieDbJsonUtilities;
import com.gbozza.android.popularmovies.utilities.NetworkUtilities;
import com.gbozza.android.popularmovies.utilities.SpannableUtilities;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Activity used to display Movie details, like release date, vote average, etc..
 */
public class MovieDetailActivity extends AppCompatActivity {

    private Context mContext;
    private Movie mMovie;
    private VideosAdapter mVideosAdapter;
    private ReviewsAdapter mReviewsAdapter;

    @BindView(R.id.iv_movie_detail_backdrop) ImageView mMovieBackdropImageView;
    @BindView(R.id.pb_movie_detail_poster) ProgressBar mMoviePosterProgressBar;
    @BindView(R.id.tv_movie_detail_vote_average) TextView mMovieVoteAverageTextView;
    @BindView(R.id.tv_movie_detail_release_date) TextView mMovieReleaseDateTextView;
    @BindView(R.id.tv_movie_detail_overview) TextView mMovieOverviewTextView;
    @BindView(R.id.tv_movie_detail_poster_error) TextView mMoviePosterErrorTextView;

    @BindView(R.id.rv_videos) RecyclerView mVideosRecyclerView;
    @BindView(R.id.rv_reviews) RecyclerView mReviewsRecyclerView;
    @BindView(R.id.iv_movie_favourite) ImageView mMovieFavouriteImageView;

    @BindString(R.string.movie_detail_vote_average) String mDetailVoteAvgLabel;
    @BindString(R.string.movie_detail_release_date) String mDetailReleaseDateLabel;
    @BindString(R.string.movie_detail_overview) String mDetailOverviewLabel;

    @BindString(R.string.movie_favourite_off_toast_msg) String mFavOffToastMsg;
    @BindString(R.string.movie_favourite_on_toast_msg) String mFavOnToastMsg;

    private static final String INTENT_MOVIE_KEY = "movieObject";
    private static final String BUNDLE_VIDEOS_KEY = "videoList";
    private static final String BUNDLE_REVIEWS_KEY = "reviewList";

    private static final String DETAIL_ELEMENT_VIDEOS = "videos";
    private static final String DETAIL_ELEMENT_REVIEWS = "reviews";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();

        Intent parentIntent = getIntent();
        if (null != parentIntent) {
            if (parentIntent.hasExtra(INTENT_MOVIE_KEY)) {
                getSupportActionBar().setHomeButtonEnabled(true);

                setContentView(R.layout.activity_movie_detail);

                ButterKnife.bind(this);

                mMovie = getIntent().getExtras().getParcelable(INTENT_MOVIE_KEY);

                if (checkFavourite(mMovie.getId())) {
                    mMovieFavouriteImageView.setBackgroundResource(R.drawable.ic_star);
                }

                Picasso.with(this)
                        .load(mMovie.buildBackdropPath(this))
                        .into(mMovieBackdropImageView, new Callback() {
                            @Override
                            public void onSuccess() {
                                mMoviePosterProgressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError() {
                                mMoviePosterProgressBar.setVisibility(View.GONE);
                                mMoviePosterErrorTextView.setRotation(-20);
                                mMoviePosterErrorTextView.setVisibility(View.VISIBLE);
                            }
                        });

                mMovieVoteAverageTextView.append(SpannableUtilities.makeBold(mDetailVoteAvgLabel));
                mMovieVoteAverageTextView.append(mMovie.getVoteAverage());
                mMovieReleaseDateTextView.append(SpannableUtilities.makeBold(mDetailReleaseDateLabel));
                mMovieReleaseDateTextView.append(mMovie.getReleaseDate());
                mMovieOverviewTextView.append(SpannableUtilities.makeBold(mDetailOverviewLabel));
                mMovieOverviewTextView.append(mMovie.getOverview());

                setTitle(mMovie.getOriginalTitle());

                LinearLayoutManager videosLinearLayoutManager = new LinearLayoutManager(mContext);
                mVideosRecyclerView.setLayoutManager(videosLinearLayoutManager);

                mVideosRecyclerView.setHasFixedSize(true);
                mVideosAdapter = new VideosAdapter();
                mVideosRecyclerView.setAdapter(mVideosAdapter);

                LinearLayoutManager reviewsLinearLayoutManager = new LinearLayoutManager(mContext);
                mReviewsRecyclerView.setLayoutManager(reviewsLinearLayoutManager);

                mReviewsRecyclerView.setHasFixedSize(true);
                mReviewsAdapter = new ReviewsAdapter();
                mReviewsRecyclerView.setAdapter(mReviewsAdapter);

                if (null != savedInstanceState) {
                    ArrayList<Video> videoList = savedInstanceState.getParcelableArrayList(BUNDLE_VIDEOS_KEY);
                    mVideosAdapter.setVideosData(videoList);
                    ArrayList<Review> reviewList = savedInstanceState.getParcelableArrayList(BUNDLE_REVIEWS_KEY);
                    mReviewsAdapter.setReviewsData(reviewList);
                } else {
                    loadElements(DETAIL_ELEMENT_VIDEOS, mMovie.getId());
                    loadElements(DETAIL_ELEMENT_REVIEWS, mMovie.getId());
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        List<Video> videosList = mVideosAdapter.getVideosData();
        if (null != videosList) {
            ArrayList<Video> videoArrayList = new ArrayList<>(videosList);
            outState.putParcelableArrayList(BUNDLE_VIDEOS_KEY, videoArrayList);
        }

        List<Review> reviewsList = mReviewsAdapter.getReviewsData();
        if (null != reviewsList) {
            ArrayList<Review> reviewArrayList = new ArrayList<>(reviewsList);
            outState.putParcelableArrayList(BUNDLE_REVIEWS_KEY, reviewArrayList);
        }
    }

    /**
     * A method that invokes the AsyncTask to populate the details required, for example
     * video trailer or reviews.
     *
     * @param element the element type to load
     * @param movieId the movie id for the specific videos we need
     */
    public void loadElements(String element, int movieId) {
        if (NetworkUtilities.isOnline(mContext)) {
            String method;
            switch (element) {
                case DETAIL_ELEMENT_VIDEOS:
                    method = NetworkUtilities.getMoviedbMethodVideos(movieId);
                    String[] videos = new String[]{method};
                    new FetchVideosTask().execute(videos);
                    break;
                case DETAIL_ELEMENT_REVIEWS:
                    method = NetworkUtilities.getMoviedbMethodReviews(movieId);
                    String[] reviews = new String[]{method};
                    new FetchReviewsTask().execute(reviews);
                    break;
            }
        }
    }

    /**
     * A method to check if a Movie is already or not flagged as favourite
     *
     * @param movieId the ID of the movie, from The MovieDB database
     * @return true or false
     */
    private boolean checkFavourite(int movieId) {
        boolean favourite = false;
        String[] selectionArgs = {String.valueOf(movieId)};
        Uri uri = FavouriteMovieEntry.buildFavouriteUriWithMovieId(movieId);
        Cursor cursor = getContentResolver().query(uri,
                null,
                FavouriteMovieEntry.COLUMN_MOVIE_ID + "=?",
                selectionArgs,
                null);
        if (null != cursor && cursor.getCount() != 0) {
            favourite = true;
            cursor.close();
        }
        return favourite;
    }

    /**
     * This method performs the insert or delete of a movie from the favourite database
     * @param view the view element coming from the layout
     */
    public void favouriteMovie(View view) {
        if (checkFavourite(mMovie.getId())) {
            Uri removeFavouriteUri = FavouriteMovieEntry.buildFavouriteUriWithMovieId(mMovie.getId());
            getContentResolver().delete(removeFavouriteUri, null, null);
            Toast.makeText(getBaseContext(), mFavOffToastMsg, Toast.LENGTH_LONG).show();
            mMovieFavouriteImageView.setBackgroundResource(R.drawable.ic_star_border_black);
        } else {
            ContentValues contentValues = new ContentValues();
            contentValues.put(FavouriteMovieEntry.COLUMN_MOVIE_ID, mMovie.getId());
            contentValues.put(FavouriteMovieEntry.COLUMN_BACKDROP_PATH, mMovie.getBackdropPath());
            contentValues.put(FavouriteMovieEntry.COLUMN_POSTER_PATH, mMovie.getPosterPath());
            contentValues.put(FavouriteMovieEntry.COLUMN_OVERVIEW, mMovie.getOverview());
            contentValues.put(FavouriteMovieEntry.COLUMN_TITLE, mMovie.getOriginalTitle());
            contentValues.put(FavouriteMovieEntry.COLUMN_RELEASE_DATE, mMovie.getReleaseDate());
            contentValues.put(FavouriteMovieEntry.COLUMN_VOTE_AVERAGE, mMovie.getVoteAverage());
            Uri favouriteUri = getContentResolver().insert(FavouriteMovieEntry.CONTENT_URI, contentValues);

            if (null != favouriteUri) {
                Toast.makeText(getBaseContext(), mFavOnToastMsg, Toast.LENGTH_LONG).show();
                mMovieFavouriteImageView.setBackgroundResource(R.drawable.ic_star);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * The background worker that executes the calls to the MovieDB service
     * Using an Inner class to avoid convolution when having to manipulate the
     * View elements in the fragment.
     */
    public class FetchVideosTask extends AsyncTask<String[], Void, List<Video>> {

        private final String TAG = FetchVideosTask.class.getSimpleName();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Video> doInBackground(String[]... params) {
            String method = params[0][0];
            Map<String, String> mapping = new HashMap<>();

            mapping.put(NetworkUtilities.getMoviedbLanguageQueryParam(), MovieListFragment.getMovieLocale());

            URL url = NetworkUtilities.buildUrl(method, mapping);

            try {
                String response = NetworkUtilities.getResponseFromHttpUrl(url);
                Log.d(TAG, response);
                JSONObject responseJson = new JSONObject(response);

                return MovieDbJsonUtilities.getVideosListFromJson(responseJson);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Video> videoList) {
            if (!(videoList.isEmpty())) {
                mVideosAdapter.setVideosData(videoList);
            }
        }
    }

    /**
     * The background worker that executes the calls to the MovieDB service
     * Using an Inner class to avoid convolution when having to manipulate the
     * View elements in the fragment.
     */
    public class FetchReviewsTask extends AsyncTask<String[], Void, List<Review>> {

        private final String TAG = FetchReviewsTask.class.getSimpleName();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Review> doInBackground(String[]... params) {
            String method = params[0][0];
            Map<String, String> mapping = new HashMap<>();

            mapping.put(NetworkUtilities.getMoviedbLanguageQueryParam(), MovieListFragment.getMovieLocale());

            URL url = NetworkUtilities.buildUrl(method, mapping);

            try {
                String response = NetworkUtilities.getResponseFromHttpUrl(url);
                Log.d(TAG, response);
                JSONObject responseJson = new JSONObject(response);

                return MovieDbJsonUtilities.getReviewsListFromJson(responseJson);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Review> reviewList) {
            if (!(reviewList.isEmpty())) {
                mReviewsAdapter.setReviewsData(reviewList);
            }
        }
    }

}
