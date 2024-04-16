package com.wzy.game.server.ai;

import com.wzy.game.server.base.Hand;
import com.wzy.game.server.base.Meld;
import com.wzy.game.server.base.MeldType;
import com.wzy.game.server.base.Tile;
import com.wzy.game.server.caculator.HuResultInterface;
import com.wzy.game.server.caculator.guangdong.GuangdongCheckParam;
import com.wzy.game.server.caculator.guangdong.GuangdongHuResult;
import com.wzy.game.server.caculator.guangdong.GuangdongJudge;
import com.wzy.game.server.model.FanInfo;
import com.wzy.game.server.model.HuFanInfo;
import com.wzy.game.server.util.CodeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.wzy.game.server.ai.ParamMode.*;
import static com.wzy.game.server.caculator.guangdong.GuangdongConstant.*;
import static com.wzy.game.server.constant.ConstVar.*;
import static com.wzy.game.server.util.CodeUtil.*;
import static com.wzy.game.server.util.FanCalculator.*;

public class AIResponse implements AIResponseInterface {

    private static final Logger logger = LoggerFactory.getLogger(AIResponse.class);
    public static final int TILE_TYPE_COUNT = 34;   //牌种类数，共16种1
    public static final int HANDS_COUNT = 14;       //最大手牌数
    public static final int MAX_VALUE = 20;         //打分体系中价值

    // 后期构建 AI 工厂，用来统一管理不同风格，不同级别的A
    // 现在保留单例模式
    // 本类AI为逻辑AI
    private static AIResponse instance = new AIResponse();

    private AIResponse() {
    }

