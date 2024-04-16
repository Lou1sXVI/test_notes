package com.wzy.game.server.model;

import java.util.List;

public class Player {
    private int position;
    private String tiles;
    private List<String> sets;
    private int isTing;
    private String seatWind;
    private int point;//点数
    private boolean riichi;

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getTiles() {
        return tiles;
    }

    public void setTiles(String tiles) {
        this.tiles = tiles;
    }

    public List<String> getSets() {
        return sets;
    }

    public void setSets(List<String> sets) {
        this.sets = sets;
    }

    public int getIsTing() {
        return isTing;
    }

    public void setIsTing(int isTing) {
        this.isTing = isTing;
    }


    public String getSeatWind() {
        return seatWind;
    }

    public void setSeatWind(String seatWind) {
        this.seatWind = seatWind;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Player{");
        sb.append("position=").append(position);
        sb.append(", tiles='").append(tiles).append('\'');
        sb.append(", sets=").append(sets);
        sb.append(", isTing=").append(isTing);
        sb.append(", seatWind='").append(seatWind).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
