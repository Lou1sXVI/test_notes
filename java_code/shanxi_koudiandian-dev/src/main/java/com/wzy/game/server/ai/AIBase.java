package com.wzy.game.server.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static com.wzy.game.server.ai.ParamMode.*;
import static com.wzy.game.server.constant.ConstVar.*;
import static com.wzy.game.server.util.CodeUtil.TILE_STRING;

public abstract class AIBase {

    private static final Logger logger = LoggerFactory.getLogger(AIBase.class);
    public static final int TILE_TYPE_COUNT = 34;   //牌种类数，共16种1
    private final String[] FAN_NAME_ARRAY = new String[]{"平和", "平和", "一条龙", "清一色", "七对", "豪华七对", "十三幺", "清一条龙", "清七对", "清豪华七对"}; // kdd番种

    //听牌后别人扔出一张牌的打牌逻辑
    public void ackWhileOthersDiscardWhenTing(int[] hands, int lastTile, List<Group> showGroup, int[] remains, GameAction ret) {
        //听哪些牌？
        List<SplitGroupsHelp> res = splitHandsNew(hands);
        filter(res, 1);
        Set<Integer> tingSet = new HashSet();
        findTingSet4Dianpao(tingSet, res);
        for (int tile : tingSet) {
            if (lastTile == tile) {
                ret.setActionType(ACTION_WIN);
                ret.setCurTile(lastTile);
                hands[lastTile]++;
                ret.setPoint(AIConstant.FAN_SCORE[getHuTypeKoudian(hands, showGroup, ret)]);
                return;
            }
        }

        //看看能不能明杠
        if (hands[lastTile] == 3) {
            hands[lastTile] -= 3;
            res = splitHandsNew(hands);
            filter(res, 1);
            Group tmp = new Group();
            List<Integer> card = new ArrayList<>();
            card.add(lastTile);
            card.add(lastTile);
            card.add(lastTile);
            card.add(lastTile);
            tmp.setCards(card);
            showGroup.add(tmp);
            boolean isTing = checkTing13cards(res, remains, new HashSet<>());
            if (isTing) {
                ret.setActionType(ACTION_MINGGANG);
                ret.setCurTile(lastTile);
                return;
            }
            showGroup.remove(tmp);
        }
        return;
    }

    public void ackWhileOthersDiscardWhenNoTingMiddleLevel(int[] hands, int lastTile, GameAction ret) {
        if (calcPaircount(hands) >= 5) return;

        if (hands[lastTile] == 3) {
            ret.setActionType(ACTION_MINGGANG);
            ret.setCurTile(lastTile);
            return;
        }

        if (hands[lastTile] == 2) {
            List<SplitGroupsHelp> res = splitHandsNew(hands);
            int wdbefore = getMinWinDistance(res);
            hands[lastTile] -= 2;
            res = splitHandsNew(hands);
            int wdafter = getMinWinDistance(res);
            if (wdafter < wdbefore) {
                ret.setActionType(ACTION_PONG);
                ret.setCurTile(lastTile);
            }
        }
    }

    //听牌后自己抓牌的打牌逻辑
    public void ackWhileSelfDrawWhenTing(int[] hands, int lastTile, List<Group> showGroup, int[] remains, GameAction ret) {
        //听哪些牌
        hands[lastTile]--;
        List<SplitGroupsHelp> res = splitHandsNew(hands);
        int myWinDis = getMinWinDistance(res);
        filter(res, myWinDis);
        Set<Integer> tingSet = new HashSet();
        findTingSet4Zimo(tingSet, res);
        for (int tile : tingSet) {
            if (lastTile == tile) {
                ret.setActionType(ACTION_WIN);
                ret.setCurTile(lastTile);
                hands[lastTile]++;
                ret.setPoint(AIConstant.FAN_SCORE[getHuTypeKoudian(hands, showGroup, ret)]);
                return;
            }
        }
        hands[lastTile]++;

        //看看能不能暗杠
        if (hands[lastTile] == 4) {
            hands[lastTile] -= 4;
            res = splitHandsNew(hands);
            filter(res, 1);
            Group tmp = new Group();
            List<Integer> card = new ArrayList<>();
            card.add(lastTile);
            card.add(lastTile);
            card.add(lastTile);
            card.add(lastTile);
            tmp.setCards(card);
            showGroup.add(tmp);
            boolean isTing = checkTing13cards(res, remains, new HashSet<>());
            if (isTing) {
                ret.setActionType(ACTION_ANGANG);
                ret.setCurTile(lastTile);
                return;
            }
            showGroup.remove(tmp);
        }

        //看看能不能补杠
        if (checkBuGang(showGroup, lastTile)) {
            ret.setActionType(ACTION_BUGANG);
            ret.setCurTile(lastTile);
            return;
        }

        //直接扔牌
        ret.setActionType(ACTION_DISCARD);
        ret.setCurTile(lastTile);
        return;
    }

    //已经是听牌牌型了，但是点数太小，不能听牌，该怎么打牌。
    public int discard4LowPointNoTing(List<SplitGroupsHelp> myResult, int hands[], List<Group> showGroup, int remains[]) {
        Set<Integer> outSet = new TreeSet<>();
        for (SplitGroupsHelp splitGroupsHelp : myResult) {
            for (SplitGroup splitGroup : splitGroupsHelp.getCurSplitGroup()) {
                if (splitGroup.getType() == SplitGroupType.SPLIT_GROUP_TYPE_SINGLE) {
                    outSet.add(splitGroup.getTile());
                } else if (splitGroup.getType() == SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG) {
                    outSet.add(splitGroup.getTile());
                    outSet.add(splitGroup.getTile() + 1);
                } else if (splitGroup.getType() == SplitGroupType.SPLIT_GROUP_TYPE_KANZHANG) {
                    outSet.add(splitGroup.getTile());
                    outSet.add(splitGroup.getTile() + 2);
                } else if (splitGroupsHelp.getCurPairCount() == 2 && splitGroup.getType() == SplitGroupType.SPLIT_GROUP_TYPE_PAIR) {
                    outSet.add(splitGroup.getTile());
                }
            }
        }

        Map<Integer, Integer> profitCountByTile = new TreeMap<>();
        for (int i : outSet) {
            hands[i]--;
            List<SplitGroupsHelp> tempResult = splitHandsNew(hands);
            hands[i]++;
            int minWinDistance = getMinWinDistance(tempResult);
            Set<Integer> profitTiles = new HashSet<>();
            for (SplitGroupsHelp splitGroupsHelp : tempResult) {
                if (minWinDistance == splitGroupsHelp.getWinDistance()) {
                    validTakeIn(splitGroupsHelp, profitTiles);
                }
            }

            //判断有效进张能不能听牌
            Set<Integer> validtakein = new HashSet<>();
            for (Integer element : profitTiles) {
                hands[i]--;
                hands[element]++;
                List<SplitGroupsHelp> tempRes = splitHandsNew(hands);
                filter(tempRes, 1);
                hands[i]++;
                hands[element]--;
                List<TingHelp> tingres = new ArrayList<>();
                boolean isTing = checkTing(tempRes, showGroup, hands, remains, tingres);
                if (isTing == true) {
                    validtakein.add(element);
                }
            }

            int count = 0;
            for (int j = 0; j < TILE_TYPE_COUNT; j++) {
                if (remains[j] > 0 && validtakein.contains(j)) {
                    count += remains[j];
                }
            }
            String builder = validtakein.stream().map(profitTile -> AIConstant.CARD_NAME[profitTile] + ',').collect(Collectors.joining());
            logger.info(i + ":" + AIConstant.CARD_NAME[i] + ":" + builder + ":" + count);
            profitCountByTile.put(i, count);
        }

        //找进张最多的那张牌
        int minCount = -1;
        int betterOut = -1;
        for (Map.Entry<Integer, Integer> entry : profitCountByTile.entrySet()) {
            if (entry.getValue() > minCount) {
                minCount = entry.getValue();
                betterOut = entry.getKey();
                continue;
            } else if (entry.getValue() == minCount) {
                if (AIConstant.CARD_VALUE[entry.getKey()] <
                        AIConstant.CARD_VALUE[betterOut]) {
                    betterOut = entry.getKey();
                    continue;
                } else if (AIConstant.CARD_VALUE[entry.getKey()] ==
                        AIConstant.CARD_VALUE[betterOut]) {
                    if (entry.getKey() % 9 < betterOut % 9) {
                        betterOut = entry.getKey();
                        continue;
                    }
                }
            }
        }

        return betterOut;
    }

    //一上听的牌型出哪张牌
    public int discard4OneStep2Ting(List<SplitGroupsHelp> myResult, int hands[], int remains[], List<Group> showGroup, int myWinDistance) {
        Set<Integer> outSet = getOutSetFromSplitGroupsHelp(myResult, hands);
        Map<Integer, Integer> profitCountByTile = new TreeMap<>();
        for (int i : outSet) {
            hands[i]--;
            List<SplitGroupsHelp> tempResult = splitHandsNew(hands);
            hands[i]++;
            int minWinDistance = getMinWinDistance(tempResult);
            if (minWinDistance > myWinDistance) {
                continue;
            }
            Set<Integer> profitTiles = new HashSet<>();
            for (SplitGroupsHelp splitGroupsHelp : tempResult) {
                if (minWinDistance == splitGroupsHelp.getWinDistance()) {
                    validTakeIn(splitGroupsHelp, profitTiles);
                }
            }

            //判断有效进张能不能听牌
            Set<Integer> validtakein = new HashSet<>();
            for (Integer element : profitTiles) {
                hands[i]--;
                hands[element]++;
                List<SplitGroupsHelp> tempRes = splitHandsNew(hands);
                filter(tempRes, 1);
                hands[i]++;
                hands[element]--;
                List<TingHelp> tingres = new ArrayList<>();
                boolean isTing = checkTing(tempRes, showGroup, hands, remains, tingres);
                if (isTing == true) {
                    validtakein.add(element);
                }
            }

            int count = 0;
            for (int j = 0; j < TILE_TYPE_COUNT; j++) {
                if (remains[j] > 0 && validtakein.contains(j)) {
                    count += remains[j];
                }
            }
            String builder = validtakein.stream().map(profitTile -> AIConstant.CARD_NAME[profitTile] + ',').collect(Collectors.joining());
            logger.info(i + ":" + AIConstant.CARD_NAME[i] + ":" + builder + ":" + count);
            profitCountByTile.put(i, count);
        }

        //找进张最多的那张牌
        int minCount = -1;
        int betterOut = -1;
        for (Map.Entry<Integer, Integer> entry : profitCountByTile.entrySet()) {
            if (entry.getValue() > minCount) {
                minCount = entry.getValue();
                betterOut = entry.getKey();
                continue;
            } else if (entry.getValue() == minCount) {
                if (AIConstant.CARD_VALUE[entry.getKey()] <
                        AIConstant.CARD_VALUE[betterOut]) {
                    betterOut = entry.getKey();
                    continue;
                } else if (AIConstant.CARD_VALUE[entry.getKey()] ==
                        AIConstant.CARD_VALUE[betterOut]) {
                    if (entry.getKey() % 9 < betterOut % 9) {
                        betterOut = entry.getKey();
                        continue;
                    }
                }
            }
        }

        return betterOut;
    }

    //对子和点数加权算牌效
    public int discardCardByWeightValue(List<SplitGroupsHelp> myResult, int hands[], int remains[], int myWinDistance) {
        Map<Integer, Double> lowProfit = findLowWeightProfit(myResult, hands, remains);

        //找进张最多的那张牌
        double minValue = -1;
        int betterOut = -1;
        for (Map.Entry<Integer, Double> entry : lowProfit.entrySet()) {
            if (entry.getValue() > minValue) {
                minValue = entry.getValue();
                betterOut = entry.getKey();
                continue;
            } else if (entry.getValue() == minValue) {
                if (AIConstant.CARD_VALUE[entry.getKey()] <
                        AIConstant.CARD_VALUE[betterOut]) {
                    betterOut = entry.getKey();
                    continue;
                }
            }
        }
        return betterOut;
    }

    //对子和点数没有加权，直接按张数算牌效
    public int discardCard(List<SplitGroupsHelp> myResult, int hands[], int remains[], int myWinDistance) {
        Map<Integer, Integer> lowProfit = findLowProfit(myResult, hands, remains, myWinDistance);

        //找进张最多的那张牌
        double minValue = -1;
        int betterOut = -1;
        for (Map.Entry<Integer, Integer> entry : lowProfit.entrySet()) {
            if (entry.getValue() > minValue) {
                minValue = entry.getValue();
                betterOut = entry.getKey();
            } else if (entry.getValue() == minValue) {
                if (AIConstant.CARD_PRIORITY_OVER2STEPS[entry.getKey()] <
                        AIConstant.CARD_PRIORITY_OVER2STEPS[betterOut]) {
                    betterOut = entry.getKey();
                }
            }
        }
        return betterOut;
    }

    //手牌张数
    public int getMinValueTile(int discardlist[]) {
        int res = -1;
        int min4discard = 10000;
        for (int i = 0; i < TILE_TYPE_COUNT; i++) {
            if (discardlist[i] > 0) {
                if (AIConstant.CARD_VALUE[i] < min4discard) {
                    min4discard = AIConstant.CARD_VALUE[i];
                    res = i;
                }
            }
        }
        return res;
    }

    //返回手牌中有几个对子
    public int calcPaircount(int hands[]) {
        int res = 0;
        for (int i = 0; i < TILE_TYPE_COUNT; i++) {
            if (hands[i] == 2 || hands[i] == 3) {
                res++;
            }
            if (hands[i] == 4) {
                res += 2;
            }
        }
        return res;
    }

    public int calcHandCount(int hands[]) {
        int res = 0;
        for (int i = 0; i < TILE_TYPE_COUNT; i++) {
            res += hands[i];
        }
        return res;
    }

    //从前往后移除所有连张,注意，两头的连张不需要移除
    public int[] removeLianzhang(int hands[]) {
        int[] res = hands.clone();
        for (int i = 25; i >= 20; i--) {
            if (res[i] >= 1 && res[i - 1] >= 1) {
                res[i]--;
                res[i - 1]--;
                return removeLianzhang(res);
            }
        }

        for (int i = 16; i >= 11; i--) {
            if (res[i] >= 1 && res[i - 1] >= 1) {
                res[i]--;
                res[i - 1]--;
                return removeLianzhang(res);
            }
        }

        for (int i = 7; i >= 2; i--) {
            if (res[i] >= 1 && res[i - 1] >= 1) {
                res[i]--;
                res[i - 1]--;
                return removeLianzhang(res);
            }
        }
        return res;
    }

    public int getBestDiscard4Qidui(int discardlist[], int remains[]) {
        int res = -1;
        int cardremain = 4;
        for (int i = 0; i < TILE_TYPE_COUNT; i++) {
            if (discardlist[i] > 0) {
                if (remains[i] < cardremain) {
                    res = i;
                    cardremain = remains[i];
                }
            }
        }
        return res;
    }

    //从后往前移除所有顺子
    public int[] removeShun(int hands[]) {
        int[] res = hands.clone();
        for (int i = 26; i >= 20; i--) {
            if (res[i] >= 1 && res[i - 1] >= 1 && res[i - 2] >= 1) {
                res[i]--;
                res[i - 1]--;
                res[i - 2]--;
                return removeShun(res);
            }
        }

        for (int i = 17; i >= 11; i--) {
            if (res[i] >= 1 && res[i - 1] >= 1 && res[i - 2] >= 1) {
                res[i]--;
                res[i - 1]--;
                res[i - 2]--;
                return removeShun(res);
            }
        }

        for (int i = 8; i >= 2; i--) {
            if (res[i] >= 1 && res[i - 1] >= 1 && res[i - 2] >= 1) {
                res[i]--;
                res[i - 1]--;
                res[i - 2]--;
                return removeShun(res);
            }
        }
        return res;
    }

