package com.wolfpakapp.wolfpak.leaderboard;

public class LeaderboardListItem {
    private int id;
    private String contentString;
    private int voteCount;

    public LeaderboardListItem(int id, String contentString, int voteCount) {
        this.id = id;
        this.contentString = contentString;
        this.voteCount = voteCount;
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
}
