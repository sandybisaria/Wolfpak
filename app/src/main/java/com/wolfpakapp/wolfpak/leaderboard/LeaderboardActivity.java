package com.wolfpakapp.wolfpak.leaderboard;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.wolfpakapp.wolfpak.R;

import java.util.ArrayList;
import java.util.List;


public class LeaderboardActivity extends Activity {

    final int ITEM_COUNT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        RecyclerView leaderboardRecyclerView = (RecyclerView) findViewById(R.id.leaderboard_recycler_view);

        List<LeaderboardListItem> listItems = new ArrayList<>();
        for (int idx = 0; idx < ITEM_COUNT; idx++) {
            int img;
            if (idx % 2 == 0)
                img = R.drawable.realtest;
            else
                img = R.drawable.wolfpaktest;
            LeaderboardListItem item = new LeaderboardListItem(idx, "It's a Wolfpak Party!", 1, img);
            listItems.add(item);
        }

        LeaderboardAdapter leaderboardAdapter = new LeaderboardAdapter(listItems);

        leaderboardRecyclerView.setHasFixedSize(true);

        leaderboardRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        leaderboardRecyclerView.setAdapter(leaderboardAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_leaderboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
