package com.wolfpakapp.wolfpak.leaderboard;

import android.content.Context;
import android.content.res.Resources;

import com.wolfpakapp.wolfpak.R;

/**
 * The LeaderboardListItem class represents posts on the leaderboard.
 */
public class LeaderboardListItem {
    private int id;
    private String contentString;
    private int originalVoteCount;
    private int updatedVoteCount;
    private VoteStatus status;
    private String url;
    private boolean isImage;

    /**
     * Instantiate a new LeaderboardListItem object. The VoteStatus is defaulted to NOT_VOTED.
     * @param id The post ID.
     * @param contentString The content string of the post.
     * @param originalVoteCount The original vote count
     * @param url The URL of the content (whether an image or video)
     * @param isImage True if the URL points to an image; false if it points to a video
     */
    public LeaderboardListItem(int id, String contentString, int originalVoteCount, String url, boolean isImage) {
        this.id = id;
        this.contentString = contentString;
        this.originalVoteCount = originalVoteCount;
        updatedVoteCount = originalVoteCount;
        this.url = url;
        this.status = VoteStatus.NOT_VOTED;
        this.isImage = isImage;
    }

    /**
     * The VoteStatus enum represents the three vote statuses (upvoted, downvoted, and not voted)
     * that a post could have.
     */
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

    /**
     * Set the original vote count of the post, and recalculates the updated vote count based on the
     * post's current vote status.
     * @param originalVoteCount The original vote count of the post, without taking into account the
     *                          user's like status.
     */
    public void setOriginalVoteCount(int originalVoteCount) {
        this.originalVoteCount = originalVoteCount;
        setStatus(status);
    }

    public VoteStatus getStatus() {
        return status;
    }

    /**
     * Sets the vote status of the post and recalculates the updated vote count.
     * @param status The new VoteStatus.
     */
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
