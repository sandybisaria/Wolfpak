package com.wolfpakapp.wolfpak.leaderboard;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;

import com.wolfpakapp.wolfpak.R;
import com.wolfpakapp.wolfpak.WolfpakLikeClient;

public class LeaderboardListItem {
    private int id;
    private String contentString;
    private final int originalVoteCount;
    private int voteCount;
    private VoteStatus status;
    private String url;
    private boolean isImage;

    public LeaderboardListItem(int id, String contentString, int voteCount, String url, boolean isImage) {
        this.id = id;
        this.contentString = contentString;
        originalVoteCount = voteCount;
        this.voteCount = voteCount;
        this.url = url;
        this.status = VoteStatus.NOT_VOTED;
        this.isImage = isImage;
    }

    public enum VoteStatus {
        NOT_VOTED(0), UPVOTED(1), DOWNVOTED(-1);
        public final int change;

        VoteStatus(int change) {
            this.change = change;
        }

        int getStatusColor(Context context) {
            Resources resources = context.getResources();
            switch (this) {
                case UPVOTED: {
                    return resources.getColor(R.color.leaderboard_view_count_background_green);
                }
                case DOWNVOTED: {
                    return resources.getColor(R.color.leaderboard_view_count_background_red);
                }
                case NOT_VOTED:
                default: {
                    return resources.getColor(R.color.leaderboard_view_count_background_grey);
                }
            }
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContentString() {
        return contentString;
    }

    public void setContentString(String contentString) {
        this.contentString = contentString;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(int voteCount) {
        this.voteCount = voteCount;
    }

    public VoteStatus getStatus() {
        return status;
    }

    public void setStatus(VoteStatus status) {
        voteCount = originalVoteCount + status.change;
        this.status = status;
    }

    public int getOriginalVoteCount() {
        return originalVoteCount;
    }

    public boolean isImage() {
        return isImage;
    }

    public void setIsImage(boolean isImage) {
        this.isImage = isImage;
    }
}
