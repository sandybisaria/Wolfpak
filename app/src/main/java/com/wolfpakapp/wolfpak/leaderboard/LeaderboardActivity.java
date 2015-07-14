package com.wolfpakapp.wolfpak.leaderboard;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.wolfpakapp.wolfpak.R;
import com.wolfpakapp.wolfpak.WolfpakRestClient;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {
    private List<LeaderboardListItem> listItems;
    private LeaderboardAdapter mAdapter;

    private FrameLayout baseLayout;
    private SwipeRefreshLayout mLayout;
    private ImageView animatingView;
    private ImageView expandedImageView;
    private VideoView expandedVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        listItems = new ArrayList<>();
        mAdapter = new LeaderboardAdapter(this, listItems);

        baseLayout = (FrameLayout) findViewById(R.id.leaderboard_frame_layout);

        animatingView = new ImageView(this);
        animatingView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        animatingView.setVisibility(View.GONE);
        baseLayout.addView(animatingView);

        ViewGroup.LayoutParams expandedParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        expandedImageView = new ImageView(this);
        expandedImageView.setLayoutParams(expandedParams);
        expandedImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        expandedImageView.setVisibility(View.GONE);
        baseLayout.addView(expandedImageView);

        expandedVideoView = new VideoView(this);
        expandedVideoView.setLayoutParams(expandedParams);
        expandedVideoView.setVisibility(View.GONE);
        baseLayout.addView(expandedVideoView);

        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        result += getResources().getDimensionPixelSize(R.dimen.abc_action_bar_default_height_material);

        RecyclerView leaderboardRecyclerView =
            (RecyclerView) findViewById(R.id.leaderboard_recycler_view);
        leaderboardRecyclerView.setHasFixedSize(true);
        leaderboardRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        leaderboardRecyclerView.setAdapter(mAdapter);

        mLayout = (SwipeRefreshLayout) findViewById(R.id.leaderboard_swipe_refresh_layout);
        mLayout.setPadding(0, result, 0, 0);
        mLayout.setProgressViewOffset(true, 0, result);

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

                                // TODO Only works when existing posts are modified
                                // TODO Allow posts to be added/removed on refresh!
                                freshListItems.add(new LeaderboardListItem(id, handle, originalVoteCount, mediaUrl, isImage));
                                for (LeaderboardListItem freshListItem : freshListItems) {
                                    for (LeaderboardListItem currentListItem : listItems) {
                                        if (freshListItem.getId() == currentListItem.getId()) {
                                            currentListItem.setStatus(LeaderboardListItem.VoteStatus.NOT_VOTED);
                                            currentListItem.setOriginalVoteCount(freshListItem.getOriginalVoteCount());

                                            if (freshListItems.indexOf(freshListItem) != listItems.indexOf(currentListItem)) {
                                                Collections.swap(listItems, freshListItems.indexOf(freshListItem), listItems.indexOf(currentListItem));
                                            }

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

    public ImageView getAnimatingView() {
        return animatingView;
    }

    public ImageView getExpandedImageView() {
        return expandedImageView;
    }

    public VideoView getExpandedVideoView() {
        return expandedVideoView;
    }

    public SwipeRefreshLayout getmLayout() {
        return mLayout;
    }
}
