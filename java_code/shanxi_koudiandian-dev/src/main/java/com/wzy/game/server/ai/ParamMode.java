package com.wzy.game.server.ai;
import com.wzy.game.server.model.ActHistory;
import com.wzy.game.server.model.RuleMahjong;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static com.wzy.game.server.util.CodeUtil.TILE_STRING;
public class ParamMode {
    public final static int ACTION_DRAW = 0;    //抓
    public final static int ACTION_DISCARD = 1; //打
    public final static int ACTION_CHOW = 2;    //吃
    public final static int ACTION_PONG = 3;    //碰
    public final static int ACTION_MINGGANG = 4;//明杠
    public final static int ACTION_BUGANG = 5;  //补杠
    public final static int ACTION_ANGANG = 6;  //暗杠
    public final static int ACTION_READY = 7;   //报听
    public final static int ACTION_WIN = 8;     //胡牌
    public final static int ACTION_PASS = 9;    //过牌
    public final static int ACTION_DOUBLE = 10;  //加倍
    public final static int ACTION_NONE = 11;    //无动作
    public final static int ACTION_XIANJIA_TIANTING = 12; //闲家验证天听

    public final static String[] ACTION_STRING = {"抓", "打", "吃", "碰", "明杠", "补杠", "暗杠", "报听", "胡", "过", "加倍", "无动作"};


    String tableid;                                         //桌子ID
    long userId;                                            //自己的ID ，日志使用
    int mySeat;                                             //自己的座位号
    int prevalent;
    int seat;
    int[][] safeTiles = new int[4][34];
    int hun;
    List<Integer> doraDirective;//包牌指示牌
    List<Integer> myHands;//编码0-33
    List<Integer> myHandsAka;                                    //自己的手牌，存放编码0-36
    List<List<Integer>> oppoHands;                                  //对手手牌不知道为空，听牌后一定可知道
    boolean[] isTing = null;                                //双方是否听牌
    List<Group> myGroups;                              //自己的碰杠内容
    List<List<Group>> oppoGroups;                            //对手的碰杠内容
    List<Integer> myDiscards;                                 //自己的打牌内容
    List<List<Integer>> oppoDiscards;                               //对手的打牌内容
    List<List<Integer>> safeCard;                               //对手的打牌内容
    int remainsLength;                                          //余牌张数
    int lastActionSeat;                                     //最后一次的操作位置
    Integer lastActionTile;                                   //最优一次的操作的牌
    int lastActionType;                                     //最后一次的动作 (主要用来区分现在是应该{抓后打牌}{别人打牌是否吃碰杠胡}{是否抢杠}{吃碰后出牌})
    Integer luckType;                                         //幸运牌
    int level;                                              //使用AI级别
    int canWinMinFan;                                       //游戏规则最小可以胡番
    int doraCount;//宝牌数
    int anGangNum;                                       //暗杠
    int oppoAnGangNum;                                       //对家暗杠数
    GameAction retAction;                                   //返回动作
    int hunNum;
    List<String> sets;
    private RuleMahjong rule;

    List<ActHistory> history;

    public List<ActHistory> getHistory() {
        return history;
    }

    public void setHistory(List<ActHistory> history) {
        this.history = history;
    }

    public List<String> getSets() {
        return sets;
    }

    public void setSets(List<String> sets) {
        this.sets = sets;
    }

    public int getHunNum() {
        return hunNum;
    }

    public void setHunNum(int hunNum) {
        this.hunNum = hunNum;
    }

    public int getHun() {
        return hun;
    }

    public void setHun(int hun) {
        this.hun = hun;
    }

    public int[][] getSafeTiles() {
        return safeTiles;
    }

    public void setSafeTiles(int[][] safeTiles) {
        this.safeTiles = safeTiles;
    }

    public List<Integer> getDoraDirective() {
        return doraDirective;
    }

    public void setDoraDirective(List<Integer> doraDirective) {
        this.doraDirective = doraDirective;
    }

    public boolean isZhang = false;

    public ParamMode() {
        myHands = new ArrayList<Integer>();                                   //自己的手牌，编码0-16，牌背-1
        oppoHands = new ArrayList<List<Integer>>();                                 //对手手牌，牌背-1，不知道就是牌背，听牌后一定可知道
        isTing = new boolean[4];                                            //双方是否听牌
        myGroups = new ArrayList<Group>();                             //自己的碰杠内容
        oppoGroups = new ArrayList<>();
        myDiscards = new ArrayList<Integer>();                                //自己的打牌内容
        oppoDiscards = new ArrayList<>();                              //对手的打牌内容
        this.lastActionTile = -1;
        this.retAction = new GameAction();
        clear();
    }

