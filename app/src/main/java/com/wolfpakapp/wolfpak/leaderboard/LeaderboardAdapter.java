package com.wolfpakapp.wolfpak.leaderboard;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

        ViewHolder viewHolder = new ViewHolder(view, new LeaderboardAdapter.ViewHolder.LeaderboardViewHolderOnClickListener() {
            @Override
            public void onImageViewClick(ImageView imageView) {
                Toast.makeText(imageView.getContext(), "You clicked the image!", Toast.LENGTH_SHORT).show();
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        LeaderboardListItem listItem = listItems.get(position);
        holder.bindListItem(listItem);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private LeaderboardListItem listItem;

        private ImageView listItemImageView;
        private TextView listItemTextView;

        private LeaderboardViewHolderOnClickListener leaderboardOnCLickListener;

        public ViewHolder(View view, LeaderboardViewHolderOnClickListener listener) {
            super(view);

            leaderboardOnCLickListener = listener;

            listItemImageView = (ImageView) view.findViewById(R.id.leaderboard_item_image_view);
            listItemTextView = (TextView) view.findViewById(R.id.leaderboard_item_text_view);

            listItemImageView.setOnClickListener(this);
        }

        public void bindListItem(LeaderboardListItem listItem) {
            this.listItem = listItem;

            listItemTextView.setText(listItem.getContent());
        }

        @Override
        public void onClick(View view) {
            if (view instanceof ImageView) {
                leaderboardOnCLickListener.onImageViewClick((ImageView) view);
            }
        }

        public interface LeaderboardViewHolderOnClickListener {
            void onImageViewClick(ImageView imageView);
        }
    }
}
