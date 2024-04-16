package com.wzy.game.server.model;

public class AckErmjNextCard {
    private int responseCode;
    private String gameID;
    private String wallCards;
    private String card;
    private String info = "";

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getGameID() {
        return gameID;
    }

    public void setGameID(String gameID) {
        this.gameID = gameID;
    }

    public String getWallCards() {
        return wallCards;
    }

    public void setWallCards(String wallCards) {
        this.wallCards = wallCards;
    }

    public String getCard() {
        return card;
    }

    public void setCard(String card) {
        this.card = card;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("AckErmjNextCard{");
        sb.append("responseCode=").append(responseCode);
        sb.append(", gameID='").append(gameID).append('\'');
        sb.append(", wallCards='").append(wallCards).append('\'');
        sb.append(", card='").append(card).append('\'');
        sb.append(", info='").append(info).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
