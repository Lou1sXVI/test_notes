package com.wzy.game.server.ai;

import com.wzy.game.server.model.ActHistory;
import com.wzy.game.server.model.Player;
import com.wzy.game.server.util.CardAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static com.wzy.game.server.constant.ConstVar.LOG_ACTION_DRAW;

public class EasyGameLogicService {

    private static final Logger logger = LoggerFactory.getLogger(EasyGameLogicService.class);

    public static EasyGameLogicService instance = new EasyGameLogicService();

    private EasyGameLogicService() {
    }

    public boolean checkWin() {
        return true;
    }

    /**
     * 判断是否直杠
     *
     * @param players
     * @param aiPos
     * @param history
     * @return
     */
    public boolean checkZhiGang(List<Player> players, int aiPos, List<ActHistory> history) {
        return true;
    }

    public boolean checkPong(int[] hand, int aiPos, List<ActHistory> history) {
        return true;
    }


    public int checkDiscard(int[] hand, List<Group> groups, int aiPos, List<ActHistory> history, int[] remainNum) {
        List<Integer> hands = new ArrayList<>();
        for (int i = 0; i < 27; i++) {
            for (int j = 0; j < hand[i]; j++) {
                hands.add(i);
            }
        }
        Map<Integer, List<AIResponse.SplitGroupsHelp>> splitRet = new HashMap<>();
        int minWd = 8;
        for (int i = 0; i < hand.length; i++) {
            if (hand[i] > 0) {
                hand[i]--;
                List<AIResponse.SplitGroupsHelp> result = new ArrayList<>();
                AIResponse.splitHands(hand, result);
                int wd = AIResponse.getMinWinDistance(result);
                AIResponse.filter(result, wd);
                if (wd < minWd) {
                    minWd = wd;
                    splitRet.clear();
                    splitRet.put(i, result);
                } else if (wd == minWd) {
                    splitRet.put(i, result);
                }
                hand[i]++;
            }
        }
        if (splitRet.size() == 1) {
            return splitRet.keySet().stream().findFirst().get();
        }
        //从和牌距离最小的出牌里选1张

        Map<Integer, Long> suitNum = hands.stream().collect(Collectors.groupingBy(e -> e / 9,
                Collectors.counting()));
        int mainSuit = getMainSuit(hand,groups);
        int best = -1;
        if (minWd == 1) { //已经听牌，选残枚*分值总和最大的，不考虑定缺
            int kongNum=0;
            List<Integer> pongTile = new ArrayList<>();
            for (int i = 0; i <groups.size();i++){
                if(groups.get(i).getCards().size()>=4){
                    kongNum++;
                }else {
                    pongTile.add(groups.get(i).getCards().get(0));
                }
            }
//           = (int) player.getMelds().stream().filter(e -> e.getType() != 0).count();
            Map<Integer, Map<Integer, Integer>> readyInfos = new HashMap<>();
            for (Map.Entry<Integer, List<AIResponse.SplitGroupsHelp>> entry : splitRet.entrySet()) {
                int[] tmpHand = Arrays.copyOf(hand, hand.length);
                tmpHand[entry.getKey()]--;
                Map<Integer, Integer> readyInfo = new HashMap<>();
                for (AIResponse.SplitGroupsHelp help : entry.getValue()) {
                    List<AIResponse.SplitGroup> group = help.getCurSplitGroup();
                    if (help.isSevenPair()) { //七对
                        int fan =   2;
                        Optional<AIResponse.SplitGroup> opt =
                                group.stream().filter(e -> e.getType() == AIResponse.SplitGroupType.SPLIT_GROUP_TYPE_SINGLE).findAny();
                        if (opt.isPresent()) {
                            int t = opt.get().getTile();
                            if (hand[t] == 3) {
                                fan++;
                            }
                            fan = Math.min(fan, 3);
                            int fen = 1 << fan;
                            readyInfo.compute(t, (k, v) -> (v == null ? fen : Math.max(v, fen)));
                        }
                    } else if (help.getCurPairCount() == 2) { //双碰
                        int fan =  + kongNum;
                        if (group.stream().allMatch(e -> e.getType() == AIResponse.SplitGroupType.SPLIT_GROUP_TYPE_KE
                                || e.getType() == AIResponse.SplitGroupType.SPLIT_GROUP_TYPE_PAIR)) {
                            fan++;
                        }
                        fan = Math.min(fan, 3);
                        int  fen = 1 << fan;
                        for (AIResponse.SplitGroup sg : group) {
                            if (sg.getType() == AIResponse.SplitGroupType.SPLIT_GROUP_TYPE_PAIR) {
                                readyInfo.compute(sg.getTile(), (k, v) -> (v == null ? fen : Math.max(v, fen)));
                            }
                        }
                    } else if (help.getCurSingleCount() == 1) { //单调
                        int fan =  + kongNum;
                        if (group.stream().allMatch(e -> e.getType() == AIResponse.SplitGroupType.SPLIT_GROUP_TYPE_KE
                                || e.getType() == AIResponse.SplitGroupType.SPLIT_GROUP_TYPE_SINGLE)) {
                            fan++;
                        }
                        fan = Math.min(fan, 3);
                        int fen = 1 << fan;
                        for (AIResponse.SplitGroup sg : group) {
                            if (sg.getType() == AIResponse.SplitGroupType.SPLIT_GROUP_TYPE_SINGLE) {
                                readyInfo.compute(sg.getTile(), (k, v) -> (v == null ? fen : Math.max(v, fen)));
                            }
                        }
                    } else { //顺子听牌
                        int fan = 0;
                        for (AIResponse.SplitGroup sg : group) {
                            if (sg.getType() == AIResponse.SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG) {
                                int key = sg.getTile();
                                int keyRank = key % 9;
                                if (keyRank > 0) {
                                    int winTile = key - 1;
                                    int tmpFan = fan;
                                    if (tmpHand[winTile] == 3
                                            || pongTile.stream().anyMatch(e -> e==winTile)) {
                                        tmpFan++;
                                    }
                                    tmpFan = Math.min(3, tmpFan);
                                    int fen = 1 << tmpFan;
                                    readyInfo.compute(winTile, (k, v) -> (v == null ? fen : Math.max(v, fen)));
                                }
                                if (keyRank <= 6) {
                                    int winTile = key + 2;
                                    int tmpFan = fan;
                                    if (tmpHand[winTile] == 3
                                            || pongTile.stream().anyMatch(e -> e==winTile)) {
                                        tmpFan++;
                                    }
                                    tmpFan = Math.min(3, tmpFan);
                                    int fen = 1 << tmpFan;
                                    readyInfo.compute(winTile, (k, v) -> (v == null ? fen : Math.max(v, fen)));
                                }
                            } else if (sg.getType() == AIResponse.SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG) {
                                int winTile = sg.getTile() + 1;
                                int tmpFan = fan;
                                if (tmpHand[winTile] == 3
                                        || pongTile.stream().anyMatch(e -> e==winTile)) {
                                    tmpFan++;
                                }
                                tmpFan = Math.min(3, tmpFan);
                                int fen = 1 << tmpFan;
                                readyInfo.compute(winTile, (k, v) -> (v == null ? fen : Math.max(v, fen)));
                            }
                        }
                    }
                }
                readyInfos.put(entry.getKey(), readyInfo);
            }
            int bestTile = -1;
            int bestScore = -1;
            for (Map.Entry<Integer, Map<Integer, Integer>> entry : readyInfos.entrySet()) {
                int score = 0;
                for (Map.Entry<Integer, Integer> info : entry.getValue().entrySet()) {
                    score += remainNum[info.getKey()] * info.getValue();
                }
                if (bestTile < 0 || score > bestScore) {
                    bestTile = entry.getKey();
                    bestScore = score;
                } else if (score == bestScore) {
                    bestTile = new Random().nextBoolean() ? bestTile : entry.getKey();
                }
            }
            best = bestTile;
        } else { //未上听则简单选牌
            for (Map.Entry<Integer, List<AIResponse.SplitGroupsHelp>> entry : splitRet.entrySet()) {
                int tile = entry.getKey();
                if (best == -1 || compareDiscardTile(hand, tile, best, mainSuit, remainNum) > 0) {
                    best = tile;
                } else if (compareDiscardTile(hand, tile, best, mainSuit, remainNum) == 0) {
                    best = new Random().nextBoolean() ? best : tile;
                }
            }
        }
        return best;
    }

