package com.wzy.game.server.model;

public class RuleMahjong {
    private int gamerNum=4;
    private int colorNum=3;
    private  int smallHu = 4;
    private int smallPao =8;

    public int getSmallHu() {
        return smallHu;
    }

    public void setSmallHu(int smallHu) {
        this.smallHu = smallHu;
    }

    public int getSmallPao() {
        return smallPao;
    }

    public void setSmallPao(int smallPao) {
        this.smallPao = smallPao;
    }
}
