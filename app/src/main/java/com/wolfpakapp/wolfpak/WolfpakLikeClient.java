package com.wolfpakapp.wolfpak;

import android.util.Log;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.wolfpakapp.wolfpak.leaderboard.LeaderboardListItem;

import org.apache.http.Header;

/**
 * The WolfpakLikeClient can be used to send likes and dislikes to the server.
 */
public class WolfpakLikeClient {
    private static final String LIKE_BASE_REL_URL = "posts/inc_likes/";
    private static final String DISLIKE_BASE_REL_URL = "posts/dec_likes/";

    /**
     * Like a post with the given ID and update the server.
     * @param postId ID of the post.
     * @param newStatus The new VoteStatus of the post.
     */
    public static void updateVoteStatus(int postId, LeaderboardListItem.VoteStatus newStatus) {
        String completeRelativeUrl;
        if (newStatus == LeaderboardListItem.VoteStatus.UPVOTED) {
            completeRelativeUrl = LIKE_BASE_REL_URL + Integer.toString(postId) + "/";
        } else {
            completeRelativeUrl = DISLIKE_BASE_REL_URL + Integer.toString(postId) + "/";
        }

        RequestParams params = new RequestParams();
        params.put("user_id","temp_test_id");


        WolfpakRestClient.put(completeRelativeUrl, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.d("Client", "Success!");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d("Client", "Failure...");
            }
        });
    }
}