    public void clear() {
        this.myHands.clear();
        this.oppoHands.clear();
        this.myGroups.clear();
        this.oppoGroups.clear();
        this.myDiscards.clear();
        this.oppoDiscards.clear();
        this.retAction.clear();
        for (int i = 0; i < 4; i++) {
            this.isTing[i] = false;
        }
        this.lastActionTile = -1;
        this.luckType = -1;
        this.userId = -1;
        this.mySeat = -1;
        this.lastActionSeat = -1;
        this.lastActionType = ACTION_NONE;
        this.level = 0;
        this.anGangNum = 0;
        this.oppoAnGangNum = 0;
    }

    public void setRule(RuleMahjong rule) {
        this.rule = rule;
    }

    public RuleMahjong getRule() {
        return rule;
    }


    public static class GameAction {
        boolean riich;
        int curTile;                                      //游戏结果要打的牌
        List<Integer> useChowOrPengOrGangTiles;               //游戏吃的时候要用的牌
        int actionType;                                     //游戏的动作
        int actionSecondTime;                               //游戏的建议思考时间
        int requestId;                                      //服务器要求验证的ID
        int point;
        List<Integer> legalAction;                          //合法动作
        List<String> fanInfo;                               //番种信息
        public int getPoint() {
            return point;
        }

        public void setPoint(int point) {
            this.point = point;
        }

        public boolean getRiichi() {
            return riich;
        }

        public void setRiich(boolean riich) {
            this.riich = riich;
        }

        public GameAction() {
            useChowOrPengOrGangTiles = new ArrayList<Integer>();
            legalAction = new ArrayList<Integer>();
            this.clear();
        }

        public void clear() {
            curTile = 0;
            actionType = ACTION_NONE;
            actionSecondTime = 1;
            legalAction.clear();
        }



        public int getCurTile() {
            return curTile;
        }

        public void setCurTile(int curTile) {
            this.curTile = curTile;
        }

        public List<Integer> getUseChowOrPengOrGangTiles() {
            return useChowOrPengOrGangTiles;
        }

        public void setUseChowOrPengOrGangTiles(List<Integer> useChowOrPengOrGangTiles) {
            this.useChowOrPengOrGangTiles = new ArrayList<Integer>(useChowOrPengOrGangTiles);
        }

        public int getActionType() {
            return actionType;
        }

        public void setActionType(int actionType) {
            this.actionType = actionType;
        }

        public int getActionSecondTime() {
            return actionSecondTime;
        }

        public void setActionSecondTime(int actionSecondTime) {
            this.actionSecondTime = actionSecondTime;
        }

        public List<Integer> getLegalAction() {
            return legalAction;
        }

        public void setLegalAction(List<Integer> legalAction) {
            if (this.legalAction == null) {
                this.legalAction = new ArrayList<>(legalAction);
            } else {
                this.legalAction.clear();
                this.legalAction.addAll(legalAction);
            }
        }


        public int getRequestId() {
            return requestId;
        }

        public void setRequestId(int requestId) {
            this.requestId = requestId;
        }

        public List<String> getFanInfo() {
            return fanInfo;
        }

        public void setFanInfo(List<String> fanInfo) {
            this.fanInfo = fanInfo;
        }

