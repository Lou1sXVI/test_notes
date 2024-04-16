package com.wzy.game.server.ai;

import lombok.Data;

@Data
public class Card {

    int color;

    int point;

    int index;

    public Card(int color, int point) {
        this(color,point, color * 9 + point);
    }

    public Card(int index)
    {
        this(index / 9,index % 9,index);
    }

    public Card(int color, int point, int index) {
        assert color >= 0 && color < 3;
        assert point >= 0 && point < 9;
        assert index >=0 && index < 34;
        this.color = color;
        this.point = point;
        this.index = index;
    }





}
