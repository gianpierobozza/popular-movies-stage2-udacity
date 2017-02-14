package com.gbozza.android.popularmovies.adapters;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
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

    /**
     * Inner class to represent the ViewHolder for the Adapter
     */
    public class VideosAdapterViewHolder extends RecyclerView.ViewHolder {
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
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.videos_view;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        return new VideosAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(VideosAdapterViewHolder videosAdapterViewHolder, int position) {
        final Video video = mVideoList.get(position);
        Picasso.with(videosAdapterViewHolder.mContext)
                .load(buildVideoUrl(video))
                .into(videosAdapterViewHolder.mVideoThumbImageView);
        videosAdapterViewHolder.mVideoNameTextView.setText(video.getName());

        videosAdapterViewHolder.mVideoThumbImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + video.getKey()));
                Intent webIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://www.youtube.com/watch?v=" + video.getKey()));
                try {
                    view.getContext().startActivity(appIntent);
                } catch (ActivityNotFoundException ex) {
                    view.getContext().startActivity(webIntent);
                }
            }
        });
    }

    private String buildVideoUrl(Video video) {
        return "http://img.youtube.com/vi/" + video.getKey() + "/0.jpg";
    }

    @Override
    public int getItemCount() {
        if (null == mVideoList) return 0;
        return mVideoList.size();
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
