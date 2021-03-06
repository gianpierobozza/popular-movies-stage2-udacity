package com.gbozza.android.popularmovies.utilities;

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

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Scanner;

/**
 * Utilities used to communicate with the The Movie DB services over the internet
 */
public final class NetworkUtilities {

    // TODO IMPORTANT: Substitute the actual key before submit with <your_api_key>
    private static final String MOVIEDB_API_KEY = "<your_api_key>";
    private static final String MOVIEDB_API_KEY_QUERY_PARAM = "api_key";
    private static final String MOVIEDB_LANGUAGE_QUERY_PARAM = "language";
    private static final String MOVIEDB_PAGE_QUERY_PARAM = "page";
    private static final String MOVIEDB_API_URL = "https://api.themoviedb.org/3";
    private static final String MOVIEDB_METHOD_POPULAR = "/movie/popular";
    private static final String MOVIEDB_METHOD_RATED = "/movie/top_rated";
    private static final String MOVIEDB_METHOD_VIDEOS = "/movie/#/videos";
    private static final String MOVIEDB_METHOD_REVIEWS = "/movie/#/reviews";

    private static final String TAG = NetworkUtilities.class.getSimpleName();

    /**
     * A method to check if the device has Internet Connectivity
     *
     * @param context application context
     * @return true or false based on the the availability of connectivity
     */
    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return null != netInfo && netInfo.isConnected();
    }

    /**
     * Builds the URL based on method string and a Map of key-value parameters
     *
     * @param method the method to call, will be appended to the api url
     * @param params a Map with the key-values to be appended as query parameters
     * @return a URL for a request to the The Movie DB API service
     */
    public static URL buildUrl(String method, Map<String, String> params) {
        Uri.Builder builder = Uri.parse(MOVIEDB_API_URL + method).buildUpon();
        Log.v(TAG, "Parse url '" + MOVIEDB_API_URL + "' with method '" + method + "'");
        builder.appendQueryParameter(MOVIEDB_API_KEY_QUERY_PARAM, MOVIEDB_API_KEY);
        Log.v(TAG, "Append api key");
        for (Map.Entry<String, String> param : params.entrySet()) {
            builder.appendQueryParameter(param.getKey(), param.getValue());
            Log.v(TAG, "Append param '" + param.getKey() + "' with value '" + param.getValue() + "'");
        }

        Uri uri = builder.build();
        URL url = null;

        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Log.v(TAG, "Built URI " + url);

        return url;
    }

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    /**
     * Getter method to get the Language query param value
     *
     * @return the Language query param value
     */
    public static String getMoviedbLanguageQueryParam() { return MOVIEDB_LANGUAGE_QUERY_PARAM; }

    /**
     * Getter method to get the Page query param value
     *
     * @return the Page param value
     */
    public static String getMoviedbPageQueryParam() {
        return MOVIEDB_PAGE_QUERY_PARAM;
    }

    /**
     * Getter method to get the Most Popular query param value
     *
     * @return the Most Popular param value
     */
    public static String getMoviedbMethodPopular() {
        return MOVIEDB_METHOD_POPULAR;
    }

    /**
     * Getter method to get the Rated query param value
     *
     * @return the Rated param value
     */
    public static String getMoviedbMethodRated() {
        return MOVIEDB_METHOD_RATED;
    }

    /**
     * Getter method to get the Videos query param value
     *
     * @param movieId the id of the movie.
     * @return the Videos param value
     */
    public static String getMoviedbMethodVideos(int movieId) {
        return MOVIEDB_METHOD_VIDEOS.replace("#", String.valueOf(movieId));
    }

    /**
     * Getter method to get the Reviews query param value
     *
     * @param movieId the id of the movie.
     * @return the Reviews param value
     */
    public static String getMoviedbMethodReviews(int movieId) {
        return MOVIEDB_METHOD_REVIEWS.replace("#", String.valueOf(movieId));
    }
}
