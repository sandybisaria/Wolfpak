package com.wolfpakapp.wolfpak.leaderboard;

import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wolfpakapp.wolfpak.R;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {
    private List<LeaderboardListItem> listItems;

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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private LeaderboardListItem listItem;

        private ImageView listItemImageView;
        private TextView listItemTextView;
        private TextView listItemViewCountTextView;

        private ViewCountOnTouchListener mListener;

        private float initialViewX;
        private float initialViewY;

        public ViewHolder(View view) {
            super(view);

            listItemImageView = (ImageView) view.findViewById(R.id.leaderboard_item_image_view);
            listItemTextView = (TextView) view.findViewById(R.id.leaderboard_item_text_view);
            listItemViewCountTextView = (TextView) view.findViewById(R.id.leaderboard_item_view_count_text_view);

            mListener = new ViewCountOnTouchListener();

            listItemViewCountTextView.setOnTouchListener(mListener);
        }

        public void bindListItem(LeaderboardListItem listItem) {
            this.listItem = listItem;

            listItemTextView.setText(listItem.getContentString());
            listItemViewCountTextView.setText(Integer.toString(listItem.getVoteCount()));
        }

        private final class ViewCountOnTouchListener implements View.OnTouchListener {
            private int activePointerId = MotionEvent.INVALID_POINTER_ID;
            float lastTouchX = 0;
            float lastTouchY = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                final int action = MotionEventCompat.getActionMasked(event);

                switch (action) {
                    case MotionEvent.ACTION_DOWN: {
                        final int pointerIndex = MotionEventCompat.getActionIndex(event);
                        final float x = MotionEventCompat.getX(event, pointerIndex);
                        final float y = MotionEventCompat.getY(event, pointerIndex);

                        lastTouchX = x;
                        lastTouchY = y;

                        activePointerId = MotionEventCompat.getPointerId(event, 0);

                        v.getParent().requestDisallowInterceptTouchEvent(true);

                        initialViewX = v.getX();
                        initialViewY = v.getY();

                        break;
                    }
                    case MotionEvent.ACTION_MOVE: {
                        final int pointerIndex = MotionEventCompat.findPointerIndex(event, activePointerId);
                        final float x = MotionEventCompat.getX(event, pointerIndex);
                        final float y = MotionEventCompat.getY(event, pointerIndex);

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
                            lastTouchX = MotionEventCompat.getX(event, newPointerIndex);
                            lastTouchY = MotionEventCompat.getY(event, newPointerIndex);
                            activePointerId = MotionEventCompat.getPointerId(event, newPointerIndex);
                        }

                        break;
                    }

                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP: {
                        activePointerId = MotionEvent.INVALID_POINTER_ID;

                        v.setX(initialViewX);
                        v.setY(initialViewY);
                    }
                }

                return true;
            }
        }
    }
}
