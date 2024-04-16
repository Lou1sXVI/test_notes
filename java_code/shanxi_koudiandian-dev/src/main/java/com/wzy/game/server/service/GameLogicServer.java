package com.wzy.game.server.service;

import com.alibaba.fastjson2.JSONObject;
import com.wzy.game.server.ai.*;
import com.wzy.game.server.constant.ConstVar;
import com.wzy.game.server.model.AckErmjGameLogic;
import com.wzy.game.server.model.ActHistory;
import com.wzy.game.server.model.Player;
import com.wzy.game.server.model.ReqGameLogic;
import com.wzy.game.server.util.CodeUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.wzy.game.server.ai.ParamMode.*;
import static com.wzy.game.server.constant.ConstVar.*;
import static com.wzy.game.server.util.CodeUtil.TILE_STRING;


public class GameLogicServer {
    private static final Logger logger = LoggerFactory.getLogger(GameLogicServer.class);
    private static GameLogicServer instance = new GameLogicServer();

    private GameLogicServer() {
    }

    public static GameLogicServer getInstance() {
        return instance;
    }

    private List<Integer> translateActionType(List<Integer> apiType) {
        if (apiType == null) return Collections.emptyList();
        List<Integer> aiType = new ArrayList<>(apiType.size());
        for (int type : apiType) {
            switch (type) {
                case ConstVar.LOG_HAND:                     //起手牌
                case ConstVar.LOG_ACTION_DRAW:              //抓牌
                    aiType.add(ACTION_DISCARD);
                    break;
                case ConstVar.LOG_ACTION_MINGGANG:          //明杠
                    aiType.add(ACTION_MINGGANG);
                    break;
                case ConstVar.LOG_ACTION_BUGANG:            //补杠
                    aiType.add(ACTION_BUGANG);
                    break;
                case ConstVar.LOG_ACTION_ANGANG:            //暗杠
                    aiType.add(ACTION_ANGANG);
                    break;
                case ConstVar.LOG_ACTION_ZIMO:              //自摸
                case ConstVar.LOG_ACTION_DIANPAO:           //点炮胡
                    aiType.add(ACTION_WIN);
                    break;
                case ConstVar.LOG_ACTION_DOWN_CHOW:         //左吃
                case ConstVar.LOG_ACTION_MIDDLE_CHOW:      //中吃
                case ConstVar.LOG_ACTION_UP_CHOW:          //右吃
                    aiType.add(ACTION_CHOW);
                    break;
                case ConstVar.LOG_ACTION_PENG:             //碰
                    aiType.add(ACTION_PONG);
                    break;
                case ConstVar.LOG_ACTION_PASS:             //Pass
                    aiType.add(ACTION_PASS);
                    break;
                case ConstVar.LOG_ACTION_TING:             //听牌
                    aiType.add(ACTION_READY);
                    break;
                case ConstVar.LOG_ACTION_DOUBLE:           //加倍
                    aiType.add(ACTION_DOUBLE);
                    break;
                default:
                    aiType.add(ACTION_NONE);
                    break;
            }
        }
        return aiType;
    }

    private String sortTile(String tiles) {
        List<String> list = new ArrayList<>(16);
        for (int i = 0; i < tiles.length(); i += 2) {
            list.add(tiles.substring(i, i + 2));
        }
        list.sort((s1, s2) -> {
            int res = Character.compare(s1.charAt(1), s2.charAt(1));
            if (res == 0) {
                return Character.compare(s1.charAt(0), s2.charAt(0));
            }
            return res;
        });
        StringBuilder ss = new StringBuilder();
        for (String s : list) {
            if (!ss.toString().endsWith(s.substring(1))) {
                ss.append(",");
            }
            ss.append(s);
        }
        return ss.toString();
    }

