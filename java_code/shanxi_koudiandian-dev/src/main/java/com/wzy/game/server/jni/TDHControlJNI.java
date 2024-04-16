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
public class TDHControlJNI {

    private static Logger logger = LoggerFactory.getLogger(TDHControlJNI.class);
    private static String LINNAEM = "tdhControlJNI";

    static {
        try {
            if (PlatformPath.SystemLoadLibrary(LINNAEM)) {
                System.out.println("load system lib " + LINNAEM + " sucess");
                logger.info("load system lib {} sucess", LINNAEM);
            } else if (PlatformPath.SystemLoadClass(LINNAEM)) {
                System.out.println("load current path lib  " + LINNAEM + "  sucess");
                logger.info("load current path lib {} sucess", LINNAEM);
            } else if (PlatformPath.SystemLoadAbsolutePath(Paths.get(PlatformPath.getDynamicLibraryDir(),
                    PlatformPath.combinationLibrayName(LINNAEM)))) {
                System.out.println("load user path lib  " + LINNAEM + "  sucess");
                logger.info("load user path lib {} sucess", LINNAEM);
            } else {
                logger.info("load failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 玩家座位号   庄家座位号  返回的牌墙
     * 牌墙中以庄家为起点抓牌 每次12张  然后每次一张  然后庄家一张牌   然后翻开一张牌
     *
     * @param playerSeat 玩家座位号(需要被控位置的座位号)，控制差牌
     * @param dealer     庄家座位号
     * @param allCards   返回的牌墙，ID 为 0 - 42
     * @return 返回的错误吗
     */
    public static native int GenarateAllCard(int playerSeat, int dealer,
                                             int[] allCards);

    /**
     * @param reqType        控制的类型 ， 0 是随机发牌， 1 是普通控牌 2 是小黑屋控牌
     * @param wall           当前剩余的牌墙，保证会移除返回的牌,ID为十六机制的ID
     * @param playerType     四个玩家的类型 0 是机器人，1 是真人玩家
     * @param curSeat        当前需要控制玩家的位置
     * @param allHands0      玩家0号位的手牌，牌的ID 为16进制的ID
     * @param allHands1      玩家1号位的手牌，牌的ID 为16进制的ID
     * @param allHands2      玩家2号位的手牌，牌的ID 为16进制的ID
     * @param allHands3      玩家3号位的手牌，牌的ID 为16进制的ID
     * @param showGrouptile0 玩家0号位置的副牌的牌值，为16进制的ID
     * @param showGrouptype0 玩家0号位置的副牌的类型，值参考本类的静态常量
     * @param showGrouptile1 玩家1号位置的副牌的牌值，为16进制的ID
     * @param showGrouptype1 玩家1号位置的副牌的类型，值参考本类的静态常量
     * @param showGrouptile2 玩家2号位置的副牌的牌值，为16进制的ID
     * @param showGrouptype2 玩家2号位置的副牌的类型，值参考本类的静态常量
     * @param showGrouptile3 玩家3号位置的副牌的牌值，为16进制的ID
     * @param showGrouptype3 玩家3号位置的副牌的类型，值参考本类的静态常量
     * @param hun            混牌的十六进制ID
     * @param dealer         发牌时候的庄家座位号
     * @param inittiles0     发牌时候，玩家0号位置的起始手牌数据
     * @param inittiles1     发牌时候，玩家1号位置的起始手牌数据
     * @param inittiles2     发牌时候，玩家2号位置的起始手牌数据
     * @param inittiles3     发牌时候，玩家3号位置的起始手牌数据
     * @param actionSeat     动作位置 {actionSeat,actionType,actionTile,actionStealed} 配套使用
     * @param actionType     动作类型，TDHAIJNI类常量值，{actionSeat,actionType,actionTile,actionStealed} 配套使用
     * @param actionTile     动作的牌，{actionSeat,actionType,actionTile,actionStealed} 配套使用
     * @param actionStealed  提供方
     * @return 返回下一张要摸的牌
     */
    public static native int getControlCard(int reqType, int[] wall, int[] playerType, int curSeat,
                                            int[] allHands0, int[] allHands1, int[] allHands2, int[] allHands3,
                                            int[] showGrouptile0, int[] showGrouptype0,
                                            int[] showGrouptile1, int[] showGrouptype1,
                                            int[] showGrouptile2, int[] showGrouptype2,
                                            int[] showGrouptile3, int[] showGrouptype3,
                                            int hun,
                                            int dealer,
                                            int[] inittiles0, int[] inittiles1, int[] inittiles2, int[] inittiles3,
                                            int[] actionSeat, int[] actionType, int[] actionTile, int[] actionStealed);


    public static void main(String[] args) {
        int playerSeat = 1;
        int dealer = 1;
        int[] allCards = new int[144];
//        for (int i = 0; i < 20; i++) {
//            int code = TDHControlJNI.GenarateAllCard(
//                    playerSeat, dealer, allCards);
//            System.out.println(allCards[0] + allCards[1] + allCards[2] + allCards[11] + allCards[12] + allCards[21]);
//        }

        int aiSeat = 1;
        int dealerSeat = 0;
        int hun = 9;//1t
        int realPlayer[] = {0, 0, 0, 1};//
        int player_0[] = {0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 3, 3};//0号玩家手牌 1w1w1w1w2w2w2w3w3w3w4w4w
        int player_1[] = {9, 9, 9, 9, 10, 10, 10, 10, 11, 11, 11, 12, 12, 27};//1号手牌 1t1t1t1t2t2t2t2t3t3t3t4t4tE
        int player_2[] = {18, 18, 18, 18, 19, 19, 19, 19, 20, 20, 20, 20, 21};//2号手牌 1b1b1b1b2b2b2b2b3b3b3b3b4b
        int player_3[] = {22, 22, 22, 22, 23, 23, 23, 23, 24, 24, 24, 24, 25};//3号手牌 5b5b5b5b6b6b6b6b7b7b7b7b8b
        int[] wall = {0x081 ,0x181 ,0x181 };//3号手牌 5b5b5b5b6b6b6b6b7b7b7b7b8b
        int[] fulu = {};//3号手牌 5b5b5b5b6b6b6b6b7b7b7b7b8b

        /*
         * 历史动作 [ [1号,打，东风]，[2号，抓，东风],[2号，打，东风]，[3号，抓，东风],[3号，打，东风]，
         *          [0号，抓，四条],[0号，打，四条]]
         */

        List<TDHAIJNI.Action> acitons = new ArrayList<>();
        acitons.add(new TDHAIJNI.Action(1,1,27,-1));

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


        int ret[] = {-1,-1,-1,-1};//返回值，依次是 座位号，动作，牌，上一个动作位

        int code = TDHControlJNI.getControlCard(
                0, wall, realPlayer, 1,
                player_0, player_1,player_2, player_3,
                fulu, fulu,
                fulu, fulu,
                fulu, fulu,
                fulu, fulu,
                0x081 ,
                0,
                player_0, player_1, player_2, player_3,
                actionSeat, actionType, actionTile, actionStealed);
        System.out.println(code);
//        if (code == 0) {
//            System.out.println("成功");
//        } else {
//            System.out.println("失败执行");
//        }
    }

}
