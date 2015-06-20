package com.wolfpakapp.wolfpak;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.ArrayList;
import java.util.List;


public class LeaderboardActivity extends ActionBarActivity {

    final int ITEM_COUNT = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        ListView listview = (ListView) findViewById(R.id.leaderboardlistview);

        List<LeaderboardListItem> itemList = new ArrayList<LeaderboardListItem>();
        for (int idx = 0; idx < ITEM_COUNT; idx++) {
            LeaderboardListItem item = new LeaderboardListItem(idx, "It's a Wolfpak Party!");
            itemList.add(item);
        }

        LeaderboardAdapter adapter = new LeaderboardAdapter(this, itemList);

        listview.setAdapter(adapter);
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
