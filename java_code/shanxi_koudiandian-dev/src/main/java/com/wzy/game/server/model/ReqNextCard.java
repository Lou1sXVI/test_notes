package com.wzy.game.server.model;

import java.util.List;

public class ReqNextCard {
    private String gameId;
    private String wall;
    private int curPos;
    private int dealer;
    private String prevailingWind;
    private int grade;
    private List<Player> players;
    private String history;

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getWall() {
        return wall;
    }

    public void setWall(String wall) {
        this.wall = wall;
    }

    public int getCurPos() {
        return curPos;
    }

    public void setCurPos(int curPos) {
        this.curPos = curPos;
    }

    public int getDealer() {
        return dealer;
    }

    public void setDealer(int dealer) {
        this.dealer = dealer;
    }

    public String getPrevailingWind() {
        return prevailingWind;
    }

    public void setPrevailingWind(String prevailingWind) {
        this.prevailingWind = prevailingWind;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public String getHistory() {
        return history;
    }

    public void setHistory(String history) {
        this.history = history;
    }
}
