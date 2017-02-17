package com.gbozza.android.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gbozza.android.popularmovies.adapters.ReviewsAdapter;
import com.gbozza.android.popularmovies.adapters.VideosAdapter;
import com.gbozza.android.popularmovies.models.Movie;
import com.gbozza.android.popularmovies.models.Review;
import com.gbozza.android.popularmovies.models.Video;
import com.gbozza.android.popularmovies.tasks.FetchReviewsTask;
import com.gbozza.android.popularmovies.tasks.FetchVideosTask;
import com.gbozza.android.popularmovies.utilities.NetworkUtilities;
import com.gbozza.android.popularmovies.utilities.SpannableUtilities;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import com.gbozza.android.popularmovies.data.FavouriteMoviesContract;

/**
 * Activity used to display Movie details, like release date, vote average, etc..
 */
public class MovieDetailActivity extends AppCompatActivity {

    public static VideosAdapter mVideosAdapter;
    public static ReviewsAdapter mReviewsAdapter;

    private Context mContext;
    private Movie mMovie;

    private final static String LABEL_TEXT_VOTE_AVERAGE = "Vote Average: ";
    private final static String LABEL_TEXT_RELEASE_DATE = "Release Date: ";
    private final static String LABEL_TEXT_OVERVIEW = "Overview: ";

    private static final String INTENT_MOVIE_KEY = "movieObject";
    private static final String BUNDLE_VIDEOS_KEY = "videoList";
    private static final String BUNDLE_REVIEWS_KEY = "reviewList";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();

