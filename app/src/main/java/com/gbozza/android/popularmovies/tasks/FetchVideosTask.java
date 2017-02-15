package com.gbozza.android.popularmovies.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.gbozza.android.popularmovies.MovieDetailActivity;
import com.gbozza.android.popularmovies.fragments.MovieListFragment;
import com.gbozza.android.popularmovies.models.Video;
import com.gbozza.android.popularmovies.utilities.MovieDbJsonUtilities;
import com.gbozza.android.popularmovies.utilities.NetworkUtilities;

import org.json.JSONObject;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The background worker that executes the calls to the MovieDB service
 */
public class FetchVideosTask extends AsyncTask<String[], Void, List<Video>> {

    private Context mContext;
    private static final String TAG = FetchVideosTask.class.getSimpleName();

    public FetchVideosTask(Context context) {
        mContext = context;
    }

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
            MovieDetailActivity.mVideosAdapter.setVideosData(videoList);
        } else {
            // TODO implement empty case, hide divider in new layout
        }
    }
}