    public int[] removeGangAndKe(int hands[]) {
        int[] res = hands.clone();
        for (int i = 0; i < TILE_TYPE_COUNT; i++) {
            if (res[i] == 4) {
                res[i] -= 4;
                return removeGangAndKe(res);
            }

            if (res[i] == 3) {
                res[i] -= 3;
                return removeGangAndKe(res);
            }
        }
        return res;
    }

    //找到一个单张，按照价值扔单张
    public int discardsinglebyvalue(int hands[], int remains[]) {
        int[] discardlist = new int[TILE_TYPE_COUNT]; // 默认初值为0
        for (int i = 27; i < TILE_TYPE_COUNT; i++) {
            if (hands[i] == 1) {
                discardlist[i] = 1;
            }
        }

        //先扔风箭，剩余张数少的先出。
        int remaincount = 4;
        int betterout = -1;
        for (int i = 0; i < TILE_TYPE_COUNT; i++) {
            if (discardlist[i] > 0) {
                if (remains[i] < remaincount) {
                    betterout = i;
                    remaincount = remains[i];
                }
            }
        }
        if (betterout != -1) {
            return betterout;
        }

        for (int i = 0; i < 27; i++) {
            if (i % 9 == 0) {
                if (hands[i] == 1 && hands[i + 1] == 0 && hands[i + 2] == 0) {
                    discardlist[i] = 1;
                }
            } else if (i % 9 == 1) {
                if (hands[i] == 1 && hands[i + 1] == 0 && hands[i + 2] == 0 && hands[i - 1] == 0) {
                    discardlist[i] = 1;
                }
            } else if (i % 9 == 7) {
                if (hands[i] == 1 && hands[i - 2] == 0 && hands[i - 1] == 0 && hands[i + 1] == 0) {
                    discardlist[i] = 1;
                }
            } else if (i % 9 == 8) {
                if (i % 9 == 8 && hands[i] == 1 && hands[i - 1] == 0 && hands[i - 2] == 0) {
                    discardlist[i] = 1;
                }
            } else {
                if (hands[i] == 1 && hands[i - 2] == 0 && hands[i - 1] == 0 && hands[i + 1] == 0 && hands[i + 2] == 0) {
                    discardlist[i] = 1;
                }
            }
        }

        int res = -1;
        int min4discard = 10000;
        for (int i = 0; i < TILE_TYPE_COUNT; i++) {
            if (discardlist[i] > 0) {
                if (AIConstant.CARD_PRIORITY_OVER2STEPS[i] < min4discard) {
                    min4discard = AIConstant.CARD_PRIORITY_OVER2STEPS[i];
                    res = i;
                }
            }
        }
        return res;
    }

    //找到一个单张，从小往大扔单张,风箭先扔
    public int discardsingleFJfirst(int hands[]) {
        for (int i = 27; i < TILE_TYPE_COUNT; i++) {
            if (hands[i] == 1) {
                return i;
            }
        }
        for (int i = 0; i < 27; i++) {
            if (i % 9 == 0) {
                if (hands[i] == 1 && hands[i + 1] == 0 && hands[i + 2] == 0) {
                    return i;
                }
            } else if (i % 9 == 1) {
                if (hands[i] == 1 && hands[i + 1] == 0 && hands[i + 2] == 0 && hands[i - 1] == 0) {
                    return i;
                }
            } else if (i % 9 == 7) {
                if (hands[i] == 1 && hands[i - 2] == 0 && hands[i - 1] == 0 && hands[i + 1] == 0) {
                    return i;
                }
            } else if (i % 9 == 8) {
                if (i % 9 == 8 && hands[i] == 1 && hands[i - 1] == 0 && hands[i - 2] == 0) {
                    return i;
                }
            } else {
                if (hands[i] == 1 && hands[i - 2] == 0 && hands[i - 1] == 0 && hands[i + 1] == 0 && hands[i + 2] == 0) {
                    return i;
                }
            }
        }

        return -1;
    }

    //找到一个单张，风箭最后扔
    public int discardsingleFJlast(int hands[]) {
        for (int i = 0; i < 27; i++) {
            if (i % 9 == 0) {
                if (hands[i] == 1 && hands[i + 1] == 0 && hands[i + 2] == 0) {
                    return i;
                }
            } else if (i % 9 == 1) {
                if (hands[i] == 1 && hands[i + 1] == 0 && hands[i + 2] == 0 && hands[i - 1] == 0) {
                    return i;
                }
            } else if (i % 9 == 7) {
                if (hands[i] == 1 && hands[i - 2] == 0 && hands[i - 1] == 0 && hands[i + 1] == 0) {
                    return i;
                }
            } else if (i % 9 == 8) {
                if (i % 9 == 8 && hands[i] == 1 && hands[i - 1] == 0 && hands[i - 2] == 0) {
                    return i;
                }
            } else {
                if (hands[i] == 1 && hands[i - 2] == 0 && hands[i - 1] == 0 && hands[i + 1] == 0 && hands[i + 2] == 0) {
                    return i;
                }
            }
        }
        for (int i = 27; i < TILE_TYPE_COUNT; i++) {
            if (hands[i] == 1) {
                return i;
            }
        }

        return -1;
    }

    public int checkQidui(List<SplitGroupsHelp> grouplist, int remain[]) {
        for (int i = 0; i < grouplist.size(); i++) {
            SplitGroupsHelp ts = grouplist.get(i);
            if (ts.getCurPairCount() == 5) {//五个对子
                //得到所有的单张，连坎张，刻子或者顺子
                List<SplitGroup> danlist = new ArrayList<>();
                List<SplitGroup> kanzhanglist = new ArrayList<>();
                List<SplitGroup> lianzhanglist = new ArrayList<>();
                for (SplitGroup sp : ts.getCurSplitGroup()) {
                    if (sp.getType() == SplitGroupType.SPLIT_GROUP_TYPE_SINGLE)
                        danlist.add(sp);

                    if (sp.getType() == SplitGroupType.SPLIT_GROUP_TYPE_KANZHANG)
                        kanzhanglist.add(sp);

                    if (sp.getType() == SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG)
                        lianzhanglist.add(sp);
                }

                //扔剩余牌最少的单张
                if (danlist.size() > 0) {
                    int betterout = -1;
                    int remaincount = 5;
                    for (int j = 0; j < danlist.size(); j++) {
                        if (remain[danlist.get(j).getTile()] < remaincount) {
                            betterout = danlist.get(j).getTile();
                            remaincount = remain[danlist.get(j).getTile()];
                        }
                    }
                    return betterout;
                }

                //扔坎张
                if (kanzhanglist.size() > 0) {
                    int betterout = -1;
                    int remaincount = 5;
                    for (int j = 0; j < kanzhanglist.size(); j++) {
                        if (remain[kanzhanglist.get(j).getTile()] < remaincount) {
                            betterout = kanzhanglist.get(j).getTile();
                            remaincount = remain[kanzhanglist.get(j).getTile()];
                        }

                        if (remain[kanzhanglist.get(j).getTile() + 2] < remaincount) {
                            betterout = kanzhanglist.get(j).getTile();
                            remaincount = remain[kanzhanglist.get(j).getTile() + 2];
                        }
                    }
                    return betterout;
                }

                //扔连张
                if (lianzhanglist.size() > 0) {
                    int betterout = -1;
                    int remaincount = 5;
                    for (int j = 0; j < lianzhanglist.size(); j++) {
                        if (remain[lianzhanglist.get(j).getTile()] < remaincount) {
                            betterout = lianzhanglist.get(j).getTile();
                            remaincount = remain[lianzhanglist.get(j).getTile()];
                        }

                        if (remain[lianzhanglist.get(j).getTile() + 1] < remaincount) {
                            betterout = lianzhanglist.get(j).getTile();
                            remaincount = remain[lianzhanglist.get(j).getTile() + 1];
                        }
                    }
                    return betterout;
                }
            }
        }
        return -1;
    }

    public int checkGang(List<SplitGroupsHelp> grouplist, int lasttile) {
        for (int i = 0; i < grouplist.size(); i++) {
            SplitGroupsHelp ts = grouplist.get(i);
            if (ts.getInitLen() % 3 == 1) {//13张牌
                for (SplitGroup g : ts.getCurSplitGroup()) {
                    if (g.getType() == SplitGroupType.SPLIT_GROUP_TYPE_KE && g.getTile() == lasttile) {
                        return lasttile;
                    }
                }
            } else {//14张牌
                for (SplitGroup g : ts.getCurSplitGroup()) {
                    if (g.getType() == SplitGroupType.SPLIT_GROUP_TYPE_ANGANG) {
                        return g.getTile();
                    }
                }
            }
        }
        return -1;
    }

    public boolean checkBuGang(List<Group> showGroup, int lasttile) {
        for (Group g : showGroup) {
            if (g.isKe() && g.getCards().get(0) == lasttile) return true;
        }
        return false;
    }

    public double checkDoubleUseScoreWithCount(double myScore, int myCount, double otherScore, int otherCount, double myScoreNow) {
        int totalCount = myCount + otherCount;
        if (totalCount > 0) {
            return (myScore * myCount - otherCount * otherCount) / totalCount - myScoreNow;
        }
        return 0;
    }

    public int countWinCount(Set<Integer> winTiles, int remain[]) {
        int winCount = 0;
        for (int tile : winTiles) {
            winCount += remain[tile];
        }
        return winCount;
    }

    public int[] parseRemains(ParamMode act) {
        int[] remains = new int[34];
        Arrays.fill(remains, 4);
        for (int i = 0; i < act.getMyHands().size(); i++) {
            int tile = act.getMyHands().get(i);
            remains[tile]--;
        }
        for (List<Integer> tiles : act.getOppoHands()) {
            for (int tile : tiles) {
                remains[tile]--;
            }
        }
        for (int tile : act.getMyDiscards()) {
            int t = tile;
            if (t > 33) {
                t = (t - 34) * 9 + 4;
            }
            remains[tile]--;
        }
        for (List<Integer> tiles : act.getOppoDiscards()) {
            for (int tile :
                    tiles) {
                int t = tile;
                if (t > 33) {
                    t = (t - 34) * 9 + 4;
                }
                remains[t]--;
            }
        }
        for (Group groups : act.getMyGroups()) {
            if (groups.isKe()) {
                remains[groups.getCards().get(0)] -= 2; //打牌的时候已经扣了1张，所以只减2
            } else if (groups.isGang()) {
                remains[groups.getCards().get(0)] = 0;
            }
        }
        for (List<Group> groupList : act.getOppoGroups()) {
            for (Group groups : groupList) {
                if (groups.isKe()) {
                    remains[groups.getCards().get(0)] -= 2; //打牌的时候已经扣了1张，所以只减2
                } else if (groups.isGang()) {
                    remains[groups.getCards().get(0)] = 0;
                }
            }
        }
        return remains;
    }

    public int[][] parseHands(ParamMode act) {
        int[][] hands = new int[4][34];
        int mySeat = act.getMySeat();
        for (int tile : act.getMyHands()) {
            if (tile > 33) {
                tile = (tile - 34) * 9 + 4;
            }
            hands[mySeat][tile]++;
        }
        return hands;
    }

    protected void moveCheckHighlevel(GameAction ret, int[] hands, int[] remains, int lastTile, int myWinDistance,
                                      List<Group> showGroup, boolean canPong, boolean canGang) {
        if (canPong) {
            int[] handsCopy = hands.clone();
            handsCopy[lastTile] -= 2;
            List<SplitGroupsHelp> tmpResult;
            tmpResult = splitHandsNew(handsCopy);
            int wd = getMinWinDistance(tmpResult);

            //碰牌后能够听牌，碰
            if (wd == 1) {
                filter(tmpResult, wd);
                List<TingHelp> tingres = new ArrayList<>();
                Group tmp = new Group();
                List<Integer> card = new ArrayList<>();
                card.add(lastTile);
                card.add(lastTile);
                card.add(lastTile);
                tmp.setCards(card);
                showGroup.add(tmp);
                boolean isTing = checkTing(tmpResult, showGroup, handsCopy, remains, tingres);
                if (isTing) {
                    ret.setActionType(ACTION_PONG);
                    ret.setCurTile(lastTile);
                    return;
                }
                showGroup.remove(tmp);
            }

            if (wd > myWinDistance) {//胡牌距离增加，不能碰
                return;
            } else if (wd == myWinDistance) {//胡牌距离不变
                filter(tmpResult, wd);
                if (remains[lastTile] == 0) {//没杠的可能，不碰
                    return;
                } else {//有杠的可能
                    //计算进张数
                    //碰牌之后的最大进张数：
                    Map<Integer, Integer> lowProfit = findLowProfit(tmpResult, handsCopy, remains, wd);
                    int aftercount = 0;
                    for (Map.Entry<Integer, Integer> entry : lowProfit.entrySet()) {
                        if (entry.getValue() > aftercount) {
                            aftercount = entry.getValue();
                        }
                    }
                    //碰牌之前的最大进张数
                    int beforecount = calcTakeincount13cards(hands, remains);
                    if (lastTile >= 27 || lastTile % 9 >= 4) {//点数大于等于5
                        if (aftercount * 100 >= beforecount * 60) {//进张数不少于原来的60%
                            ret.setActionType(ACTION_PONG);
                            ret.setCurTile(lastTile);
                            return;
                        } else {
                            return;
                        }
                    } else {//点数小于5
                        if (aftercount * 100 >= beforecount * 70) {//进张数不少于原来的70%
                            ret.setActionType(ACTION_PONG);
                            ret.setCurTile(lastTile);
                            return;
                        } else {
                            return;
                        }
                    }
                }
            } else {//胡牌距离变小
                //计算进张数
                //碰牌之后的最大进张数：
                filter(tmpResult, wd);
                Map<Integer, Integer> lowProfit = findLowProfit(tmpResult, handsCopy, remains, wd);
                int aftercount = 0;
                for (Map.Entry<Integer, Integer> entry : lowProfit.entrySet()) {
                    if (entry.getValue() > aftercount) {
                        aftercount = entry.getValue();
                    }
                }
                //碰牌之前的最大进张数
                int beforecount = calcTakeincount13cards(hands, remains);

                if (remains[lastTile] == 0) {//没杠的可能，不碰
                    if (aftercount * 100 >= beforecount * 60) {//进张数不少于原来的60%
                        ret.setActionType(ACTION_PONG);
                        ret.setCurTile(lastTile);
                        return;
                    } else {
                        return;
                    }
                } else {//有杠的可能
                    if (lastTile >= 27 || lastTile % 9 >= 4) {//点数大于等于5
                        if (aftercount * 100 >= beforecount * 40) {//进张数不少于原来的60%
                            ret.setActionType(ACTION_PONG);
                            ret.setCurTile(lastTile);
                            return;
                        } else {
                            return;
                        }
                    } else {//点数小于5
                        if (aftercount * 100 >= beforecount * 50) {//进张数不少于原来的70%
                            ret.setActionType(ACTION_PONG);
                            ret.setCurTile(lastTile);
                            return;
                        } else {
                            return;
                        }
                    }
                }
            }
        }

        //这里判断能不能明杠
        if (canGang) {
            int[] handsCopy = hands.clone();
            handsCopy[lastTile] -= 3;
            List<SplitGroupsHelp> tmpResult;
            tmpResult = splitHandsNew(handsCopy);
            int wd = getMinWinDistance(tmpResult);
            if (wd > myWinDistance) {
                return;
            } else {
                ret.setActionType(ACTION_MINGGANG);
                ret.setCurTile(lastTile);
                return;
            }
        }
    }

