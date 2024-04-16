package com.wzy.game.server.model;

import java.util.ArrayList;
import java.util.List;

public class AckTdhGameLogic {

    private int code;
    private String gameId;
    private long userId;
    private List<String> action = new ArrayList<>();
    private long delay;
    private String msg;

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

    public List<String> getAction() {
        return action;
    }

    public void setAction(List<String> action) {
        this.action = action;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("AckDzmjGameLogic{");
        sb.append("code=").append(code);
        sb.append(", gameId='").append(gameId).append('\'');
        sb.append(", userId=").append(userId);
        sb.append(", action=").append(action);
        sb.append(", delay=").append(delay);
        sb.append('}');
        return sb.toString();
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
}