        @Override
        public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append("返回:[").append(ACTION_STRING[actionType]).append(TILE_STRING[this.curTile])
                    .append(" Time=").append(actionSecondTime);
            if (useChowOrPengOrGangTiles.size() > 0) {
                for (Integer stone : useChowOrPengOrGangTiles) {
                    buffer.append(TILE_STRING[stone]);
                }
            }
            if (legalAction.size() > 0) {
                for (int legalAction : this.legalAction) {
                    buffer.append(ACTION_STRING[legalAction]).append(",");
                }
            } else {
                buffer.append(" 没有合法动作");
            }
            return buffer.toString();
        }
    }


    public int getOppoAnGangNum() {
        return oppoAnGangNum;
    }

    public void setOppoAnGangNum(int oppoAnGangNum) {
        this.oppoAnGangNum = oppoAnGangNum;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getMySeat() {
        return mySeat;
    }

    public void setMySeat(int mySeat) {
        this.mySeat = mySeat;
    }

    public List<Integer> getMyHands() {
        return myHands;
    }

    public void setMyHands(List<Integer> myHands) {
        this.myHands = myHands;
    }

    public List<Integer> getMyHandsAka() {
        return myHandsAka;
    }

    public void setMyHandsAka(List<Integer> myHandsAka) {
        this.myHandsAka = myHandsAka;
    }

    public List<List<Integer>> getOppoHands() {
        return oppoHands;
    }

    public void setOppoHands(List<List<Integer>> oppoHands) {
        this.oppoHands = oppoHands;
    }

    public int getDoraCount() {
        return doraCount;
    }

    public void setDoraCount(int doraCount) {
        this.doraCount = doraCount;
    }

    public boolean[] getIsTing() {
        return isTing;
    }

    public void setIsTing(boolean[] isTing) {
        this.isTing = isTing;
    }


    public List<Group> getMyGroups() {
        return myGroups;
    }

    public void setMyGroups(List<Group> myGroups) {
        this.myGroups = myGroups;
    }

    public List<List<Group>> getOppoGroups() {
        return oppoGroups;
    }

    public void setOppoGroups(List<List<Group>> oppoGroups) {
        this.oppoGroups = oppoGroups;
    }

    public List<Integer> getMyDiscards() {
        return myDiscards;
    }

    public void setMyDiscards(List<Integer> myDiscards) {
        this.myDiscards = myDiscards;
    }


    public int getLastActionSeat() {
        return lastActionSeat;
    }

    public void setLastActionSeat(int lastActionSeat) {
        this.lastActionSeat = lastActionSeat;
    }

    public Integer getLastActionTile() {
        return lastActionTile;
    }

    public void setLastActionTile(Integer lastActionTile) {
        this.lastActionTile = lastActionTile;
    }

    public int getLastActionType() {
        return lastActionType;
    }

    public void setLastActionType(int lastActionType) {
        this.lastActionType = lastActionType;
    }

    public Integer getLuckType() {
        return luckType;
    }

    public void setLuckType(Integer luckType) {
        this.luckType = luckType;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getCanWinMinFan() {
        return canWinMinFan;
    }

    public void setCanWinMinFan(int canWinMinFan) {
        this.canWinMinFan = canWinMinFan;
    }

    public GameAction getRetAction() {
        return retAction;
    }

    public void setRetAction(GameAction retAction) {
        this.retAction = retAction;
    }

    public String getTableid() {
        return tableid;
    }

    public void setTableid(String tableid) {
        this.tableid = tableid;
    }

    public int getRemainsLength() {
        return remainsLength;
    }

    public void setRemainsLength(int remainsLength) {
        this.remainsLength = remainsLength;
    }

    public int getAnGangNum() {
        return anGangNum;
    }

    public void setAnGangNum(int anGangNum) {
        this.anGangNum = anGangNum;
    }

    public int getPrevalent() {
        return prevalent;
    }

    public void setPrevalent(int prevalent) {
        this.prevalent = prevalent;
    }

    public int getSeat() {
        return seat;
    }

    public void setSeat(int seat) {
        this.seat = seat;
    }

    public List<List<Integer>> getOppoDiscards() {
        return oppoDiscards;
    }

    public void setOppoDiscards(List<List<Integer>> oppoDiscards) {
        this.oppoDiscards = oppoDiscards;
    }

    public List<List<Integer>> getSafeCard() {
        return safeCard;
    }

    public void setSafeCard(List<List<Integer>> safeCard) {
        this.safeCard = safeCard;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        //Collections.sort(this.myHands);
        buf.append("hands=");
        Collections.sort(this.myHands);
        for (int tile : this.myHands) {
            buf.append(TILE_STRING[tile]);
        }
        buf.append(" tiles=");
        for (int tile : this.myHands) {
            buf.append(tile).append(",");
        }

        buf.append(" oppoTiles=");
        for (List<Integer> tiles : this.oppoHands) {
            buf.append(":");
            for (int tile : tiles) {
                buf.append(TILE_STRING[tile]);
            }
        }

        buf.append(" discard=");
        for (int tile : this.myDiscards) {
            buf.append(TILE_STRING[tile]);
        }

        buf.append(" oppoDiscard=");
        for (List<Integer> list : this.oppoDiscards) {
            buf.append(":");
            for (int i :
                    list) {
                buf.append(TILE_STRING[i]);
            }
        }

        buf.append("  myGroups=");
        for (Group g : this.myGroups) {
            for (int s : g.getCards()) {
                buf.append(TILE_STRING[s]);
            }
            buf.append(",");
        }

        buf.append("  oppoGroups=");
        for (List<Group> g : this.oppoGroups) {
            for (int i = 0; i < g.size(); i++) {
                for (int s : g.get(i).getCards()) {
                    buf.append(TILE_STRING[s]);
                }
            }

            buf.append(",");
        }

        return buf.toString();
    }
}
