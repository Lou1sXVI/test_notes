package com.wzy.game.server.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.wzy.game.server.ai.ParamMode.*;

public class AILevel1 extends AIBase implements AIResponseInterface {

    private static final Logger logger = LoggerFactory.getLogger(AILevel1.class);


    private static final AILevel1 instance = new AILevel1();

    private AILevel1() {
    }

    public static AILevel1 getInstance() {
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

            //暗杠
            if (hands[lastTile] == 4) {
                ret.setActionType(ACTION_ANGANG);
                ret.setCurTile(lastTile);
                return;
            }

            List<SplitGroupsHelp> myResult = splitHandsNew(hands);
            int myWinDistance = getMinWinDistance(myResult);
            filter(myResult, myWinDistance);
            logger.info("myWinDistance:" + myWinDistance);
            for (int i = 0; i < myResult.size(); i++) {
                logger.info(myResult.get(i).toString());
            }

            //这种情况是组牌组好了，但是不满足胡牌条件
            if (myWinDistance == 0) {
                //从第一张开始扔
                for (int i = 0; i < TILE_TYPE_COUNT; i++) {
                    if (hands[i] > 0) {
                        ret.setActionType(ACTION_DISCARD);
                        ret.setCurTile(i);
                        return;
                    }
                }
            }

            if (myWinDistance == 1) {
                List<TingHelp> tingres = new ArrayList<>();
                boolean isTing = checkTing(myResult, showGroup, hands, remains, tingres);
                if (isTing) {
                    ret.setActionType(ACTION_READY);
                    ret.setCurTile(getBestTing(remains, tingres));
                    return;
                }
            }

            int tmp = discardsingleFJfirst(hands);
            if (tmp != -1) {
                ret.setActionType(ACTION_DISCARD);
                ret.setCurTile(tmp);
                return;
            }

            //如果没有单张，先移除顺子，再移除刻，看看有没有单张
            int[] handstmp = removeShun(hands);
            handstmp = removeGangAndKe(handstmp);
            tmp = discardsingleFJfirst(handstmp);
            if (tmp != -1) {
                ret.setActionType(ACTION_DISCARD);
                ret.setCurTile(tmp);
                return;
            }

            //只有对子数量为1，或者2的时候，才移除一个对子，否则对子的价值降低，不需要移除这个对子。
            if (calcPaircount(handstmp) == 1) {
                //找到第一个对子，移除这个对子之后再判断单张
                for (int i = 0; i < TILE_TYPE_COUNT; i++) {
                    if (handstmp[i] == 2) {
                        handstmp[i] -= 2;
                        break;
                    }
                }
                tmp = discardsingleFJfirst(handstmp);
                if (tmp != -1) {
                    ret.setActionType(ACTION_DISCARD);
                    ret.setCurTile(tmp);
                    return;
                }
            }

            //移除所有的连张
            int[] handstmp2 = removeLianzhang(handstmp);
            //移除完之后没手牌了，不能移除，加上去，从小往大扔一张牌
            if (calcHandCount(handstmp2) == 0) {
                ret.setActionType(ACTION_DISCARD);
                ret.setCurTile(getMinValueTile(handstmp));
                return;
            } else {
                tmp = discardsingleFJfirst(handstmp2);
                if (tmp != -1) {
                    ret.setActionType(ACTION_DISCARD);
                    ret.setCurTile(tmp);
                    return;
                }
            }

            //以上情况都没有单张，就从第一张手牌开始扔
            for (int i = TILE_TYPE_COUNT - 1; i >= 0; i--) {
                if (handstmp[i] > 0) {
                    ret.setActionType(ACTION_DISCARD);
                    ret.setCurTile(i);
                    return;
                }
            }

            return;
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
            int remainsLen = act.getRemainsLength();
            int lastTile = act.getLastActionTile();
            List<Group> showGroup = act.getMyGroups();

            if (act.getIsTing()[mySeat]) {
                ackWhileOthersDiscardWhenTing(hands, lastTile, showGroup, remains, ret);
            } else {
                //能碰就碰，能杠就杠
                if (hands[lastTile] == 2) {
                    ret.setActionType(ACTION_PONG);
                    ret.setCurTile(lastTile);
                    return;
                }

                if (hands[lastTile] == 3) {
                    ret.setActionType(ACTION_MINGGANG);
                    ret.setCurTile(lastTile);
                    return;
                }
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
