package com.wzy.game.server.model;

import java.util.List;
import java.util.Map;

public class ReqControlCard {

    private String gameId;
    private long userId;
    private int curPos;
    private int AILevel;
    private List<List<String>> history;
    private Map<String, String> rule;
    private List<List<String>> userInfo;//四个玩家的类型 0 是机器人，1 是真人玩家
    private List<List<String>> legalAct;
    private int dealer;//        庄家的位置（0-3）
    private String hun;//           混牌的
    private List<Integer> realPlayer; //四个玩家的类型
    private List<List<Integer>> cards;
    private int reqType;        //控制的类型 ， 0 是随机发牌， 1 是普通控牌 2 是小黑屋控牌
    private List<String> wall;          ///当前剩余的牌墙，保证会移除返回的牌,ID为十六机制的ID
    private List<List<String>> initTiles;//起始手牌
    private List<List<String>> showGroup;//起始手牌


    public List<List<String>> getShowGroup() {
        return showGroup;
    }

    public void setShowGroup(List<List<String>> showGroup) {
        this.showGroup = showGroup;
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

    public List<Integer> getRealPlayer() {
        return realPlayer;
    }

    public void setRealPlayer(List<Integer> realPlayer) {
        this.realPlayer = realPlayer;
    }

    public List<List<Integer>> getCards() {
        return cards;
    }

    public void setCards(List<List<Integer>> cards) {
        this.cards = cards;
    }

    public int getReqType() {
        return reqType;
    }

    public void setReqType(int reqType) {
        this.reqType = reqType;
    }

    public List<String> getWall() {
        return wall;
    }

    public void setWall(List<String> wall) {
        this.wall = wall;
    }

    public List<List<String>> getInitTiles() {
        return initTiles;
    }

    public void setInitTiles(List<List<String>> initTiles) {
        this.initTiles = initTiles;
    }
}
