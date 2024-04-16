package com.wzy.game.server.model;

import java.util.List;

public class HuFanInfo {
    private long point;//分数
    private String fanName;//番名
    private String fanNameChinese;

    private String status;
    private int index;
    private List<Integer> keyTiles;
    private int  distance;
    private int keyLess;

    private List<Integer> uselessTiles;
    public List<Integer> getKeyTiles() {
        return this.keyTiles;
    }

    public List<Integer> getUselessTiles() {
        return this.uselessTiles;
    }

    public void setUselessTiles(List<Integer> uselessTiles) {
        this.uselessTiles = uselessTiles;
    }

    public void setKeyTiles(List<Integer> keyTiles) {
        this.keyTiles = keyTiles;
    }

    public int getDistance() {
        return this.distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getKeyLess() {
        return this.keyLess;
    }

    public void setKeyLess(int keyLess) {
        this.keyLess = keyLess;
    }

    public int getIndex() {
        return this.index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public long getPoint() {
        return this.point;
    }

    public void setPoint(long point) {
        this.point = point;
    }

    public String getFanName() {
        return this.fanName;
    }

    public void setFanName(String fanName) {
        this.fanName = fanName;
    }

    public String getFanNameChinese() {
        return this.fanNameChinese;
    }

    public void setFanNameChinese(String fanNameChinese) {
        this.fanNameChinese = fanNameChinese;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "FanInfo{" + fanName + "}";
    }
}
