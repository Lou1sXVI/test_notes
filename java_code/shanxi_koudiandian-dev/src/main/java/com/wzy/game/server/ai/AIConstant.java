package com.wzy.game.server.ai;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AIConstant {
    public static final int TILE_TYPE_COUNT = 27;   //牌种类数，共16种1
    public static final int HANDS_COUNT = 14;       //最大手牌数
    public static final int MAX_VALUE = 20;         //打分体系中价值


    public static final int PINGHU = 1;
    public static final int YITIAOLONG = 2;
    public static final int QINGYISE = 3;
    public static final int QIDUI = 4;
    public static final int HAOHUAQIDUI = 5;
    public static final int SHISANYAO = 6;
    public static final int QINGYITIAOLONG = 7;
    public static final int QINGQIDUI = 8;
    public static final int QINGHAOHUAQIDUI = 9;

    public static final int[] FAN_SCORE = {0, 0, 20, 20, 20, 40, 60, 40, 40, 60};

    public static final String[] CARD_NAME = {
            "1W","2W","3W","4W","5W","6W","7W","8W","9W",
            "1T","2T","3T","4T","5T","6T","7T","8T","9T",
            "1B","2B","3B","4B","5B","6B","7B","8B","9B",
            "DF","NF","XF","BF","ZJ","FC","BB"};

    public static final int[] CARD_PRIORITY_4_DISCARD = {
            10,15,20,50,50,50,50,40,30,
            10,15,20,50,50,50,50,40,30,
            10,15,20,50,50,50,50,40,30,
            25,25,25,25,25,25,25};

    //超过2上听的扔牌顺序
    public static final int[] CARD_PRIORITY_OVER2STEPS = {
            10,15,20,50,50,50,50,40,17,
            10,15,20,50,50,50,50,40,17,
            10,15,20,50,50,50,50,40,17,
            9,9,9,9,9,9,9};

    public static final int[] CARD_VALUE = {
            10,20,40,50,50,50,50,50,30,
            10,20,40,50,50,50,50,50,30,
            10,20,40,50,50,50,50,50,30,
            9,9,9,9,9,9,9};

    public static final Map<String,Integer> NAME_INDEX ;
    static {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < CARD_NAME.length; i++) {
            map.put(CARD_NAME[i], i);
        }
        NAME_INDEX = Collections.unmodifiableMap(map);
    }

    public static final Random random = new Random(System.nanoTime());
}
