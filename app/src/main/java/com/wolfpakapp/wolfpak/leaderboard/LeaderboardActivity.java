package com.wolfpakapp.wolfpak.leaderboard;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.wolfpakapp.wolfpak.R;
import com.wolfpakapp.wolfpak.WolfpakRestClient;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class LeaderboardActivity extends Activity {
    private List<LeaderboardListItem> listItems;
    private LeaderboardAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        listItems = new ArrayList<>();
        RecyclerView leaderboardRecyclerView = (RecyclerView) findViewById(R.id.leaderboard_recycler_view);

        mAdapter = new LeaderboardAdapter(listItems);

        leaderboardRecyclerView.setHasFixedSize(true);

        leaderboardRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        leaderboardRecyclerView.setAdapter(mAdapter);

        WolfpakRestClient.get("posts/leaderboard/", null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                final JSONArray resArray;
                try {
                    resArray = new JSONArray(new String(bytes));
                    for (int idx = 0; idx < resArray.length(); idx++) {
                        JSONObject listItemObject = resArray.getJSONObject(idx);
                        boolean isImage = listItemObject.optBoolean("is_image");
                        if (isImage) {
                            int id = listItemObject.optInt("id");
                            String handle = listItemObject.optString("handle");
                            int voteCount = listItemObject.optInt("likes");
                            String mediaUrl = listItemObject.optString("media_url");
                            listItems.add(new LeaderboardListItem(id, handle, voteCount, mediaUrl));
                        }

                        mAdapter.notifyDataSetChanged();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                Toast.makeText(LeaderboardActivity.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        });
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
