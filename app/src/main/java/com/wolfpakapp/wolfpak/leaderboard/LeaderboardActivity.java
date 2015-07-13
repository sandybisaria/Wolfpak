package com.wolfpakapp.wolfpak.leaderboard;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.wolfpakapp.wolfpak.R;
import com.wolfpakapp.wolfpak.WolfpakRestClient;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {
    private List<LeaderboardListItem> listItems;
    private LeaderboardAdapter mAdapter;

    private ImageView expandedImageView;
    private VideoView expandedVideoView;
    private RelativeLayout animatingContainer;
    private ImageView animatingImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        // Instantiate the adapter for the RecyclerView and the ArrayList that stores the posts
        listItems = new ArrayList<>();
        mAdapter = new LeaderboardAdapter(this, listItems);

        // Set up the RecyclerView
        RecyclerView leaderboardRecyclerView =
            (RecyclerView) findViewById(R.id.leaderboard_recycler_view);
        leaderboardRecyclerView.setHasFixedSize(true);
        leaderboardRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        leaderboardRecyclerView.setAdapter(mAdapter);

        // Set up the SwipeRefreshLayout that the RecyclerView is contained in
        final SwipeRefreshLayout mLayout =
                (SwipeRefreshLayout) findViewById(R.id.leaderboard_swipe_refresh_layout);
        mLayout.setColorSchemeResources(R.color.wolfpak_red);

        // Load the leaderboard posts
        WolfpakRestClient.get("posts/leaderboard/", null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                final JSONArray resArray;
                try {
                    resArray = new JSONArray(new String(bytes));
                    for (int idx = 0; idx < resArray.length(); idx++) {
                        JSONObject listItemObject = resArray.getJSONObject(idx);

                        boolean isImage = listItemObject.optBoolean("is_image");
                        int id = listItemObject.optInt("id");
                        String handle = listItemObject.optString("handle");
                        int originalVoteCount = listItemObject.optInt("likes");
                        String mediaUrl = listItemObject.optString("media_url");

                        listItems.add(new LeaderboardListItem(id, handle, originalVoteCount, mediaUrl, isImage));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                Toast.makeText(LeaderboardActivity.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        });

        // Every time the leaderboard is refreshed, load the latest posts
        mLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                WolfpakRestClient.get("posts/leaderboard/", null, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int i, Header[] headers, byte[] bytes) {
                        final JSONArray resArray;
                        List<LeaderboardListItem> freshListItems = new ArrayList<>();
                        try {
                            resArray = new JSONArray(new String(bytes));
                            for (int idx = 0; idx < resArray.length(); idx++) {
                                JSONObject listItemObject = resArray.getJSONObject(idx);

                                boolean isImage = listItemObject.optBoolean("is_image");
                                int id = listItemObject.optInt("id");
                                String handle = listItemObject.optString("handle");
                                int originalVoteCount = listItemObject.optInt("likes");
                                String mediaUrl = listItemObject.optString("media_url");

                                freshListItems.add(new LeaderboardListItem(id, handle, originalVoteCount, mediaUrl, isImage));
                                for (LeaderboardListItem freshListItem : freshListItems) {
                                    for (LeaderboardListItem currentListItem : listItems) {
                                        if (freshListItem.getId() == currentListItem.getId()) {
                                            currentListItem.setStatus(LeaderboardListItem.VoteStatus.NOT_VOTED);
                                            currentListItem.setOriginalVoteCount(freshListItem.getOriginalVoteCount());
                                            break;
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        mAdapter.notifyDataSetChanged();
                        mLayout.setRefreshing(false);
                    }

                    @Override
                    public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                        Toast.makeText(LeaderboardActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                        mLayout.setRefreshing(false);
                    }
                });
            }
        });

        // Set up the expanded and animating views
        expandedImageView = new ImageView(this);
        expandedImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        expandedImageView.setVisibility(View.GONE);

        expandedVideoView = new VideoView(this);
        expandedVideoView.setVisibility(View.GONE);

        animatingContainer = new RelativeLayout(this);
        animatingContainer.setVisibility(View.GONE);

        animatingImageView = new ImageView(this);
        animatingImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        animatingImageView.setVisibility(View.GONE);

        WindowManager manager = getWindowManager();

        WindowManager.LayoutParams expandedParams = new WindowManager.LayoutParams();
        expandedParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        expandedParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        // Allows the expanded View to be drawn over the Action Bar and notification area
//        expandedParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        // Ensures that the expanded View is positioned over the Action Bar and notification area
        expandedParams.flags =  WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
//        expandedParams.format = PixelFormat.TRANSLUCENT;
//        expandedParams.gravity = Gravity.BOTTOM | Gravity.CENTER;

        // In order for the animatingImageView to be animate-able, it must be contained in a layout
        WindowManager.LayoutParams animatingContainerParams = new WindowManager.LayoutParams();
//        animatingContainerParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        // The layout (and thus the animation) can not be interacted with
        animatingContainerParams.flags =  WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        // Required so that the layout doesn't obscure the Views below the Window
        animatingContainerParams.format = PixelFormat.TRANSLUCENT;

        manager.addView(expandedImageView, expandedParams);
        manager.addView(expandedVideoView, expandedParams);
        manager.addView(animatingContainer, animatingContainerParams);
        animatingContainer.addView(animatingImageView);
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

    public ImageView getExpandedImageView() {
        return expandedImageView;
    }

    public VideoView getExpandedVideoView() {
        return expandedVideoView;
    }

    public ImageView getAnimatingImageView() {
        return animatingImageView;
    }

    public RelativeLayout getAnimatingContainer() {
        return animatingContainer;
    }

    public void setAnimationVisibility(int visibility) {
        animatingContainer.setVisibility(visibility);
        animatingImageView.setVisibility(visibility);
    }
}
