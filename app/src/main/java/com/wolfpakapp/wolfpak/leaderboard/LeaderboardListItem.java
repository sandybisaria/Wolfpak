package com.wolfpakapp.wolfpak.leaderboard;

import java.util.Enumeration;

public class LeaderboardListItem {
    private int id;
    private String contentString;
    private int voteCount;
    private int imageSource;
    private VoteStatus status;


    public LeaderboardListItem(int id, String contentString, int voteCount, int imageSource) {
        this.id = id;
        this.contentString = contentString;
        this.voteCount = voteCount;
        this.imageSource = imageSource;
        this.status = VoteStatus.NOT_VOTED;
    }

    public enum VoteStatus {
        NOT_VOTED, UPVOTED, DOWNVOTED
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
        this.status = status;
    }
}
