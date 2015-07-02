package com.wolfpakapp.wolfpak.leaderboard;

import java.util.Enumeration;

public class LeaderboardListItem {
    private int id;
    private String contentString;
    private final int originalVoteCount;
    private int voteCount;
    private int imageSource;
    private VoteStatus status;


    public LeaderboardListItem(int id, String contentString, int voteCount, int imageSource) {
        this.id = id;
        this.contentString = contentString;
        originalVoteCount = voteCount;
        this.voteCount = voteCount;
        this.imageSource = imageSource;
        this.status = VoteStatus.NOT_VOTED;
    }

    public enum VoteStatus {
        NOT_VOTED(0), UPVOTED(1), DOWNVOTED(-1);
        public final int change;

        private VoteStatus(int change) {
            this.change = change;
        }
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

    public int getImageSource() {
        return imageSource;
    }

    public void setImageSource(int imageSource) {
        this.imageSource = imageSource;
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
}
