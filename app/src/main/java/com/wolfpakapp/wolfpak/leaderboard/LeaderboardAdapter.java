package com.wolfpakapp.wolfpak.leaderboard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.squareup.picasso.Picasso;
import com.wolfpakapp.wolfpak.R;

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
    private Activity mActivity;

    private ImageView animatingView;
    private Animator mCurrentAnimator;
    private final Interpolator INTERPOLATOR = new OvershootInterpolator(1.4f);

    public LeaderboardAdapter(Activity mActivity, List<LeaderboardListItem> listItems) {
        this.listItems = listItems;
        this.mActivity = mActivity;
        mLayout = (SwipeRefreshLayout) mActivity.findViewById(R.id.leaderboard_swipe_refresh_layout);
        animatingView = (ImageView) mActivity.findViewById(R.id.leaderboard_animating_view);
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    /**
     * Called when RecyclerView needs a new
     * {@link com.wolfpakapp.wolfpak.leaderboard.LeaderboardAdapter.ViewHolder} of the given type to
     * represent an item.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
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

        public ViewHolder(View view) {
            super(view);

            listItemView = view;
            mActivity = (Activity) listItemView.getContext();

            listItemTextView = (TextView) listItemView.findViewById(R.id.leaderboard_item_text_view);
            listItemViewCountTextView = (TextView) listItemView.findViewById(R.id.leaderboard_item_view_count_text_view);

            contentLayout = (LinearLayout) listItemView.findViewById(R.id.leaderboard_item_content_view);

            previewDimen = (int) mActivity.getResources().getDimension(R.dimen.leaderboard_item_preview_size);
        }

        public void bindListItem(final LeaderboardListItem listItem) {
            this.listItem = listItem;

            listItemTextView.setText(listItem.getContentString());

            listItemViewCountTextView.setText(Integer.toString(listItem.getVoteCount()));
            listItemViewCountTextView.setOnTouchListener(new ViewCountOnTouchListener());

            contentLayout.removeAllViews();

            if (listItem.isImage()) {
                listItemImageView = new ImageView(listItemView.getContext());
                listItemImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                listItemImageView.setCropToPadding(true);
                listItemImageView.setOnClickListener(new ImageViewOnClickListener());

                contentLayout.addView(listItemImageView);

                Picasso.with(listItemView.getContext()).load(listItem.getUrl()).into(listItemImageView);
            } else {
                listItemVideoView = new VideoView(listItemView.getContext());

                contentLayout.addView(listItemVideoView);

                final Uri uri = Uri.parse(listItem.getUrl());
                listItemVideoView.setVideoURI(uri);
                listItemVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        listItemVideoView.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                            final int action = MotionEventCompat.getActionMasked(event);
                            switch (action) {
                                case MotionEvent.ACTION_UP: {
                                    final VideoView expandedVideoView = (VideoView) mActivity.findViewById(R.id.leaderboard_expanded_video_view);
                                    expandedVideoView.setVideoURI(uri);
                                    expandVideoView(listItemVideoView, expandedVideoView);
                                }
                            }

                            return true;
                            }
                        });
                    }
                });
            }
        }

        private final class ViewCountOnTouchListener implements View.OnTouchListener {
            private int activePointerId = MotionEvent.INVALID_POINTER_ID;

            private float initialViewX;
            private float initialViewY;

            float lastTouchX = 0;
            float lastTouchY = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                final int action = MotionEventCompat.getActionMasked(event);

                switch (action) {
                    case MotionEvent.ACTION_DOWN: {
                        mLayout.setEnabled(false);

//                        final int pointerIndex = MotionEventCompat.getActionIndex(event);
                        final float x = event.getRawX(); //MotionEventCompat.getX(event, pointerIndex);
                        final float y = event.getRawY(); //MotionEventCompat.getY(event, pointerIndex);

                        lastTouchX = x;
                        lastTouchY = y;

                        activePointerId = MotionEventCompat.getPointerId(event, 0);

                        v.getParent().requestDisallowInterceptTouchEvent(true);

                        initialViewX = v.getX();
                        initialViewY = v.getY();

//                        String s = Float.toString(lastTouchX) + " " + Float.toString(lastTouchY) +
//                                " " + Float.toString(initialViewX) + " " + Float.toString(initialViewY);
//                        Log.d("TEST", s);

                        final int indexOfFrontChild = recyclerView.indexOfChild(listItemView);
                        recyclerView.setChildDrawingOrderCallback(new RecyclerView.ChildDrawingOrderCallback() {
                            private int nextChildIndexToRender;
                            @Override
                            public int onGetChildDrawingOrder(int childCount, int iteration) {
                                if (iteration == childCount - 1) {
                                    // in the last iteration return the index of the child
                                    // we want to bring to front (and reset nextChildIndexToRender)
                                    nextChildIndexToRender = 0;
                                    return indexOfFrontChild;
                                } else {
                                    if (nextChildIndexToRender == indexOfFrontChild) {
                                        // skip this index; we will render it during last iteration
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
//                        final int pointerIndex = MotionEventCompat.findPointerIndex(event, activePointerId);
                        final float x = event.getRawX(); //MotionEventCompat.getX(event, pointerIndex);
                        final float y = event.getRawY(); //MotionEventCompat.getY(event, pointerIndex);

                        final float dx = x - lastTouchX;
                        final float dy = y - lastTouchY;

                        v.setX(v.getX() + dx);
                        v.setY(v.getY() + dy);

                        GradientDrawable bg = (GradientDrawable) v.getResources().getDrawable(R.drawable.leaderboard_item_view_count_background);
                        if (v.getY() < initialViewY) {
                            if (listItem.getStatus() == LeaderboardListItem.VoteStatus.UPVOTED) {
                                bg.setColor(v.getResources().getColor(R.color.leaderboard_view_count_background_grey));
                            } else {
                                bg.setColor(v.getResources().getColor(R.color.leaderboard_view_count_background_green));
                            }
                        } else if (v.getY() > initialViewY) {
                            if (listItem.getStatus() == LeaderboardListItem.VoteStatus.DOWNVOTED) {
                                bg.setColor(v.getResources().getColor(R.color.leaderboard_view_count_background_grey));
                            } else {
                                bg.setColor(v.getResources().getColor(R.color.leaderboard_view_count_background_red));
                            }
                        }
                        v.setBackground(bg);
                        v.invalidate();

                        lastTouchX = x;
                        lastTouchY = y;

                        break;
                    }
                    case MotionEvent.ACTION_POINTER_UP: {
                        final int pointerIndex = MotionEventCompat.getActionIndex(event);
                        final int pointerId = MotionEventCompat.findPointerIndex(event, pointerIndex);
                        if (pointerId == activePointerId) {
                            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                            lastTouchX = event.getRawX(); //MotionEventCompat.getX(event, pointerIndex);
                            lastTouchY = event.getRawY(); //MotionEventCompat.getY(event, pointerIndex);
                            activePointerId = MotionEventCompat.getPointerId(event, newPointerIndex);
                        }

                        break;
                    }

                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP: {
                        mLayout.setEnabled(true);

                        GradientDrawable bg = (GradientDrawable) v.getResources().getDrawable(R.drawable.leaderboard_item_view_count_background);
                        if (v.getY() < initialViewY) {
                            if (listItem.getStatus() == LeaderboardListItem.VoteStatus.UPVOTED) {
                                listItem.setStatus(LeaderboardListItem.VoteStatus.NOT_VOTED);
                            } else {
                                listItem.setStatus(LeaderboardListItem.VoteStatus.UPVOTED);
                            }
                        } else if (v.getY() > initialViewY) {
                            if (listItem.getStatus() == LeaderboardListItem.VoteStatus.DOWNVOTED) {
                                listItem.setStatus(LeaderboardListItem.VoteStatus.NOT_VOTED);
                            } else {
                                listItem.setStatus(LeaderboardListItem.VoteStatus.DOWNVOTED);
                            }
                        }
                        v.setBackground(bg);
                        listItemViewCountTextView.setText(Integer.toString(listItem.getVoteCount()));
                        v.invalidate();

                        activePointerId = MotionEvent.INVALID_POINTER_ID;

                        ObjectAnimator xAnim = ObjectAnimator.ofFloat(v, "X", v.getX(), initialViewX);
                        ObjectAnimator yAnim = ObjectAnimator.ofFloat(v, "Y", v.getY(), initialViewY);

                        xAnim.setDuration(350);
                        yAnim.setDuration(350);

                        xAnim.setInterpolator(INTERPOLATOR);
                        yAnim.setInterpolator(INTERPOLATOR);

                        xAnim.start();
                        yAnim.start();

                        recyclerView.setChildDrawingOrderCallback(null);
                    }
                }

                return true;
            }
        }

        private final class ImageViewOnClickListener implements View.OnClickListener {
            @Override
            public void onClick(final View view) {
                final ImageView expandedImageView = (ImageView) mActivity.findViewById(R.id.leaderboard_expanded_image_view);
                Picasso.with(listItemView.getContext()).load(listItem.getUrl()).into(expandedImageView);
                Picasso.with(listItemView.getContext()).load(listItem.getUrl()).into(animatingView);

                expandView(view, expandedImageView);
            }
        }

        private void expandView(final View initialView, final View expandView) {
            final int ANIM_DURATION = 1000;

            final Rect startBounds = new Rect();
            final Rect finalBounds = new Rect();
            final Point globalOffset = new Point();

            initialView.getGlobalVisibleRect(startBounds);
            mActivity.findViewById(R.id.leaderboard_frame_layout).getGlobalVisibleRect(finalBounds, globalOffset);
            startBounds.offset(-globalOffset.x, -globalOffset.y);
            finalBounds.offset(-globalOffset.x, -globalOffset.y);

            if (mCurrentAnimator != null) {
                mCurrentAnimator.cancel();
            }

            initialView.setAlpha(0f);

            animatingView.setVisibility(ImageView.VISIBLE);
            animatingView.setPivotX(0f);
            animatingView.setPivotY(0f);

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
            set.setInterpolator(INTERPOLATOR);
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mCurrentAnimator = null;
                    animatingView.setVisibility(View.GONE);
                    expandView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    mCurrentAnimator = null;
                }
            });

            set.start();
            mCurrentAnimator = set;

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
                    set.setInterpolator(INTERPOLATOR);
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
                }
            });
        }

        private void expandVideoView(final View initialView, final VideoView expandView) {
            final int ANIM_DURATION = 1000;

            final Rect startBounds = new Rect();
            final Rect finalBounds = new Rect();
            final Point globalOffset = new Point();
            final int HEIGHT_OFFSET = 75;

            initialView.getGlobalVisibleRect(startBounds);
            mActivity.findViewById(R.id.leaderboard_frame_layout).getGlobalVisibleRect(finalBounds, globalOffset);
            startBounds.offset(-globalOffset.x, -globalOffset.y);
            finalBounds.offset(-globalOffset.x, -globalOffset.y);

            if (mCurrentAnimator != null) {
                mCurrentAnimator.cancel();
            }

            initialView.setAlpha(0f);

            expandView.setVisibility(ImageView.VISIBLE);
            expandView.setPivotX(0f);
            expandView.setPivotY(0f);

            ValueAnimator widthAnimator = ValueAnimator.ofInt(previewDimen, finalBounds.width());
            widthAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    clipViewToGlobalBounds(expandView);
                    expandView.getLayoutParams().width = (int) animation.getAnimatedValue();
                    expandView.requestLayout();
                }
            });
            ValueAnimator heightAnimator =
                    ValueAnimator.ofInt(previewDimen, finalBounds.height() + HEIGHT_OFFSET);
            heightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    clipViewToGlobalBounds(expandView);
                    expandView.getLayoutParams().height = (int) animation.getAnimatedValue();
                    expandView.requestLayout();
                }
            });

            AnimatorSet set = new AnimatorSet();
            set.play(ObjectAnimator.ofFloat(expandView, View.X, startBounds.left, finalBounds.left))
                    .with(ObjectAnimator.ofFloat(expandView, View.Y, startBounds.top, finalBounds.top))
                    .with(widthAnimator).with(heightAnimator);
            set.setDuration(ANIM_DURATION);
            set.setInterpolator(INTERPOLATOR);
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mCurrentAnimator = null;
                    expandView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            if (mCurrentAnimator != null) {
                                mCurrentAnimator.cancel();
                            }

                            ValueAnimator widthAnimator = ValueAnimator.ofInt(finalBounds.width(), previewDimen);
                            widthAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator animation) {
                                    clipViewToGlobalBounds(expandView);
                                    expandView.getLayoutParams().width = (int) animation.getAnimatedValue();
                                    expandView.requestLayout();
                                }
                            });
                            ValueAnimator heightAnimator = ValueAnimator.ofInt(finalBounds.height() + HEIGHT_OFFSET, previewDimen);
                            heightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator animation) {
                                    clipViewToGlobalBounds(expandView);
                                    expandView.getLayoutParams().height = (int) animation.getAnimatedValue();
                                    expandView.requestLayout();
                                }
                            });

                            AnimatorSet set = new AnimatorSet();
                            set.play(ObjectAnimator.ofFloat(expandView, View.X, finalBounds.left, startBounds.left))
                                    .with(ObjectAnimator.ofFloat(expandView, View.Y, finalBounds.top, startBounds.top))
                                    .with(widthAnimator).with(heightAnimator);
                            set.setDuration(ANIM_DURATION);
                            set.setInterpolator(INTERPOLATOR);
                            set.addListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    initialView.setAlpha(1f);
                                    expandView.setVisibility(View.GONE);
                                    mCurrentAnimator = null;
                                }

                                @Override
                                public void onAnimationCancel(Animator animation) {
                                    initialView.setAlpha(1f);
                                    expandView.setVisibility(View.GONE);
                                    mCurrentAnimator = null;
                                }
                            });

                            set.start();
                            mCurrentAnimator = set;
                        }
                    });
                    expandView.start();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    mCurrentAnimator = null;
                }
            });

            set.start();
            mCurrentAnimator = set;
        }

        private void clipViewToGlobalBounds(View view) {
            Rect clipBounds = new Rect();
            Point globalOffset = new Point();
            view.getGlobalVisibleRect(clipBounds, globalOffset);
            clipBounds.offset(-globalOffset.x, -globalOffset.y);
            view.setClipBounds(clipBounds);
        }
    }
}
