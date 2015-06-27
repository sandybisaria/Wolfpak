package com.wolfpakapp.wolfpak.leaderboard;

import android.support.v7.widget.RecyclerView;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wolfpakapp.wolfpak.R;

import org.w3c.dom.Text;

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

        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
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

        public ViewHolder(View view) {
            super(view);

            listItemImageView = (ImageView) view.findViewById(R.id.leaderboard_item_image_view);
            listItemTextView = (TextView) view.findViewById(R.id.leaderboard_item_text_view);
            listItemViewCountTextView = (TextView) view.findViewById(R.id.leaderboard_item_view_count_text_view);
        }

        public void bindListItem(LeaderboardListItem listItem) {
            this.listItem = listItem;

            listItemTextView.setText(listItem.getContentString());
            listItemViewCountTextView.setText(Integer.toString(listItem.getVoteCount()));
        }
    }
}
