package com.wolfpakapp.wolfpak.leaderboard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.squareup.picasso.Picasso;
import com.wolfpakapp.wolfpak.R;

import java.net.URI;
import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {
    private List<LeaderboardListItem> listItems;
    private RecyclerView recyclerView;

    private Animator mCurrentAnimator;
    private final int mAnimationDuration = 1000;
    private final OvershootInterpolator mInterpolator = new OvershootInterpolator(1.4f);

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

        private LeaderboardListItem listItem;

        private TextView listItemTextView;
        private TextView listItemViewCountTextView;

        private LinearLayout contentLayout;

        private ImageView listItemImageView;
        private VideoView listItemVideoView;

        public ViewHolder(View view) {
            super(view);

            listItemView = view;

            listItemTextView = (TextView) listItemView.findViewById(R.id.leaderboard_item_text_view);
            listItemViewCountTextView = (TextView) listItemView.findViewById(R.id.leaderboard_item_view_count_text_view);

            contentLayout = (LinearLayout) listItemView.findViewById(R.id.leaderboard_item_content_view);
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

                        xAnim.setInterpolator(mInterpolator);
                        yAnim.setInterpolator(mInterpolator);

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
            public void onClick(View v) {
                final View imgView = v;
                Activity mActivity = (Activity)imgView.getContext();

                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                final ImageView expandedImageView = (ImageView) mActivity.findViewById(R.id.leaderboard_expanded_image_view);
                Picasso.with(listItemView.getContext()).load(listItem.getUrl()).into(expandedImageView);

                final Rect startBounds = new Rect();
                final Rect finalBounds = new Rect();
                final Point globalOffset = new Point();

                imgView.getGlobalVisibleRect(startBounds);
                mActivity.findViewById(R.id.leaderboard_frame_layout).getGlobalVisibleRect(finalBounds, globalOffset);
                startBounds.offset(-globalOffset.x, -globalOffset.y);
                finalBounds.offset(-globalOffset.x, -globalOffset.y);

                float startScale;
                if ((float) finalBounds.width() / finalBounds.height()
                        > (float) startBounds.width() / startBounds.height()) {
                    // Extend start bounds horizontally
                    startScale = (float) startBounds.height() / finalBounds.height();
                    float startWidth = startScale * finalBounds.width();
                    float deltaWidth = (startWidth - startBounds.width()) / 2;
                    startBounds.left -= deltaWidth;
                    startBounds.right += deltaWidth;
                } else {
                    // Extend start bounds vertically
                    startScale = (float) startBounds.width() / finalBounds.width();
                    float startHeight = startScale * finalBounds.height();
                    float deltaHeight = (startHeight - startBounds.height()) / 2;
                    startBounds.top -= deltaHeight;
                    startBounds.bottom += deltaHeight;
                }

                imgView.setAlpha(0f);
                expandedImageView.setVisibility(ImageView.VISIBLE);

                expandedImageView.setPivotX(0f);
                expandedImageView.setPivotY(0f);

                AnimatorSet set = new AnimatorSet();
                set
                        .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                                startBounds.left, finalBounds.left))
                        .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                                startBounds.top, finalBounds.top))
                        .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                                startScale, 1f)).with(ObjectAnimator.ofFloat(expandedImageView,
                        View.SCALE_Y, startScale, 1f));
                set.setDuration(mAnimationDuration);
                set.setInterpolator(mInterpolator);
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

                Integer colorFrom = 0x00ffffff;//recyclerView.getResources().getColor(android.R.color.white);
                Integer colorTo = 0xff000000;//recyclerView.getResources().getColor(android.R.color.black);
                ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
                colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        expandedImageView.setBackgroundColor((Integer)animator.getAnimatedValue());
                    }

                });
                colorAnimation.setDuration(mAnimationDuration);

                colorAnimation.start();
                set.start();
                mCurrentAnimator = set;

                final float startScaleFinal = startScale;
                expandedImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mCurrentAnimator != null) {
                            mCurrentAnimator.cancel();
                        }

                        AnimatorSet set = new AnimatorSet();
                        set.play(ObjectAnimator
                                .ofFloat(expandedImageView, View.X, startBounds.left))
                                .with(ObjectAnimator
                                        .ofFloat(expandedImageView,
                                                View.Y,startBounds.top))
                                .with(ObjectAnimator
                                        .ofFloat(expandedImageView,
                                                View.SCALE_X, startScaleFinal))
                                .with(ObjectAnimator
                                        .ofFloat(expandedImageView,
                                                View.SCALE_Y, startScaleFinal));
                        set.setDuration(mAnimationDuration);
                        set.setInterpolator(mInterpolator);
                        set.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                imgView.setAlpha(1f);
                                expandedImageView.setVisibility(View.GONE);
                                mCurrentAnimator = null;
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {
                                imgView.setAlpha(1f);
                                expandedImageView.setVisibility(View.GONE);
                                mCurrentAnimator = null;
                            }
                        });
                        Integer colorFrom = 0xff000000;//recyclerView.getResources().getColor(android.R.color.black);
                        Log.d("BLACK", Integer.toHexString(colorFrom));
                        Integer colorTo = 0x00ffffff;//recyclerView.getResources().getColor(android.R.color.white);
                        Log.d("WHITE", Integer.toHexString(colorTo));
                        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
                        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                            @Override
                            public void onAnimationUpdate(ValueAnimator animator) {
                                expandedImageView.setBackgroundColor((Integer)animator.getAnimatedValue());
                            }

                        });
                        colorAnimation.setDuration(mAnimationDuration);

                        colorAnimation.start();
                        set.start();
                        mCurrentAnimator = set;
                    }
                });
            }
        }
    }
}