    //13张牌，听哪些牌？
    public void findTingSet4Dianpao(Set<Integer> winTiles, List<SplitGroupsHelp> mgs) {
        winTiles.clear();
        for (SplitGroupsHelp sg : mgs) {
            if (sg.getWinDistance() == 1) {
                for (SplitGroup g : sg.getCurSplitGroup()) {
                    if (g.getType() == SplitGroupType.SPLIT_GROUP_TYPE_SINGLE) {
                        winTiles.add(g.getTile());
                        break;
                    } else if (g.getType() == SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG) {
                        if (g.getTile() < 27) {
                            if (g.getTile() % 9 != 0) {
                                winTiles.add(g.getTile() - 1);
                            }
                            if (g.getTile() % 9 != 7) {
                                winTiles.add(g.getTile() + 2);
                            }
                        }
                        break;
                    } else if (g.getType() == SplitGroupType.SPLIT_GROUP_TYPE_KANZHANG) {
                        if (g.getTile() < 27) {
                            winTiles.add(g.getTile() + 1);
                        }
                        break;
                    } else if (g.getType() == SplitGroupType.SPLIT_GROUP_TYPE_PAIR
                            && sg.getCurPairCount() == 2) {
                        winTiles.add(g.getTile());
                    }
                }
            }
        }

        winTiles.removeIf(tile -> tile < 27 && tile % 9 < 5);
    }

    public void findTingSet4Zimo(Set<Integer> winTiles, List<SplitGroupsHelp> mgs) {
        winTiles.clear();
        for (SplitGroupsHelp sg : mgs) {
            if (sg.getWinDistance() == 1) {
                for (SplitGroup g : sg.getCurSplitGroup()) {
                    if (g.getType() == SplitGroupType.SPLIT_GROUP_TYPE_SINGLE) {
                        winTiles.add(g.getTile());
                        break;
                    } else if (g.getType() == SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG) {
                        if (g.getTile() < 27) {

                            if (g.getTile() % 9 != 0) {
                                winTiles.add(g.getTile() - 1);
                            }
                            if (g.getTile() % 9 != 7) {
                                winTiles.add(g.getTile() + 2);
                            }
                        }
                        break;
                    } else if (g.getType() == SplitGroupType.SPLIT_GROUP_TYPE_KANZHANG) {
                        if (g.getTile() < 27) {
                            winTiles.add(g.getTile() + 1);
                        }
                        break;
                    } else if (g.getType() == SplitGroupType.SPLIT_GROUP_TYPE_PAIR
                            && sg.getCurPairCount() == 2) {
                        winTiles.add(g.getTile());
                    }
                }
            }
        }

        winTiles.removeIf(tile -> tile < 27 && tile % 9 < 2);
    }

    public int getMinWinDistance(List<SplitGroupsHelp> groups) {
        int distance = 8;
        for (SplitGroupsHelp group : groups) {
            if (distance > group.getWinDistance()) {
                distance = group.getWinDistance();
            }
        }
        return distance;
    }

    public void filter(List<SplitGroupsHelp> result, int winDistance) {
        Iterator<SplitGroupsHelp> iter = result.iterator();
        while (iter.hasNext()) {
            SplitGroupsHelp it = iter.next();
            if (it.getWinDistance() > winDistance) {
                iter.remove();
            }
        }
    }

    public enum SplitGroupType {
        SPLIT_GROUP_TYPE_MINGGANG("明杠"),
        SPLIT_GROUP_TYPE_ANGANG("暗杠"),
        SPLIT_GROUP_TYPE_BUGANG("补杠"),
        SPLIT_GROUP_TYPE_SHUN("顺"),
        SPLIT_GROUP_TYPE_KE("刻"),
        SPLIT_GROUP_TYPE_PAIR("对"),
        SPLIT_GROUP_TYPE_LIANZHANG("连张"),
        SPLIT_GROUP_TYPE_KANZHANG("坎张"),
        SPLIT_GROUP_TYPE_SINGLE("单牌"),
        SPLIT_GROUP_TYPE_EMPTY("空");
        private String name;

        private SplitGroupType(String str) {
            this.name = str;
        }
    }

    public static class SplitGroup implements Comparable<SplitGroup> {
        private SplitGroupType type;
        private int tile;

        public SplitGroup(SplitGroupType type, int tile) {
            this.type = type;
            this.tile = tile;
        }

        public SplitGroup(SplitGroup group) {
            this.type = group.getType();
            this.tile = group.getTile();
        }

        public SplitGroupType getType() {
            return type;
        }

        public void setType(SplitGroupType type) {
            this.type = type;
        }

        public int getTile() {
            return tile;
        }

        public void setTile(int tile) {
            this.tile = tile;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            SplitGroup that = (SplitGroup) o;

            if (tile != that.tile) return false;
            return type == that.type;
        }

        @Override
        public int hashCode() {
            int result = type != null ? type.hashCode() : 0;
            result = 31 * result + tile;
            return result;
        }

        @Override
        public String toString() {
            StringBuffer buf = new StringBuffer();
            switch (this.type) {
                case SPLIT_GROUP_TYPE_MINGGANG:
                case SPLIT_GROUP_TYPE_ANGANG:
                case SPLIT_GROUP_TYPE_BUGANG:
                    buf.append(TILE_STRING[this.tile]).append(TILE_STRING[this.tile])
                            .append(TILE_STRING[this.tile]).append(TILE_STRING[this.tile])
                            .append(",");
                    break;
                case SPLIT_GROUP_TYPE_SHUN:
                    buf.append(TILE_STRING[this.tile]).append(TILE_STRING[this.tile + 1])
                            .append(TILE_STRING[this.tile + 2]).append(",");
                    break;
                case SPLIT_GROUP_TYPE_KE:
                    buf.append(TILE_STRING[this.tile]).append(TILE_STRING[this.tile])
                            .append(TILE_STRING[this.tile]).append(",");
                    break;
                case SPLIT_GROUP_TYPE_PAIR:
                    buf.append(TILE_STRING[this.tile]).append(TILE_STRING[this.tile])
                            .append(",");
                    break;
                case SPLIT_GROUP_TYPE_LIANZHANG:
                    buf.append(TILE_STRING[this.tile]).append(TILE_STRING[this.tile + 1])
                            .append(",");
                    break;
                case SPLIT_GROUP_TYPE_KANZHANG:
                    buf.append(TILE_STRING[this.tile]).append(TILE_STRING[this.tile + 2])
                            .append(",");
                    break;
                case SPLIT_GROUP_TYPE_SINGLE:
                    buf.append(TILE_STRING[this.tile]).append(",");
                    break;
                case SPLIT_GROUP_TYPE_EMPTY:
                    break;
            }
            return buf.toString();
        }

        public SplitGroup clone() {
            return new SplitGroup(this.type, this.tile);
        }

        @Override
        public int compareTo(SplitGroup splitGroup) {
            int compare = this.type.compareTo(splitGroup.type);
            if (compare != 0) return compare;
            return Integer.compare(this.tile, splitGroup.tile);
        }

        public int getCount() {
            switch (getType()) {
                case SPLIT_GROUP_TYPE_MINGGANG:
                case SPLIT_GROUP_TYPE_ANGANG:
                case SPLIT_GROUP_TYPE_BUGANG:
                    return 4;
                case SPLIT_GROUP_TYPE_SHUN:
                case SPLIT_GROUP_TYPE_KE:
                    return 3;
                case SPLIT_GROUP_TYPE_PAIR:
                case SPLIT_GROUP_TYPE_LIANZHANG:
                case SPLIT_GROUP_TYPE_KANZHANG:
                    return 2;
                case SPLIT_GROUP_TYPE_SINGLE:
                    return 1;
                case SPLIT_GROUP_TYPE_EMPTY:
                    break;
            }
            return 0;
        }
    }

    public class TingHelp {
        public int discardtile;//扔哪张手牌
        public int[] tingtile;//听哪些手牌
        public int[] hutype;//胡的类型

        public TingHelp() {
            tingtile = new int[TILE_TYPE_COUNT];
            hutype = new int[TILE_TYPE_COUNT];
            clear();
        }

        private void clear() {
            for (int i = 0; i < TILE_TYPE_COUNT; i++) {
                tingtile[i] = 0;
                hutype[i] = 0;
            }
        }
    }

    public class SplitGroupsHelp {
        private int initLen;        //初始手牌长度
        private int[] leftTilesNum; //未拆分手牌
        private int leftTileCount;  //未拆分手牌剩余张树
        private List<SplitGroup> curSplitGroup; //已拆分出的牌组
        private int curSingleCount; //单数量
        private int curPairCount; //对数量
        private int curConnectCount; //搭数量
        private int winDistance; //该拆分的和牌距离
        private boolean isSevenPair; //是否是七对拆分
        private double tileValues[]; //分值

        public SplitGroupsHelp() {
            leftTilesNum = new int[TILE_TYPE_COUNT];
            tileValues = new double[TILE_TYPE_COUNT];
            curSplitGroup = new ArrayList<SplitGroup>();
            clear();
        }

        public void clear() {
            for (int i = 0; i < TILE_TYPE_COUNT; i++) {
                leftTilesNum[i] = 0;
                tileValues[i] = 0;
            }
            curSplitGroup.clear();
            this.leftTileCount = 0;
            this.curSingleCount = 0;
            this.curPairCount = 0;
            this.curConnectCount = 0;
            this.winDistance = 7;
            this.initLen = 0;
            this.isSevenPair = false;
        }


        public int[] getLeftTilesNum() {
            return leftTilesNum;
        }

        public void setLeftTilesNum(int[] leftTilesNum) {
            this.leftTilesNum = leftTilesNum;
        }

        public void addLeftTilesNum(int index, int num) {
            this.leftTilesNum[index] += num;
            this.leftTileCount += num;
        }

        public void reduceLeftTilesNum(int index, int num) {
            this.leftTilesNum[index] -= num;
            this.leftTileCount -= num;
        }

        public int getLeftTileCount() {
            return leftTileCount;
        }

        public void setLeftTileCount(int leftTileCount) {
            this.leftTileCount = leftTileCount;
        }

        public List<SplitGroup> getCurSplitGroup() {
            return curSplitGroup;
        }

        public void setCurSplitGroup(List<SplitGroup> curSplitGroup) {
            this.curSplitGroup = curSplitGroup;
        }

        public void addSplitGroup(SplitGroup group) {
            if (group != null) {
                switch (group.getType()) {
                    case SPLIT_GROUP_TYPE_PAIR:
                        this.curPairCount += 1;
                        break;
                    case SPLIT_GROUP_TYPE_LIANZHANG:
                    case SPLIT_GROUP_TYPE_KANZHANG:
                        this.curConnectCount += 1;
                        break;
                    case SPLIT_GROUP_TYPE_SINGLE:
                        this.curSingleCount += 1;
                        break;
                }
                this.curSplitGroup.add(group);
            }
        }

        public boolean removeSplit(SplitGroup group) {
            if (group != null) {
                switch (group.getType()) {
                    case SPLIT_GROUP_TYPE_PAIR:
                        this.curPairCount -= 1;
                        break;
                    case SPLIT_GROUP_TYPE_LIANZHANG:
                    case SPLIT_GROUP_TYPE_KANZHANG:
                        this.curConnectCount -= 1;
                        break;
                    case SPLIT_GROUP_TYPE_SINGLE:
                        this.curSingleCount -= 1;
                        break;
                }
                return this.curSplitGroup.remove(group);
            }
            return false;
        }

        public int getCurSingleCount() {
            return curSingleCount;
        }

        public void setCurSingleCount(int curSingleCount) {
            this.curSingleCount = curSingleCount;
        }

        public int getCurPairCount() {
            return curPairCount;
        }

        public void setCurPairCount(int curPairCount) {
            this.curPairCount = curPairCount;
        }

        public int getCurConnectCount() {
            return curConnectCount;
        }

        public void setCurConnectCount(int curConnectCount) {
            this.curConnectCount = curConnectCount;
        }

        public int getWinDistance() {
            return winDistance;
        }

        public void setWinDistance(int winDistance) {
            this.winDistance = winDistance;
        }

        public boolean isSevenPair() {
            return isSevenPair;
        }

        public void setSevenPair(boolean sevenPair) {
            isSevenPair = sevenPair;
        }

        public double[] getTileValues() {
            return tileValues;
        }

        public void setTileValues(double[] tileValues) {
            this.tileValues = tileValues;
        }

        public int getInitLen() {
            return initLen;
        }

        public void setInitLen(int initLen) {
            this.initLen = initLen;
        }

        public SplitGroupsHelp clone() {
            SplitGroupsHelp group = new SplitGroupsHelp();
            group.leftTileCount = this.leftTileCount;
            group.leftTilesNum = this.leftTilesNum.clone();
            for (SplitGroup sg : this.curSplitGroup) {
                group.addSplitGroup(sg.clone());
            }
            group.winDistance = this.winDistance;
            group.isSevenPair = this.isSevenPair;
            group.tileValues = this.tileValues.clone();
            group.initLen = this.initLen;
            return group;
        }

        @Override
        public String toString() {
            StringBuffer buf = new StringBuffer();
            if (leftTileCount > 0) {
                for (int i = 0; i < 16; i++) {
                    for (int j = 0; j < leftTilesNum[i]; j++) {
                        buf.append(TILE_STRING[i]);
                    }
                }
            }
            buf.append(" WinDistance=").append(this.winDistance).append(" ");
            for (SplitGroup sg : this.curSplitGroup) {
                buf.append(sg.toString());
            }
            buf.append(" value=(");
            for (int i = 0; i < 34; i++) {
                buf.append(this.tileValues[i]).append(",");
            }
            //buf.append(" Num=").append(leftTilesNum);
            return buf.toString();
        }
    }

