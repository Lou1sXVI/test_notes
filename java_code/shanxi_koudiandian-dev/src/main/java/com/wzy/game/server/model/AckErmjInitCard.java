package com.wzy.game.server.model;

import java.util.ArrayList;
import java.util.List;

public class AckErmjInitCard {
    private int responseCode = -1;
    private List<String> tiles = new ArrayList<>();
    private List<String> cards = new ArrayList<>();
    private String gameID;
    private String prevailingWind;
    private String info = "";

    public List<String> getCards() {
        return cards;
    }

    public void setCards(List<String> cards) {
        this.cards = cards;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public List<String> getTiles() {
        return tiles;
    }

    public void setTiles(List<String> tiles) {
        this.tiles = tiles;
    }

    public String getGameID() {
        return gameID;
    }

    public void setGameID(String gameID) {
        this.gameID = gameID;
    }

    public String getPrevailingWind() {
        return prevailingWind;
    }

    public void setPrevailingWind(String prevailingWind) {
        this.prevailingWind = prevailingWind;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("AckErmjInitCard{");
        sb.append("responseCode=").append(responseCode);
        sb.append(", gameSequence=").append(tiles);
        sb.append(", gameID='").append(gameID).append('\'');
        sb.append(", prevailingWind='").append(prevailingWind).append('\'');
        sb.append(", info='").append(info).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
