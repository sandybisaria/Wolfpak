package com.wolfpakapp.wolfpak.leaderboard;

import android.animation.ObjectAnimator;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.wolfpakapp.wolfpak.R;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {
    private List<LeaderboardListItem> listItems;
    private static RecyclerView recyclerView;

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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private LeaderboardListItem listItem;

        private View listItemView;

        private ImageView listItemImageView;
        private TextView listItemTextView;
        private TextView listItemViewCountTextView;

        public ViewHolder(View view) {
            super(view);

            listItemView = view;

            listItemImageView = (ImageView) view.findViewById(R.id.leaderboard_item_image_view);
            listItemTextView = (TextView) view.findViewById(R.id.leaderboard_item_text_view);
            listItemViewCountTextView = (TextView) view.findViewById(R.id.leaderboard_item_view_count_text_view);

            listItemViewCountTextView.setOnTouchListener(new ViewCountOnTouchListener());
        }

        public void bindListItem(LeaderboardListItem listItem) {
            this.listItem = listItem;

            listItemTextView.setText(listItem.getContentString());
            listItemViewCountTextView.setText(Integer.toString(listItem.getVoteCount()));
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
                        activePointerId = MotionEvent.INVALID_POINTER_ID;

                        ObjectAnimator xAnim = ObjectAnimator.ofFloat(v, "X", v.getX(), initialViewX);
                        ObjectAnimator yAnim = ObjectAnimator.ofFloat(v, "Y", v.getY(), initialViewY);

                        xAnim.setDuration(350);
                        yAnim.setDuration(350);

                        xAnim.setInterpolator(new OvershootInterpolator(1.4f));
                        yAnim.setInterpolator(new OvershootInterpolator(1.4f));

                        xAnim.start();
                        yAnim.start();

                        recyclerView.setChildDrawingOrderCallback(LeaderboardAdapter.defaultCallback);
                    }
                }

                return true;
            }
        }
    }
}
