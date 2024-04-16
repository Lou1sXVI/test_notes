package com.wzy.game.server.model;

import java.util.List;
import java.util.Map;

public class ReqTdhGameLogic {

    private String gameId;
    private long userId;
    private int curPos;
    private int AILevel;
    private List<List<String>> history;
    private Map<String,String> rule;
    private List<List<String>> userInfo;
    private List<List<String>> legalAct;
    private int dealer;//        庄家的位置（0-3）
    private String hun;//           混牌的

    public int getDealer() {
        return dealer;
    }

    public void setDealer(int dealer) {
        this.dealer = dealer;
    }

    public String getHun() {
        return hun;
    }

    public void setHun(String hun) {
        this.hun = hun;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getCurPos() {
        return curPos;
    }

    public void setCurPos(int curPos) {
        this.curPos = curPos;
    }

    public int getAILevel() {
        return AILevel;
    }

    public void setAILevel(int AILevel) {
        this.AILevel = AILevel;
    }

    public List<List<String>> getHistory() {
        return history;
    }

    public void setHistory(List<List<String>> history) {
        this.history = history;
    }

    public Map<String, String> getRule() {
        return rule;
    }

    public void setRule(Map<String, String> rule) {
        this.rule = rule;
    }

    public List<List<String>> getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(List<List<String>> userInfo) {
        this.userInfo = userInfo;
    }

    public List<List<String>> getLegalAct() {
        return legalAct;
    }

    public void setLegalAct(List<List<String>> legalAct) {
        this.legalAct = legalAct;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ReqDzmjGameLogic{");
        sb.append("gameId='").append(gameId).append('\'');
        sb.append(", userId=").append(userId);
        sb.append(", curPos=").append(curPos);
        sb.append(", AILevel=").append(AILevel);
        sb.append(", history=").append(history);
        sb.append(", rule=").append(rule);
        sb.append(", userInfo=").append(userInfo);
        sb.append(", legalAct=").append(legalAct);
        sb.append('}');
        return sb.toString();
    }
}
