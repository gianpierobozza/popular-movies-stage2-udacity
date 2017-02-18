package com.gbozza.android.popularmovies.tasks;

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

    private static final String TAG = FetchVideosTask.class.getSimpleName();

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