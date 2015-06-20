package com.wolfpakapp.wolfpak;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class LeaderboardAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private List<LeaderboardListItem> itemList;

    public LeaderboardAdapter(Activity context, List<LeaderboardListItem> itemList) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.itemList = itemList;
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            v = inflater.inflate(R.layout.leaderboard_list_item, null);
        }

        LeaderboardListItem item = itemList.get(position);
        ((TextView) v.findViewById(R.id.leaderboarditemtextview)).setText(item.getContent());

        return v;
    }
}
