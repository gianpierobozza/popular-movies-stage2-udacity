package com.gbozza.android.popularmovies.adapters;

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

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gbozza.android.popularmovies.R;
import com.gbozza.android.popularmovies.models.Video;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Adapter to manage the Videos RecyclerView in the Movie Detail Activity
 */
public class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.VideosAdapterViewHolder> {

    private List<Video> mVideoList;
    private Context mContext;

    /**
     * Inner class to represent the ViewHolder for the Adapter
     */
    class VideosAdapterViewHolder extends RecyclerView.ViewHolder {
        final ImageView mVideoThumbImageView;
        final TextView mVideoNameTextView;
        Context mContext;

        /**
         * Constructor to the ViewHolder class
         *
         * @param view the we are going to inflate
         */
        VideosAdapterViewHolder(View view) {
            super(view);
            mVideoThumbImageView = (ImageView) view.findViewById(R.id.iv_video_thumbnail);
            mVideoNameTextView = (TextView) view.findViewById(R.id.tv_video_name);
            mContext = view.getContext();
        }
    }

    @Override
    public VideosAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        mContext = viewGroup.getContext();
        int layoutIdForListItem = R.layout.videos_view;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        return new VideosAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(VideosAdapterViewHolder videosAdapterViewHolder, int position) {
        final Video video = mVideoList.get(position);
        Picasso.with(videosAdapterViewHolder.mContext)
                .load(buildVideoUrl(video.getKey()))
                .into(videosAdapterViewHolder.mVideoThumbImageView);
        videosAdapterViewHolder.mVideoNameTextView.setText(video.getName());

        videosAdapterViewHolder.mVideoThumbImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Resources.getSystem()
                        .getString(R.string.movie_detail_youtube_vendor) + video.getKey()));
                Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Resources.getSystem()
                        .getString(R.string.movie_detail_youtube_video_link) + video.getKey()));
                try {
                    view.getContext().startActivity(appIntent);
                } catch (ActivityNotFoundException ex) {
                    view.getContext().startActivity(webIntent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (null == mVideoList) return 0;
        return mVideoList.size();
    }

    /**
     * Utility method to build the Youtube Thumbnail Image from a video key
     *
     * @param videoKey the identification key of a video
     * @return the url of the thumb service
     */
    private String buildVideoUrl(String videoKey) {
        return mContext.getString(R.string.movie_detail_youtube_thumbnail_service).replace("#", videoKey);
    }

    /**
     * Setter method for the Video List Object
     *
     * @param videoList the List containing Video Objects
     */
    public void setVideosData(List<Video> videoList) {
        if (null == mVideoList) {
            mVideoList = videoList;
        } else {
            mVideoList.addAll(videoList);
        }
        notifyDataSetChanged();
    }

    /**
     * Getter method for the Video List Object
     *
     * @return the Video List
     */
    public List<Video> getVideosData() {
        return mVideoList;
    }

}
