package com.gbozza.android.popularmovies.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gbozza.android.popularmovies.R;
import com.gbozza.android.popularmovies.adapters.MoviesAdapter;
import com.gbozza.android.popularmovies.models.Movie;
import com.gbozza.android.popularmovies.tasks.FetchMoviesTask;
import com.gbozza.android.popularmovies.utilities.BottomRecyclerViewScrollListener;
import com.gbozza.android.popularmovies.utilities.NetworkUtilities;

import java.util.ArrayList;
import java.util.List;

/**
 * A Class that extends Fragment to implement the Movie List structure
 */
public class MovieListFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    // TODO FIX MEMORY LEAK!!!
    public static ProgressBar mLoadingIndicator;
    public static TextView mErrorMessageDisplay;
    public static SwipeRefreshLayout mSwipeContainer;
    public static MoviesAdapter mMoviesAdapter;

    private Context mContext;
    private BottomRecyclerViewScrollListener mScrollListener;
    private int mPage;
    private int mSorting;
    private static String mMovieLocale;

    private static final int SORTING_POPULAR = 1;
    private static final int SORTING_RATED = 2;
    private static final int SORTING_FAVOURITES = 3;
    private static final String BUNDLE_MOVIES_KEY = "movieList";
    private static final String BUNDLE_PAGE_KEY = "currentPage";
    private static final String BUNDLE_SORTING_KEY = "currentSorting";
    private static final String BUNDLE_ERROR_KEY = "errorShown";

    private static final String TAG = MovieListFragment.class.getSimpleName();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Boolean errorShown = false;
        if (savedInstanceState != null) {
            errorShown = savedInstanceState.getBoolean(BUNDLE_ERROR_KEY);
        }

        View rootView = inflater.inflate(R.layout.movie_list_fragment, container, false);
        mContext = getContext();
        setupSharedPreferences();

        if (savedInstanceState != null && !errorShown) {
            mPage = savedInstanceState.getInt(BUNDLE_PAGE_KEY);
            mSorting = savedInstanceState.getInt(BUNDLE_SORTING_KEY);
        } else {
            mPage = 1;
        }

        final int columns = getResources().getInteger(R.integer.grid_columns);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext, columns, GridLayoutManager.VERTICAL, false);
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.rv_posters);
        recyclerView.setLayoutManager(gridLayoutManager);

        recyclerView.setHasFixedSize(true);
        mMoviesAdapter = new MoviesAdapter();
        recyclerView.setAdapter(mMoviesAdapter);

        mLoadingIndicator = (ProgressBar) rootView.findViewById(R.id.pb_loading_indicator);
        mScrollListener = new BottomRecyclerViewScrollListener(gridLayoutManager, mPage) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Log.d(TAG, "Loading page: " + String.valueOf(page));
                mPage = page;
                loadCards(mSorting);
            }
        };
        recyclerView.addOnScrollListener(mScrollListener);

        mSwipeContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.sr_swipe_container);
        mSwipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mErrorMessageDisplay.setVisibility(View.INVISIBLE);
                clearGridView();
                loadCards(mSorting);
            }
        });
        mSwipeContainer.setColorSchemeResources(R.color.colorAccent);

        mErrorMessageDisplay = (TextView) rootView.findViewById(R.id.tv_error_message_display);

        if (savedInstanceState != null && !errorShown) {
            ArrayList<Movie> movieList = savedInstanceState.getParcelableArrayList(BUNDLE_MOVIES_KEY);
            mMoviesAdapter.setMoviesData(movieList);
        } else {
            loadCards(mSorting);
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        List<Movie> movieList = mMoviesAdapter.getMoviesData();
        if (movieList != null) {
            ArrayList<Movie> movieArrayList = new ArrayList<>(mMoviesAdapter.getMoviesData());
            outState.putParcelableArrayList(BUNDLE_MOVIES_KEY, movieArrayList);
            outState.putInt(BUNDLE_PAGE_KEY, mPage);
            outState.putInt(BUNDLE_SORTING_KEY, mSorting);
        } else {
            if (mErrorMessageDisplay.isShown()) {
                outState.putBoolean(BUNDLE_ERROR_KEY, true);
            }
        }
    }

    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        String sorting = sharedPreferences.getString(getString(R.string.pref_sorting_key),
                getResources().getString(R.string.pref_sorting_default));
        if (sorting.equals(getString(R.string.pref_sorting_popular_value))) {
            mSorting = SORTING_POPULAR;
        } else if (sorting.equals(getString(R.string.pref_sorting_rated_value))) {
            mSorting = SORTING_RATED;
        }

        mMovieLocale = sharedPreferences.getString(getString(R.string.pref_locale_key),
                getResources().getString(R.string.pref_locale_default));

        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // TODO implement this?
    }

    public static String getMovieLocale() {
        return mMovieLocale;
    }

    /**
     * A method that invokes the AsyncTask to populate the RecyclerView,
     * it's based on the sorting option selected by the user. Default is "most popular"
     *
     * @param sorting the way of sorting selected by the user
     */
    public void loadCards(int sorting) {
        if (NetworkUtilities.isOnline(mContext)) {
            String method;
            switch (sorting) {
                case SORTING_POPULAR:
                    method = NetworkUtilities.getMoviedbMethodPopular();
                    break;
                case SORTING_RATED:
                    method = NetworkUtilities.getMoviedbMethodRated();
                    break;
                default:
                    method = NetworkUtilities.getMoviedbMethodPopular();
                    break;
            }
            String[] movies = new String[]{method, String.valueOf(mPage)};
            new FetchMoviesTask(mContext).execute(movies);
        } else {
            showErrorMessage(R.string.error_no_connectivity, mContext);
            if (mSwipeContainer.isRefreshing()) {
                mSwipeContainer.setRefreshing(false);
            }
        }
    }

    /**
     * Reset the GridView properties and adapter
     */
    public void clearGridView() {
        mScrollListener.resetState();
        mPage = 1;
        mMoviesAdapter.clear();
    }

    /**
     * Display the specific error message in the TextView
     *
     * @param messageId the resource id of the error string
     */
    public static void showErrorMessage(int messageId, Context context) {
        mErrorMessageDisplay.setText(context.getText(messageId));
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

}