    public int doAction(ReqGameLogic req, AckErmjGameLogic ack) throws Exception {
        //设置局号返回参数
        ack.setGameID(req.getGameId());
        logger.info("req: [{}] {}", sortTile(req.getPlayers().get(req.getCurPos()).getTiles()), JSONObject.toJSONString(req));
        ack.setUserID(req.getUserID());
        //打牌参数设置
        ParamMode paramMode = new ParamMode();
        paramMode.setTableid(req.getGameId());
        paramMode.setUserId(req.getUserID());
        paramMode.setMySeat(req.getCurPos());
        paramMode.setLevel(req.getLevel());
        List<List<Group>> groupList = new ArrayList<>();
        List<List<Integer>> discardList = new ArrayList<>();
        //转化包牌
        List<Integer> doraSign = new ArrayList<>();
        String hun = req.getHun();
        paramMode.setDoraDirective(doraSign);
        for (int i = 0; i < 4; i++) {
            groupList.add(new ArrayList<Group>());
        }
        for (int i = 0; i < 4; i++) {
            discardList.add(new ArrayList<Integer>());
        }
        paramMode.setOppoGroups(groupList);
        paramMode.setOppoDiscards(discardList);
        paramMode.setRemainsLength(req.getRemain());//剩余牌张数
        if (req.getRemain() == 0 && StringUtils.isNotBlank(req.getWall()) && req.getWall().length() != 0) {
            paramMode.setRemainsLength(req.getWall().length() / 2);
            /*增加remain和wall参数兼容性*/
        }
        //提取玩家信息
        List<Integer> cacheHands = new ArrayList<>();
        boolean[] isTing = new boolean[4];
        boolean transWeile = false;
        for (Player play : req.getPlayers()) {
            //提取手牌部分
            cacheHands.clear();
            if (play.getTiles() != null) {
                for (int i = 0; i < 7; i++) {
                    if (play.getTiles().contains(i + "f")) {
                        transWeile = true;
                    }
                }
                CodeUtil.getCardsList(cacheHands, play.getTiles());
            }
            String wind = play.getSeatWind();

            if (play.getPosition() == req.getCurPos()) {
                paramMode.setMyHandsAka(cacheHands);
                List<Integer> noAka = new ArrayList<>();
                for (int i = 0; i < cacheHands.size(); i++) {
                    int tile = cacheHands.get(i);
                    if (tile > 33) {
                        paramMode.setHunNum(paramMode.getHunNum() + 1);
                    } else {
                        noAka.add(tile);
                    }
                }
                paramMode.getMyHands().addAll(noAka);
                if (StringUtils.isNotBlank(wind)) {
                    paramMode.setSeat(CodeUtil.getTileCode(wind) % 27);
                }
            } else {
            }
            cacheHands.clear();
            //提取玩家的吃碰杠的牌
            for (String str : play.getSets()) {
                cacheHands.clear();
                CodeUtil.getCardsList(cacheHands, str);
                if(cacheHands.size()>0) {
                    Group g1 = new Group();

                    g1.setFrom(cacheHands.get(0));
                    g1.getCards().addAll(cacheHands);
                    for (int i = 0; i < cacheHands.size(); i++) {
                        int tile = cacheHands.get(i);
                    }
                    if (play.getPosition() == req.getCurPos()) {
                        paramMode.getMyGroups().add(g1);
                        for (int i = 0; i < g1.getCards().size(); i++) {
                            if (i == 4) {
                                break;
                            } else {
                            }
                        }
                    } else {
                        List<Group> groupsTmp = paramMode.getOppoGroups().get(play.getPosition());
                        groupsTmp.add(g1);
                        paramMode.getOppoGroups().set(play.getPosition(), groupsTmp);
                    }
                }else{

                }
            }
            isTing[play.getPosition()] = play.getIsTing() > 0;
        }
        paramMode.setIsTing(isTing);

        //提取历史信息以便获得打牌记录
        //绝安牌集合
        int[][] safeTiles = new int[4][34];
        int[] safeTileTotal = new int[34];
        int round = 0;
        String[] event = StringUtils.split(req.getHistory().trim(), ",");//会忽略空格的内容
        List<ActHistory> history = new ArrayList<>();
        for (String str : event) {
            String[] action = StringUtils.splitByWholeSeparatorPreserveAllTokens(str, ":");
            if (action.length != 3) {
                logger.error("{} 遇见一个不合法参数", Arrays.toString(action));
                continue;
            }
            int actionType = Integer.parseInt(action[0]);
            int seat = Integer.parseInt(action[1]);
            if (seat < 0 || seat > 3) {
                continue;
            }
            if (Integer.parseInt(action[0]) == ConstVar.LOG_ACTION_DISCARD ||
                    Integer.parseInt(action[0]) == LOG_ACTION_TOUCHDISCAR ||
                    Integer.parseInt(action[0]) == LOG_ACTION_TING) {
                int pos = Integer.parseInt(action[1]);
                if (Integer.parseInt(action[1]) == req.getCurPos()) {
                    int code = CodeUtil.getTileCode(action[2]);
//                    if(code>=34){
//                        //转换赤宝牌
//                        code = code%34*9+4;
//                    }
                    if (code != -1) {
                        paramMode.getMyDiscards().add(code);
                    } else {//历史动作中有非正常牌值
                        ack.setInfo(str);
                        return ConstVar.RESPONSE_CODE_FAID_REQ_PARAM;
                    }
                } else {
                    int code = CodeUtil.getTileCode(action[2]);
                    if (seat == req.getDealer()) {
                        round++;
                    }
                    if (code != -1 && code < 34) {
                        List<Integer> tiles = paramMode.getOppoDiscards().get((seat + 4 - paramMode.getMySeat()) % 4 - 1);
                        tiles.add(code);
                        paramMode.getOppoDiscards().set((seat + 4 - paramMode.getMySeat()) % 4 - 1, tiles);
                        safeTiles[seat][code] = 1;
                        for (int i = 0; i < 4; i++) {
                            int newSeat = (seat + i) % 4;
                        }
                    } else {//历史动作中有非正常牌值
//                        ack.setInfo(str);
//                        return ConstVar.RESPONSE_CODE_FAID_REQ_PARAM;
                    }
                }
            } else if (Integer.parseInt(action[0]) == ConstVar.LOG_ACTION_ANGANG) {
                if (Integer.parseInt(action[1]) == req.getCurPos()) {
                    paramMode.setAnGangNum(paramMode.getAnGangNum() + 1);
                } else {
                    paramMode.setOppoAnGangNum(paramMode.getOppoAnGangNum() + 1);
                }
            }
            ActHistory actHistory = new ActHistory(action[0], action[1], action[2]);
            history.add(actHistory);
        }
        //提取最后一次的动作
        String lastTile = "";
        boolean validAction = true;
        if (event.length == 0) {
            validAction = false;
            paramMode.setLastActionType(ACTION_DRAW);
            if (paramMode.getMyHands() == null) {
                System.out.println(1);
            }
            paramMode.setLastActionTile(paramMode.getMyHands().get(0));
            lastTile = TILE_STRING[paramMode.getMyHands().get(0)];
            paramMode.setLastActionSeat(paramMode.getMySeat());
        }
        int lastCount = 1;

        while (validAction) {
            String[] lastaction = StringUtils.splitByWholeSeparatorPreserveAllTokens(event[event.length - lastCount], ":");
            // 如果上一個是pass 則指向上上個
            if (lastaction[0].equals(String.valueOf(LOG_ACTION_PASS))) {
                if (event.length >= 2) {
                    lastaction = StringUtils.splitByWholeSeparatorPreserveAllTokens(event[event.length - lastCount - 1], ":");
                }
            }
            paramMode.setLastActionSeat(Integer.parseInt(lastaction[1]));
            int code = CodeUtil.getTileCode(lastaction[2]);
            if (code > 33) {
                code = 34;
            }
            if (code != -1) {
                lastTile = lastaction[2];
                paramMode.setLastActionTile(code);
            } else {
                ack.setInfo(event[event.length - 1]);
                return ConstVar.RESPONSE_CODE_FAID_REQ_PARAM;
            }
            switch (Integer.parseInt(lastaction[0])) {
                case ConstVar.LOG_ACTION_DRAW:             //抓牌
                    paramMode.setLastActionType(ACTION_DRAW);
//                    paramMode.getMyHands().add(code);
                    validAction = false;
                    break;
                case ConstVar.LOG_ACTION_DISCARD:           //手切
                case ConstVar.LOG_ACTION_TOUCHDISCAR:       //摸切
                    paramMode.setLastActionType(ACTION_DISCARD);
                    validAction = false;
                    break;
                case LOG_ACTION_TING:
                    paramMode.setLastActionType(ACTION_DISCARD);
                    validAction = false;
                    break;
                case ConstVar.LOG_ACTION_MINGGANG:          //明杠
                    paramMode.setLastActionType(ACTION_MINGGANG);
                    validAction = false;
                    break;
                case ConstVar.LOG_ACTION_BUGANG:            //补杠
                    paramMode.setLastActionType(ACTION_BUGANG);
                    validAction = false;
                    break;
                case ConstVar.LOG_ACTION_ANGANG:            //暗杠
                    paramMode.setLastActionType(ACTION_ANGANG);
                    validAction = false;
                    break;
                case ConstVar.LOG_ACTION_DOWN_CHOW:         //左吃
                case ConstVar.LOG_ACTION_MIDDLE_CHOW:      //中吃
                case ConstVar.LOG_ACTION_UP_CHOW:          //右吃
                    paramMode.setLastActionType(ACTION_CHOW);
                    validAction = false;
                    break;
                case ConstVar.LOG_ACTION_PENG:             //碰
                    paramMode.setLastActionType(ACTION_PONG);
                    validAction = false;
                    break;
                case ConstVar.LOG_ACTION_PASS:
                    lastCount++;
                    break;
                case ConstVar.LOG_ACTION_DIANPAO:
                case ConstVar.LOG_ACTION_ZIMO:
                    ack.setInfo("游戏已完场");
                    return ConstVar.RESPONSE_CODE_FAID_REQ_PARAM;
                case ConstVar.LOG_HAND:

                default:
                    ack.setInfo("历史记录不合法");
                    return ConstVar.RESPONSE_CODE_FAID_REQ_PARAM;
            }
        }

        //生成一个随机数

//        // 合法动作添加
//        GameAction action = paramMode.getRetAction();
//        paramMode.setHistory(history);
//        action.setLegalAction(translateActionType(req.getLegalAct()));
//        if(paramMode.getLevel() == 5 ){//6级机器人
//            AILevel6.getInstance().doAction(paramMode);
//        }else if(paramMode.getLevel() == 4 ){//5级机器人
//            AILevel5.getInstance().doAction(paramMode);
//        }else if(paramMode.getLevel() == 3 ){//4级机器人
//            AILevel4.getInstance().doAction(paramMode);
//        }else if(paramMode.getLevel() == 2 ){//三级机器人
//            AILevel3.getInstance().doAction(paramMode);
//        }else if(paramMode.getLevel() == 1 ) {//二级机器人
//            AILevel2_v1.getInstance().doAction(paramMode);
//        }else {//一级机器人
//            AILevel1.getInstance().doAction(paramMode);
//        }

        double t = Math.random();
        // 合法动作添加
        GameAction action = paramMode.getRetAction();
        paramMode.setHistory(history);
        action.setLegalAction(translateActionType(req.getLegalAct()));
        if (paramMode.getLevel() == 5) {//6级机器人
            AILevel6.getInstance().doAction(paramMode);
        } else if (paramMode.getLevel() == 4) {//5级机器人
            if (t > 0.7) {
                AILevel6.getInstance().doAction(paramMode);
            } else {
                AILevel5.getInstance().doAction(paramMode);
            }
        } else if (paramMode.getLevel() == 3) {//4级机器人
            if (t > 0.4) {
                AILevel5.getInstance().doAction(paramMode);
            } else {
                AILevel3.getInstance().doAction(paramMode);
            }
        } else if (paramMode.getLevel() == 2) {//三级机器人
            if (t > 0.9) {
                AILevel5.getInstance().doAction(paramMode);
            } else {
                AILevel3.getInstance().doAction(paramMode);
            }
        } else if (paramMode.getLevel() == 1) {//二级机器人
            if (t > 0.45) {
                AILevel3.getInstance().doAction(paramMode);
            } else {
                AILevel1.getInstance().doAction(paramMode);
            }
        } else if (paramMode.getLevel() == 0) {//一级机器人
            AILevel1.getInstance().doAction(paramMode);
        }

        //更新返回的动作和牌张
        ack.setTile(CodeUtil.TILE_STRING[paramMode.getRetAction().getCurTile()]);
        if (transWeile) {
            ack.setTile(CodeUtil.TILE_STRING_WEILE[paramMode.getRetAction().getCurTile()]);
        }
        ack.setDelay(100 + new Random().nextInt(2000));
        switch (paramMode.getRetAction().getActionType()) {
            case ACTION_DRAW:             //抓牌
                ack.setAction(ConstVar.LOG_ACTION_DRAW);
                break;
            case ACTION_DISCARD:           //打牌
                ack.setAction(ConstVar.LOG_ACTION_DISCARD);
                //是否摸切
                if (StringUtils.equals(CodeUtil.TILE_STRING[paramMode.getLastActionTile()], ack.getTile())) {
                    ack.setAction(LOG_ACTION_TOUCHDISCAR);
                }
                break;
            case ACTION_MINGGANG:          //明杠
                ack.setAction(ConstVar.LOG_ACTION_MINGGANG);
                break;
            case ACTION_BUGANG:            //补杠
                ack.setAction(ConstVar.LOG_ACTION_BUGANG);
                break;
            case ACTION_ANGANG:            //暗杠
                ack.setAction(ConstVar.LOG_ACTION_ANGANG);
                break;
            case ACTION_CHOW:
                if (paramMode.getRetAction().getUseChowOrPengOrGangTiles().size() > 0) {
                    int tile1 = paramMode.getRetAction().getUseChowOrPengOrGangTiles().get(0);
                    int tile2 = paramMode.getRetAction().getUseChowOrPengOrGangTiles().get(1);
                    int tile = paramMode.getRetAction().getCurTile();
                    if (tile < tile1 && tile < tile2) {
                        ack.setAction(ConstVar.LOG_ACTION_DOWN_CHOW);
                    } else if (tile > tile1 && tile > tile2) {
                        ack.setAction(ConstVar.LOG_ACTION_UP_CHOW);
                    } else {
                        ack.setAction(ConstVar.LOG_ACTION_MIDDLE_CHOW);
                    }
                }
                break;
            case ACTION_PONG:             //碰
                ack.setAction(ConstVar.LOG_ACTION_PENG);
                break;
            case ACTION_WIN:
                ack.setPoint(paramMode.getRetAction().getPoint());
                if (paramMode.getLastActionType() == ACTION_DRAW) {
                    ack.setAction(ConstVar.LOG_ACTION_ZIMO);
                } else {
                    ack.setAction(ConstVar.LOG_ACTION_DIANPAO);
                }
                ack.setTile(lastTile);
                ack.setFanInfo(paramMode.getRetAction().getFanInfo());
                break;
            case ACTION_READY:
                ack.setAction(LOG_ACTION_TING);
                break;
            default:
                ack.setAction(ConstVar.LOG_ACTION_PASS);
                break;
        }
        ack.setPoint(paramMode.getRetAction().getPoint());
        ack.setResponseCode(RESPONSE_CODE_SUCESS);
        logger.info("ack: {},{}", paramMode.getRetAction(), JSONObject.toJSONString(ack));
        return RESPONSE_CODE_SUCESS;
    }

    public static void main(String[] args) {
    }
}
