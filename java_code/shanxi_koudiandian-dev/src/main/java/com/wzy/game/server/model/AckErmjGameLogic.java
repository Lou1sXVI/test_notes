package com.wzy.game.server.model;

import java.util.List;

public class AckErmjGameLogic {
    private int responseCode;
    private String gameID;
    private long userID;
    private String tile;
    private int action;
    private long delay;
    private boolean doubly;
    private String info = "";
    private int point;
    private List<String> fanInfo;

    public boolean isDoubly() {
        return doubly;
    }

    public void setDoubly(boolean doubly) {
        this.doubly = doubly;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getGameID() {
        return gameID;
    }

    public void setGameID(String gameID) {
        this.gameID = gameID;
    }

    public long getUserID() {
        return userID;
    }

    public void setUserID(long userID) {
        this.userID = userID;
    }

    public String getTile() {
        return tile;
    }

    public void setTile(String tile) {
        this.tile = tile;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("AckErmjGameLogic{");
        sb.append("responseCode=").append(responseCode);
        sb.append(", gameID='").append(gameID).append('\'');
        sb.append(", userID=").append(userID);
        sb.append(", tile='").append(tile).append('\'');
        sb.append(", action=").append(action);
        sb.append(", delay=").append(delay);
        sb.append(", info='").append(info).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public void setPoint(int point) {
        this.point = point;
    }

    public int getPoint() {
        return point;
    }

    public List<String> getFanInfo() {
        return fanInfo;
    }

    public void setFanInfo(List<String> fanInfo) {
        this.fanInfo = fanInfo;
    }
}
