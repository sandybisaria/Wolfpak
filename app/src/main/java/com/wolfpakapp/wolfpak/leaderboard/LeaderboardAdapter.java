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
import android.net.Uri;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.squareup.picasso.Picasso;
import com.wolfpakapp.wolfpak.R;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {
    private List<LeaderboardListItem> listItems;
    private RecyclerView recyclerView;

    private Animator mCurrentAnimator;
    private final Interpolator INTERPOLATOR = new OvershootInterpolator(1.4f);

    public LeaderboardAdapter(List<LeaderboardListItem> listItems) {
        this.listItems = listItems;
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.leaderboard_list_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        LeaderboardListItem listItem = listItems.get(position);
        holder.bindListItem(listItem);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        this.recyclerView = recyclerView;
    }

    public static final RecyclerView.ChildDrawingOrderCallback defaultCallback = new RecyclerView.ChildDrawingOrderCallback() {
        @Override
        public int onGetChildDrawingOrder(int childCount, int i) {
            return i;
        }
    };

    public class ViewHolder extends RecyclerView.ViewHolder {
        private View listItemView;
        private Activity mActivity;

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

        public void bindListItem(LeaderboardListItem listItem) {
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

                Uri uri = Uri.parse(listItem.getUrl());
                listItemVideoView.setVideoURI(uri);
                listItemVideoView.start();
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

                        recyclerView.setChildDrawingOrderCallback(LeaderboardAdapter.defaultCallback);
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

                expandView(view, expandedImageView);
            }
        }

        private void expandView(final View initialView, final View expandedView) {
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

            expandedView.setVisibility(ImageView.VISIBLE);
            expandedView.setPivotX(0f);
            expandedView.setPivotY(0f);

            ValueAnimator widthAnimator = ValueAnimator.ofInt(previewDimen, finalBounds.width());
            widthAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    clipViewToGlobalBounds(expandedView);
                    expandedView.getLayoutParams().width = (int) animation.getAnimatedValue();
                    expandedView.requestLayout();
                }
            });
            ValueAnimator heightAnimator = ValueAnimator.ofInt(previewDimen, finalBounds.height());
            heightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    clipViewToGlobalBounds(expandedView);
                    expandedView.getLayoutParams().height = (int) animation.getAnimatedValue();
                    expandedView.requestLayout();
                }
            });

            AnimatorSet set = new AnimatorSet();
            set.play(ObjectAnimator.ofFloat(expandedView, View.X, startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedView, View.Y, startBounds.top, finalBounds.top))
                .with(widthAnimator).with(heightAnimator);
            set.setDuration(ANIM_DURATION);
            set.setInterpolator(INTERPOLATOR);
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mCurrentAnimator = null;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    mCurrentAnimator = null;
                }
            });

            set.start();
            mCurrentAnimator = set;

            expandedView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mCurrentAnimator != null) {
                        mCurrentAnimator.cancel();
                    }

                    ValueAnimator widthAnimator = ValueAnimator.ofInt(finalBounds.width(), previewDimen);
                    widthAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            clipViewToGlobalBounds(expandedView);
                            expandedView.getLayoutParams().width = (int) animation.getAnimatedValue();
                            expandedView.requestLayout();
                        }
                    });
                    ValueAnimator heightAnimator = ValueAnimator.ofInt(finalBounds.height(), previewDimen);
                    heightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            clipViewToGlobalBounds(expandedView);
                            expandedView.getLayoutParams().height = (int) animation.getAnimatedValue();
                            expandedView.requestLayout();
                        }
                    });

                    AnimatorSet set = new AnimatorSet();
                    set.play(ObjectAnimator.ofFloat(expandedView, View.X, finalBounds.left, startBounds.left))
                            .with(ObjectAnimator.ofFloat(expandedView, View.Y, finalBounds.top, startBounds.top))
                            .with(widthAnimator).with(heightAnimator);
                    set.setDuration(ANIM_DURATION);
                    set.setInterpolator(INTERPOLATOR);
                    set.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            initialView.setAlpha(1f);
                            expandedView.setVisibility(View.GONE);
                            mCurrentAnimator = null;
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            initialView.setAlpha(1f);
                            expandedView.setVisibility(View.GONE);
                            mCurrentAnimator = null;
                        }
                    });

                    set.start();
                    mCurrentAnimator = set;
                }
            });
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
