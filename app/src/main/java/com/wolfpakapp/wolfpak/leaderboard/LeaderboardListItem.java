package com.wolfpakapp.wolfpak.leaderboard;

public class LeaderboardListItem {
    private int id;
    private String content;

    public LeaderboardListItem() {

    }

    public LeaderboardListItem(int id, String content) {
        this.id = id;
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