        Intent parentIntent = getIntent();
        if (parentIntent != null) {
            if (parentIntent.hasExtra(INTENT_MOVIE_KEY)) {
                getSupportActionBar().setHomeButtonEnabled(true);

                setContentView(R.layout.activity_movie_detail);

                ImageView movieBackdropImageView = (ImageView) findViewById(R.id.iv_movie_detail_backdrop);
                final ProgressBar moviePosterProgressBar = (ProgressBar) findViewById(R.id.pb_movie_detail_poster);
                TextView movieVoteAverageTextView = (TextView) findViewById(R.id.tv_movie_detail_vote_average);
                TextView movieReleaseDateTextView = (TextView) findViewById(R.id.tv_movie_detail_release_date);
                TextView movieOverviewTextView = (TextView) findViewById(R.id.tv_movie_detail_overview);
                final TextView moviePosterErrorTextView = (TextView) findViewById(R.id.tv_movie_detail_poster_error);

                mMovie = getIntent().getExtras().getParcelable(INTENT_MOVIE_KEY);

                if (checkFavourite(mMovie.getId())) {
                    ImageView movieFavouriteImageView = (ImageView) findViewById(R.id.iv_movie_favourite);
                    movieFavouriteImageView.setBackgroundResource(R.drawable.ic_star);
                }

                Context context = getApplicationContext();
                Picasso.with(context)
                        .load(mMovie.buildBackdropPath(context))
                        .into(movieBackdropImageView, new Callback() {
                            @Override
                            public void onSuccess() {
                                moviePosterProgressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError() {
                                moviePosterProgressBar.setVisibility(View.GONE);
                                moviePosterErrorTextView.setRotation(-20);
                                moviePosterErrorTextView.setVisibility(View.VISIBLE);
                            }
                        });

                movieVoteAverageTextView.append(SpannableUtilities.makeBold(LABEL_TEXT_VOTE_AVERAGE));
                movieVoteAverageTextView.append(mMovie.getVoteAverage());
                movieReleaseDateTextView.append(SpannableUtilities.makeBold(LABEL_TEXT_RELEASE_DATE));
                movieReleaseDateTextView.append(mMovie.getReleaseDate());
                movieOverviewTextView.append(SpannableUtilities.makeBold(LABEL_TEXT_OVERVIEW));
                movieOverviewTextView.append(mMovie.getOverview());

                setTitle(mMovie.getOriginalTitle());

                LinearLayoutManager videosLinearLayoutManager = new LinearLayoutManager(mContext);
                RecyclerView videosRecyclerView = (RecyclerView) findViewById(R.id.rv_videos);
                videosRecyclerView.setLayoutManager(videosLinearLayoutManager);

                videosRecyclerView.setHasFixedSize(true);
                mVideosAdapter = new VideosAdapter();
                videosRecyclerView.setAdapter(mVideosAdapter);

                LinearLayoutManager reviewsLinearLayoutManager = new LinearLayoutManager(mContext);
                RecyclerView reviewsRecyclerView = (RecyclerView) findViewById(R.id.rv_reviews);
                reviewsRecyclerView.setLayoutManager(reviewsLinearLayoutManager);

                reviewsRecyclerView.setHasFixedSize(true);
                mReviewsAdapter = new ReviewsAdapter();
                reviewsRecyclerView.setAdapter(mReviewsAdapter);

                if (savedInstanceState != null) {
                    ArrayList<Video> videoList = savedInstanceState.getParcelableArrayList(BUNDLE_VIDEOS_KEY);
                    mVideosAdapter.setVideosData(videoList);
                    ArrayList<Review> reviewList = savedInstanceState.getParcelableArrayList(BUNDLE_REVIEWS_KEY);
                    mReviewsAdapter.setReviewsData(reviewList);
                } else {
                    load("videos", mMovie.getId());
                    load("reviews", mMovie.getId());
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        List<Video> videoList = mVideosAdapter.getVideosData();
        if (videoList != null) {
            ArrayList<Video> videoArrayList = new ArrayList<>(mVideosAdapter.getVideosData());
            outState.putParcelableArrayList(BUNDLE_VIDEOS_KEY, videoArrayList);
        } else {
            // TODO implement video error case
        }

        List<Review> reviewList = mReviewsAdapter.getReviewsData();
        if (reviewList != null) {
            ArrayList<Review> reviewArrayList = new ArrayList<>(mReviewsAdapter.getReviewsData());
            outState.putParcelableArrayList(BUNDLE_REVIEWS_KEY, reviewArrayList);
        } else {
            // TODO implement review error case
        }
    }

    /**
     * A method that invokes the AsyncTask to populate the RecyclerView,
     * it's based on the sorting option selected by the user. Default is "most popular"
     *
     * @param element the element type to load
     * @param movieId the movie id for the specific videos we need
     */
    public void load(String element, int movieId) {
        if (NetworkUtilities.isOnline(mContext)) {
            String method;
            switch (element) {
                case "videos":
                    method = NetworkUtilities.getMoviedbMethodVideos(movieId);
                    String[] videos = new String[]{method};
                    new FetchVideosTask(mContext).execute(videos);
                    break;
                case "reviews":
                    method = NetworkUtilities.getMoviedbMethodReviews(movieId);
                    String[] reviews = new String[]{method};
                    new FetchReviewsTask(mContext).execute(reviews);
                    break;
                default:
                    return;
            }
        } else {
            // TODO implement error case
        }
    }

    private boolean checkFavourite(int movieId) {
        boolean favourite = false;
        String[] selectionArgs = { String.valueOf(movieId) };
        Uri uri = FavouriteMoviesContract.FavouriteMovieEntry.buildFavouriteUriWithMovieId(movieId);
        Cursor cursor = getContentResolver().query(uri,
                null,
                FavouriteMoviesContract.FavouriteMovieEntry.COLUMN_MOVIE_ID + "=?",
                selectionArgs,
                null);
        if (null != cursor && cursor.getCount() != 0) {
            favourite = true;
            cursor.close();
        }
        return favourite;
    }

    public void favouriteMovie(View view) {
        if (checkFavourite(mMovie.getId())) {
            Uri unfavouriteUri = FavouriteMoviesContract.FavouriteMovieEntry.buildFavouriteUriWithMovieId(mMovie.getId());
            getContentResolver().delete(unfavouriteUri, null, null);

            Toast.makeText(getBaseContext(), getString(R.string.movie_favourite_off_toast_msg), Toast.LENGTH_LONG).show();

            ImageView movieFavouriteImageView = (ImageView) findViewById(R.id.iv_movie_favourite);
            movieFavouriteImageView.setBackgroundResource(R.drawable.ic_star_border_black);
        } else {
            ContentValues contentValues = new ContentValues();
            contentValues.put(FavouriteMoviesContract.FavouriteMovieEntry.COLUMN_MOVIE_ID, mMovie.getId());
            contentValues.put(FavouriteMoviesContract.FavouriteMovieEntry.COLUMN_BACKDROP_PATH, mMovie.getBackdropPath());
            contentValues.put(FavouriteMoviesContract.FavouriteMovieEntry.COLUMN_POSTER_PATH, mMovie.getPosterPath());
            contentValues.put(FavouriteMoviesContract.FavouriteMovieEntry.COLUMN_OVERVIEW, mMovie.getOverview());
            contentValues.put(FavouriteMoviesContract.FavouriteMovieEntry.COLUMN_TITLE, mMovie.getOriginalTitle());
            contentValues.put(FavouriteMoviesContract.FavouriteMovieEntry.COLUMN_RELEASE_DATE, mMovie.getReleaseDate());
            contentValues.put(FavouriteMoviesContract.FavouriteMovieEntry.COLUMN_VOTE_AVERAGE, mMovie.getVoteAverage());
            Uri favouriteUri = getContentResolver().insert(FavouriteMoviesContract.FavouriteMovieEntry.CONTENT_URI, contentValues);

            if (favouriteUri != null) {
                Toast.makeText(getBaseContext(), getString(R.string.movie_favourite_on_toast_msg), Toast.LENGTH_LONG).show();

                ImageView movieFavouriteImageView = (ImageView) findViewById(R.id.iv_movie_favourite);
                movieFavouriteImageView.setBackgroundResource(R.drawable.ic_star);
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

}