    /**
     * @param hands
     */
    public List<SplitGroupsHelp> splitHandsNew(int[] hands) {
        List<SplitGroupsHelp> result = new LinkedList<>();
        int len = 0;
        for (int i = 0; i < TILE_TYPE_COUNT; i++) {
            len = len + hands[i];
        }


        List<SplitGroup> splitGroups = new LinkedList<>();
        splitNew(hands, splitGroups, result);
        Iterator<SplitGroupsHelp> iterator = result.iterator();
        // 删除 不正常的组牌方式， 如对子拆成两个单，4刻拆成两对
        while (iterator.hasNext()) {
            SplitGroupsHelp gruops = iterator.next();
            Set<SplitGroup> singleAndPair = new HashSet<>();
            boolean hasSame = false;
            List<SplitGroup> curSplitGroup = gruops.getCurSplitGroup();
            for (SplitGroup s : curSplitGroup) {
                if (s.getType() == SplitGroupType.SPLIT_GROUP_TYPE_SINGLE || (s.getType() == SplitGroupType.SPLIT_GROUP_TYPE_PAIR && !gruops.isSevenPair())) {
                    if (singleAndPair.contains(s)) {
                        hasSame = true;
                        break;
                    } else {
                        singleAndPair.add(s);
                    }
                }
            }
            if (hasSame) iterator.remove();
            else {
                // 删除 本可以组成 连张 坎张 对子， 却组成各单牌的奇葩组合
                boolean strangeGroups = false;
                for (int i = 0; i < curSplitGroup.size(); i++) {
                    if (curSplitGroup.get(i).getType() == SplitGroupType.SPLIT_GROUP_TYPE_SINGLE) {
                        int tile = curSplitGroup.get(i).getTile();
                        int color = AIUtils.color(tile);
                        for (int j = 0; j < curSplitGroup.size(); j++) {
                            if (i != j) {
                                int tempTile = curSplitGroup.get(j).getTile();
                                if (AIUtils.color(tempTile) == color) {
                                    switch (curSplitGroup.get(j).getType()) {
                                        case SPLIT_GROUP_TYPE_KE:
                                        case SPLIT_GROUP_TYPE_PAIR:
                                            if (tile == tempTile) strangeGroups = true;
                                            break;
                                        case SPLIT_GROUP_TYPE_LIANZHANG:
                                            if (tempTile % 9 < 7) {
                                                if (tile == tempTile + 2) {
                                                    strangeGroups = true;
                                                }
                                            }
                                            if (tempTile % 9 > 0) {
                                                if (tile == tempTile - 1) {
                                                    strangeGroups = true;
                                                }
                                            }
                                            break;
                                        case SPLIT_GROUP_TYPE_KANZHANG:
                                            if (tile == tempTile + 1) strangeGroups = true;
                                            break;
                                        case SPLIT_GROUP_TYPE_SINGLE:
                                            if (tempTile < 27 && tile < 27 && Math.abs(tempTile - tile) <= 2 && AIUtils.color(tempTile) == AIUtils.color(tile))
                                                strangeGroups = true;
                                            break;
                                    }
                                    if (strangeGroups) break;
                                }
                            }
                        }
                        if (strangeGroups) {
                            iterator.remove();
                            break;
                        }
                    }
                }
            }
        }
        if (len >= 13) {
            SplitGroupsHelp sp = findSevenPair(hands);
            if (sp.getCurPairCount() >= 4) {
                result.add(sp);
            }
        }
        return result;
    }


