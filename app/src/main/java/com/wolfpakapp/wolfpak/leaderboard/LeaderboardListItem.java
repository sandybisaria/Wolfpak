package com.wolfpakapp.wolfpak.leaderboard;

import android.content.Context;
import android.content.res.Resources;

import com.wolfpakapp.wolfpak.R;

public class LeaderboardListItem {
    private int id;
    private String contentString;
    private int originalVoteCount;
    private int updatedVoteCount;
    private VoteStatus status;
    private String url;
    private boolean isImage;

    public LeaderboardListItem(int id, String contentString, int originalVoteCount, String url, boolean isImage) {
        this.id = id;
        this.contentString = contentString;
        this.originalVoteCount = originalVoteCount;
        updatedVoteCount = originalVoteCount;
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

    public int getOriginalVoteCount() {
        return originalVoteCount;
    }

    public void setOriginalVoteCount(int originalVoteCount) {
        this.originalVoteCount = originalVoteCount;
        setStatus(status);
    }

    public VoteStatus getStatus() {
        return status;
    }

    public void setStatus(VoteStatus status) {
        updatedVoteCount = originalVoteCount + status.change;
        this.status = status;
    }

    public int getUpdatedVoteCount() {
        return updatedVoteCount;
    }

    public boolean isImage() {
        return isImage;
    }

    public void setIsImage(boolean isImage) {
        this.isImage = isImage;
    }
}
