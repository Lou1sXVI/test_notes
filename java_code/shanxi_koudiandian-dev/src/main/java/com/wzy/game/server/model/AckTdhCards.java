package com.wzy.game.server.model;

import java.util.ArrayList;
import java.util.List;

public class AckTdhCards {

    private int code;
    private String gameId;
    private long userId;
    private List<List<String>> cards = new ArrayList<>();
    private long delay;
    private String msg;
    private List<Integer> winDistance;
    private String hun;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
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

    public List<List<String>> getCards() {
        return cards;
    }

    public void setCards(List<List<String>> cards) {
        this.cards = cards;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<Integer> getWinDistance() {
        return winDistance;
    }

    public void setWinDistance(List<Integer> winDistance) {
        this.winDistance = winDistance;
    }

    public String getHun() {
        return hun;
    }

    public void setHun(String hun) {
        this.hun = hun;
    }
}
