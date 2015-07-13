package com.wolfpakapp.wolfpak.leaderboard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.squareup.picasso.Picasso;
import com.wolfpakapp.wolfpak.R;
import com.wolfpakapp.wolfpak.WolfpakLikeClient;

import java.util.HashMap;
import java.util.List;

/**
 * The LeaderboardAdapter provides a binding from a set of
 * {@link com.wolfpakapp.wolfpak.leaderboard.LeaderboardListItem} objects to a
 * {@link RecyclerView}.
 *
 * @see android.support.v7.widget.RecyclerView.Adapter
 * @see RecyclerView
 * @see com.wolfpakapp.wolfpak.leaderboard.LeaderboardAdapter.ViewHolder
 */
public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {
    private List<LeaderboardListItem> listItems;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout mLayout;
    private LeaderboardActivity mActivity;

//    private ImageView animatingView;
    private Animator mCurrentAnimator;

    /**
     * Instantiate a new LeaderboardAdapter with the given List of LeaderboardListItem objects
     * @param mActivity The Activity which created the Adapter
     * @param listItems The List of LeaderboardListItem objects
     */
    public LeaderboardAdapter(LeaderboardActivity mActivity, List<LeaderboardListItem> listItems) {
        this.listItems = listItems;
        this.mActivity = mActivity;
        mLayout = (SwipeRefreshLayout) mActivity.findViewById(R.id.leaderboard_swipe_refresh_layout);
//        animatingView = (ImageView) mActivity.findViewById(R.id.leaderboard_animating_view);
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type to represent an item.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     * @return A new {@link com.wolfpakapp.wolfpak.leaderboard.LeaderboardAdapter.ViewHolder}
     *         that holds a View of the given view type.
     */
    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.leaderboard_list_item, parent, false);

        return new ViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method
     * uppdates the contents of the
     * {@link com.wolfpakapp.wolfpak.leaderboard.LeaderboardAdapter.ViewHolder#listItemView} to
     * reflect the item at the given position.
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *               item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        LeaderboardListItem listItem = listItems.get(position);
        holder.bindListItem(listItem);
    }

    /**
     *  Called by RecyclerView when it starts observing this Adapter. Stores a reference to the
     *  RecyclerView instance that the Adapter is attached to.
     */
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    /**
     * The ViewHolder describes a leaderboard item view and metadata about its place within the
     * RecyclerView.
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        private View listItemView;

        private LeaderboardListItem listItem;

        private TextView listItemTextView;
        private TextView listItemViewCountTextView;

        private LinearLayout contentLayout;

        private ImageView listItemImageView;
        private VideoView listItemVideoView;

        private final int previewDimen;

        /**
         * Instantiate the ViewHolder with the given item View
         * @param view The View that represents a single list item
         */
        public ViewHolder(View view) {
            super(view);

            listItemView = view;

            listItemTextView = (TextView) listItemView.findViewById(R.id.leaderboard_item_text_view);
            listItemViewCountTextView = (TextView) listItemView.findViewById(R.id.leaderboard_item_view_count_text_view);

            contentLayout = (LinearLayout) listItemView.findViewById(R.id.leaderboard_item_content_view);

            previewDimen = (int) mActivity.getResources().getDimension(R.dimen.leaderboard_item_preview_size);
        }

        /**
         * Bind a list item to the ViewHolder
         * @param listItem The {@link LeaderboardListItem} that will be bound to the ViewHolder
         */
        public void bindListItem(final LeaderboardListItem listItem) {
            this.listItem = listItem;

            listItemTextView.setText(listItem.getContentString());

            // Ensure that the view count is the correct color
            updateViewCountBackground(listItem.getStatus());
            listItemViewCountTextView.setOnTouchListener(new ViewCountOnTouchListener());

            contentLayout.removeAllViews();

            // Load the content into the appropriate thumnbnail view and add it to the layout
            if (listItem.isImage()) {
                listItemImageView = new ImageView(mActivity);
                listItemImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                listItemImageView.setCropToPadding(true);
                Picasso.with(mActivity).load(listItem.getUrl()).into(listItemImageView);

                listItemImageView.setOnClickListener(new ImageViewOnClickListener());

                contentLayout.addView(listItemImageView);
            } else {
                listItemVideoView = new VideoView(mActivity);
                Uri uri = Uri.parse(listItem.getUrl());
                listItemVideoView.setVideoURI(uri);

                listItemVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        listItemVideoView.setOnTouchListener(new VideoViewOnTouchListener());
                    }
                });

                contentLayout.addView(listItemVideoView);
            }
        }

        /**
         * The ViewCountOnTouchListener encapsulates the callback when touch events are sent to the
         * ViewCountTextView. It allows  the View to be dragged and released and defines the
         * appropriate behaviors for said actions.
         */
        private final class ViewCountOnTouchListener implements View.OnTouchListener {
            private int activePointerId = MotionEvent.INVALID_POINTER_ID;

            private float initialViewX;
            private float initialViewY;

            private final int ANIM_DURATION = 350;
            private final Interpolator VIEW_COUNT_INTERPOLATOR = new OvershootInterpolator(1.4f);

            float lastTouchX = 0;
            float lastTouchY = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int action = MotionEventCompat.getActionMasked(event);

                switch (action) {
                    case MotionEvent.ACTION_DOWN: {
                        // When a finger presses on the view count, store the original location of
                        // the View and record the pointer ID of the finger.
                        mLayout.setEnabled(false);

                        final float x = event.getRawX();
                        final float y = event.getRawY();

                        lastTouchX = x;
                        lastTouchY = y;

                        activePointerId = MotionEventCompat.getPointerId(event, 0);

                        // Ensure that the View receives touch events before any elements under it.
                        v.getParent().requestDisallowInterceptTouchEvent(true);

                        initialViewX = v.getX();
                        initialViewY = v.getY();

                        // Ensure that the RecyclerView draws the listItemView containing the view
                        // count before all other listItemViews.
                        final int indexOfFrontChild = recyclerView.indexOfChild(listItemView);
                        recyclerView.setChildDrawingOrderCallback(new RecyclerView.ChildDrawingOrderCallback() {
                            private int nextChildIndexToRender;
                            @Override
                            public int onGetChildDrawingOrder(int childCount, int iteration) {
                                if (iteration == childCount - 1) {
                                    nextChildIndexToRender = 0;
                                    return indexOfFrontChild;
                                } else {
                                    if (nextChildIndexToRender == indexOfFrontChild) {
                                        nextChildIndexToRender++;
                                    }
                                    return nextChildIndexToRender++;
                                }
                            }
                        });
                        recyclerView.invalidate();

                        break;
                    }
                    case MotionEvent.ACTION_MOVE: {
                        final float x = event.getRawX();
                        final float y = event.getRawY();

                        // Calculate the change in pointer position.
                        final float dx = x - lastTouchX;
                        final float dy = y - lastTouchY;

                        // Adjusts the view count based on dx and dy.
                        v.setX(v.getX() + dx);
                        v.setY(v.getY() + dy);

                        // Update the color of the view count.
                        if (v.getY() < initialViewY) {
                            if (listItem.getStatus() == LeaderboardListItem.VoteStatus.UPVOTED) {
                                updateViewCountBackground(LeaderboardListItem.VoteStatus.NOT_VOTED);
                            } else {
                                updateViewCountBackground(LeaderboardListItem.VoteStatus.UPVOTED);
                            }
                        } else if (v.getY() > initialViewY) {
                            if (listItem.getStatus() == LeaderboardListItem.VoteStatus.DOWNVOTED) {
                                updateViewCountBackground(LeaderboardListItem.VoteStatus.NOT_VOTED);
                            } else {
                                updateViewCountBackground(LeaderboardListItem.VoteStatus.DOWNVOTED);
                            }
                        }

                        lastTouchX = x;
                        lastTouchY = y;

                        break;
                    }
                    case MotionEvent.ACTION_POINTER_UP: {
                        // When a finger is lifted (but not the last finger)
                        final int pointerIndex = MotionEventCompat.getActionIndex(event);
                        final int pointerId = MotionEventCompat.findPointerIndex(event, pointerIndex);
                        // Ensure that the correct pointer ID is being used (in case the view
                        // switched pointers
                        if (pointerId == activePointerId) {
                            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                            lastTouchX = event.getRawX();
                            lastTouchY = event.getRawY();
                            activePointerId = MotionEventCompat.getPointerId(event, newPointerIndex);
                        }

                        break;
                    }

                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP: {
                        mLayout.setEnabled(true);

                        // Determine the new VoteStatus of the post.
                        LeaderboardListItem.VoteStatus newStatus = null;
                        if (v.getY() < initialViewY) {
                            if (listItem.getStatus() == LeaderboardListItem.VoteStatus.UPVOTED) {
                                newStatus = LeaderboardListItem.VoteStatus.NOT_VOTED;
                            } else {
                                newStatus = LeaderboardListItem.VoteStatus.UPVOTED;
                            }
                        } else if (v.getY() > initialViewY) {
                            if (listItem.getStatus() == LeaderboardListItem.VoteStatus.DOWNVOTED) {
                                newStatus = LeaderboardListItem.VoteStatus.NOT_VOTED;
                            } else {
                                newStatus = LeaderboardListItem.VoteStatus.DOWNVOTED;
                            }
                        }
                        // Update the post's VoteStatus and the view count background.
                        listItem.setStatus(newStatus);
                        updateViewCountBackground(newStatus);

                        // Update the vote status of the post on the server.
                        WolfpakLikeClient.updateVoteStatus(listItem.getId(), newStatus);

                        activePointerId = MotionEvent.INVALID_POINTER_ID;

                        // Animate the view count so that it returns to the starting position.
                        AnimatorSet animatorSet = new AnimatorSet();
                        ObjectAnimator xAnim = ObjectAnimator.ofFloat(v, "X", v.getX(), initialViewX);
                        ObjectAnimator yAnim = ObjectAnimator.ofFloat(v, "Y", v.getY(), initialViewY);
                        animatorSet.play(xAnim).with(yAnim);
                        animatorSet.setDuration(ANIM_DURATION);
                        animatorSet.setInterpolator(VIEW_COUNT_INTERPOLATOR);

                        animatorSet.start();

                        // Reset the RecyclerView's drawing order of the posts.
                        recyclerView.setChildDrawingOrderCallback(null);
                    }
                }

                return true;
            }
        }

        /**
         * Update the ViewCountTextView. Sets the background color based on the given VoteStatus,
         * but does NOT update the {@link LeaderboardListItem} VoteStatus! Updates the view count so
         * that it reflects the LeaderboardListItem's CURRENT VoteStatus.
         * @param status The VoteStatus that determines the View's background color.
         */
        private void updateViewCountBackground(LeaderboardListItem.VoteStatus status) {
            GradientDrawable bg = (GradientDrawable) listItemViewCountTextView.getResources()
                    .getDrawable(R.drawable.leaderboard_item_view_count_background);
            int statusColor = status.getStatusColor(mActivity);
            bg.setColor(statusColor);
            listItemViewCountTextView.setBackground(bg);
            listItemViewCountTextView.setText(Integer.toString(listItem.getUpdatedVoteCount()));
            listItemViewCountTextView.invalidate();
        }

        /**
         * The ImageViewOnClickListener encapsulates the callback when the ImageView is clicked.
         * This is intended for the thumbnail images in each list item.
         */
        private final class ImageViewOnClickListener implements View.OnClickListener {
            @Override
            public void onClick(final View view) {
                final ImageView expandedImageView = mActivity.getExpandedImageView();

                Picasso.with(mActivity).load(listItem.getUrl()).into(expandedImageView);
                Picasso.with(mActivity).load(listItem.getUrl()).into(mActivity.getAnimatingImageView());

                animateView(listItemImageView, expandedImageView);
            }
        }

        /**
         * The VideoViewOnTouchListener encapsulates the callback when the VideoView is clicked.
         * This is intended for the thumbnail videos in each list item.
         */
        private final class VideoViewOnTouchListener implements View.OnTouchListener {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int action = MotionEventCompat.getActionMasked(event);
                switch (action) {
                    case MotionEvent.ACTION_UP: {
                        final VideoView expandedVideoView = mActivity.getExpandedVideoView();
                        Uri uri = Uri.parse(listItem.getUrl());
                        expandedVideoView.setVideoURI(uri);

                        MediaMetadataRetriever mRetriever = new MediaMetadataRetriever();
                        mRetriever.setDataSource(listItem.getUrl(), new HashMap<String, String>());
                        Bitmap frame = mRetriever.getFrameAtTime(0);
                        mActivity.getAnimatingImageView().setImageBitmap(frame);

                        animateView(listItemVideoView, expandedVideoView);
                    }
                }

                return true;
            }
        }

        /**
         * Animate the thumbnail view and set the appropriate behavior for the expanded View.
         * @param initialView The View that was clicked (ImageView) or touched (VideoView).
         * @param expandView The fullscreen View. Will be an ImageView when an ImageView was clicked
         *                   and a VideoView when a VideoView was touched.
         */
        private void animateView(final View initialView, final View expandView) {
            final int ANIM_DURATION = 500;
            final Interpolator THUMBNAIL_EXPANDING_INTERPOLATOR = new LinearInterpolator();

            final Rect startBounds = new Rect();
            final Rect finalBounds = new Rect();
            final Point globalOffset = new Point(0, 0);

            final ImageView animatingView = mActivity.getAnimatingImageView();
//            final int HEIGHT_OFFSET = 75;

//            mActivity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
//            mActivity.getSupportActionBar().hide();

            initialView.getGlobalVisibleRect(startBounds);
            expandView.getGlobalVisibleRect(finalBounds);
            startBounds.offset(-globalOffset.x, -globalOffset.y);
            finalBounds.offset(-globalOffset.x, -globalOffset.y);

            Log.d("START", startBounds.toString());
            Log.d("FINAL", finalBounds.toString());

            if (mCurrentAnimator != null) {
                mCurrentAnimator.cancel();
            }

            initialView.setAlpha(0f);

            animatingView.setVisibility(ImageView.VISIBLE);

            ValueAnimator widthAnimator = ValueAnimator.ofInt(previewDimen, finalBounds.width());
            widthAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    clipViewToGlobalBounds(animatingView);
                    animatingView.getLayoutParams().width = (int) animation.getAnimatedValue();
                    animatingView.requestLayout();
                }
            });
            ValueAnimator heightAnimator = ValueAnimator.ofInt(previewDimen, finalBounds.height());
            heightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    clipViewToGlobalBounds(animatingView);
                    animatingView.getLayoutParams().height = (int) animation.getAnimatedValue();
                    animatingView.requestLayout();
                }
            });

            AnimatorSet set = new AnimatorSet();
            set.play(ObjectAnimator.ofFloat(animatingView, View.X, startBounds.left, finalBounds.left))
                    .with(ObjectAnimator.ofFloat(animatingView, View.Y, startBounds.top, finalBounds.top))
                    .with(widthAnimator).with(heightAnimator);
            set.setDuration(ANIM_DURATION);
            set.setInterpolator(THUMBNAIL_EXPANDING_INTERPOLATOR);
            if (listItem.isImage()) {
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mCurrentAnimator = null;
                        expandView.setVisibility(View.VISIBLE);
                        animatingView.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        mCurrentAnimator = null;
                        animatingView.setVisibility(View.GONE);
                    }
                });
                expandView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mCurrentAnimator != null) {
                            mCurrentAnimator.cancel();
                        }

                        animatingView.setVisibility(View.VISIBLE);
                        expandView.setVisibility(View.GONE);

                        ValueAnimator widthAnimator = ValueAnimator.ofInt(finalBounds.width(), previewDimen);
                        widthAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                clipViewToGlobalBounds(animatingView);
                                animatingView.getLayoutParams().width = (int) animation.getAnimatedValue();
                                animatingView.requestLayout();

                            }
                        });
                        ValueAnimator heightAnimator = ValueAnimator.ofInt(finalBounds.height(), previewDimen);
                        heightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                clipViewToGlobalBounds(animatingView);
                                animatingView.getLayoutParams().height = (int) animation.getAnimatedValue();
                                animatingView.requestLayout();
                            }
                        });

                        AnimatorSet set = new AnimatorSet();
                        set.play(ObjectAnimator.ofFloat(animatingView, View.X, finalBounds.left, startBounds.left))
                                .with(ObjectAnimator.ofFloat(animatingView, View.Y, finalBounds.top, startBounds.top))
                                .with(widthAnimator).with(heightAnimator);
                        set.setDuration(ANIM_DURATION);
                        set.setInterpolator(THUMBNAIL_EXPANDING_INTERPOLATOR);
                        set.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                initialView.setAlpha(1f);
                                animatingView.setVisibility(View.GONE);
                                mCurrentAnimator = null;
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {
                                initialView.setAlpha(1f);
                                animatingView.setVisibility(View.GONE);
                                mCurrentAnimator = null;
                            }
                        });

                        set.start();
                        mCurrentAnimator = set;

