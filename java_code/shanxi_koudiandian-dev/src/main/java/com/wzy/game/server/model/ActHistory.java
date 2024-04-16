package com.wzy.game.server.model;

public class ActHistory {
    private int seat;
    private int act;
    private String tile;


    public ActHistory(String act, String seat, String tile) {
        this.seat = Integer.parseInt(seat);
        this.act = Integer.parseInt(act);
        this.tile= tile;
    }

    public int getSeat() {
        return seat;
    }

    public void setSeat(int seat) {
        this.seat = seat;
    }

    public int getAct() {
        return act;
    }

    public void setAct(int act) {
        this.act = act;
    }

    public String getTile() {
        return tile;
    }

    public void setTile(String tile) {
        this.tile = tile;
    }
}