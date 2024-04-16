package com.wzy.game.server.model;

public class ReqErmjInitCard {
    private String gameID;
    private int dealer;
    private String prevailingWind;
    private int goodPos;
    private int grade;
    private int minScore;


    public int getMinScore() {
        return minScore;
    }

    public void setMinScore(int minScore) {
        this.minScore = minScore;
    }

    public String getGameID() {
        return gameID;
    }

    public void setGameID(String gameID) {
        this.gameID = gameID;
    }

    public int getDealer() {
        return dealer;
    }

    public void setDealer(int dealer) {
        this.dealer = dealer;
    }

    public String getPrevailingWind() {
        return prevailingWind;
    }

    public void setPrevailingWind(String prevailingWind) {
        this.prevailingWind = prevailingWind;
    }

    public int getGoodPos() {
        return goodPos;
    }

    public void setGoodPos(int goodPos) {
        this.goodPos = goodPos;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ReqErmjInitCard{");
        sb.append("gameID='").append(gameID).append('\'');
        sb.append(", dealer=").append(dealer);
        sb.append(", prevailingWind='").append(prevailingWind).append('\'');
        sb.append(", goodPos=").append(goodPos);
        sb.append(", grade=").append(grade);
        sb.append('}');
        return sb.toString();
    }
}