    public static AIResponse getInstance() {
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
            case ACTION_DRAW: {//自己抓牌
                logicDrawTile(act);
            }
            break;
            case ACTION_DISCARD: {//别人打牌
                logicDiscardTile(act);
            }
            break;
            case ACTION_BUGANG: {//别人补杠
                logicAfterBuGangTile(act);
            }
            break;
            case ACTION_CHOW: {//吃
                logicAfterChowTile(act);
            }
            break;
            case ACTION_PONG: {//碰
                logicAfterPongTile(act);
            }
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
    private static void logicDrawTile(ParamMode act) {
        if (act.getLastActionSeat() == act.getMySeat() && act.getLastActionType() == ACTION_DRAW) {//自己抓牌
            GameAction ret = act.getRetAction();//准备部分返回数据
            ret.getUseChowOrPengOrGangTiles().clear();
            int winMode = -1;
            if (act.getLastActionType() == ACTION_DRAW) {
                winMode = WIN_MODE_ZIMO;
            }
            //确定双方座位号
            int mySeat = act.getMySeat();
            int dealerPos = (Math.abs(act.getPrevalent() - act.getSeat()) + mySeat) % 4;

            //解析双方手牌和余牌信息
            int[][] hands = parseHands(act);
            int[] remains = parseRemains(act);
            int remainsLen = act.getRemainsLength();//这个更新为内部计算了

            //上一次的牌
            int lastTile = act.getLastActionTile();
            int hunNum = act.getHunNum();
            boolean lastTileHun = false;
            if (lastTile > 33) {
                lastTile = act.getMyHands().get(0);
                lastTileHun = true;
            }
            int handLen = act.getMyHands().size() + hunNum;
            int[] hand = hands[mySeat].clone();
            if (handLen % 3 == 2) {
                if (!lastTileHun) {
                    hands[mySeat][lastTile]--;
                    hand[lastTile]--;
                }
            }
            int hun = (act.getHun());
            if (hun >= 0 && hun < 34) {
                hunNum = hands[mySeat][hun];
            }
            List<SplitGroupsHelp> myResult = new ArrayList<>();
            splitHandsWithHun(hands[mySeat], myResult, hun, hunNum);
            int myWinDitance = getMinWinDistance(myResult);
            if (lastTileHun) {
                myWinDitance += 1;
            }
            Set<Integer> winTiles = new HashSet<>();
            if (myWinDitance <= 1) {//之前听牌
                filter(myResult, myWinDitance);
                findTingSetHun(winTiles, hands[mySeat], hun, myWinDitance, hunNum);
                if (!winTiles.contains(hun)) {
                    winTiles.add(hun);
                }
            }

            if (handLen % 3 == 2) {
                if (!lastTileHun) {
                    hands[mySeat][lastTile]++;
                }
            }
            if (hun >= 0 && hun < 34 && hands[mySeat][hun] == 4) {
                for (int i = 0; i < TILE_STRING.length; i++) {
                    winTiles.add(i);
                }
            }
            //List<HuFanInfo> fanInfoList = findNearestWithWd(act, hands[mySeat], new ArrayList<>());
            //step 1 判断和牌
            if (winTiles.contains(lastTile)) {
                if (act.getIsTing()[mySeat]) {
                    ret.setActionType(ACTION_WIN);
                    ret.setCurTile(act.getLastActionTile());
                    return;
                } else {//自己没有报听
                    if (false) {
                        ret.setActionType(ACTION_DISCARD);
                        ret.setCurTile(act.getLastActionTile());
                        return;
                    } else {
                        //找到听牌最多的
                        int winCount = 0;
                        List<Integer> wint = new ArrayList<>();//被打掉的牌
                        String tileLast = TILE_STRING_FAN[lastTile];
                        if (lastTileHun) {
                            tileLast = TILE_STRING_FAN[34];
                        }
                        double scoreNow = countWinNowScore(getHandStr(hand, hunNum), act.getSets(), winMode, tileLast);
                        double scoreAfter = 0.00;
                        double oppoScore = 0.00;
                        boolean oppoHandsSeen = false;
                        int tmpOppoWinCount = 0;
                        Set<Integer> tmpOppoWinTiles = new HashSet<>();
                        for (int i = 0; i < TILE_TYPE_COUNT; i++) {
                            if (hands[mySeat][i] > 0) {
                                Set<Integer> tmpWinTiles = new HashSet<>();
                                hands[mySeat][i]--; //暂时去掉一张
                                List<SplitGroupsHelp> tmpResult = new ArrayList<>();
                                splitHands(hands[mySeat], tmpResult);
                                int tmpWinDitance = getMinWinDistance(tmpResult);
                                filter(tmpResult, tmpWinDitance);
                                findTingSetHun(tmpWinTiles, hands[mySeat], hun, tmpWinDitance, hunNum);
                                int tmpwc = countWinCount(tmpWinTiles, remains);

                                if (tmpWinDitance == 1) {
                                    double scoreTmp = 0;//countWinScore(tmpWinTiles, remains, hands[mySeat], act.getMyGroups(), act.getAnGangNum(), 0);
                                    boolean furiten = false;
                                    scoreTmp = scoreTmp * tmpwc / (tmpwc + tmpOppoWinCount);
                                    if (oppoScore > 0) {
                                        scoreTmp = scoreTmp - oppoScore * tmpOppoWinCount / (tmpwc + tmpOppoWinCount);
                                    }
                                    if (scoreAfter < scoreTmp) {
                                        scoreAfter = scoreTmp;
                                        winCount = tmpwc;
                                        wint.clear();
                                        wint.add(i);
                                    } else if (scoreAfter == scoreTmp) {
                                        wint.add(i);

                                    }

                                } else {
                                    if (tmpwc > winCount) {
                                        winCount = tmpwc;
                                        wint.clear();
                                        wint.add(i);
                                    } else if (tmpwc == winCount) {
                                        wint.add(i);
                                    }
                                }
                                hands[mySeat][i]++; //把去掉的放回来
                            }
                        }
                        if (act.getLevel() <= LEVEL2) {
                            if (ret.getLegalAction().contains(ACTION_WIN) && scoreNow >= 2) {
                                ret.setActionType(ACTION_WIN);
                                ret.setCurTile(act.getLastActionTile());
                                ret.setPoint((int) scoreNow);
                                return;
                            }
                        }
                        // TODO: 2022/9/23  增加出牌选择
                        boolean buGangOrNot = false;
                        int tile = wint.get(0);
                        if (ret.getLegalAction().contains(ParamMode.ACTION_BUGANG)) {
                            for (Group sge : act.getMyGroups()) {
                                if (sge.isKe()) {
                                    if (sge.getCards().get(0) == tile) {
                                        buGangOrNot = true;
                                        tile = sge.getCards().get(0);
                                    }
                                }
                            }
                        }
//                        }
                        if (buGangOrNot) {
                            ret.setActionType(ACTION_BUGANG);
                            ret.setCurTile(tile);
                            ret.getUseChowOrPengOrGangTiles().add(tile);
                            ret.getUseChowOrPengOrGangTiles().add(tile);
                            ret.getUseChowOrPengOrGangTiles().add(tile);
                            return;
                        }
                        ret.setCurTile(tile);
                        ret.setActionType(ACTION_DISCARD);
                    }
                }
            } else {//不能和牌
                //已经报听，且没和牌，直接打该牌
                if (act.getIsTing()[mySeat]) {
                    ret.setActionType(ACTION_DISCARD);
                    ret.setCurTile(act.getLastActionTile());
                } else {
                    //拆分并计算和牌距离
                    splitHands(hands[mySeat], myResult);
                    myWinDitance = getMinWinDistance(myResult);
                    List<Integer> valueless = new ArrayList<>();
                    if (myWinDitance == 1) {
                        //找到听牌最多的
                        int winCount = 0;
                        double scoreOld = 0.00;
                        List<Integer> wint = new ArrayList<>();//被打掉的牌
                        for (int i = 0; i < TILE_TYPE_COUNT; i++) {
                            if (hands[mySeat][i] > 0) {
                                Set<Integer> tmpWinTiles = new HashSet<>();
                                hands[mySeat][i]--; //暂时去掉一张
                                List<SplitGroupsHelp> tmpResult = new ArrayList<>();
                                splitHands(hands[mySeat], tmpResult);
                                int tmpWinDitance = getMinWinDistance(tmpResult);
                                if (tmpWinDitance == 1) {
                                    filter(tmpResult, tmpWinDitance);
                                    if (hunNum == 0) {
                                        findTingSet(tmpWinTiles, tmpResult, hunNum);
                                    }
                                    int tmpwc = countWinCount(tmpWinTiles, remains);
                                    double score = 0;
                                    if (score > scoreOld) {
                                        scoreOld = score;
                                        wint.clear();
                                        wint.add(i);
                                    } else if (score == scoreOld) {
                                        if (tmpwc > winCount) {
                                            wint.clear();
                                            wint.add(i);
                                        } else {
                                            wint.add(i);
                                        }
                                    } else {

                                    }
                                }
                                hands[mySeat][i]++; //把去掉的放回来
                            }
                        }
                        boolean isRiichi = false;
                        int tile = lastTile;
                        if (lastTile > 33) {
                            lastTile = act.getMyHands().get(0);
                        }
                        if (wint.size() > 0) {
                            tile = wint.get(0);
                        }
                        int[] myHands = hands[mySeat];
                        myHands[lastTile]++;
                        for (int stone : act.getMyHands()) {
                            if (tile == stone) {
                                ret.setCurTile(stone);
                            }
                        }
                        myHands[lastTile]--;
                        ret.setActionType(ACTION_DISCARD);
                    } else {
                        if (act.getLevel() == LEVEL1) {
                            int dis = EasyGameLogicService.instance.checkDiscard(hands[mySeat], act.getMyGroups(), mySeat, act.getHistory(), remains);
                            ret.setActionType(ACTION_DISCARD);
                            ret.setCurTile(dis);
                        }
                        if (act.getLevel() >= LEVEL2) {
                            int[] handTmp = hands[mySeat].clone();
                            //todo 找番种开始;
                            List<HuFanInfo> fanInfos = findNearest(act, hand);
                            List<HuFanInfo> canBe = new ArrayList<>();
                            int[] honorImportant = new int[7];
                            boolean keepHonor = false;
                            for (int i = 0; i < fanInfos.size(); i++) {
                                HuFanInfo huFanInfo = fanInfos.get(i);
                                int distance = huFanInfo.getDistance();
                                if (distance-hunNum < 4) {
                                    System.out.println(1);
                                    canBe.add(huFanInfo);
                                    for (int j = 0; j < huFanInfo.getKeyTiles().size(); j++) {
                                        honorImportant[huFanInfo.getKeyTiles().get(j)]++;
                                        keepHonor=true;
                                    }
                                }
                            }
                            int suit = getMainSuitWithHonors(handTmp, act.getMyGroups());
                            filter(myResult, myWinDitance + 1);
                            //寻找分值最低的牌
                            List<Integer> tiles = new ArrayList<>();
                            boolean hun1Se = false;
                            if(suit>2){
                                hun1Se=true;
                                suit=suit-3;
                            }
                            if (suit >= 0) {
                                int handSuitCount = 0;
                                for (int i = suit * 9; i < suit * 9 + 9; i++) {
                                    handTmp[i] = 0;
                                }
                                tiles = findLowProfit(myResult, myWinDitance, handTmp, remains, remainsLen);

                            }
                            if (tiles.size() == 0) {
                                tiles = findLowProfit(myResult, myWinDitance, hands[mySeat], remains, remainsLen);
                            }
                            //检查是否可以补明杠
                            int[][] safeTiles = act.getSafeTiles();
                            int[] safeTilesThis = new int[34];
                            for (int i = 0; i < safeTiles.length; i++) {
                            }
                            int t = -1;
                            int safeScore = 0;
                            for (int i = 0; i < safeTilesThis.length; i++) {
                                if (safeTilesThis[i] > 0 && safeTilesThis[i] > safeScore) {
                                    t = i;
                                    safeScore = safeTilesThis[i];
                                }
                            }
                            if(act.getLevel()>=LEVEL3){
                                if(!keepHonor&&!hun1Se){
                                    for (int i = 27; i < 34; i++) {
                                        if(tiles.contains(i)){
                                            t=i;
                                        }
                                    }
                                }else{
                                    for (int i = 0; i < 27; i++) {
                                        if(tiles.contains(i)){
                                            t=i;
                                        }
                                    }
                                }
                            }
                            if (t == -1) {
                                t = tiles.get(new Random().nextInt(tiles.size()));
                            }
                            ret.setActionType(ACTION_DISCARD);
                            ret.setCurTile(t);
                        }
                    }
                    for (Group sge : act.getMyGroups()) {
                        if (sge.isKe() && ret.getLegalAction().contains(ParamMode.ACTION_BUGANG)) {
                            if (valueless.contains(sge.getCards().get(0)) || ret.getCurTile() == sge.getCards().get(0)) {
                                ret.setActionType(ACTION_BUGANG);
                                ret.setCurTile(sge.getCards().get(0));
                                ret.getUseChowOrPengOrGangTiles().add(sge.getCards().get(0));
                                ret.getUseChowOrPengOrGangTiles().add(sge.getCards().get(1));
                                ret.getUseChowOrPengOrGangTiles().add(sge.getCards().get(2));
                                return;
                            }
                        }
                    }
                    //检查暗杠
                    if (ret.getLegalAction().contains(ACTION_ANGANG)) {
                        boolean hk = hiddenKong(hands[mySeat], myResult, myWinDitance, remains, remainsLen, ret);
                        if (hk && ret.getLegalAction().contains(ACTION_ANGANG)) {
                            return;
                        }
                    }
                    int[] myHands = hands[mySeat];
                    //随机选一张打
//                    Random random = new Random();
//                    int t = valueless.get(random.nextInt(valueless.size()));
                    return;
                }
            }
        } else {//不处理,添加日志
        }
    }


    /**
     * 广东麻将算番
     **/

    public static int countWinNowScore(String hStr, List<String> sets, int winMode, String winTiles) {
        int score = 0;
        Hand hand = Hand.parseString(hStr);
        winTiles = winTiles.replaceAll("s", "t");
        List<Meld> melds = new ArrayList<>();
        if (sets != null) {
            for (int i = 0; i < sets.size(); i++) {
                String set = sets.get(i);
                MeldType mt = getGzType(set);
                if (mt != null) {
                    melds.add(new Meld(Tile.parseTiles(set), mt, 0));
                }
            }
        }
        GuangdongCheckParam gzParam = new GuangdongCheckParam(hand.getTiles(), Tile.parseString(winTiles), melds,
                winMode);
        GuangdongJudge gzJudge = new GuangdongJudge();
        HuResultInterface gzResult = gzJudge.checkWin(gzParam);
        GuangdongHuResult guangdongHuResult = (GuangdongHuResult) gzJudge.checkWin(gzParam);
        return guangdongHuResult.getMaxFans();
    }

    public static GuangdongHuResult countWinNowResult(String hStr, List<String> sets, int winMode, String winTiles) {
        int score = 0;
        Hand hand = Hand.parseString(hStr);
        winTiles = winTiles.replaceAll("s", "t");
        List<Meld> melds = new ArrayList<>();
        if (sets != null) {
            for (int i = 0; i < sets.size(); i++) {
                String set = sets.get(i);
                MeldType mt = getGzType(set);
                if (mt != null) {
                    melds.add(new Meld(Tile.parseTiles(set), mt, 0));
                }
            }
        }
        GuangdongCheckParam gzParam = new GuangdongCheckParam(hand.getTiles(), Tile.parseString(winTiles), melds,
                winMode);
        GuangdongJudge gzJudge = new GuangdongJudge();
        HuResultInterface gzResult = gzJudge.checkWin(gzParam);
        GuangdongHuResult guangdongHuResult = (GuangdongHuResult) gzJudge.checkWin(gzParam);
        return guangdongHuResult;
    }

    public static boolean hiddenKong(int hands[], List<SplitGroupsHelp> groups, int myDistance
            , int remain[], int remainCount, GameAction ret) {
        Map<Double, Integer> tm = new HashMap<Double, Integer>();
        for (int i = TILE_TYPE_COUNT - 1; i >= 0; i--) {
            if (hands[i] == 4) {
                //从所有牌型中找出包含该刻的牌型
                List<SplitGroupsHelp> sr = new ArrayList<SplitGroupsHelp>();
                for (SplitGroupsHelp it : groups) {
                    boolean use = false;
                    for (int k = 0; k < it.getCurSplitGroup().size(); k++) {
                        if (it.getCurSplitGroup().get(k).getType() == SplitGroupType.SPLIT_GROUP_TYPE_KE
                                && it.getCurSplitGroup().get(k).getTile() == i) {
                            use = true;
                            break;
                        }
                    }
                    if (use) {
                        sr.add(it.clone());
                    }
                }
                //判断是否是备选暗杠
                if (sr.size() > 0) {
                    List<Integer> valueless = new ArrayList<Integer>();
                    double mv = findMinValue(sr, myDistance, valueless, false, remain, remainCount);
                    if (valueless.contains(Integer.valueOf(i))) {
                        tm.put(mv, i);
                    }
                }
            }
        }
        //存在暗杠备选牌，则暗杠
        if (tm.size() > 0) {
            //map<double, int>::iterator it = tm.begin();
            Iterator<Map.Entry<Double, Integer>> iter = tm.entrySet().iterator();
            Map.Entry<Double, Integer> it = iter.next();
            int tile = it.getValue();
            ret.setCurTile(tile);
            ret.setActionType(ACTION_ANGANG);
            return true;
        }
        return false;
    }

    public static int countWinCount(Set<Integer> winTiles, int remain[]) {
        int winCount = 0;
        for (int tile : winTiles) {
            winCount += remain[tile];
        }
        return winCount;
    }

    public static int[] parseRemains(ParamMode act) {
        int[] remains = new int[34];
        Arrays.fill(remains, 4);
        int remainLen = 64;
        List<Integer> hands = act.getMyHands();
        for (int tile : act.getMyHands()) {
            int t = tile;
            if (t > 33) {
                t = (t - 34) * 9 + 4;
            }
            remains[t]--;
            remainLen--;
        }
        for (List<Integer> tiles : act.getOppoHands()) {
            if (tiles.size() >= 0) {
                for (int tile : tiles) {
                    remains[tile]--;
                    remainLen--;
                }
            }

        }
        for (int tile : act.getMyDiscards()) {
            int t = tile;
            if (t > 33) {
                t = (t - 34) * 9 + 4;
            }
            remains[tile]--;
            remainLen--;
        }
        for (List<Integer> tiles : act.getOppoDiscards()) {
            for (int tile :
                    tiles) {
                int t = tile;
                if (t > 33) {
                    t = (t - 34) * 9 + 4;
                }
                remains[t]--;
                remainLen--;
            }
        }
        for (Group groups : act.getMyGroups()) {
            for (int tile : groups.getCards()) {
                int t = tile;
                if (t > 33) {
                    t = (t - 34) * 9 + 4;
                }
                remains[t]--;
                remainLen--;
            }
            if (groups.isKe()) {
                int t = groups.getCards().get(0);
                if (t > 33) {
                    t = (t - 34) * 9 + 4;
                }
                remains[t]--;
                remainLen++;
            } else {
                remains[groups.getFrom()]--;
                remainLen++;
            }
        }
        for (List<Group> groupList :
                act.getOppoGroups()) {
            for (Group groups : groupList) {
                remainLen = remainLen + groups.getCards().size();
                for (int tile : groups.getCards()) {
                    if (tile > 0) {
                        int t = tile;
                        if (t > 33) {
                            t = (t - 34) * 9 + 4;
                        }
                        remains[t]--;
                    }
                }
                if (groups.isKe()) {
                    int t = groups.getCards().get(0);
                    if (t > 33) {
                        t = (t - 34) * 9 + 4;
                    }
                    remains[t]--;
                } else {
                    remains[groups.getFrom()]++;
                }
            }
        }
        return remains;
    }

    public static int[][] parseHands(ParamMode act) {
        int[][] hands = new int[4][34];
        int mySeat = act.getMySeat();
        int oppoSeat = mySeat == 0 ? 1 : 0;
        for (int tile : act.getMyHands()) {
            if (tile > 33) {
                tile = (tile - 34) * 9 + 4;
            }
            hands[mySeat][tile]++;
        }
//        for (int i = 0; i < act.getOppoHands().size(); i++) {
//            for (int tile : act.getOppoHands()) {
//                if (tile >= 0) {
//                    hands[oppoSeat][tile]++;
//                }
//            }
//        }
        return hands;
    }

    /**
     * 吃碰杠和逻辑
     *
     * @param act
     */
    private static void logicDiscardTile(ParamMode act) {
        if (act.getLastActionSeat() != act.getMySeat()
                && act.getLastActionType() == ACTION_DISCARD && act.getLastActionTile() < 34) {//对手打牌
            GameAction ret = act.getRetAction();
            ret.setCurTile(act.getLastActionTile());
            ret.getUseChowOrPengOrGangTiles().clear();
            ret.setActionType(ParamMode.ACTION_PASS);
            ret.setActionSecondTime(2);
            int mySeat = act.getMySeat();
            int oppoSeat = mySeat == 0 ? 1 : 0;
            int hun = act.getHun();
            int[][] hands = parseHands(act);
            List<Integer> legalAction = new ArrayList<>();
            int lastSeat = act.getLastActionSeat();
            //统计余牌信息
            int[] remains = parseRemains(act);
            int remainsLen = act.getRemainsLength();
            int lastTile = act.getLastActionTile();
            if (lastTile > 33) {
                ret.setActionType(ACTION_PASS);
                ret.setCurTile(act.getLastActionTile());
                ret.getUseChowOrPengOrGangTiles().add(lastTile);
                ret.getUseChowOrPengOrGangTiles().add(lastTile);
                return;
            }

            int hunNum = 0;
            if (hun >= 0 && hun < 34) {
                hunNum = hands[mySeat][hun];
            } else {
                hunNum = act.getHunNum();
            }
            if (act.getLevel() == LEVEL1) {
                //LEVEL1,吃碰杠都要
                if (hands[act.getMySeat()][lastTile] == 3 && ret.getLegalAction().contains(ParamMode.ACTION_MINGGANG)) {
                    ret.setActionType(ACTION_MINGGANG);
                    ret.setCurTile(act.getLastActionTile());
                    ret.getUseChowOrPengOrGangTiles().add(lastTile);
                    ret.getUseChowOrPengOrGangTiles().add(lastTile);
                    ret.getUseChowOrPengOrGangTiles().add(lastTile);
                    return;
                } else if (hands[act.getMySeat()][lastTile] == 2 && !act.getIsTing()[mySeat] && ret.getLegalAction().contains(ParamMode.ACTION_PONG)) {
//                    ret.setActionType(ACTION_PONG);
                    ret.setActionType(ACTION_PASS);
                    ret.setCurTile(act.getLastActionTile());
                    ret.getUseChowOrPengOrGangTiles().add(lastTile);
                    ret.getUseChowOrPengOrGangTiles().add(lastTile);
                    return;
                } else {
                    act.getRetAction().setActionType(ParamMode.ACTION_PASS);
                    act.getRetAction().setCurTile(act.getLastActionTile());
                    return;
                }
            } else {
                //得到拆分
                int winMode = WIN_MODE_DIANPAO;
                if (act.getRemainsLength() == 0) {
                    winMode = winMode | WIN_MODE_HAIDI;
                }
                double scoreNow = 0;
                List<SplitGroupsHelp> myResult = new ArrayList<>();
                splitHandsWithHun(hands[mySeat], myResult, hun, hunNum);
                int myWinDitance = getMinWinDistance(myResult);
                filter(myResult, myWinDitance);
                Set<Integer> winTiles = new HashSet<>();
                if (myWinDitance <= 1) {//之前听牌
                    findTingSetHun(winTiles, hands[mySeat], hun, myWinDitance, hunNum);
                }
                if (winTiles.contains(lastTile) && ret.getLegalAction().contains(ACTION_WIN)) {//可以和
                    scoreNow = countWinNowScore(getHandStr(hands[mySeat], hunNum), act.getSets(), winMode, TILE_STRING_FAN[act.getLastActionTile()]);

                    if (scoreNow > 8) {
                        ret.setActionType(ACTION_WIN);
                        ret.setCurTile(act.getLastActionTile());
                        return;
                    }
                }
                int suit = getMainSuitWithHonors(hands[mySeat], act.getMyGroups());
                moveCheck(act, ret, mySeat, hands, remains, remainsLen, lastTile, (List<SplitGroupsHelp>) myResult, myWinDitance, suit);
            }
        }
    }


    /**
     * 中阶判断吃碰杠
     *
     * @param act
     * @param ret
     * @param mySeat
     * @param hands
     * @param remains
     * @param remainsLen
     * @param lastTile
     * @param myResult
     * @param myWinDistance
     */
    private static void moveCheck(ParamMode act, GameAction ret, int mySeat, int[][] hands, int[] remains,
                                  int remainsLen, int lastTile, List<SplitGroupsHelp> myResult, int myWinDistance, int suit) {
        if (!act.getIsTing()[mySeat]) {//未报听，则考虑吃碰杠
            int[] score = {0, 0, 0, 0}; //分别表示碰，左吃，中吃，右吃
            SplitGroupsHelp mgs[] = new SplitGroupsHelp[4];
            if (hands[mySeat][lastTile] >= 2) {
                score[0] = 1; //碰优先级3
            }
            if (score[0] != 0) {
                for (SplitGroupsHelp it : myResult) {
                    int s = it.getWinDistance() == myWinDistance ? 100 : 10;

                    if (!it.isSevenPair()) {
                        for (SplitGroup sg : it.getCurSplitGroup()) {
                            if (score[0] != 0 && it.getCurPairCount() > 1
                                    && sg.getType() == SplitGroupType.SPLIT_GROUP_TYPE_PAIR
                                    && sg.getTile() == lastTile) {
                                mgs[0] = it.clone();
                                score[0] += s;
                            }
                        }
                    }
                }
                int maxi = 0;
                int level = act.getLevel();
                int hun = (act.getHun());

                int hunNum = 0;
                if (hun >= 0 && hun < 34) {
                    hunNum = hands[mySeat][hun];
                }
                for (int i = 1; i < 4; i++) {
                    int[] handsCopy = hands[mySeat].clone();
                    List<Group> groups = act.getMyGroups();
                    List<Integer> handsList = new ArrayList<>();
                    handsList.add(lastTile);
                    switch (maxi) {
                        case 0:
                            handsCopy[lastTile] -= 2;
                            handsList.add(lastTile);
                            handsList.add(lastTile);
                            break;
                        default:
                            break;
                    }
                    List<SplitGroupsHelp> tmpResult = new ArrayList<SplitGroupsHelp>();
                    splitHandsWithHun(handsCopy, tmpResult, hun, hunNum);
                    int wd = getMinWinDistance(tmpResult);
                    if (wd < myWinDistance) {
                        score[maxi] = 100;
                    }

                }
                if (score[maxi] >= 100) {//可吃碰
                    //copy一份手牌，预演一下
                    //拆分并计算和牌距离
                    boolean pongNot = false;
                    int[] handsCopy = hands[mySeat].clone();
                    List<SplitGroupsHelp> tmpResult = new ArrayList<SplitGroupsHelp>();
                    splitHands(handsCopy, tmpResult);
                    int wd = getMinWinDistance(tmpResult);
                    boolean menQing = true;
                    if (act.getMyGroups().size() != 0) {
                        for (int i = 0; i < act.getMyGroups().size(); i++) {
                            if (act.getMyGroups().get(i).getCards().size() != 5) {
                                menQing = false;
                            }
                        }
                    }
                    if (menQing && wd == 1) {
                        pongNot = false;
                    } else {
                        if (lastTile > 33) {
                            //todo 需要判断是否做清一色
                            pongNot = true;
                        } else {
                            if (lastTile / 9 == suit) {
                                pongNot = true;
                            }
                        }
                    }
                    filter(tmpResult, wd + 1);
                    List<Integer> valueless = new ArrayList<Integer>();
                    findMinValue(tmpResult, wd, valueless, false, remains, remainsLen);
                    if (valueless.size() > 0) {
                        if (valueless.contains(lastTile) && handsCopy[lastTile] == 3) {
                            if (maxi == 0) {//碰啥打啥则改为杠
                                if (ret.getLegalAction().contains(ParamMode.ACTION_MINGGANG)) {
                                    if (pongNot) {
                                        ret.setActionType(ACTION_MINGGANG);
                                        ret.setCurTile(act.getLastActionTile());
                                        for (int stone : act.getMyHands()) {
                                            if (lastTile == stone) {
                                                ret.getUseChowOrPengOrGangTiles().add(stone);
                                            }
                                        }
                                    }
                                }
                            } else {

                            }//吃啥打啥，悲剧……
                        } else {//顺利吃碰，则把预演结果copy过来
                            if (maxi == 0) {
                                if (ret.getLegalAction().contains(ParamMode.ACTION_PONG) && pongNot) {
                                    ret.setActionType(ACTION_PONG);
                                    ret.setCurTile(act.getLastActionTile());
                                    int size = 0;
                                    for (int stone : act.getMyHands()) {
                                        if (lastTile == stone) {
                                            ret.getUseChowOrPengOrGangTiles().add(stone);
                                            size++;
                                        }
                                        if (size == 2) {
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 吃后打牌逻辑
     *
     * @param act
     */
    private static void logicAfterChowTile(ParamMode act) {
        if (act.getLastActionSeat() == act.getMySeat()
                && act.getLastActionType() == ACTION_CHOW) {
            //自己吃牌，假设为抓后打牌,读取规则判断是否可食替，仅两面吃时有食替
            act.setLastActionSeat(act.getMySeat());
            act.setLastActionType(ACTION_DRAW);
            act.setLastActionTile(act.getMyHands().get(0));
            logicDrawTile(act);
        } else {//不处理，添加日志

        }
    }

    /**
     * 碰后打牌逻辑
     *
     * @param act
     */
    private static void logicAfterPongTile(ParamMode act) {
        if (act.getLastActionSeat() == act.getMySeat()
                && act.getLastActionType() == ParamMode.ACTION_PONG) {
            //自己碰牌，假设为抓后打牌,判断是否可食替,不可食替就不可打碰的牌
            act.setLastActionSeat(act.getMySeat());
            act.setLastActionType(ACTION_DRAW);
            act.setLastActionTile(act.getMyHands().get(0));
            logicDrawTile(act);
        } else {//不处理，添加日志

        }
    }


    /**
     * 别人补杠时候，判断是否可以抢杠胡
     *
     * @param act
     */
    private static void logicAfterBuGangTile(ParamMode act) {
        if (act.getLastActionSeat() != act.getMySeat()
                && act.getLastActionType() == ACTION_BUGANG) {//对手补杠
            GameAction ret = act.getRetAction();
            if (ret.getLegalAction().contains(ACTION_WIN)) {//服务器认为可以胡，则胡牌
                int winMode = WIN_MODE_QIANGGANG;
                int score = countWinNowScore(CodeUtil.getHandStrFromList(act.getMyHands(), act.getHunNum()), act.getSets(), winMode, TILE_STRING_FAN[act.getLastActionTile()]);
                if (score > 8) {
                    ret.setActionType(ParamMode.ACTION_WIN);
                } else {
                    ret.setActionType(ParamMode.ACTION_PASS);
                }
            } else {//否则pass
                ret.setActionType(ParamMode.ACTION_PASS);
            }
            ret.setCurTile(act.getLastActionTile());
            ret.getUseChowOrPengOrGangTiles().clear();
            ret.setActionSecondTime(2);
        } else {//不处理，添加日志

        }
    }

    public static void findTingSetHun(Set<Integer> winTiles, int[] hand, int hun, int wd, int hunNum) {
        if (wd < 1 && hunNum > 0) {
            for (int i = 0; i < TILE_TYPE_COUNT; i++) {
                winTiles.add(i);
            }
            return;
        }
        for (int i = 0; i < TILE_TYPE_COUNT; i++) {
            hand[i]++;
            List<SplitGroupsHelp> myResult = new ArrayList<>();
            splitHandsWithHun(hand, myResult, hun, hunNum);
            int wdNew = getMinWinDistance(myResult);
            if (wdNew < 1) {
                winTiles.add(i);
            }
            hand[i]--;
        }
    }

    public static void findTingSet(Set<Integer> winTiles, List<SplitGroupsHelp> mgs, int hunNum) {
        winTiles.clear();

        for (SplitGroupsHelp sg : mgs) {
            if (sg.getWinDistance() == 1) {
                int hunNumNew = hunNum;
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
                            && sg.getCurPairCount() == (2 + hunNum)) {
                        winTiles.add(g.getTile());
                    }
                }
            }
            if (sg.getWinDistance() < 1) {
                for (int i = 0; i < TILE_TYPE_COUNT; i++) {
                    if (!winTiles.contains(i)) winTiles.add(i);
                }
            }
        }
    }

    public static int getMinWinDistance(List<SplitGroupsHelp> groups) {
        int distance = 8;
        for (SplitGroupsHelp group : groups) {
            if (distance > group.getWinDistance()) {
                distance = group.getWinDistance();
            }
        }
        return distance;
    }

    public static void filter(List<SplitGroupsHelp> result, int winDistance) {
        Iterator<SplitGroupsHelp> iter = result.iterator();
        while (iter.hasNext()) {
            SplitGroupsHelp it = iter.next();
            if (it.getWinDistance() > winDistance) {
                iter.remove();
            }
        }
    }

    //begin 下面是打牌拆分部分的逻辑
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

    public static class SplitGroup {
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
    }

    public static class SplitGroupsHelp {
        private int initLen;        //初始手牌长度
        private int leftTilesNum[]; //未拆分手牌
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
            for (int i = 0; i < 16; i++) {
                buf.append(this.tileValues[i]).append(",");
            }
            //buf.append(" Num=").append(leftTilesNum);
            return buf.toString();
        }
    }

    /**
     * @param hands
     * @param result
     */
    public static void splitHands(int hands[], List<SplitGroupsHelp> result) {
        result.clear();
        SplitGroupsHelp initGroup = new SplitGroupsHelp();
        int len = 0;
        for (int i = 0; i < TILE_TYPE_COUNT; i++) {
            initGroup.addLeftTilesNum(i, hands[i]);
            len = len + hands[i];
        }
        initGroup.setInitLen(len);

        if (initGroup.getLeftTileCount() >= HANDS_COUNT - 1) {
            SplitGroupsHelp sp = findSevenPair(hands);
            result.add(sp);
        }
        split(initGroup, result);
        Iterator<SplitGroupsHelp> iterator = result.iterator();
        // 删除 不正常的组牌方式， 如对子拆成两个单，4刻拆成两对
        while (iterator.hasNext()) {
            SplitGroupsHelp gruops = iterator.next();
            Set<SplitGroup> singleAndPair = new HashSet<>();
            boolean hasSame = false;
            for (SplitGroup s : gruops.curSplitGroup) {
                if (s.getType() == SplitGroupType.SPLIT_GROUP_TYPE_SINGLE || (s.getType() == SplitGroupType.SPLIT_GROUP_TYPE_PAIR && !gruops.isSevenPair)) {
                    if (singleAndPair.contains(s)) {
                        hasSame = true;
                        break;
                    } else {
                        singleAndPair.add(s);
                    }
                }
            }
            if (hasSame) iterator.remove();
        }
    }

    /**
     * 查找七对拆分
     *
     * @param hands
     * @return
     */
    private static SplitGroupsHelp findSevenPair(int hands[]) {
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

    /**
     * 3N+2 格式拆分
     *
     * @param curSplitGroup
     * @param result
     */
    private static void split(SplitGroupsHelp curSplitGroup, List<SplitGroupsHelp> result) {
        if (curSplitGroup.getLeftTileCount() == 0) {//所有拆分完成
            result.add(countWindDistance(curSplitGroup));//记录结果并返回
            return;
        }
        for (int i = 0; i < TILE_TYPE_COUNT; i++) {
            if (curSplitGroup.getLeftTileCount() == 0) {//最后一张单牌移除完成，停止后续查找
                return;
            }
            if (curSplitGroup.getLeftTilesNum()[i] > 0) {
                if (curSplitGroup.getLeftTilesNum()[i] >= 3) {// 刻
                    SplitGroupsHelp nextDeepGroups = curSplitGroup.clone();
                    nextDeepGroups.reduceLeftTilesNum(i, 3);
                    nextDeepGroups.addSplitGroup(new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_KE, i));
                    split(nextDeepGroups, result);
                }
                if (i < 27) {
                    if (i % 9 <= 6 && curSplitGroup.getLeftTilesNum()[i + 1] > 0
                            && curSplitGroup.getLeftTilesNum()[i + 2] > 0) {//顺
                        SplitGroupsHelp nextDeepGroups = curSplitGroup.clone();
                        nextDeepGroups.reduceLeftTilesNum(i, 1);
                        nextDeepGroups.reduceLeftTilesNum(i + 1, 1);
                        nextDeepGroups.reduceLeftTilesNum(i + 2, 1);
                        nextDeepGroups.addSplitGroup(new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_SHUN, i));
                        split(nextDeepGroups, result);
                    }
                    if (i % 9 <= 7 && curSplitGroup.getLeftTilesNum()[i + 1] > 0) {//连张
                        SplitGroupsHelp nextDeepGroups = curSplitGroup.clone();
                        nextDeepGroups.reduceLeftTilesNum(i, 1);
                        nextDeepGroups.reduceLeftTilesNum(i + 1, 1);
                        nextDeepGroups.addSplitGroup(new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG, i));
                        split(nextDeepGroups, result);
                    }

                    if (i % 9 <= 6 && curSplitGroup.getLeftTilesNum()[i + 2] > 0) {//隔张
                        SplitGroupsHelp nextDeepGroups = curSplitGroup.clone();
                        nextDeepGroups.reduceLeftTilesNum(i, 1);
                        nextDeepGroups.reduceLeftTilesNum(i + 2, 1);
                        nextDeepGroups.addSplitGroup(new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_KANZHANG, i));
                        split(nextDeepGroups, result);
                    }
                }
                if (curSplitGroup.getLeftTilesNum()[i] >= 2) {//对
                    SplitGroupsHelp nextDeepGroups = curSplitGroup.clone();
                    nextDeepGroups.reduceLeftTilesNum(i, 2);
                    nextDeepGroups.addSplitGroup(new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_PAIR, i));
                    split(nextDeepGroups, result);
                }

                //单张
                curSplitGroup.reduceLeftTilesNum(i, 1);
                curSplitGroup.addSplitGroup(new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_SINGLE, i));
                split(curSplitGroup, result);
            }
        }
    }

    /**
     * 修改为 3N+2 形式的计算上听距离 0=胡牌 1=听牌
     *
     * @param group
     * @return
     */
    private static SplitGroupsHelp countWindDistance(SplitGroupsHelp group) {
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

    /**
     * 计算有混时拆分
     *
     * @param hands
     * @param result
     * @param hun
     * @param hunNum
     */
    public static void splitHandsWithHun(int[] hands, List<SplitGroupsHelp> result, int hun, int hunNum) {
        if (hun >= 0 && hun < 34) {
            hunNum = hands[hun];
            hands[hun] = 0;
        }
        result.clear();
        SplitGroupsHelp initGroup = new SplitGroupsHelp();
        int len = 0;
        for (int i = 0; i < TILE_TYPE_COUNT; i++) {
            if (i != hun)
                initGroup.addLeftTilesNum(i, hands[i]);
            len = len + hands[i];
        }
        initGroup.setInitLen(len + hunNum);

        if (initGroup.getLeftTileCount() >= (HANDS_COUNT - 1 - hunNum)) {
            SplitGroupsHelp sp = findSevenPair(hands);
            sp.setWinDistance(sp.getWinDistance() - hunNum);
            result.add(sp);
        }
        splitHun(initGroup, result, hunNum);
        Iterator<SplitGroupsHelp> iterator = result.iterator();
        // 删除 不正常的组牌方式， 如对子拆成两个单，4刻拆成两对
        while (iterator.hasNext()) {
            SplitGroupsHelp Groups = iterator.next();
            Set<SplitGroup> singleAndPair = new HashSet<>();
            boolean hasSame = false;
            for (SplitGroup s : Groups.curSplitGroup) {
                if (s.getType() == SplitGroupType.SPLIT_GROUP_TYPE_SINGLE || (s.getType() == SplitGroupType.SPLIT_GROUP_TYPE_PAIR && !Groups.isSevenPair)) {
                    if (singleAndPair.contains(s)) {
                        hasSame = true;
                        break;
                    } else {
                        singleAndPair.add(s);
                    }
                }
            }
            if (hasSame) {
                iterator.remove();
            }
        }
        if (hun >= 0 && hun < 34) {
            hands[hun] = hunNum;
        }
    }

    private static int calWinDisWithHun(SplitGroupsHelp group, int hunNum) {
        int pairNum = group.getCurPairCount(), conNum = group.curConnectCount, tileNum = group.getInitLen();
        int dis = tileNum / 3;//tileNum在传入的时候已经计算了混数目
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
        return dis - hunNum;
    }

    /**
     * 修改为 3N+2 形式的计算上听距离 0=胡牌 1=听牌
     *
     * @param group
     * @return
     */
    private static SplitGroupsHelp countWindDistanceWithHun(SplitGroupsHelp group, int hunNum) {
        group.setWinDistance(calWinDisWithHun(group, hunNum));

        return group;
    }

    /**
     * 3N+2 格式拆分
     *
     * @param curSplitGroup
     * @param result
     */
    private static void splitHun(SplitGroupsHelp curSplitGroup, List<SplitGroupsHelp> result, int hunNum) {
        if (curSplitGroup.getLeftTileCount() == 0) {//所有拆分完成
            int i = getMinWinDistance(result);
            countWindDistanceWithHun(curSplitGroup, hunNum);
            if (i <= 1 && curSplitGroup.getWinDistance() <= i) {
                result.add(curSplitGroup);//记录结果并返回
            } else if (curSplitGroup.getWinDistance() <= i + 1) {
                result.add(curSplitGroup);//记录结果并返回
            }
            return;
        }
        for (int i = 0; i < TILE_TYPE_COUNT; i++) {
            if (curSplitGroup.getLeftTileCount() == 0) {//最后一张单牌移除完成，停止后续查找
                return;
            }
            if (curSplitGroup.getLeftTilesNum()[i] > 0) {
                if (i % 9 <= 6 && i <= 26 && i <= 26 && curSplitGroup.getLeftTilesNum()[i + 1] > 0
                        && curSplitGroup.getLeftTilesNum()[i + 2] > 0) {//顺
                    SplitGroupsHelp nextDeepGroups = curSplitGroup.clone();
                    nextDeepGroups.reduceLeftTilesNum(i, 1);
                    nextDeepGroups.reduceLeftTilesNum(i + 1, 1);
                    nextDeepGroups.reduceLeftTilesNum(i + 2, 1);
                    nextDeepGroups.addSplitGroup(new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_SHUN, i));
                    splitHun(nextDeepGroups, result, hunNum);
                }
                if (curSplitGroup.getLeftTilesNum()[i] >= 2) {//对
                    SplitGroupsHelp nextDeepGroups = curSplitGroup.clone();
                    nextDeepGroups.reduceLeftTilesNum(i, 2);
                    nextDeepGroups.addSplitGroup(new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_PAIR, i));
                    splitHun(nextDeepGroups, result, hunNum);
                }
                if (curSplitGroup.getLeftTilesNum()[i] >= 3) {// 刻
                    SplitGroupsHelp nextDeepGroups = curSplitGroup.clone();
                    nextDeepGroups.reduceLeftTilesNum(i, 3);
                    nextDeepGroups.addSplitGroup(new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_KE, i));
                    splitHun(nextDeepGroups, result, hunNum);
                }


                if (i % 9 <= 7 && i <= 26 && curSplitGroup.getLeftTilesNum()[i + 1] > 0) {//连张
                    SplitGroupsHelp nextDeepGroups = curSplitGroup.clone();
                    nextDeepGroups.reduceLeftTilesNum(i, 1);
                    nextDeepGroups.reduceLeftTilesNum(i + 1, 1);
                    nextDeepGroups.addSplitGroup(new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG, i));
                    splitHun(nextDeepGroups, result, hunNum);
                }

                if (i % 9 <= 6 && i <= 26 && curSplitGroup.getLeftTilesNum()[i + 2] > 0) {//隔张
                    SplitGroupsHelp nextDeepGroups = curSplitGroup.clone();
                    nextDeepGroups.reduceLeftTilesNum(i, 1);
                    nextDeepGroups.reduceLeftTilesNum(i + 2, 1);
                    nextDeepGroups.addSplitGroup(new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_KANZHANG, i));
                    splitHun(nextDeepGroups, result, hunNum);
                }
                //单张
                curSplitGroup.reduceLeftTilesNum(i, 1);
                curSplitGroup.addSplitGroup(new SplitGroup(SplitGroupType.SPLIT_GROUP_TYPE_SINGLE, i));
                splitHun(curSplitGroup, result, hunNum);
            }
        }
    }

    private static int calWinDis(SplitGroupsHelp group) {
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

    //算分部分
    public static double findMinValue(List<SplitGroupsHelp> result, int winDistance,
                                      List<Integer> valueless, boolean allDanger,
                                      int[] remain, int remainCount) {
        double[] values = new double[TILE_TYPE_COUNT];
        for (int i = 0; i < TILE_TYPE_COUNT; i++) {
            values[i] = 0;
        }
        for (SplitGroupsHelp it : result) {
//            if(it.getWinDistance()<=winDistance) {
            countValue(it, remain, remainCount);
            double r = it.getWinDistance() == winDistance ? 1.0 : 0.5;
            for (int i = 0; i < TILE_TYPE_COUNT; i++) {
                values[i] += it.getTileValues()[i] * r;
            }
        }
        double minValue = MAX_VALUE * result.size() * 10;

//        System.out.println();
//        for (int i = 0; i < 16; i++) {
//            System.out.print((int) values[i] + ",");
//        }
//        System.out.println();

        for (int i = 0; i < TILE_TYPE_COUNT; i++) {
            //if (allDanger || !dangerTiles.contains(Integer.valueOf(i)))
            {//如果考虑危险张，则只处理非危险张
                if (Math.abs(minValue - values[i]) < 0.000001) {
                    valueless.add(i);
                } else if (values[i] < minValue) {
                    minValue = values[i];
                    valueless.clear();
                    valueless.add(i);
                }
            }
        }
        return minValue;
    }

    //纯牌效
    public static List<Integer> findLowProfit(List<SplitGroupsHelp> result, int winDistance,
                                              int[] hand,
                                              int[] remain, int remainCount) {
        double[] values = new double[TILE_TYPE_COUNT];
        for (int i = 0; i < TILE_TYPE_COUNT; i++) {
            values[i] = 0;
        }
        int countMax = 0;
        List<Integer> tileToDiscard = new ArrayList<>();
        for (int i = 0; i < TILE_TYPE_COUNT; i++) {
            StringBuilder tileChange = new StringBuilder();
            int count = 0;
            if (hand[i] > 0) {
                hand[i]--;
                tileChange.append("打").append(TILE_STRING[i]);
                List<SplitGroupsHelp> myResult = new ArrayList<>();
                splitHands(hand, myResult);
                int myWinDistance = getMinWinDistance(myResult);
                if (myWinDistance <= winDistance) {
                    for (int j = 0; j < TILE_TYPE_COUNT; j++) {
                        if (i == j) {
                            continue;
                        }
                        hand[j]++;

                        if (j < 27||hand[j]<3) {
                            List<SplitGroupsHelp> myResultDep = new ArrayList<>();
                            splitHands(hand, myResultDep);
                            int myWinDistanceDept = getMinWinDistance(myResultDep);
                            if (myWinDistanceDept < myWinDistance) {
                                values[j] += remain[j];
                                count += remain[j];
                                tileChange.append("摸").append(TILE_STRING[j]).append(":").append(remain[j]).append("枚数,");
                            }
                        }else{
                            if(hand[j]==3){
                                values[j] += remain[j];
                                count += remain[j];
                                tileChange.append("摸").append(TILE_STRING[j]).append(":").append(remain[j]).append("枚数,");
                            }
                        }
                        hand[j]--;
                    }
                }
                hand[i]++;
                tileChange.append("共计:").append(count);
                System.out.println(tileChange.toString());
                if (count > countMax) {
                    countMax = count;
                    tileToDiscard.clear();
                    tileToDiscard.add(i);
                } else if (count == countMax) {
                    tileToDiscard.add(i);
                }
            }
        }
        return tileToDiscard;
    }

    public static void countValue(SplitGroupsHelp mg, int[] remain, int remainCount) {
        boolean noPair = (mg.getCurPairCount() == 0);
        for (int i = 0; i < TILE_TYPE_COUNT; i++) {
            mg.getTileValues()[i] = MAX_VALUE;
        }
        for (SplitGroup sp : mg.getCurSplitGroup()) {
            if (sp.getType() == SplitGroupType.SPLIT_GROUP_TYPE_SINGLE) {
                int tile = sp.getTile();
                double value = singleValue(tile, (mg.isSevenPair() || noPair), remain, remainCount);
                if (!mg.isSevenPair() && tile < 27) {
                    value += sequenceValue(tile, remain, remainCount);
                }
                if (value < mg.getTileValues()[tile]) {
                    mg.getTileValues()[tile] = value;
                }
            } else if (sp.getType() == SplitGroupType.SPLIT_GROUP_TYPE_KANZHANG
                    || sp.getType() == SplitGroupType.SPLIT_GROUP_TYPE_LIANZHANG) {
                int tile0 = sp.getTile();
                int tile1 = sp.getType() == SplitGroupType.SPLIT_GROUP_TYPE_KANZHANG ? tile0 + 2 : tile0 + 1;
                double value = connectorValue(sp, remain, remainCount);
                double value0 = value + singleValue(tile0, noPair, remain, remainCount)
                        + sequenceValue(tile0, remain, remainCount);
                double value1 = value + singleValue(tile1, noPair, remain, remainCount)
                        + sequenceValue(tile1, remain, remainCount);
                if (value0 < mg.getTileValues()[tile0]) {
                    mg.getTileValues()[tile0] = value0;
                }
                if (value1 < mg.getTileValues()[tile1]) {
                    mg.getTileValues()[tile1] = value1;
                }
            } else if (sp.getType() == SplitGroupType.SPLIT_GROUP_TYPE_PAIR) {
                int tile = sp.getTile();
                if (mg.isSevenPair()) {
//                    double value = 10;
//                    if (value < mg.getTileValues()[tile]) {
//                        mg.getTileValues()[tile] = value;
//                    }
                } else if (mg.winDistance > 1) {
                    double value = pairValue(tile, mg.getCurPairCount(), remain, remainCount);
                    if (tile < 27) {
                        value += sequenceValue(tile, remain, remainCount);
                    }
                    if (value < mg.getTileValues()[tile]) {
                        mg.getTileValues()[tile] = value;
                    }
                } else if (mg.getWinDistance() == 1) {

                }
            }
        }
    }

    private static double pairValue(int tile, int pairCount, int[] remain, int remainCount) {
        //double value = (pairCount == 1 && tile < 9) ? remain[tile] + 10 : remain[tile];
        double value = pairCount == 1 ? 10 : remain[tile];
        return value;
    }

    private static double connectorValue(SplitGroup meld, int[] remain, int remainCount) {
        double value = 0;
        if (meld.getTile() < 27) {
            if (meld.getType() == SplitGroupType.SPLIT_GROUP_TYPE_KANZHANG) {
                value = remain[meld.getTile() + 1];
            } else {
                if (meld.getTile() != 0) {
                    value += remain[meld.getTile() - 1];
                }
                if (meld.getTile() != 7) {
                    value += remain[meld.getTile() + 2];
                }
            }
        }
        return value;
    }

    public static double sequenceValue(int tile, int[] remain, int remainCount) {
        int m1 = tile <= 1 ? 0 : Math.min(remain[tile - 1], remain[tile - 2]);
        int m2 = (tile == 0 || tile == 8) ? 0 : Math.min(remain[tile - 1], remain[tile + 1]);
        int m3 = tile >= 7 ? 0 : Math.min(remain[tile + 1], remain[tile + 2]);
        if (remainCount != 0) {
            return (remainCount + 1.0) * (m1 + m2 + m3) / (remainCount * 3);
        } else {
            return 0;
        }
    }

    public static double singleValue(int tile, boolean noPair, int[] remain, int remainCount) {
        double value;
        if (noPair) {
            switch (remain[tile]) {
                case 3:
                    value = 3;
                    break;
                case 2:
                    value = 1;
                    break;
                case 1:
                    value = 0.5;
                    break;
                default:
                    value = 0;
            }
        } else {
            switch (remain[tile]) {
                case 3:
                    value = 1;
                    break;
                case 2:
                    value = 0.333;
                    break;
                default:
                    value = 0;
            }
        }
        if (remainCount != 0) {
            value *= (remainCount + 1.0) / remainCount;
        }
        return value;
    }
}
