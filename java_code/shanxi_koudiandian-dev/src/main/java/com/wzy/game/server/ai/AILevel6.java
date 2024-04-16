package com.wzy.game.server.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.wzy.game.server.ai.ParamMode.*;

public class AILevel6 extends AIBase implements AIResponseInterface {

    private static final Logger logger = LoggerFactory.getLogger(AILevel6.class);


    private static final AILevel6 instance = new AILevel6();

    private AILevel6() {
    }

    public static AILevel6 getInstance() {
        return instance;
    }

    /**
     * 处理打牌逻辑分支
     *
     * @param act
     */
    @Override
    public void doAction(ParamMode act) {
        //根据上一次动作，进入逻辑分支
        switch (act.getLastActionType()) {
            case ACTION_DRAW: //自己抓牌
                logicDrawTile(act);
                break;
            case ACTION_DISCARD: //别人打牌
                logicDiscardTile(act);
                break;
            case ACTION_BUGANG: //别人补杠
                logicAfterBuGangTile(act);
                break;
            case ACTION_PONG: //碰
                logicAfterPongTile(act);
                break;
            default:
                break;
        }
    }

    /**
     * 抓牌后打牌逻辑
     *
     * @param act
     */
    private void logicDrawTile(ParamMode act) {
        GameAction ret = act.getRetAction();//准备部分返回数据
        ret.getUseChowOrPengOrGangTiles().clear();
        TreeSet<Integer> legalActions = new TreeSet<>(ret.getLegalAction());

        //确定双方座位号
        int mySeat = act.getMySeat();

        //解析双方手牌和余牌信息
        int[] hands = parseHands(act)[mySeat];
        int[] remains = parseRemains(act);
        List<Group> showGroup = act.getMyGroups();
        int lastTile = act.getLastActionTile();

        //有没有听牌
        if (act.getIsTing()[mySeat]) {//已听牌
            ackWhileSelfDrawWhenTing(hands, lastTile, showGroup, remains, ret);
            return;
        } else {//没有听牌
            //补杠
            for (Group sge : act.getMyGroups()) {
                if (sge.isKe() && ret.getLegalAction().contains(ParamMode.ACTION_BUGANG)) {
                    if (lastTile == sge.getCards().get(0)) {
                        ret.setActionType(ACTION_BUGANG);
                        ret.setCurTile(sge.getCards().get(0));
                        ret.getUseChowOrPengOrGangTiles().add(sge.getCards().get(0));
                        ret.getUseChowOrPengOrGangTiles().add(sge.getCards().get(1));
                        ret.getUseChowOrPengOrGangTiles().add(sge.getCards().get(2));
                        return;
                    }
                }
            }

            //走七对的逻辑
            if (showGroup.size() == 0) {
                int paircount = calcPaircount(hands);
                if (paircount == 5) {//五对的话一定走七对的逻辑
                    int[] hands_qidui = hands.clone();
                    for (int i = 0; i < TILE_TYPE_COUNT; i++) {
                        if (hands_qidui[i] == 2 || hands_qidui[i] == 3) {
                            hands_qidui[i] -= 2;
                        } else if (hands_qidui[i] == 4) {
                            hands_qidui[i] -= 4;
                        }
                    }
                    ret.setActionType(ACTION_DISCARD);
                    ret.setCurTile(getBestDiscard4Qidui(hands_qidui, remains));
                    return;
                }

                if (paircount == 6) {//能不能听牌？
                    List<SplitGroupsHelp> myResult = splitHandsNew(hands);
                    int myWinDistance = getMinWinDistance(myResult);
                    filter(myResult, myWinDistance);
                    if (myWinDistance == 1) {
                        List<TingHelp> tingres = new ArrayList<>();
                        boolean isTing = checkTing(myResult, showGroup, hands, remains, tingres);
                        if (isTing) {
                            ret.setActionType(ACTION_READY);
                            ret.setCurTile(getBestTing(remains, tingres));
                            return;
                        }
                    }
                }
            }

            //暗杠
            if (hands[lastTile] == 4) {
                ret.setActionType(ACTION_ANGANG);
                ret.setCurTile(lastTile);
                return;
            }

            //拆分手牌
            List<SplitGroupsHelp> myResult = splitHandsNew(hands);
            int myWinDistance = getMinWinDistance(myResult);
            filter(myResult, myWinDistance);
            logger.info("myWinDistance:" + myWinDistance);
            for (int i = 0; i < myResult.size(); i++) {
                logger.info(myResult.get(i).toString());
            }

            //这种情况是组牌组好了，但是不满足胡牌条件
            if (myWinDistance == 0) {
                //依次扔掉一张，看进张效率
                List<Integer> discardtile = new ArrayList<Integer>();
                List<Integer> wincountlist = new ArrayList<Integer>();
                for (int i = 0; i < TILE_TYPE_COUNT; i++) {
                    if (hands[i] > 0) {
//                        if (i < 27 && i % 9 <= 5) continue;//不能扔1-5点的牌
                        hands[i]--;
                        List<SplitGroupsHelp> res = splitHandsNew(hands);
                        int myWinDis = getMinWinDistance(res);
                        filter(res, myWinDis);
                        Set<Integer> tingSet = new HashSet();
                        findTingSet4Dianpao(tingSet, res);
                        int wincount = countWinCount(tingSet, remains);
                        discardtile.add(i);
                        wincountlist.add(wincount);
                        hands[i]++;
                    }
                }

                if (discardtile.size() > 0) {
                    int betterout = -1;
                    int wincount = -1;
                    for (int i = 0; i < discardtile.size(); i++) {
                        if (wincountlist.get(i) > wincount) {
                            betterout = discardtile.get(i);
                            wincount = wincountlist.get(i);
                        }
                    }
                    if (wincount == 0) {
                        ret.setActionType(ACTION_DISCARD);
                        ret.setCurTile(betterout);
                        return;
                    } else {
                        ret.setActionType(ACTION_READY);
                        ret.setCurTile(betterout);
                        return;
                    }
                }
            }

            //上听
            if (myWinDistance == 1) {
                List<TingHelp> tingres = new ArrayList<>();
                boolean isTing = checkTing(myResult, showGroup, hands, remains, tingres);
                if (isTing) {
                    ret.setActionType(ACTION_READY);
                    ret.setCurTile(getBestTing(remains, tingres));
                    return;
                } else {//从搭子中扔牌，看进张听牌的数量
                    int betterOut = discard4LowPointNoTing(myResult, hands, showGroup, remains);
                    if (betterOut != -1) {
                        ret.setActionType(ACTION_DISCARD);
                        ret.setCurTile(betterOut);
                        return;
                    }
                }
            }

            //一上听
            if (myWinDistance == 2) {
                int betterOut = discard4OneStep2Ting(myResult, hands, remains, showGroup, myWinDistance);
                if (betterOut != -1) {
                    ret.setActionType(ACTION_DISCARD);
                    ret.setCurTile(betterOut);
                    return;
                }
            }

            //有单张直接扔单张
            int tmp = discardsinglebyvalue(hands,remains);
            if (tmp != -1) {
                ret.setActionType(ACTION_DISCARD);
                ret.setCurTile(tmp);
                return;
            }

            int betterOut = discardCardByWeightValue(myResult, hands, remains, myWinDistance);
            if (betterOut != -1) {
                ret.setActionType(ACTION_DISCARD);
                ret.setCurTile(betterOut);
            }
        }
    }