    private int compareSuit(int[] s0, int[] s1) {
        int num0 = Arrays.stream(s0).sum();
        int num1 = Arrays.stream(s1).sum();
        int numDiff = num0 - num1;
        if (Math.abs(numDiff) >= 2) {
            return numDiff;
        }
        if (Math.abs(numDiff) == 1) {
            int kongNum0 = (int) Arrays.stream(s0).filter(e -> e == 4).count();
            int kongNum1 = (int) Arrays.stream(s1).filter(e -> e == 4).count();
            int kongNumDiff = kongNum0 - kongNum1;
            if (kongNumDiff != 0) {
                return kongNumDiff;
            }
            int pongNum0 = (int) Arrays.stream(s0).filter(e -> e == 3).count();
            int pongNum1 = (int) Arrays.stream(s1).filter(e -> e == 3).count();
            int pongNumDiff = pongNum0 - pongNum1;
            if (pongNumDiff != 0) {
                return pongNumDiff;
            }
            return numDiff;
        }
        int kongNum0 = (int) Arrays.stream(s0).filter(e -> e == 4).count();
        int kongNum1 = (int) Arrays.stream(s1).filter(e -> e == 4).count();
        int kongNumDiff = kongNum0 - kongNum1;
        if (kongNumDiff != 0) {
            return kongNumDiff;
        }
        int pongNum0 = (int) Arrays.stream(s0).filter(e -> e == 3).count();
        int pongNum1 = (int) Arrays.stream(s1).filter(e -> e == 3).count();
        int pongNumDiff = pongNum0 - pongNum1;
        if (pongNumDiff != 0) {
            return pongNumDiff;
        }
        int pairNum0 = (int) Arrays.stream(s0).filter(e -> e == 2).count();
        int pairNum1 = (int) Arrays.stream(s1).filter(e -> e == 2).count();
        int pairNumDiff = pairNum0 - pairNum1;
        if (pairNumDiff != 0) {
            return pongNumDiff;
        }
        return 0;
    }

