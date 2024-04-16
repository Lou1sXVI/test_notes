package com.wzy.game.server.model;

import java.util.List;

public class ReqGameLogic {
    private String gameId;
    private long userID;
    private int curPos;
    private int dealer;
    private int remain;
    private String prevailingWind;
    private int grade;
    private List<Player> players;
    private String history;
    private List<Integer> legalAct;
    private String wall;
    private RuleMahjong rule;

    private int maxFan;
    private String hun;//鬼牌，not指示牌


    public RuleMahjong getRule() {
        return rule;
    }

    public void setRule(RuleMahjong rule) {
        this.rule = rule;
    }

    public String getHun() {
        return hun;
    }

    public void setHun(String hun) {
        this.hun = hun;
    }

    public int getMaxFan() {
        return maxFan;
    }

    public void setMaxFan(int maxFan) {
        this.maxFan = maxFan;
    }

    public String getWall() {
        return wall;
    }

    public void setWall(String wall) {
        this.wall = wall;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public long getUserID() {
        return userID;
    }

    public void setUserID(long userID) {
        this.userID = userID;
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

    public int getRemain() {
        return remain;
    }

    public void setRemain(int remain) {
        this.remain = remain;
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

    public List<Integer> getLegalAct() {
        return legalAct;
    }

    public void setLegalAct(List<Integer> legalAct) {
        this.legalAct = legalAct;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ReqGameLogic{");
        sb.append("gameId='").append(gameId).append('\'');
        sb.append(", userID=").append(userID);
        sb.append(", curPos=").append(curPos);
        sb.append(", dealer=").append(dealer);
        sb.append(", remain=").append(remain);
        sb.append(", prevailingWind='").append(prevailingWind).append('\'');
        sb.append(", grade=").append(grade);
        sb.append(", players=").append(players);
        sb.append(", history='").append(history).append('\'');
        sb.append(", legalAct=").append(legalAct);
        sb.append('}');
        return sb.toString();
    }
    private int level;
    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