//                        mActivity.getWindow().getDecorView().setSystemUiVisibility(0);
//                        mActivity.getSupportActionBar().show();
                    }
                });
            } else {
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mCurrentAnimator = null;
                        expandView.setVisibility(View.VISIBLE);
                        animatingView.setVisibility(View.GONE);

                        VideoView expandVideoView = (VideoView) expandView;
                        expandVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                if (mCurrentAnimator != null) {
                                    mCurrentAnimator.cancel();
                                }

                                animatingView.setVisibility(View.VISIBLE);
                                expandView.setVisibility(View.GONE);

                                MediaMetadataRetriever mRetriever = new MediaMetadataRetriever();
                                mRetriever.setDataSource(listItem.getUrl(), new HashMap<String, String>());
                                Bitmap frame = mRetriever.getFrameAtTime();
                                animatingView.setImageBitmap(frame);

                                ValueAnimator widthAnimator = ValueAnimator.ofInt(finalBounds.width(), previewDimen);
                                widthAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                    @Override
                                    public void onAnimationUpdate(ValueAnimator animation) {
                                        clipViewToGlobalBounds(animatingView);
                                        animatingView.getLayoutParams().width = (int) animation.getAnimatedValue();
                                        animatingView.requestLayout();
                                    }
                                });
                                ValueAnimator heightAnimator = ValueAnimator.ofInt(finalBounds.height(), previewDimen);
                                heightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                    @Override
                                    public void onAnimationUpdate(ValueAnimator animation) {
                                        clipViewToGlobalBounds(animatingView);
                                        animatingView.getLayoutParams().height = (int) animation.getAnimatedValue();
                                        animatingView.requestLayout();
                                    }
                                });

                                AnimatorSet set = new AnimatorSet();
                                set.play(ObjectAnimator.ofFloat(animatingView, View.X, finalBounds.left, startBounds.left))
                                        .with(ObjectAnimator.ofFloat(animatingView, View.Y, finalBounds.top, startBounds.top))
                                        .with(widthAnimator).with(heightAnimator);
                                set.setDuration(ANIM_DURATION);
                                set.setInterpolator(THUMBNAIL_EXPANDING_INTERPOLATOR);
                                set.addListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        initialView.setAlpha(1f);
                                        animatingView.setVisibility(View.GONE);
                                        mCurrentAnimator = null;
                                    }

                                    @Override
                                    public void onAnimationCancel(Animator animation) {
                                        initialView.setAlpha(1f);
                                        animatingView.setVisibility(View.GONE);
                                        mCurrentAnimator = null;
                                    }
                                });

                                set.start();
                                mCurrentAnimator = set;

//                                mActivity.getWindow().getDecorView().setSystemUiVisibility(0);
//                                mActivity.getSupportActionBar().show();
                            }
                        });

                        expandVideoView.start();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        mCurrentAnimator = null;
                        animatingView.setVisibility(View.GONE);

//                        mActivity.getWindow().getDecorView().setSystemUiVisibility(0);
//                        mActivity.getSupportActionBar().show();
                    }
                });
            }

            set.start();
            mCurrentAnimator = set;
        }

        /**
         * Ensure that the View is clipped to its global visible bounds.
         * @param view The View to be clipped
         */
        private void clipViewToGlobalBounds(View view) {
            Rect clipBounds = new Rect();
            Point globalOffset = new Point();
            view.getGlobalVisibleRect(clipBounds, globalOffset);
            clipBounds.offset(-globalOffset.x, -globalOffset.y);
            view.setClipBounds(clipBounds);
        }
    }
}