    public int getMainSuit(int[] tileArr, List<Group> groups) {
        int mainSuit = -1;
        if (!groups.isEmpty()) {
            int mainSuitNum = 0;
            for (Group group : groups) {
                int s = group.getCards().get(0)/ 9;
                if (mainSuit < 0 || mainSuit == s) {
                    mainSuit = s;
                    mainSuitNum += 3;
                } else {
                    return -1;
                }
            }
            if (groups.size() >= 3) {
                return mainSuit;
            }
            if (mainSuit >= 0) {
                int[] tmp = Arrays.copyOfRange(tileArr, 9 * mainSuit, 9 * (mainSuit + 1));
                mainSuitNum += (Arrays.stream(tmp).sum());
                if (mainSuitNum >= 9) {
                    return mainSuit;
                }
            }
        } else {
            for (int i = 0; i < 3; i++) {
                int[] tmp = Arrays.copyOfRange(tileArr, 9 * i, 9 * (i + 1));
                int num = Arrays.stream(tmp).sum();
                if (num >= 9) {
                    return i;
                }
            }
        }
        return -1;
    }

    private int compareDiscardTile(int[] tileArray, int t0, int t1, int mainSuit, int[] remainNum) {
        if (mainSuit >= 0) {
            int s0 = t0 / 9;
            int s1 = t1 / 9;
            if (s0 != s1) {
                return s0 == mainSuit ? 1 : -1;
            }
        }
        int numDiff = tileArray[t0] - tileArray[t1];
        if (numDiff != 0) {
            return numDiff;
        }
        int n0 = getNearbyNum(t0, tileArray);
        int n1 = getNearbyNum(t1, tileArray);
        int nDiff = n0 - n1;
        if (nDiff != 0) {
            return nDiff;
        }
        return 0;
    }

    private static int getNearbyNum(int tile, int[] tileArray) {
        if(tile<27) {
            int r = tile % 9;
            int nb0 = (r == 0 ? 0 : tileArray[tile - 1]) + (r == 8 ? 0 : tileArray[tile + 1]);
            int nb1 = (r <= 1 ? 0 : tileArray[tile - 2]) + (r >= 7 ? 0 : tileArray[tile + 2]);
            int nb = nb0 * 2 + nb1;
            return nb;
        }else{
            return 0;
        }
    }

}
