package com.wzy.game.server.model;

public class ReqGenCards {
    private String  gameId;
    private int  playerSeat;
    private int  dealer;
    private int  goodLevel;//0为玩家位置好牌，1位玩家位置好牌
    private int  fan;//0为玩家位置好牌，1位玩家位置好牌
    private String  wall;
    private String hun;


    public int getFan() {
        return fan;
    }

    public void setFan(int fan) {
        this.fan = fan;
    }

    public int getGoodLevel() {
        return goodLevel;
    }

    public void setGoodLevel(int goodLevel) {
        this.goodLevel = goodLevel;
    }

    public String getWall() {
        return wall;
    }

    public void setWall(String wall) {
        this.wall = wall;
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

    public int getPlayerSeat() {
        return playerSeat;
    }

    public void setPlayerSeat(int playerSeat) {
        this.playerSeat = playerSeat;
    }

    public int getDealer() {
        return dealer;
    }

    public void setDealer(int dealer) {
        this.dealer = dealer;
    }
}
