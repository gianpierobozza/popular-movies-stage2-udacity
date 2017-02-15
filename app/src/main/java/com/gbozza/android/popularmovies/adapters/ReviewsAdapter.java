package com.gbozza.android.popularmovies.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gbozza.android.popularmovies.R;
import com.gbozza.android.popularmovies.models.Review;
import com.gbozza.android.popularmovies.utilities.SpannableUtilities;

import java.util.List;

/**
 * Adapter to manage the Reviews RecyclerView in the Movie Detail Activity
 */
public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewsAdapterViewHolder> {

    private List<Review> mReviewList;

    final static String LABEL_TEXT_REVIEW_AUTHOR = "Review by: ";

    /**
     * Inner class to represent the ViewHolder for the Adapter
     */
    public class ReviewsAdapterViewHolder extends RecyclerView.ViewHolder {
        final TextView mReviewAuthorTextView;
        final TextView mReviewContentTextView;
        final TextView mReviewSeeMoreTextView;
        final TextView mReviewCollapeTextView;

        Context mContext;

        /**
         * Constructor to the ViewHolder class
         *
         * @param view the we are going to inflate
         */
        ReviewsAdapterViewHolder(View view) {
            super(view);
            mReviewAuthorTextView = (TextView) view.findViewById(R.id.tv_review_author);
            mReviewContentTextView = (TextView) view.findViewById(R.id.tv_review_content);
            mReviewSeeMoreTextView = (TextView) view.findViewById(R.id.tv_review_see_more);
            mReviewCollapeTextView = (TextView) view.findViewById(R.id.tv_review_collapse);
            mContext = view.getContext();
        }
    }

    @Override
    public ReviewsAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.reviews_view;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        return new ReviewsAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ReviewsAdapterViewHolder reviewsAdapterViewHolder, int position) {
        Review review = mReviewList.get(position);
        reviewsAdapterViewHolder.mReviewAuthorTextView.append(SpannableUtilities.makeBold(LABEL_TEXT_REVIEW_AUTHOR));
        reviewsAdapterViewHolder.mReviewAuthorTextView.append(review.getAuthor());
        reviewsAdapterViewHolder.mReviewContentTextView.setText(review.getContent());
        reviewsAdapterViewHolder.mReviewSeeMoreTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reviewsAdapterViewHolder.mReviewContentTextView.setMaxLines(Integer.MAX_VALUE);
                reviewsAdapterViewHolder.mReviewCollapeTextView.setVisibility(View.VISIBLE);
                reviewsAdapterViewHolder.mReviewSeeMoreTextView.setVisibility(View.INVISIBLE);
            }
        });
        reviewsAdapterViewHolder.mReviewCollapeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reviewsAdapterViewHolder.mReviewContentTextView.setMaxLines(4);
                reviewsAdapterViewHolder.mReviewCollapeTextView.setVisibility(View.INVISIBLE);
                reviewsAdapterViewHolder.mReviewSeeMoreTextView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (null == mReviewList) return 0;
        return mReviewList.size();
    }

    /**
     * Setter method for the Review List Object
     *
     * @param reviewList the List containing Review Objects
     */
    public void setReviewsData(List<Review> reviewList) {
        if (null == mReviewList) {
            mReviewList = reviewList;
        } else {
            mReviewList.addAll(reviewList);
        }
        notifyDataSetChanged();
    }

    /**
     * Getter method for the Review List Object
     *
     * @return the Review List
     */
    public List<Review> getReviewsData() {
        return mReviewList;
    }
}