    /**
     * 查找七对拆分
     *
     * @param hands
     * @return
     */
    protected SplitGroupsHelp findSevenPair(int[] hands) {
        SplitGroupsHelp sevenPairGroup = new SplitGroupsHelp();
        sevenPairGroup.setSevenPair(true);
        for (int i = 0; i < TILE_TYPE_COUNT; i++) {
            if (hands[i] == 4) {
                sevenPairGroup.addSplitGroup(new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_PAIR, i));
                sevenPairGroup.addSplitGroup(new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_PAIR, i));
            } else if (hands[i] == 3) {
                sevenPairGroup.addSplitGroup(new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_PAIR, i));
                sevenPairGroup.addSplitGroup(new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_SINGLE, i));
            } else if (hands[i] == 2) {
                sevenPairGroup.addSplitGroup(new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_PAIR, i));
            } else if (hands[i] == 1) {
                sevenPairGroup.addSplitGroup(new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_SINGLE, i));
            }
        }
        sevenPairGroup.setWinDistance(7 - sevenPairGroup.getCurPairCount());
        return sevenPairGroup;
    }

    public Set<SplitGroup> getAllGroups(int[] hands) {
        Set<SplitGroup> splitGroupSet = new TreeSet<>(SplitGroup::compareTo);
        for (int i = 0; i < TILE_TYPE_COUNT; i++) {
            if (hands[i] > 0) {
                if (hands[i] == 4) {// 杠
                    splitGroupSet.add(new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_ANGANG, i));
                }
                if (hands[i] == 3) {// 刻
                    splitGroupSet.add(new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_KE, i));
                }
                if (hands[i] >= 2) {//对
                    splitGroupSet.add(new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_PAIR, i));
                }

                if (i >= 27) {
                    continue;
                }

                if (i % 9 <= 6 && hands[i + 1] > 0 && hands[i + 2] > 0) {//顺
                    splitGroupSet.add(new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_SHUN, i));
                }
                if (i % 9 <= 7 && hands[i + 1] > 0) {//连张
                    splitGroupSet.add(new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG, i));
                }

                if (i % 9 <= 6 && hands[i + 2] > 0) {//隔张
                    splitGroupSet.add(new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_KANZHANG, i));
                }

            }
        }
        return splitGroupSet;
    }

    public SplitGroupsHelp fillOtherGroups(int[] hands, List<SplitGroup> curGroups) {
        for (int i = 0; i < TILE_TYPE_COUNT; i++) {
            switch (hands[i]) {
                case 1:
                    curGroups.add(new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_SINGLE, i));
                    break;
                case 2:
                    curGroups.add(new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_PAIR, i));
                    break;
                case 3:
                case 4:
                    curGroups.add(new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_KE, i));
                    break;
            }
        }
        SplitGroupsHelp initGroup = new SplitGroupsHelp();
        int count = 0;
        for (SplitGroup curGroup : curGroups) {
            initGroup.addSplitGroup(curGroup);
            count += curGroup.getCount();
        }
        initGroup.setInitLen(count);
        return countWindDistance(initGroup);
    }

    public void add(int[] hands, SplitGroup group) {
        switch (group.getType()) {
            case SPLIT_GROUP_TYPE_ANGANG:
                hands[group.tile] += 4;
                break;
            case SPLIT_GROUP_TYPE_KE:
                hands[group.tile] += 3;
                break;
            case SPLIT_GROUP_TYPE_PAIR:
                hands[group.tile] += 2;
                break;
            case SPLIT_GROUP_TYPE_SINGLE:
                hands[group.tile] += 1;
                break;
            case SPLIT_GROUP_TYPE_SHUN:
                hands[group.tile] += 1;
                hands[group.tile + 1] += 1;
                hands[group.tile + 2] += 1;
                break;
            case SPLIT_GROUP_TYPE_LIANZHANG:
                hands[group.tile] += 1;
                hands[group.tile + 1] += 1;
                break;
            case SPLIT_GROUP_TYPE_KANZHANG:
                hands[group.tile] += 1;
                hands[group.tile + 2] += 1;
                break;
        }
    }

    public void remove(int[] hands, SplitGroup group) {
        switch (group.getType()) {
            case SPLIT_GROUP_TYPE_ANGANG:
                hands[group.tile] -= 4;
                break;
            case SPLIT_GROUP_TYPE_KE:
                hands[group.tile] -= 3;
                break;
            case SPLIT_GROUP_TYPE_PAIR:
                hands[group.tile] -= 2;
                break;
            case SPLIT_GROUP_TYPE_SINGLE:
                hands[group.tile] -= 1;
                break;
            case SPLIT_GROUP_TYPE_SHUN:
                hands[group.tile] -= 1;
                hands[group.tile + 1] -= 1;
                hands[group.tile + 2] -= 1;
                break;
            case SPLIT_GROUP_TYPE_LIANZHANG:
                hands[group.tile] -= 1;
                hands[group.tile + 1] -= 1;
                break;
            case SPLIT_GROUP_TYPE_KANZHANG:
                hands[group.tile] -= 1;
                hands[group.tile + 2] -= 1;
                break;
        }
    }

    /**
     * 3N+2 格式拆分
     *
     * @param curSplitGroup
     * @param result
     */
    protected void splitNew(int[] hands, List<SplitGroup> curSplitGroup, List<SplitGroupsHelp> result) {
        Set<SplitGroup> allGroups = getAllGroups(hands);
        if (allGroups.isEmpty()) {
            result.add(fillOtherGroups(hands, curSplitGroup));
            return;
        }
        for (SplitGroup group : allGroups) {
            if (curSplitGroup.isEmpty() || group.compareTo(curSplitGroup.get(curSplitGroup.size() - 1)) >= 0) {
                remove(hands, group);
                LinkedList<SplitGroup> splitGroups = new LinkedList<>(curSplitGroup);
                splitGroups.add(group);
                splitNew(hands, splitGroups, result);
                add(hands, group);
            }
        }
    }

    /**
     * 修改为 3N+2 形式的计算上听距离 0=胡牌 1=听牌
     *
     * @param group
     * @return
     */
    protected SplitGroupsHelp countWindDistance(SplitGroupsHelp group) {
//        int distance = (group.getCurSingleCount() + group.getCurConnectCount()
//                + group.getCurPairCount()) / 3;
//        if (group.getCurPairCount() == 0) {
//            group.setWinDistance(group.getCurConnectCount() >= distance ? distance + 1 : 2 * distance - group.getCurConnectCount() + 1);
//        } else {
//            int tmp = group.getCurConnectCount() + group.getCurPairCount() - 1;
//            group.setWinDistance(tmp > distance ? distance : 2 * distance - tmp);
//        }
//        int winDistance = (group.getInitLen() - 1) / 3 + 1;
//        for (SplitGruop s : group.getCurSplitGroup()) {
//            if (s.getType() == SplitGroupType.SPLIT_GROUP_TYPE_SHUN
//                    || s.getType() == SplitGroupType.SPLIT_GROUP_TYPE_KE
//                    || s.getType() == SplitGroupType.SPLIT_GROUP_TYPE_MINGGANG
//                    || s.getType() == SplitGroupType.SPLIT_GROUP_TYPE_BUGANG
//                    || s.getType() == SplitGroupType.SPLIT_GROUP_TYPE_ANGANG) {
//                winDistance--;
//            }
//        }
//        int pair = 0;
//        if (group.getCurPairCount() > 0) {
//            winDistance--;
//            pair = group.getCurPairCount() - 1;
//        }
//
//        int dazi = (pair) + (group.getCurConnectCount());
//        if (dazi < winDistance) {
//            winDistance = winDistance + (winDistance - dazi);
//        }
        group.setWinDistance(calWinDis(group));

        return group;
    }

    protected int calWinDis(SplitGroupsHelp group) {
        int pairNum = group.getCurPairCount(), conNum = group.curConnectCount, tileNum = group.getInitLen();
        int dis = tileNum / 3;
        for (SplitGroup s : group.getCurSplitGroup()) {
            if (s.getType() == SplitGroupType.SPLIT_GROUP_TYPE_SHUN
                    || s.getType() == SplitGroupType.SPLIT_GROUP_TYPE_KE
                    || s.getType() == SplitGroupType.SPLIT_GROUP_TYPE_MINGGANG
                    || s.getType() == SplitGroupType.SPLIT_GROUP_TYPE_BUGANG
                    || s.getType() == SplitGroupType.SPLIT_GROUP_TYPE_ANGANG) {
                dis--;
            }
        }
        if (pairNum == 0) {
            if (dis > conNum) {
                dis += (dis - conNum);
            }
            dis += 1;
        } else {
            if (dis > conNum + pairNum - 1) {
                dis += (dis - (conNum + pairNum - 1));
            }
        }
        return dis;
    }

    public int getHuTypeKoudian(int[] hands, List<Group> showGroup) {
        if (checkShiSanYao(hands)) return AIConstant.SHISANYAO;
        if (checkQingHaoHuaQiDui(hands, showGroup)) return AIConstant.QINGHAOHUAQIDUI;
        if (checkQingQiDui(hands, showGroup)) return AIConstant.QINGQIDUI;
        if (checkQingYiTiaoLong(hands, showGroup)) return AIConstant.QINGYITIAOLONG;
        if (checkHaoHuaQiDui(hands, showGroup)) return AIConstant.HAOHUAQIDUI;
        if (checkQiDui(hands, showGroup)) return AIConstant.QIDUI;
        if (checkYiTiaoLong(hands)) return AIConstant.YITIAOLONG;
        if (checkQingYiSe(hands, showGroup)) return AIConstant.QINGYISE;
        if (checkWinNoJang(hands, showGroup)) return AIConstant.PINGHU;
        return 0;
    }

    public int getHuTypeKoudian(int[] hands, List<Group> showGroup, GameAction ret) {
        List<String> fanInfo = new ArrayList<>();
        ret.setFanInfo(fanInfo);
        if (checkShiSanYao(hands)) {
            fanInfo.add(FAN_NAME_ARRAY[AIConstant.SHISANYAO]);
            return AIConstant.SHISANYAO;
        }
        if (checkQingHaoHuaQiDui(hands, showGroup)) {
            fanInfo.add(FAN_NAME_ARRAY[AIConstant.QINGHAOHUAQIDUI]);
            return AIConstant.QINGHAOHUAQIDUI;
        }
        if (checkQingQiDui(hands, showGroup)) {
            fanInfo.add(FAN_NAME_ARRAY[AIConstant.QINGQIDUI]);
            return AIConstant.QINGQIDUI;
        }
        if (checkQingYiTiaoLong(hands, showGroup)) {
            fanInfo.add(FAN_NAME_ARRAY[AIConstant.QINGYITIAOLONG]);
            return AIConstant.QINGYITIAOLONG;
        }
        if (checkHaoHuaQiDui(hands, showGroup)) {
            fanInfo.add(FAN_NAME_ARRAY[AIConstant.HAOHUAQIDUI]);
            return AIConstant.HAOHUAQIDUI;
        }
        if (checkQiDui(hands, showGroup)) {
            fanInfo.add(FAN_NAME_ARRAY[AIConstant.QIDUI]);
            return AIConstant.QIDUI;
        }
        if (checkYiTiaoLong(hands)) {
            fanInfo.add(FAN_NAME_ARRAY[AIConstant.YITIAOLONG]);
            return AIConstant.YITIAOLONG;
        }
        if (checkQingYiSe(hands, showGroup)) {
            fanInfo.add(FAN_NAME_ARRAY[AIConstant.QINGYISE]);
            return AIConstant.QINGYISE;
        }
        if (checkWinNoJang(hands, showGroup)) {
            fanInfo.add(FAN_NAME_ARRAY[AIConstant.PINGHU]);
            return AIConstant.PINGHU;
        }
        fanInfo.add(FAN_NAME_ARRAY[0]);
        return 0;
    }

    public int getBestTing(int[] remains, List<TingHelp> res) {
        List<Integer> remainnum = new ArrayList<>();
        List<Integer> bestHu = new ArrayList<>();
        List<Integer> highpoint = new ArrayList<>();
        for (int i = 0; i < res.size(); i++) {
            TingHelp tinghelp = res.get(i);
            int num = 0;
            int hutype = 1;
            int point = 0;
            for (int j = 0; j < TILE_TYPE_COUNT; j++) {
                if (tinghelp.tingtile[j] > 0) {
                    num += remains[j];
                    if (tinghelp.hutype[j] > hutype) {
                        hutype = tinghelp.hutype[j];
                    }
                    if (j >= 27) {
                        if (j > point) point = 10;
                    } else {
                        if (j % 9 >= point) point = j % 9 + 1;
                    }
                }
            }
            remainnum.add(num);
            bestHu.add(hutype);
            highpoint.add(point);
        }

        //找胡牌进张数量多的
        int discardtile = res.get(0).discardtile;
        boolean bfind = false;
        for (int i = 1; i < res.size(); i++) {
            if (remainnum.get(i) > remainnum.get(i - 1)) {
                discardtile = res.get(i).discardtile;
                bfind = true;
            }

            if (remainnum.get(i) < remainnum.get(i - 1)) bfind = true;
        }

        //胡牌进张数量一样多，找胡牌牌型大的
        if (!bfind) {
            bfind = false;
            for (int i = 1; i < res.size(); i++) {
                if (bestHu.get(i) > bestHu.get(i - 1)) {
                    discardtile = res.get(i).discardtile;
                    bfind = true;
                }
            }
        }

        //找胡牌点数最高的
        if (!bfind) {
            bfind = false;
            for (int i = 1; i < res.size(); i++) {
                if (highpoint.get(i) > highpoint.get(i - 1)) {
                    discardtile = res.get(i).discardtile;
                    bfind = true;
                }
            }
        }
        return discardtile;
    }

    //13张牌checkting,返回听哪些牌
    public boolean checkTing13cards(List<SplitGroupsHelp> myResult, int[] remains, Set<Integer> res) {
        for (SplitGroupsHelp splitGroupsHelp : myResult) {
            if (splitGroupsHelp.getWinDistance() == 0) {
                continue;
            }

            List<SplitGroup> pairgroup = new ArrayList<>();
            List<SplitGroup> singlegroup = new ArrayList<>();
            List<SplitGroup> dazigroup = new ArrayList<>();
            //找出可以做将的
            for (SplitGroup splitGroup : splitGroupsHelp.getCurSplitGroup()) {
                if (splitGroup.getType() == SplitGroupType.SPLIT_GROUP_TYPE_SINGLE) {
                    singlegroup.add(splitGroup);
                }

                if (splitGroup.getType() == SplitGroupType.SPLIT_GROUP_TYPE_PAIR) {
                    pairgroup.add(splitGroup);
                }

                if (splitGroup.getType() == SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG ||
                        splitGroup.getType() == SplitGroupType.SPLIT_GROUP_TYPE_KANZHANG) {
                    dazigroup.add(splitGroup);
                }
            }

            //没有将，单吊将
            if (pairgroup.size() == 0 || pairgroup.size() == 6) {
                if (singlegroup.size() == 1) {
                    if (singlegroup.get(0).getTile() % 9 >= 5 || singlegroup.get(0).getTile() >= 27) {
                        if (remains[singlegroup.get(0).getTile()] > 0) {
                            res.add(singlegroup.get(0).getTile());
                        }
                    }
                }
            } else if (pairgroup.size() == 1) {//一对将
                int[] tmp = getNeededTile(dazigroup.get(0));

                //不能听比6小的,小于5的听牌都置为0
                for (int i = 0; i < TILE_TYPE_COUNT; i++) {
                    if (tmp[i] > 0) {
                        if (i >= 27 || i % 9 >= 5) {
                            if (remains[i] > 0) {
                                res.add(i);
                            }
                        }
                    }
                }
            } else if (splitGroupsHelp.getCurPairCount() == 2) {
                if (pairgroup.get(0).getTile() % 9 >= 5 || pairgroup.get(0).getTile() >= 27) {
                    if (remains[pairgroup.get(0).getTile()] > 0) {
                        res.add(pairgroup.get(0).getTile());
                    }
                }
                if (pairgroup.get(1).getTile() % 9 >= 5 || pairgroup.get(1).getTile() >= 27) {
                    if (remains[pairgroup.get(1).getTile()] > 0) {
                        res.add(pairgroup.get(1).getTile());
                    }
                }
            }
        }

        if (res.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    //判断14张牌是不是能打出一张听牌。如果能听牌，打出哪张牌，听哪些牌，胡什么番都放在res中返回。
    public boolean checkTing(List<SplitGroupsHelp> myResult, List<Group> showGroup, int[] hand, int[] remains, List<TingHelp> res) {
        for (SplitGroupsHelp splitGroupsHelp : myResult) {
            if (splitGroupsHelp.getWinDistance() == 0) {
                continue;
            }

            List<SplitGroup> pairgroup = new ArrayList<>();
            List<SplitGroup> singlegroup = new ArrayList<>();
            List<SplitGroup> dazigroup = new ArrayList<>();
            //找出可以做将的
            for (SplitGroup splitGroup : splitGroupsHelp.getCurSplitGroup()) {
                if (splitGroup.getType() == SplitGroupType.SPLIT_GROUP_TYPE_SINGLE) {
                    singlegroup.add(splitGroup);
                }

                if (splitGroup.getType() == SplitGroupType.SPLIT_GROUP_TYPE_PAIR) {
                    pairgroup.add(splitGroup);
                }

                if (splitGroup.getType() == SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG ||
                        splitGroup.getType() == SplitGroupType.SPLIT_GROUP_TYPE_KANZHANG) {
                    dazigroup.add(splitGroup);
                }
            }

            //这里有种情况，就是没有将，剩下的不是两张单，而是一个搭子，处理一下
            if (pairgroup.size() == 0 && singlegroup.size() == 0 && dazigroup.size() == 1) {
                if (dazigroup.get(0).type == SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG) {
                    singlegroup.add(new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_SINGLE, dazigroup.get(0).getTile()));
                    singlegroup.add(new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_SINGLE, dazigroup.get(0).getTile() + 1));
                    dazigroup.remove(0);
                } else if (dazigroup.get(0).type == SplitGroupType.SPLIT_GROUP_TYPE_KANZHANG) {
                    singlegroup.add(new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_SINGLE, dazigroup.get(0).getTile()));
                    singlegroup.add(new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_SINGLE, dazigroup.get(0).getTile() + 2));
                    dazigroup.remove(0);
                }
            }

            //没有将，只可能两张单张,或者搭子
            if (pairgroup.size() == 0 || pairgroup.size() == 6) {
                if (singlegroup.size() == 2) {
                    if ((singlegroup.get(1).getTile() % 9 >= 5 || singlegroup.get(1).getTile() >= 27) &&
                            remains[singlegroup.get(1).getTile()] > 0) {//听6点以上
                        TingHelp tinghelp1 = new TingHelp();
                        tinghelp1.discardtile = singlegroup.get(0).getTile();
                        tinghelp1.tingtile[singlegroup.get(1).getTile()]++;
                        hand[singlegroup.get(0).getTile()]--;
                        hand[singlegroup.get(1).getTile()]++;
                        tinghelp1.hutype[singlegroup.get(1).getTile()] = getHuTypeKoudian(hand, showGroup);
                        hand[singlegroup.get(0).getTile()]++;
                        hand[singlegroup.get(1).getTile()]--;
                        res.add(tinghelp1);
                    }

                    if ((singlegroup.get(0).getTile() % 9 >= 5 || singlegroup.get(0).getTile() >= 27) &&
                            remains[singlegroup.get(0).getTile()] > 0) {
                        TingHelp tinghelp2 = new TingHelp();
                        tinghelp2.discardtile = singlegroup.get(1).getTile();
                        tinghelp2.tingtile[singlegroup.get(0).getTile()]++;
                        hand[singlegroup.get(1).getTile()]--;
                        hand[singlegroup.get(0).getTile()]++;
                        tinghelp2.hutype[singlegroup.get(0).getTile()] = getHuTypeKoudian(hand, showGroup);
                        hand[singlegroup.get(1).getTile()]++;
                        hand[singlegroup.get(0).getTile()]--;
                        res.add(tinghelp2);
                    }
                }
            } else if (pairgroup.size() == 1) {//一对将
                TingHelp tinghelp = new TingHelp();
                //这种情况是进来的那张牌放到暗杠里面去了，不在single里。
                if (singlegroup.size() != 0) {
                    tinghelp.discardtile = singlegroup.get(0).getTile();
                } else {
                    tinghelp.discardtile = -1;
                }
                tinghelp.tingtile = getNeededTile(dazigroup.get(0));

                //不能听比6小的,小于5的听牌都置为0
                for (int i = 0; i < TILE_TYPE_COUNT; i++) {
                    if (i < 27) {
                        if (tinghelp.tingtile[i] > 0 && i % 9 <= 4) tinghelp.tingtile[i] = 0;
                    }
                }

                //不能听绝张
                for (int i = 0; i < TILE_TYPE_COUNT; i++) {
                    if (tinghelp.tingtile[i] > 0 && remains[i] == 0) {
                        tinghelp.tingtile[i] = 0;
                    }
                }

                if (singlegroup.size() != 0) {
                    hand[singlegroup.get(0).getTile()]--;
                    for (int i = 0; i < TILE_TYPE_COUNT; i++) {
                        if (tinghelp.tingtile[i] > 0) {
                            hand[i]++;
                            tinghelp.hutype[i] = getHuTypeKoudian(hand, showGroup);
                            hand[i]--;
                            res.add(tinghelp);
                        }
                    }
                    hand[singlegroup.get(0).getTile()]++;
                }
            } else if (splitGroupsHelp.getCurPairCount() == 2) {
                //两个对子，胡对倒
                TingHelp tinghelp = new TingHelp();
                //这种情况是进来的那张牌放到暗杠里面去了，不在single里。
                if (singlegroup.size() != 0) {
                    tinghelp.discardtile = singlegroup.get(0).getTile();
                } else {
                    tinghelp.discardtile = -1;
                }
                tinghelp.tingtile[pairgroup.get(0).getTile()]++;
                tinghelp.tingtile[pairgroup.get(1).getTile()]++;

                //不能听比6小的,小于5的听牌都置为0
                for (int i = 0; i < TILE_TYPE_COUNT; i++) {
                    if (i < 27) {
                        if (tinghelp.tingtile[i] > 0 && i % 9 <= 4) tinghelp.tingtile[i] = 0;
                    }
                }

                //不能听绝张
                for (int i = 0; i < TILE_TYPE_COUNT; i++) {
                    if (tinghelp.tingtile[i] > 0 && remains[i] == 0) {
                        tinghelp.tingtile[i] = 0;
                    }
                }

                if (singlegroup.size() != 0) {
                    hand[singlegroup.get(0).getTile()]--;
                    for (int i = 0; i < TILE_TYPE_COUNT; i++) {
                        if (tinghelp.tingtile[i] > 0) {
                            hand[i]++;
                            tinghelp.hutype[i] = getHuTypeKoudian(hand, showGroup);
                            hand[i]--;
                            res.add(tinghelp);
                        }
                    }
                    hand[singlegroup.get(0).getTile()]++;
                }
            }
        }

        //整理合并res，听牌加在一起
        if (res.size() > 0) {
            for (int i = 1; i < res.size(); i++) {
                for (int j = i; j < res.size(); j++) {
                    if (res.get(j).discardtile == res.get(i - 1).discardtile) {
                        for (int k = 0; k < TILE_TYPE_COUNT; k++) {
                            res.get(i - 1).tingtile[k] = (res.get(i - 1).tingtile[k] + res.get(j).tingtile[k]) > 0 ? 1 : 0;
                            res.get(i - 1).hutype[k] = res.get(j).hutype[k] > res.get(i - 1).hutype[k] ? res.get(j).hutype[k] : res.get(i - 1).hutype[k];
                        }
                        res.remove(j);
                    }
                }
            }
            return true;
        }
        return false;
    }

    //返回边张，坎张组成成牌需要的tile
    public int[] getNeededTile(SplitGroup groupin) {
        int[] res = new int[TILE_TYPE_COUNT];
        if (groupin.getType() == SplitGroupType.SPLIT_GROUP_TYPE_KANZHANG) {
            res[groupin.getTile() + 1]++;
        }

        if (groupin.getType() == SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG) {
            if (groupin.getTile() % 9 == 0) {
                res[groupin.getTile() + 2]++;
            } else if (groupin.getTile() % 9 == 7) {
                res[groupin.getTile() - 1]++;
            } else {
                res[groupin.getTile() - 1]++;
                res[groupin.getTile() + 2]++;
            }
        }

        return res;
    }

    public boolean checkQingYiTiaoLong(int[] hands, List<Group> showGroup) {
        if (checkYiTiaoLong(hands) && checkQingYiSe(hands, showGroup))
            return true;
        return false;
    }

    public boolean checkYiTiaoLong(int[] hands) {
        int i = 0;
        if (hands[i] >= 1 && hands[i + 1] >= 1 && hands[i + 2] >= 1 && hands[i + 3] >= 1 && hands[i + 4] >= 1 &&
                hands[i + 5] >= 1 && hands[i + 6] >= 1 && hands[i + 7] >= 1 && hands[i + 8] >= 1)
            return true;

        i = 9;
        if (hands[i] >= 1 && hands[i + 1] >= 1 && hands[i + 2] >= 1 && hands[i + 3] >= 1 && hands[i + 4] >= 1 &&
                hands[i + 5] >= 1 && hands[i + 6] >= 1 && hands[i + 7] >= 1 && hands[i + 8] >= 1)
            return true;

        i = 18;
        if (hands[i] >= 1 && hands[i + 1] >= 1 && hands[i + 2] >= 1 && hands[i + 3] >= 1 && hands[i + 4] >= 1 &&
                hands[i + 5] >= 1 && hands[i + 6] >= 1 && hands[i + 7] >= 1 && hands[i + 8] >= 1)
            return true;

        return false;
    }

    public boolean checkShiSanYao(int[] hands) {
        if (hands[0] >= 1 && hands[8] >= 1 && hands[9] >= 1 && hands[17] >= 1 && hands[18] >= 1 && hands[26] >= 1 &&
                hands[27] >= 1 && hands[28] >= 1 && hands[29] >= 1 && hands[30] >= 1 && hands[31] >= 1 && hands[32] >= 1 && hands[33] >= 1)
            return true;
        return false;
    }

    public boolean checkQingHaoHuaQiDui(int[] hands, List<Group> showGroup) {
        if (!showGroup.isEmpty()) return false;
        int gang = 0;
        int dui = 0;
        for (int i = 0; i < 9; i++) {
            if (hands[i] == 4) {
                if (gang > 0) {
                    dui += 2;
                } else {
                    gang++;
                }
            } else if (hands[i] >= 2) {
                dui++;
            }
        }
        if (gang > 0 && dui >= 5) {
            return true;
        }
        gang = 0;
        dui = 0;
        for (int i = 9; i < 18; i++) {
            if (hands[i] == 4) {
                if (gang > 0) {
                    dui += 2;
                } else {
                    gang++;
                }
            } else if (hands[i] >= 2) {
                dui++;
            }
        }

        if (gang > 0 && dui >= 5) {
            return true;
        }

        gang = 0;
        dui = 0;
        for (int i = 18; i < 27; i++) {
            if (hands[i] == 4) {
                if (gang > 0) {
                    dui += 2;
                } else {
                    gang++;
                }
            } else if (hands[i] >= 2) {
                dui++;
            }
        }
        return gang > 0 && dui >= 5;
    }

    public boolean checkQingQiDui(int[] hands, List<Group> showGroup) {
        if (!showGroup.isEmpty()) return false;
        int dui = 0;
        for (int i = 0; i < 9; i++) {
            if (hands[i] >= 2) {
                dui++;
            }
        }
        if (dui >= 7) return true;

        dui = 0;
        for (int i = 9; i < 18; i++) {
            if (hands[i] >= 2) {
                dui++;
            }
        }
        if (dui >= 7) return true;

        dui = 0;
        for (int i = 18; i < 27; i++) {
            if (hands[i] >= 2) {
                dui++;
            }
        }
        return dui >= 7;
    }

    public boolean checkHaoHuaQiDui(int[] hands, List<Group> showGroup) {
        if (!showGroup.isEmpty()) return false;
        int gang = 0;
        int dui = 0;
        for (int i = 0; i < 27; i++) {
            if (hands[i] == 4) {
                if (gang > 0) {
                    dui += 2;
                } else {
                    gang++;
                }
            } else if (hands[i] >= 2) {
                dui++;
            }
        }
        return gang > 0 && dui >= 5;
    }

    public boolean checkQiDui(int[] hands, List<Group> showGroup) {
        if (!showGroup.isEmpty()) return false;
        int dui = 0;
        for (int i = 0; i < 27; i++) {
            if (hands[i] >= 2) {
                dui++;
            }
        }
        return dui >= 7;
    }

    public boolean checkQingYiSe(int[] hands, List<Group> showGroup) {
        if (showGroup.isEmpty()) {
            for (int color = 0; color < 3; color++) {
                int[] clone = hands.clone();
                for (int i = 0; i < clone.length; i++) {
                    if (AIUtils.color(i) != color) {
                        clone[i] = 0;
                    }
                }
                if (checkWinNoJang(clone, showGroup)) return true;
            }
        } else {
            int color = showGroup.get(0).getColor();
            for (int i = 1; i < showGroup.size(); i++) {
                if (color != showGroup.get(i).getColor()) {
                    return false;
                }
            }
            int[] clone = hands.clone();
            for (int i = 0; i < clone.length; i++) {
                if (AIUtils.color(i) != color) {
                    clone[i] = 0;
                }
            }
            if (checkWinNoJang(clone, showGroup)) return true;
        }
        return false;
    }

    public boolean checkWinNoJang(int[] hands, List<Group> showGroup) {
        List<Group> allJong = getAllJong(hands);
//        移除将牌，然后组合所有可能
        for (Group jong : allJong) {
            remove(hands, jong);
            boolean checkedWin = checkWinNoJang(hands, new ArrayList<>(), showGroup.size());
            add(hands, jong);
            if (checkedWin) return true;
        }
        return false;
    }

    /**
     * 在移除有将牌的情况下，找出可以胡牌的组合
     *
     * @param hands
     * @param myGroup
     * @return
     */
    public boolean checkWinNoJang(int[] hands, List<Group> myGroup, int showGroupSize) {
        if (showGroupSize + myGroup.size() >= 3) return hasKeGangShun(hands);
        List<Group> allGroup = getAllGroup(hands);
        //if (myGroup.size() + allGroup.size() + showGroupSize< 4) return false;
        allGroup.sort(Group.comparator);
        for (Group group : allGroup) {
            Group last = myGroup.isEmpty() ? null : myGroup.get(0);
            if (Group.comparator.compare(group, last) >= 0) {
                remove(hands, group);
                myGroup.add(group);
                boolean checkedWin = checkWinNoJang(hands, myGroup, showGroupSize);
                myGroup.remove(myGroup.size() - 1);
                add(hands, group);
                if (checkedWin) return true;
            }
        }
        return false;
    }

    /**
     * 判断首牌是否包含刻杠
     *
     * @param hands
     * @return
     */
    public boolean hasKeGangShun(int[] hands) {
        for (int i = 0; i < 9; i++) {
            if (hands[i] > 0) {
                if (hands[i] >= 3) return true;
                if (i < 7) {
                    if (hands[i] > 0 && hands[i + 1] > 0 && hands[i + 2] > 0) return true;
                }
            }
        }

        for (int i = 9; i < 18; i++) {
            if (hands[i] > 0) {
                if (hands[i] >= 3) return true;
                if (i < 16) {
                    if (hands[i] > 0 && hands[i + 1] > 0 && hands[i + 2] > 0) return true;
                }
            }
        }

        for (int i = 18; i < 27; i++) {
            if (hands[i] > 0) {
                if (hands[i] >= 3) return true;
                if (i < 25) {
                    if (hands[i] > 0 && hands[i + 1] > 0 && hands[i + 2] > 0) return true;
                }
            }
        }
        return false;
    }

    public List<Group> getAllGroup(int[] hands) {
        List<Group> groups = new ArrayList<>(8);
        for (int i = 0; i < 9; i++) {
            if (hands[i] > 0) {
                if (i < 7) {
                    if (hands[i] > 0 && hands[i + 1] > 0 && hands[i + 2] > 0) {
                        Group shun = new Group();
                        shun.getCards().add(i);
                        shun.getCards().add(i + 1);
                        shun.getCards().add(i + 2);
                        groups.add(shun);
                    }
                }
                if (hands[i] == 4) {
                    Group ke = new Group();
                    ke.getCards().add(i);
                    ke.getCards().add(i);
                    ke.getCards().add(i);
                    Group gang = new Group();
                    gang.getCards().add(i);
                    gang.getCards().add(i);
                    gang.getCards().add(i);
                    gang.getCards().add(i);
                    groups.add(ke);
                    groups.add(gang);
                } else if (hands[i] == 3) {
                    Group ke = new Group();
                    ke.getCards().add(i);
                    ke.getCards().add(i);
                    ke.getCards().add(i);
                    groups.add(ke);
                }
            }
        }

        for (int i = 9; i < 18; i++) {
            if (hands[i] > 0) {
                if (i < 16) {
                    if (hands[i] > 0 && hands[i + 1] > 0 && hands[i + 2] > 0) {
                        Group shun = new Group();
                        shun.getCards().add(i);
                        shun.getCards().add(i + 1);
                        shun.getCards().add(i + 2);
                        groups.add(shun);
                    }
                }
                if (hands[i] == 4) {
                    Group ke = new Group();
                    ke.getCards().add(i);
                    ke.getCards().add(i);
                    ke.getCards().add(i);
                    Group gang = new Group();
                    gang.getCards().add(i);
                    gang.getCards().add(i);
                    gang.getCards().add(i);
                    gang.getCards().add(i);
                    groups.add(ke);
                    groups.add(gang);
                } else if (hands[i] == 3) {
                    Group ke = new Group();
                    ke.getCards().add(i);
                    ke.getCards().add(i);
                    ke.getCards().add(i);
                    groups.add(ke);
                }
            }
        }

        for (int i = 18; i < 27; i++) {
            if (hands[i] > 0) {
                if (i < 25) {
                    if (hands[i] > 0 && hands[i + 1] > 0 && hands[i + 2] > 0) {
                        Group shun = new Group();
                        shun.getCards().add(i);
                        shun.getCards().add(i + 1);
                        shun.getCards().add(i + 2);
                        groups.add(shun);
                    }
                }
                if (hands[i] == 4) {
                    Group ke = new Group();
                    ke.getCards().add(i);
                    ke.getCards().add(i);
                    ke.getCards().add(i);
                    Group gang = new Group();
                    gang.getCards().add(i);
                    gang.getCards().add(i);
                    gang.getCards().add(i);
                    gang.getCards().add(i);
                    groups.add(ke);
                    groups.add(gang);
                } else if (hands[i] == 3) {
                    Group ke = new Group();
                    ke.getCards().add(i);
                    ke.getCards().add(i);
                    ke.getCards().add(i);
                    groups.add(ke);
                }
            }
        }

        return groups;
    }

    public List<Group> getAllJong(int[] hands) {
        List<Group> allJong = new ArrayList<>(8);
        for (int i = 0; i < hands.length; i++) {
            if (hands[i] >= 2) {
                Group group = new Group();
                group.getCards().add(i);
                group.getCards().add(i);
                allJong.add(group);
            }
        }
        return allJong;
    }

    public void remove(int[] hands, Group group) {
        for (Integer card : group.getCards()) {
            hands[card]--;
        }
    }

    public void add(int[] hands, Group group) {
        for (Integer card : group.getCards()) {
            hands[card]++;
        }
    }

    public int[] getColorSize(int[] hands) {
        int[] colorSize = new int[4];
        for (int i = 0; i < 9; i++) {
            if (hands[i] > 0) colorSize[0] += hands[i];
        }
        for (int i = 9; i < 18; i++) {
            if (hands[i] > 0) colorSize[1] += hands[i];
        }
        for (int i = 18; i < 27; i++) {
            if (hands[i] > 0) colorSize[2] += hands[i];
        }
        for (int i = 27; i < 34; i++) {
            if (hands[i] > 0) colorSize[3] += hands[i];
        }
        return colorSize;
    }

    //实现功能：在curGroups中将第i个group替换成newGroup，返回替换后牌的胡牌距离。
    int getWinDistance(List<SplitGroup> curGroups, int i, SplitGroup newGroup) {
        ArrayList<SplitGroup> splitGroups = new ArrayList<>(curGroups);
        splitGroups.remove(i);
        splitGroups.add(newGroup);
        SplitGroupsHelp initGroup = new SplitGroupsHelp();
        int count = 0;
        for (SplitGroup curGroup : splitGroups) {
            initGroup.addSplitGroup(curGroup);
            count += curGroup.getCount();
        }
        initGroup.setInitLen(count);
        return calWinDis(initGroup);
    }

    public void validTakeIn_LowLevel(SplitGroupsHelp splitGroupsHelp, Set<Integer> profitTiles) {
        int winDistance = splitGroupsHelp.getWinDistance();
        List<SplitGroup> curSplitGroup = splitGroupsHelp.getCurSplitGroup();
        for (int i = 0; i < curSplitGroup.size(); i++) {
            SplitGroup splitGroup = curSplitGroup.get(i);
            int tile = splitGroup.getTile();
            switch (splitGroup.getType()) {
                case SPLIT_GROUP_TYPE_PAIR:
                    if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_KE, tile)) < winDistance) {
                        profitTiles.add(tile);
                    }
                    break;
                case SPLIT_GROUP_TYPE_SINGLE:
                    if (splitGroupsHelp.isSevenPair()) {
                        profitTiles.add(tile);
                        continue;
                    }

                    //单张组成对子
                    if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_PAIR, tile)) < winDistance) {
                        profitTiles.add(tile);
                    }

                    //单张组成连张和坎张
                    if (tile < 27) {
                        if (tile % 9 == 0) {
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG, tile)) < winDistance) {
                                profitTiles.add(tile + 1);
                            }
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_KANZHANG, tile)) < winDistance) {
                                profitTiles.add(tile + 2);
                            }
                        } else if (tile % 9 == 1) {
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG, tile - 1)) < winDistance) {
                                profitTiles.add(tile - 1);
                            }
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG, tile)) < winDistance) {
                                profitTiles.add(tile + 1);
                            }
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_KANZHANG, tile)) < winDistance) {
                                profitTiles.add(tile + 2);
                            }
                        } else if (tile % 9 == 7) {
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG, tile)) < winDistance) {
                                profitTiles.add(tile + 1);
                            }
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG, tile - 1)) < winDistance) {
                                profitTiles.add(tile - 1);
                            }
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_KANZHANG, tile - 2)) < winDistance) {
                                profitTiles.add(tile - 2);
                            }
                        } else if (tile % 9 == 8) {
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG, tile - 1)) < winDistance) {
                                profitTiles.add(tile - 1);
                            }
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_KANZHANG, tile - 2)) < winDistance) {
                                profitTiles.add(tile - 2);
                            }
                        } else {
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG, tile - 1)) < winDistance) {
                                profitTiles.add(tile - 1);
                            }
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_KANZHANG, tile - 2)) < winDistance) {
                                profitTiles.add(tile - 2);
                            }
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG, tile)) < winDistance) {
                                profitTiles.add(tile + 1);
                            }
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_KANZHANG, tile)) < winDistance) {
                                profitTiles.add(tile + 2);
                            }
                        }
                    }
                    break;
                case SPLIT_GROUP_TYPE_LIANZHANG:
                    if (tile % 9 == 0) {
                        if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_SHUN, tile)) < winDistance) {
                            profitTiles.add(tile + 2);
                        }
                    } else if (tile % 9 == 7) {
                        if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_SHUN, tile - 1)) < winDistance) {
                            profitTiles.add(tile - 1);
                        }
                    } else {
                        if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_SHUN, tile - 1)) < winDistance) {
                            profitTiles.add(tile - 1);
                        }
                        if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_SHUN, tile)) < winDistance) {
                            profitTiles.add(tile + 2);
                        }
                    }
                    break;
                case SPLIT_GROUP_TYPE_KANZHANG:
                    if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_SHUN, tile)) < winDistance) {
                        profitTiles.add(tile + 1);
                    }
                    break;
            }
        }
    }

    public void validTakeIn(SplitGroupsHelp splitGroupsHelp, Set<Integer> profitTiles) {
        int winDistance = splitGroupsHelp.getWinDistance();
        List<SplitGroup> curSplitGroup = splitGroupsHelp.getCurSplitGroup();
        for (int i = 0; i < curSplitGroup.size(); i++) {
            SplitGroup splitGroup = curSplitGroup.get(i);
            int tile = splitGroup.getTile();
            switch (splitGroup.getType()) {
                case SPLIT_GROUP_TYPE_PAIR:
                    if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_KE, tile)) < winDistance) {
                        profitTiles.add(tile);
                    }
                    break;
                case SPLIT_GROUP_TYPE_SINGLE:
                    if (splitGroupsHelp.isSevenPair()) {
                        profitTiles.add(tile);
                        continue;
                    }

                    //单张组成对子
                    if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_PAIR, tile)) < winDistance) {
                        profitTiles.add(tile);
                    }

                    //单张组成连张和坎张
                    if (tile < 27) {
                        if (tile % 9 == 0) {
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG, tile)) < winDistance) {
                                profitTiles.add(tile + 1);
                            }
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_KANZHANG, tile)) < winDistance) {
                                profitTiles.add(tile + 2);
                            }
                        } else if (tile % 9 == 1) {
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG, tile - 1)) < winDistance) {
                                profitTiles.add(tile - 1);
                            }
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG, tile)) < winDistance) {
                                profitTiles.add(tile + 1);
                            }
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_KANZHANG, tile)) < winDistance) {
                                profitTiles.add(tile + 2);
                            }
                        } else if (tile % 9 == 7) {
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG, tile)) < winDistance) {
                                profitTiles.add(tile + 1);
                            }
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG, tile - 1)) < winDistance) {
                                profitTiles.add(tile - 1);
                            }
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_KANZHANG, tile - 2)) < winDistance) {
                                profitTiles.add(tile - 2);
                            }
                        } else if (tile % 9 == 8) {
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG, tile - 1)) < winDistance) {
                                profitTiles.add(tile - 1);
                            }
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_KANZHANG, tile - 2)) < winDistance) {
                                profitTiles.add(tile - 2);
                            }
                        } else {
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG, tile - 1)) < winDistance) {
                                profitTiles.add(tile - 1);
                            }
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_KANZHANG, tile - 2)) < winDistance) {
                                profitTiles.add(tile - 2);
                            }
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG, tile)) < winDistance) {
                                profitTiles.add(tile + 1);
                            }
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_KANZHANG, tile)) < winDistance) {
                                profitTiles.add(tile + 2);
                            }
                        }
                    }
                    break;
                case SPLIT_GROUP_TYPE_LIANZHANG:
                    if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_PAIR, tile)) < winDistance) {
                        profitTiles.add(tile);
                    }
                    if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_PAIR, tile + 1)) < winDistance) {
                        profitTiles.add(tile + 1);
                    }
                    if (tile % 9 == 0) {
                        if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_SHUN, tile)) < winDistance) {
                            profitTiles.add(tile + 2);
                        }
                    } else if (tile % 9 == 7) {
                        if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_SHUN, tile - 1)) < winDistance) {
                            profitTiles.add(tile - 1);
                        }
                    } else {
                        if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_SHUN, tile - 1)) < winDistance) {
                            profitTiles.add(tile - 1);
                        }
                        if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_SHUN, tile)) < winDistance) {
                            profitTiles.add(tile + 2);
                        }
                    }
                    break;
                case SPLIT_GROUP_TYPE_KANZHANG:
                    if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_PAIR, tile)) < winDistance) {
                        profitTiles.add(tile);
                    }
                    if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_PAIR, tile + 2)) < winDistance) {
                        profitTiles.add(tile + 2);
                    }
                    if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_SHUN, tile)) < winDistance) {
                        profitTiles.add(tile + 1);
                    }
                    break;
            }
        }
    }

    public void getValidTakeInCount(SplitGroupsHelp splitGroupsHelp, Map<Integer, Integer> profitCountByTile, int remain[]) {
//        Map<Integer, Integer> profitCountByTile = new TreeMap<>();
        Set<Integer> profitTiles = new HashSet<>();
        int winDistance = splitGroupsHelp.getWinDistance();
        List<SplitGroup> curSplitGroup = splitGroupsHelp.getCurSplitGroup();
        for (int i = 0; i < curSplitGroup.size(); i++) {
            SplitGroup splitGroup = curSplitGroup.get(i);
            int tile = splitGroup.getTile();
            switch (splitGroup.getType()) {
                case SPLIT_GROUP_TYPE_PAIR:
                    if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_KE, tile)) < winDistance) {
                        if (remain[tile] == 1) {

                        }
                        profitTiles.add(tile);
                    }
                    break;
                case SPLIT_GROUP_TYPE_SINGLE:
                    if (splitGroupsHelp.isSevenPair()) {
                        profitTiles.add(tile);
                        continue;
                    }

                    //单张组成对子
                    if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_PAIR, tile)) < winDistance) {
                        profitTiles.add(tile);
                    }

                    //单张组成连张和坎张
                    if (tile < 27) {
                        if (tile % 9 == 0) {
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG, tile)) < winDistance) {
                                profitTiles.add(tile + 1);
                            }
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_KANZHANG, tile)) < winDistance) {
                                profitTiles.add(tile + 2);
                            }
                        } else if (tile % 9 == 1) {
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG, tile - 1)) < winDistance) {
                                profitTiles.add(tile - 1);
                            }
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG, tile)) < winDistance) {
                                profitTiles.add(tile + 1);
                            }
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_KANZHANG, tile)) < winDistance) {
                                profitTiles.add(tile + 2);
                            }
                        } else if (tile % 9 == 7) {
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG, tile)) < winDistance) {
                                profitTiles.add(tile + 1);
                            }
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG, tile - 1)) < winDistance) {
                                profitTiles.add(tile - 1);
                            }
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_KANZHANG, tile - 2)) < winDistance) {
                                profitTiles.add(tile - 2);
                            }
                        } else if (tile % 9 == 8) {
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG, tile - 1)) < winDistance) {
                                profitTiles.add(tile - 1);
                            }
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_KANZHANG, tile - 2)) < winDistance) {
                                profitTiles.add(tile - 2);
                            }
                        } else {
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG, tile - 1)) < winDistance) {
                                profitTiles.add(tile - 1);
                            }
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_KANZHANG, tile - 2)) < winDistance) {
                                profitTiles.add(tile - 2);
                            }
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG, tile)) < winDistance) {
                                profitTiles.add(tile + 1);
                            }
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_KANZHANG, tile)) < winDistance) {
                                profitTiles.add(tile + 2);
                            }
                        }
                    }
                    break;
                case SPLIT_GROUP_TYPE_LIANZHANG:
                    if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_PAIR, tile)) < winDistance) {
                        profitTiles.add(tile);
                    }
                    if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_PAIR, tile + 1)) < winDistance) {
                        profitTiles.add(tile + 1);
                    }
                    if (tile % 9 == 0) {
                        if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_SHUN, tile)) < winDistance) {
                            profitTiles.add(tile + 2);
                        }
                    } else if (tile % 9 == 7) {
                        if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_SHUN, tile - 1)) < winDistance) {
                            profitTiles.add(tile - 1);
                        }
                    } else {
                        if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_SHUN, tile - 1)) < winDistance) {
                            profitTiles.add(tile - 1);
                        }
                        if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_SHUN, tile)) < winDistance) {
                            profitTiles.add(tile + 2);
                        }
                    }
                    break;
                case SPLIT_GROUP_TYPE_KANZHANG:
                    if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_PAIR, tile)) < winDistance) {
                        profitTiles.add(tile);
                    }
                    if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_PAIR, tile + 2)) < winDistance) {
                        profitTiles.add(tile + 2);
                    }
                    if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_SHUN, tile)) < winDistance) {
                        profitTiles.add(tile + 1);
                    }
                    break;
            }
        }
    }

    public Set<Integer> getOutSetFromSplitGroupsHelp(List<SplitGroupsHelp> myResult, int hands[]) {
        Set<Integer> outSet = new HashSet<>();
        for (SplitGroupsHelp splitGroupsHelp : myResult) {
            List<SplitGroup> pairgroup = new ArrayList<>();
            List<SplitGroup> lianzhanggroup = new ArrayList<>();
            List<SplitGroup> kanzhanggroup = new ArrayList<>();
            for (SplitGroup splitGroup : splitGroupsHelp.getCurSplitGroup()) {
                if (splitGroup.getType() == SplitGroupType.SPLIT_GROUP_TYPE_SINGLE) {
                    outSet.add(splitGroup.getTile());
                } else if (splitGroup.getType() == SplitGroupType.SPLIT_GROUP_TYPE_PAIR) {
                    pairgroup.add(splitGroup);
                } else if (splitGroup.getType() == SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG) {
                    lianzhanggroup.add(splitGroup);
                } else if (splitGroup.getType() == SplitGroupType.SPLIT_GROUP_TYPE_KANZHANG) {
                    kanzhanggroup.add(splitGroup);
                }
            }

            //需要单独考虑对子吗？
            //WinDistance=2 4b5b6b,9b9b,1t2t,5t7t,4b,7b
            //WinDistance=2 5b6b7b,4b4b,9b9b,1t2t,5t7t
            if (splitGroupsHelp.getCurPairCount() + splitGroupsHelp.getCurConnectCount() - 1 > splitGroupsHelp.getWinDistance()) {
                //2个对子，可以拆一个对子，1个对子，就不要拆这个对子
                if (splitGroupsHelp.getCurPairCount() > 1) {
                    for (int i = 0; i < pairgroup.size(); i++) {
                        outSet.add(pairgroup.get(i).getTile());
                    }
                }

                for (int i = 0; i < lianzhanggroup.size(); i++) {
                    outSet.add(lianzhanggroup.get(i).getTile());
                    outSet.add(lianzhanggroup.get(i).getTile() + 1);
                }

                for (int i = 0; i < kanzhanggroup.size(); i++) {
                    outSet.add(kanzhanggroup.get(i).getTile());
                    outSet.add(kanzhanggroup.get(i).getTile() + 2);
                }
            }
        }

        if (outSet.isEmpty()) {
            for (SplitGroupsHelp splitGroupsHelp : myResult) {
                int curPairCount = splitGroupsHelp.getCurPairCount();
                for (SplitGroup splitGroup : splitGroupsHelp.getCurSplitGroup()) {
                    if (curPairCount > 1 && splitGroup.getType() == SplitGroupType.SPLIT_GROUP_TYPE_PAIR) {
                        outSet.add(splitGroup.getTile());
                    } else if (splitGroup.getType() == SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG) {
                        outSet.add(splitGroup.getTile());
                        outSet.add(splitGroup.getTile() + 1);
                    } else if (splitGroup.getType() == SplitGroupType.SPLIT_GROUP_TYPE_KANZHANG) {
                        outSet.add(splitGroup.getTile());
                        outSet.add(splitGroup.getTile() + 2);
                    }
                }
            }
        }
        if (outSet.isEmpty()) {
            for (int i = 0; i < hands.length; i++) {
                if (hands[i] > 0) outSet.add(i);
            }
        }
        return outSet;
    }

    //13张牌算进张牌效
    public int calcTakeincount13cards(int[] hands, int[] remain) {
        List<SplitGroupsHelp> tempResult = splitHandsNew(hands);
        int minWinDistance = getMinWinDistance(tempResult);
        Set<Integer> profitTiles = new HashSet<>();
        for (SplitGroupsHelp splitGroupsHelp : tempResult) {
            if (minWinDistance == splitGroupsHelp.getWinDistance()) {
                validTakeIn(splitGroupsHelp, profitTiles);
            }
        }

        int count = 0;
        for (int j = 0; j < TILE_TYPE_COUNT; j++) {
            if (remain[j] > 0 && profitTiles.contains(j)) {
                count += remain[j];
            }
        }
        return count;
    }

    public Map<Integer, Integer> findLowProfit(List<SplitGroupsHelp> myResult, int[] hand, int[] remain, int myWinDistance) {
        Set<Integer> outSet = getOutSetFromSplitGroupsHelp(myResult, hand);
        Map<Integer, Integer> profitCountByTile = new TreeMap<>();
        for (int i : outSet) {
            hand[i]--;
            List<SplitGroupsHelp> tempResult = splitHandsNew(hand);
            hand[i]++;
            int minWinDistance = getMinWinDistance(tempResult);
            if (minWinDistance > myWinDistance) {
                continue;
            }
            Set<Integer> profitTiles = new HashSet<>();
            for (SplitGroupsHelp splitGroupsHelp : tempResult) {
                if (minWinDistance == splitGroupsHelp.getWinDistance()) {
                    validTakeIn(splitGroupsHelp, profitTiles);
                }
            }

            int count = 0;
            for (int j = 0; j < TILE_TYPE_COUNT; j++) {
                if (remain[j] > 0 && profitTiles.contains(j)) {
                    count += remain[j];
                }
            }
            String builder = profitTiles.stream().map(profitTile -> AIConstant.CARD_NAME[profitTile] + ',').collect(Collectors.joining());
            logger.info(i + ":" + AIConstant.CARD_NAME[i] + ":" + builder + ":" + count);
            profitCountByTile.put(i, count);
        }
        return profitCountByTile;
    }

    public void validTakeInValue(SplitGroupsHelp splitGroupsHelp, Map<Integer, Double> profitTiles, int[] remain) {
        int winDistance = splitGroupsHelp.getWinDistance();
        List<SplitGroup> curSplitGroup = splitGroupsHelp.getCurSplitGroup();
        for (int i = 0; i < curSplitGroup.size(); i++) {
            SplitGroup splitGroup = curSplitGroup.get(i);
            int tile = splitGroup.getTile();
            double tilevalue = tile;
            switch (splitGroup.getType()) {
                case SPLIT_GROUP_TYPE_PAIR:
                    if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_KE, tile)) < winDistance) {
                        double weight = (tile >= 27 || tile % 9 >= 5) ? 1.2 : 1;//大于6，权重变为1.2
                        double value = 0;
                        if (remain[tile] == 1) {//对子剩一张，一张当三张
                            value = 3 * weight;
                        } else if (remain[tile] == 2) {//对子剩两张，一张当九张
                            value = 9 * weight;
                        }
                        profitTiles.merge(tile, value, Math::max);
                    }
                    break;
                case SPLIT_GROUP_TYPE_SINGLE:
                    double weight;
                    double value;
                    weight = (tile >= 27 || tile % 9 >= 5) ? 1.2 : 1;
                    value = weight * remain[tile];
                    if (splitGroupsHelp.isSevenPair()) {
                        profitTiles.merge(tile, value, Math::max);
                        continue;
                    }

                    //对子
                    if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_PAIR, tile)) < winDistance) {
                        profitTiles.merge(tile, value, Math::max);
                    }

                    //连张
                    //坎张
                    if (tile < 27) {
                        if (tile % 9 == 0) {
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG, tile)) < winDistance) {
                                weight = ((tile + 1) >= 27 || (tile + 1) % 9 >= 5) ? 1.2 : 1;
                                value = weight * remain[tile + 1];
                                profitTiles.merge(tile + 1, value, Math::max);
                            }
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_KANZHANG, tile)) < winDistance) {
                                weight = ((tile + 2) >= 27 || (tile + 2) % 9 >= 5) ? 1.2 : 1;
                                value = weight * remain[tile + 2];
                                profitTiles.merge(tile + 2, value, Math::max);
                            }
                        } else if (tile % 9 == 1) {
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG, tile - 1)) < winDistance) {
                                weight = ((tile - 1) >= 27 || (tile - 1) % 9 >= 5) ? 1.2 : 1;
                                value = weight * remain[tile - 1];
                                profitTiles.merge(tile - 1, value, Math::max);
                            }
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG, tile)) < winDistance) {
                                weight = ((tile + 1) >= 27 || (tile + 1) % 9 >= 5) ? 1.2 : 1;
                                value = weight * remain[tile + 1];
                                profitTiles.merge(tile + 1, value, Math::max);
                            }
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_KANZHANG, tile)) < winDistance) {
                                weight = ((tile + 2) >= 27 || (tile + 2) % 9 >= 5) ? 1.2 : 1;
                                value = weight * remain[tile + 2];
                                profitTiles.merge(tile + 2, value, Math::max);
                            }
                        } else if (tile % 9 == 7) {
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG, tile)) < winDistance) {
                                weight = ((tile + 1) >= 27 || (tile + 1) % 9 >= 5) ? 1.2 : 1;
                                value = weight * remain[tile + 1];
                                profitTiles.merge(tile + 1, value, Math::max);
                            }
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG, tile - 1)) < winDistance) {
                                weight = ((tile - 1) >= 27 || (tile - 1) % 9 >= 5) ? 1.2 : 1;
                                value = weight * remain[tile - 1];
                                profitTiles.merge(tile - 1, value, Math::max);
                            }
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_KANZHANG, tile - 2)) < winDistance) {
                                weight = ((tile - 2) >= 27 || (tile - 2) % 9 >= 5) ? 1.2 : 1;
                                value = weight * remain[tile - 2];
                                profitTiles.merge(tile - 2, value, Math::max);
                            }
                        } else if (tile % 9 == 8) {
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG, tile - 1)) < winDistance) {
                                weight = ((tile - 1) >= 27 || (tile - 1) % 9 >= 5) ? 1.2 : 1;
                                value = weight * remain[tile - 1];
                                profitTiles.merge(tile - 1, value, Math::max);
                            }
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_KANZHANG, tile - 2)) < winDistance) {
                                weight = ((tile - 2) >= 27 || (tile - 2) % 9 >= 5) ? 1.2 : 1;
                                value = weight * remain[tile - 2];
                                profitTiles.merge(tile - 2, value, Math::max);
                            }
                        } else {
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG, tile - 1)) < winDistance) {
                                weight = ((tile - 1) >= 27 || (tile - 1) % 9 >= 5) ? 1.2 : 1;
                                value = weight * remain[tile - 1];
                                profitTiles.merge(tile - 1, value, Math::max);
                            }
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_KANZHANG, tile - 2)) < winDistance) {
                                weight = ((tile - 2) >= 27 || (tile - 2) % 9 >= 5) ? 1.2 : 1;
                                value = weight * remain[tile - 2];
                                profitTiles.merge(tile - 2, value, Math::max);
                            }
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG, tile)) < winDistance) {
                                weight = ((tile + 1) >= 27 || (tile + 1) % 9 >= 5) ? 1.2 : 1;
                                value = weight * remain[tile + 1];
                                profitTiles.merge(tile + 1, value, Math::max);
                            }
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_KANZHANG, tile)) < winDistance) {
                                weight = ((tile + 2) >= 27 || (tile + 2) % 9 >= 5) ? 1.2 : 1;
                                value = weight * remain[tile + 2];
                                profitTiles.merge(tile + 2, value, Math::max);
                            }
                        }
                    }
                    break;
                case SPLIT_GROUP_TYPE_LIANZHANG:
                    //对子
                    if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_PAIR, tile)) < winDistance) {
                        weight = (tile >= 27 || tile % 9 >= 5) ? 1.2 : 1;
                        value = weight * remain[tile];
                        profitTiles.merge(tile, value, Math::max);
                    }
                    //对子
                    if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_PAIR, tile + 1)) < winDistance) {
                        weight = ((tile + 1) >= 27 || (tile + 1) % 9 >= 5) ? 1.2 : 1;
                        value = weight * remain[tile + 1];
                        profitTiles.merge(tile + 1, value, Math::max);
                    }

                    if (tile % 9 == 0) {
                        if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_SHUN, tile)) < winDistance) {
                            weight = ((tile + 2) >= 27 || (tile + 2) % 9 >= 5) ? 1.2 : 1;
                            value = weight * remain[tile + 2];
                            profitTiles.merge(tile + 2, value, Math::max);
                        }
                    } else if (tile % 9 == 7) {
                        if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_SHUN, tile - 1)) < winDistance) {
                            weight = ((tile - 1) >= 27 || (tile - 1) % 9 >= 5) ? 1.2 : 1;
                            value = weight * remain[tile - 1];
                            profitTiles.merge(tile - 1, value, Math::max);
                        }
                    } else {
                        if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_SHUN, tile - 1)) < winDistance) {
                            weight = ((tile - 1) >= 27 || (tile - 1) % 9 >= 5) ? 1.2 : 1;
                            value = weight * remain[tile - 1];
                            profitTiles.merge(tile - 1, value, Math::max);
                        }
                        if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_SHUN, tile)) < winDistance) {
                            weight = ((tile + 2) >= 27 || (tile + 2) % 9 >= 5) ? 1.2 : 1;
                            value = weight * remain[tile + 2];
                            profitTiles.merge(tile + 2, value, Math::max);
                        }
                    }
                    break;
                case SPLIT_GROUP_TYPE_KANZHANG:
                    //对子
                    if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_PAIR, tile)) < winDistance) {
                        weight = (tile >= 27 || tile % 9 >= 5) ? 1.2 : 1;
                        value = weight * remain[tile];
                        profitTiles.merge(tile, value, Math::max);
                    }
                    //对子
                    if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_PAIR, tile + 2)) < winDistance) {
                        weight = ((tile + 2) >= 27 || (tile + 2) % 9 >= 5) ? 1.2 : 1;
                        value = weight * remain[tile + 2];
                        profitTiles.merge(tile + 2, value, Math::max);
                    }

                    if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_SHUN, tile)) < winDistance) {
                        weight = ((tile + 1) >= 27 || (tile + 1) % 9 >= 5) ? 1.2 : 1;
                        value = weight * remain[tile + 1];
                        profitTiles.merge(tile + 1, value, Math::max);
                    }
                    break;
                case SPLIT_GROUP_TYPE_KE:
                    if (remain[tile] != 0) {
                        weight = (tile >= 27 || tile % 9 >= 5) ? 6 : 3;
                        value = weight * remain[tile];
                        profitTiles.merge(tile, value, Math::max);
                    }
                    break;
            }
        }
    }

    //进张考虑两步
    public void validTakeInValue2Steps(SplitGroupsHelp splitGroupsHelp, Map<Integer, Double> profitTiles, int[] remain) {
        int winDistance = splitGroupsHelp.getWinDistance();
        List<SplitGroup> curSplitGroup = splitGroupsHelp.getCurSplitGroup();
        for (int i = 0; i < curSplitGroup.size(); i++) {
            SplitGroup splitGroup = curSplitGroup.get(i);
            int tile = splitGroup.getTile();
            double tilevalue = tile;
            switch (splitGroup.getType()) {
                case SPLIT_GROUP_TYPE_PAIR:
                    if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_KE, tile)) < winDistance) {
                        double weight = (tile >= 27 || tile % 9 >= 5) ? 1.2 : 1;//大于6，权重变为1.2
                        double value = 0;
                        if (remain[tile] == 1) {//对子剩一张，一张当三张
                            value = 3 * weight;
                        } else if (remain[tile] == 2) {//对子剩两张，一张当九张
                            value = 9 * weight;
                        }
                        profitTiles.merge(tile, value, Math::max);
                    }
                    break;
                case SPLIT_GROUP_TYPE_SINGLE:
                    double weight;
                    double value;
                    weight = (tile >= 27 || tile % 9 >= 5) ? 1.2 : 1;
                    value = weight * remain[tile];
                    if (splitGroupsHelp.isSevenPair()) {
                        profitTiles.merge(tile, value, Math::max);
                        continue;
                    }

                    //对子
                    if (splitGroupsHelp.getCurPairCount() == 0) {//没有对子，单张组成将就可以
                        if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_PAIR, tile)) < winDistance) {
                            profitTiles.merge(tile, value, Math::max);
                        }
                    } else {//已经有对子了，单张要能够组成刻才算有效进张
                        if (remain[tile] >= 2) {
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_PAIR, tile)) < winDistance) {
                                profitTiles.merge(tile, value, Math::max);
                            }
                        }
                    }


                    //连张
                    //坎张
                    if (tile < 27) {
                        if (tile % 9 == 0) {
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG, tile)) < winDistance) {
                                weight = ((tile + 1) >= 27 || (tile + 1) % 9 >= 5) ? 1.2 : 1;
                                value = weight * Math.min(remain[tile + 1], remain[tile + 2]);
                                profitTiles.merge(tile + 1, value, Math::max);
                            }
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_KANZHANG, tile)) < winDistance) {
                                weight = ((tile + 2) >= 27 || (tile + 2) % 9 >= 5) ? 1.2 : 1;
                                value = weight * Math.min(remain[tile + 1], remain[tile + 2]);
                                profitTiles.merge(tile + 2, value, Math::max);
                            }
                        } else if (tile % 9 == 1) {
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG, tile - 1)) < winDistance) {
                                weight = ((tile - 1) >= 27 || (tile - 1) % 9 >= 5) ? 1.2 : 1;
                                value = weight * Math.min(remain[tile - 1], remain[tile + 1]);
                                profitTiles.merge(tile - 1, value, Math::max);
                            }
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG, tile)) < winDistance) {
                                weight = ((tile + 1) >= 27 || (tile + 1) % 9 >= 5) ? 1.2 : 1;
                                value = weight * remain[tile + 1];
                                value = weight * Math.min(remain[tile + 1], remain[tile - 1] + remain[tile + 2]);
                                profitTiles.merge(tile + 1, value, Math::max);
                            }
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_KANZHANG, tile)) < winDistance) {
                                weight = ((tile + 2) >= 27 || (tile + 2) % 9 >= 5) ? 1.2 : 1;
                                value = weight * Math.min(remain[tile + 1], remain[tile + 2]);
                                profitTiles.merge(tile + 2, value, Math::max);
                            }
                        } else if (tile % 9 == 7) {
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG, tile)) < winDistance) {
                                weight = ((tile + 1) >= 27 || (tile + 1) % 9 >= 5) ? 1.2 : 1;
                                value = weight * Math.min(remain[tile - 1], remain[tile + 1]);
                                profitTiles.merge(tile + 1, value, Math::max);
                            }
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG, tile - 1)) < winDistance) {
                                weight = ((tile - 1) >= 27 || (tile - 1) % 9 >= 5) ? 1.2 : 1;
                                value = weight * Math.min(remain[tile - 1], remain[tile - 2] + remain[tile + 1]);
                                profitTiles.merge(tile - 1, value, Math::max);
                            }
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_KANZHANG, tile - 2)) < winDistance) {
                                weight = ((tile - 2) >= 27 || (tile - 2) % 9 >= 5) ? 1.2 : 1;
                                value = weight * Math.min(remain[tile - 2], remain[tile - 1]);
                                profitTiles.merge(tile - 2, value, Math::max);
                            }
                        } else if (tile % 9 == 8) {
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG, tile - 1)) < winDistance) {
                                weight = ((tile - 1) >= 27 || (tile - 1) % 9 >= 5) ? 1.2 : 1;
                                value = weight * Math.min(remain[tile - 1], remain[tile - 2]);
                                profitTiles.merge(tile - 1, value, Math::max);
                            }
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_KANZHANG, tile - 2)) < winDistance) {
                                weight = ((tile - 2) >= 27 || (tile - 2) % 9 >= 5) ? 1.2 : 1;
                                value = weight * Math.min(remain[tile - 1], remain[tile - 2]);
                                profitTiles.merge(tile - 2, value, Math::max);
                            }
                        } else {
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG, tile - 1)) < winDistance) {
                                weight = ((tile - 1) >= 27 || (tile - 1) % 9 >= 5) ? 1.2 : 1;
                                value = weight * Math.min(remain[tile - 1], remain[tile - 2] + remain[tile + 1]);
                                profitTiles.merge(tile - 1, value, Math::max);
                            }
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_KANZHANG, tile - 2)) < winDistance) {
                                weight = ((tile - 2) >= 27 || (tile - 2) % 9 >= 5) ? 1.2 : 1;
                                value = weight * Math.min(remain[tile - 1], remain[tile - 2]);
                                profitTiles.merge(tile - 2, value, Math::max);
                            }
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG, tile)) < winDistance) {
                                weight = ((tile + 1) >= 27 || (tile + 1) % 9 >= 5) ? 1.2 : 1;
                                value = weight * Math.min(remain[tile + 1], remain[tile + 2] + remain[tile - 1]);
                                profitTiles.merge(tile + 1, value, Math::max);
                            }
                            if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_KANZHANG, tile)) < winDistance) {
                                weight = ((tile + 2) >= 27 || (tile + 2) % 9 >= 5) ? 1.2 : 1;
                                value = weight * Math.min(remain[tile + 1], remain[tile + 2]);
                                profitTiles.merge(tile + 2, value, Math::max);
                            }
                        }
                    }
                    break;
                case SPLIT_GROUP_TYPE_LIANZHANG:
                    //对子
                    if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_PAIR, tile)) < winDistance) {
                        weight = (tile >= 27 || tile % 9 >= 5) ? 1.2 : 1;
                        value = weight * remain[tile];
                        profitTiles.merge(tile, value, Math::max);
                    }
                    //对子
                    if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_PAIR, tile + 1)) < winDistance) {
                        weight = ((tile + 1) >= 27 || (tile + 1) % 9 >= 5) ? 1.2 : 1;
                        value = weight * remain[tile + 1];
                        profitTiles.merge(tile + 1, value, Math::max);
                    }

                    if (tile % 9 == 0) {
                        if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_SHUN, tile)) < winDistance) {
                            weight = ((tile + 2) >= 27 || (tile + 2) % 9 >= 5) ? 1.2 : 1;
                            value = weight * remain[tile + 2];
                            profitTiles.merge(tile + 2, value, Math::max);
                        }
                    } else if (tile % 9 == 7) {
                        if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_SHUN, tile - 1)) < winDistance) {
                            weight = ((tile - 1) >= 27 || (tile - 1) % 9 >= 5) ? 1.2 : 1;
                            value = weight * remain[tile - 1];
                            profitTiles.merge(tile - 1, value, Math::max);
                        }
                    } else {
                        if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_SHUN, tile - 1)) < winDistance) {
                            weight = ((tile - 1) >= 27 || (tile - 1) % 9 >= 5) ? 1.2 : 1;
                            value = weight * remain[tile - 1];
                            profitTiles.merge(tile - 1, value, Math::max);
                        }
                        if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_SHUN, tile)) < winDistance) {
                            weight = ((tile + 2) >= 27 || (tile + 2) % 9 >= 5) ? 1.2 : 1;
                            value = weight * remain[tile + 2];
                            profitTiles.merge(tile + 2, value, Math::max);
                        }
                    }
                    break;
                case SPLIT_GROUP_TYPE_KANZHANG:
                    //对子
                    if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_PAIR, tile)) < winDistance) {
                        weight = (tile >= 27 || tile % 9 >= 5) ? 1.2 : 1;
                        value = weight * remain[tile];
                        profitTiles.merge(tile, value, Math::max);
                    }
                    //对子
                    if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_PAIR, tile + 2)) < winDistance) {
                        weight = ((tile + 2) >= 27 || (tile + 2) % 9 >= 5) ? 1.2 : 1;
                        value = weight * remain[tile + 2];
                        profitTiles.merge(tile + 2, value, Math::max);
                    }

                    if (getWinDistance(curSplitGroup, i, new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_SHUN, tile)) < winDistance) {
                        weight = ((tile + 1) >= 27 || (tile + 1) % 9 >= 5) ? 1.2 : 1;
                        value = weight * remain[tile + 1];
                        profitTiles.merge(tile + 1, value, Math::max);
                    }
                    break;
            }
        }
    }

    public Map<Integer, Double> findLowWeightProfit(List<SplitGroupsHelp> myResult, int[] hand, int[] remain) {
        Set<Integer> outSet = getOutSetFromSplitGroupsHelp(myResult, hand);
        if (outSet.isEmpty()) {
            for (int i = 0; i < hand.length; i++) {
                if (hand[i] > 0) outSet.add(i);
            }
        }

        int oldDis = getMinWinDistance(myResult);
        Map<Integer, Double> profitCountByTile = new TreeMap<>();
        for (int i : outSet) {
            hand[i]--;
            List<SplitGroupsHelp> tempResult = splitHandsNew(hand);
            hand[i]++;
            int minWinDistance = getMinWinDistance(tempResult);
            if (minWinDistance > oldDis && oldDis > 0) continue;
            Map<Integer, Double> profitTileRemainValue = new TreeMap<>();
            for (SplitGroupsHelp splitGroupsHelp : tempResult) {
                if (minWinDistance == splitGroupsHelp.getWinDistance()) {
                    validTakeInValue(splitGroupsHelp, profitTileRemainValue, remain);
                    //validTakeInValue2Steps(splitGroupsHelp, profitTileRemainValue, remain);
                }
            }

            double count = profitTileRemainValue.values().stream().mapToDouble(value -> value).sum();
            String builder = profitTileRemainValue.keySet().stream().map(profitTile -> AIConstant.CARD_NAME[profitTile] + ',').collect(Collectors.joining());
            logger.info(i + ":" + AIConstant.CARD_NAME[i] + ":" + builder + ":" + count);
            profitCountByTile.put(i, count);
        }
        return profitCountByTile;
    }

    public Map<Integer, Integer> findLowProfit_lowlevel(List<SplitGroupsHelp> myResult, int[] hand, int[] remain, int myWinDistance) {
        Set<Integer> outSet = getOutSetFromSplitGroupsHelp(myResult, hand);
        Map<Integer, Integer> profitCountByTile = new TreeMap<>();
        for (int i : outSet) {
            hand[i]--;
            List<SplitGroupsHelp> tempResult = splitHandsNew(hand);
            hand[i]++;
            int minWinDistance = getMinWinDistance(tempResult);
            if (minWinDistance > myWinDistance) {
                continue;
            }
            Set<Integer> profitTiles = new HashSet<>();
            for (SplitGroupsHelp splitGroupsHelp : tempResult) {
                if (minWinDistance == splitGroupsHelp.getWinDistance()) {
                    validTakeIn_LowLevel(splitGroupsHelp, profitTiles);
                }
            }
//            profitTiles.remove(i); // 打这张还是上张 有点不合理，先移除
            int count = 0;
            for (int j = 0; j < TILE_TYPE_COUNT; j++) {
                if (remain[j] > 0 && profitTiles.contains(j)) {
                    count += remain[j];
                }
            }
            String builder = profitTiles.stream().map(profitTile -> AIConstant.CARD_NAME[profitTile] + ',').collect(Collectors.joining());
            logger.info(i + ":" + AIConstant.CARD_NAME[i] + ":" + builder + ":" + count);
            profitCountByTile.put(i, count);
        }
        return profitCountByTile;
    }
}
