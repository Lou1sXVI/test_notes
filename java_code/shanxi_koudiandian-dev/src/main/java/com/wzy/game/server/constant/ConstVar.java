package com.wzy.game.server.constant;

public class ConstVar {

    public static final int RESPONSE_CODE_SUCESS = 0;//请求成功
    public static final int RESPONSE_CODE_FAID_CONTENTTYPE = 199;//请求体类型不对
    public static final int RESPONSE_CODE_FAID_JSON_FORMAT = 198;//json 格式不对
    public static final int RESPONSE_CODE_FAID_REQ_PARAM = 197;//错误的请求参数
    public static final int RESPONSE_CODE_FAID_REQ_EXCEPTION = 196;//执行异常
    public static final int RESPONSE_CODE_FAID_HISTORY = 195; //历史信息错误
    public static final int RESPONSE_CODE_FAIL_SHOW = 194; //副露信息错误

    public static final int RESPONSE_CODE_FAID_REQ_HAND = 1000;//玩家手牌不对
    public static final int RESPONSE_CODE_FAID_REQ_HISTORY = RESPONSE_CODE_FAID_REQ_HAND - 1;//历史信息不对
    public static final int RESPONSE_CODE_FAID_REQ_WALL = RESPONSE_CODE_FAID_REQ_HAND - 2;//牌墙信息不对
    public static final int RESPONSE_CODE_FAID_REQ_TYPE = -20000;//游戏类型不对
    public static final int RESPONSE_CODE_FAID_REQ_SEAT = -30000;//游戏位置错误
    public static final int RESPONSE_CODE_FAID_REQ_PLAY = -40000;//玩家信息错误


    public static final int LOG_HAND = 0;                     //起手牌
    public static final int LOG_ACTION_DRAW = 1;              //抓牌
    public static final int LOG_ACTION_DISCARD = 2;           //手切
    public static final int LOG_ACTION_MINGGANG = 3;          //明杠
    public static final int LOG_ACTION_BUGANG = 4;            //补杠
    public static final int LOG_ACTION_ANGANG = 5;            //暗杠
    public static final int LOG_ACTION_TOUCHDISCAR = 6;       //摸切
    public static final int LOG_ACTION_ZIMO = 7;              //自摸
    public static final int LOG_ACTION_DIANPAO = 8;           //点炮胡
    public static final int LOG_ACTION_DOWN_CHOW = 9;         //左吃
    public static final int LOG_ACTION_MIDDLE_CHOW = 10;      //中吃
    public static final int LOG_ACTION_UP_CHOW = 11;          //右吃
    public static final int LOG_ACTION_ERROR = 12;            //错误
    public static final int LOG_ACTION_SET_HUN = 13;          //设置混
    public static final int LOG_ACTION_PENG = 14;             //碰
    public static final int LOG_RULE = 15;                    //桌子信息
    public static final int LOG_ACTION_DINGQUE = 16;          //定缺
    public static final int LOG_ACTION_HUANSANZHANG = 17;     //换三张
    public static final int LOG_ACTION_PASS = 18;             //Pass
    public static final int LOG_ACTION_TING = 19;             //听牌
    public static final int LOG_ACTION_DOUBLE = 20;           //加倍
    public static int LEVEL1 = 0;
    public static int LEVEL2 = 1;
    public static int LEVEL3 = 2;
    public static int LEVEL4 = 3;
    public static int LEVEL5 = 4;
    public static int LEVEL6 = 5;
}
