package com.wzy.game.server.jni;

import com.wzy.game.server.util.PlatformPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @ProjectName: ScmjServer
 * @Package: com.wzy.game.server.jni
 * @ClassName: TDHAIJNI
 * @Author: zhanghx
 * @Description:
 * @Date: 2021/1/19 10:25
 * @Version: 1.0
 */
public class TDHAIJNI {
    public static final int ACT_DRAW = 0; //摸牌
    public static final int ACT_H_DISCARD = 1; //手切，打出的是手中的牌，吃碰之后都是手切
    public static final int ACT_D_DISCARD = 2; //摸切，打出的是刚摸到的牌
    public static final int ACT_L_CHOW = 3; //左吃，吃的牌是最小点, 例如45吃3
    public static final int ACT_M_CHOW = 4; //中吃，吃的牌是中间点，例如24吃3
    public static final int ACT_R_CHOW = 5; //右吃，吃的牌是最大点，例如12吃3
    public static final int ACT_PONG = 6;//碰
    public static final int ACT_C_KONG = 7; //暗杠
    public static final int ACT_E_KONG = 8; //直杠
    public static final int ACT_P_KONG = 9; //补杠
    public static final int ACT_WIN = 10; //和
    public static final int ACT_PASS = 11; //过

    private static Logger logger = LoggerFactory.getLogger(TDHAIJNI.class);
    private static String LINNAEM = "TdhAI";
    static {
        try {
            if (PlatformPath.SystemLoadLibrary(LINNAEM)) {
                System.out.println("load system lib "+ LINNAEM +" sucess");
                logger.info("load system lib {} sucess", LINNAEM);
            } else if (PlatformPath.SystemLoadClass(LINNAEM)) {
                System.out.println("load current path lib  "+LINNAEM +"  sucess");
                logger.info("load current path lib {} sucess", LINNAEM);
            } else if(PlatformPath.SystemLoadAbsolutePath(Paths.get(PlatformPath.getDynamicLibraryDir(),
                    PlatformPath.combinationLibrayName(LINNAEM)))){
                System.out.println("load user path lib  "+LINNAEM +"  sucess");
                logger.info("load user path lib {} sucess", LINNAEM);
            } else {
                logger.info("load failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param aiSeat        ai的位置（0-3）
     * @param dealer        庄家的位置（0-3）
     * @param hun           混牌的
     * @param realPlayer    四个玩家的类型
     * @param player_0      0号位置玩家的手牌， 长度为13/14， 牌张编码为0-34，万，条，饼，风，箭
     * @param player_1      1号位置玩家的手牌   长度为13/14， 牌张编码为0-34，万，条，饼，风，箭
     * @param player_2      2号位置玩家的手牌   长度为13/14， 牌张编码为0-34，万，条，饼，风，箭
     * @param player_3      3号位置玩家的手牌   长度为13/14， 牌张编码为0-34，万，条，饼，风，箭
     * @param actionSeat    动作位置 {actionSeat,actionType,actionTile,actionStealed} 配套使用
     * @param actionType    动作类型，TDHAIJNI类常量值，{actionSeat,actionType,actionTile,actionStealed} 配套使用
     * @param actionTile    动作的牌，{actionSeat,actionType,actionTile,actionStealed} 配套使用
     * @param actionStealed 提供方
     * @return 0为正常处理，其余值为异常
     */


    public static native int TDHAIJNI(int aiSeat, int dealer, int hun, int realPlayer[],
                                      int player_0[], int player_1[], int player_2[], int player_3[],
                                      int actionSeat[], int actionType[], int actionTile[], int actionStealed[],
                                      int legalActionSeat[], int legalActionType[], int legalActionTile[], int legalActionStealed[],
                                      int ret[]);


    public static native int JPHAIJNI(int aiSeat, int dealer,
                                      int player_0[], int player_1[], int player_2[], int player_3[],
                                      int actionSeat[], int actionType[], int actionTile[], int actionStealed[],
                                      int legalActionSeat[], int legalActionType[], int legalActionTile[], int legalActionStealed[],
                                      int ret[]);


    public static class Action{
        public int seat;
        public int type;
        public int tile;
        public int steald;
        public Action(int seat,int type,int tile,int steal){
            this.seat = seat;
            this.type = type;
            this.tile = tile;
            this.steald = steal;
        }

        public int getSeat() {
            return seat;
        }

        public void setSeat(int seat) {
            this.seat = seat;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public int getTile() {
            return tile;
        }

        public void setTile(int tile) {
            this.tile = tile;
        }

        public int getSteald() {
            return steald;
        }

        public void setSteald(int steald) {
            this.steald = steald;
        }
    }


    public static void main(String[] args) {
        int aiSeat = 1;
        int dealerSeat = 0;
        int hun = 9;//1t
        int realPlayer[] = {0, 0, 0, 1};//
        int player_0[] = {0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 3, 3};//0号玩家手牌 1w1w1w1w2w2w2w3w3w3w4w4w
        int player_1[] = {9, 9, 9, 9, 10, 10, 10, 10, 11, 11, 11, 12, 12, 27};//1号手牌 1t1t1t1t2t2t2t2t3t3t3t4t4tE
        int player_2[] = {18, 18, 18, 18, 19, 19, 19, 19, 20, 20, 20, 20, 21};//2号手牌 1b1b1b1b2b2b2b2b3b3b3b3b4b
        int player_3[] = {22, 22, 22, 22, 23, 23, 23, 23, 24, 24, 24, 24, 25};//3号手牌 5b5b5b5b6b6b6b6b7b7b7b7b8b

        /*
         * 历史动作 [ [1号,打，东风]，[2号，抓，东风],[2号，打，东风]，[3号，抓，东风],[3号，打，东风]，
         *          [0号，抓，四条],[0号，打，四条]]
         */

        List<Action> acitons = new ArrayList<>();
        acitons.add(new Action(1,ACT_H_DISCARD,27,-1));
        acitons.add(new Action(2,ACT_DRAW,27,-1));
        acitons.add(new Action(2,ACT_H_DISCARD,27,-1));
        acitons.add(new Action(3,ACT_DRAW,27,-1));
        acitons.add(new Action(3,ACT_H_DISCARD,27,-1));
        acitons.add(new Action(0,ACT_DRAW,4,-1));
        acitons.add(new Action(0,ACT_H_DISCARD,4,-1));

        int actionSeat[] = new int[acitons.size()];
        int actionType[] = new int[acitons.size()];
        int actionTile[] = new int[acitons.size()];
        int actionStealed[] = new int[acitons.size()];
        for(int i=0;i<acitons.size();i++){
            actionSeat[i] = acitons.get(i).getSeat();
            actionType[i] = acitons.get(i).getType();
            actionTile[i] = acitons.get(i).getTile();
            actionStealed[i] = acitons.get(i).getSteald();
        }

        /**
         * 合法动作
         * [ [1号,右吃，四条]，[1号，碰，四条]]
         */
        List<Action> legalAcitons = new ArrayList<>();
        legalAcitons.add(new Action(1,ACT_R_CHOW,4,0));
        legalAcitons.add(new Action(1,ACT_PONG,4,0));
        int legalActionSeat[] = new int[legalAcitons.size()];
        int legalActionType[] = new int[legalAcitons.size()];
        int legalActionTile[] = new int[legalAcitons.size()];
        int legalActionStealed[] = new int[legalAcitons.size()];
        for(int i=0;i<legalAcitons.size();i++){
            legalActionSeat[i] = legalAcitons.get(i).getSeat();
            legalActionType[i] = legalAcitons.get(i).getType();
            legalActionTile[i] = legalAcitons.get(i).getTile();
            legalActionStealed[i] = legalAcitons.get(i).getSteald();
        }

        int ret[] = {-1,-1,-1,-1};//返回值，依次是 座位号，动作，牌，上一个动作位

        int code = TDHAIJNI.TDHAIJNI(
                aiSeat,dealerSeat,hun,realPlayer,
                player_0,player_1,player_2,player_3,
                actionSeat,actionType,actionTile,actionStealed,legalActionSeat,
                legalActionType,legalActionTile,legalActionStealed,
                ret);
        if(code == 0){
            System.out.println("成功");
        }else{
            System.out.println("失败执行");
        }
    }

}