    private void logicDiscardTile(ParamMode act) {
        if (act.getLastActionSeat() != act.getMySeat()
                && act.getLastActionType() == ACTION_DISCARD) {//对手打牌
            GameAction ret = act.getRetAction();
            ret.setCurTile(act.getLastActionTile());
            ret.getUseChowOrPengOrGangTiles().clear();
            ret.setActionType(ParamMode.ACTION_PASS);
            ret.setActionSecondTime(2);
            int mySeat = act.getMySeat();
            int[] hands = parseHands(act)[mySeat];

            //统计余牌信息
            int[] remains = parseRemains(act);
            int lastTile = act.getLastActionTile();
            List<Group> showGroup = act.getMyGroups();

            //已听牌
            if (act.getIsTing()[mySeat]) {
                ackWhileOthersDiscardWhenTing(hands, lastTile, showGroup, remains, ret);
            } else {
                if (calcPaircount(hands) >= 5) return;

                boolean canGang = false, canPong = false;
                if (hands[lastTile] == 2) canPong = true;
                if (hands[lastTile] == 3) canGang = true;

                if (canGang == false && canPong == false) {
                    return;
                }

                List<SplitGroupsHelp> myResult = splitHandsNew(hands);
                int myWinDistance = getMinWinDistance(myResult);
                filter(myResult, myWinDistance);
                moveCheckHighlevel(ret, hands, remains, lastTile, myWinDistance, showGroup, canPong, canGang);
            }
        }
    }

    /**
     * 碰后打牌逻辑
     *
     * @param act
     */
    private void logicAfterPongTile(ParamMode act) {
        if (act.getLastActionSeat() == act.getMySeat()
                && act.getLastActionType() == ParamMode.ACTION_PONG) {
            act.getRetAction().getLegalAction().remove(Integer.valueOf(ACTION_WIN));
            act.getRetAction().getLegalAction().remove(Integer.valueOf(ACTION_ANGANG));
            act.getRetAction().getLegalAction().remove(Integer.valueOf(ACTION_BUGANG));
            act.setLastActionSeat(act.getMySeat());
            act.setLastActionType(ACTION_DRAW);
            act.setLastActionTile(act.getMyHands().get(0));
            logicDrawTile(act);
        }
    }


    /**
     * 别人补杠时候，判断是否可以抢杠胡
     *
     * @param act
     */
    private void logicAfterBuGangTile(ParamMode act) {
        if (act.getLastActionSeat() != act.getMySeat()
                && act.getLastActionType() == ACTION_BUGANG) {//对手补杠

            GameAction ret = act.getRetAction();
            ret.setCurTile(act.getLastActionTile());
            ret.getUseChowOrPengOrGangTiles().clear();
            ret.setActionType(ParamMode.ACTION_PASS);
            int mySeat = act.getMySeat();
            int[] hands = parseHands(act)[mySeat];
            int lastTile = act.getLastActionTile();

            //能不能胡牌
            if (act.getIsTing()[mySeat]) {
                //听哪些牌
                List<SplitGroupsHelp> res = splitHandsNew(hands);
                int myWinDis = getMinWinDistance(res);
                filter(res, myWinDis);
                Set<Integer> tingSet = new HashSet();
                findTingSet4Dianpao(tingSet, res);
                for (int tile : tingSet) {
                    if (lastTile == tile) {
                        hands[lastTile]++;
                        ret.setActionType(ACTION_WIN);
                        ret.setCurTile(lastTile);
                        List<Group> showGroup = act.getMyGroups();
                        ret.setPoint(AIConstant.FAN_SCORE[getHuTypeKoudian(hands, showGroup)]);
                        return;
                    }
                }
            }
        }
    }
}
