package com.wzy.game.server.util;

import com.wzy.game.server.base.MeldType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.wzy.game.server.constant.ConstVar.*;

public class CodeUtil {
    private static final Logger logger = LoggerFactory.getLogger(CodeUtil.class);

    public static final String TILE_STRING[] = {
            "1w", "2w", "3w", "4w", "5w", "6w", "7w", "8w", "9w",
            "1t", "2t", "3t", "4t", "5t", "6t", "7t", "8t", "9t",
            "1b", "2b", "3b", "4b", "5b", "6b", "7b", "8b", "9b",
            "Ef", "Sf", "Wf", "Nf", "Bj", "Fj", "Zj",
            "1h", "2h", "3h", "4h", "5h", "6h", "7h", "8h"
    };

    public static final String TILE_STRING_FAN[] = {
            "1w", "2w", "3w", "4w", "5w", "6w", "7w", "8w", "9w",
            "1t", "2t", "3t", "4t", "5t", "6t", "7t", "8t", "9t",
            "1b", "2b", "3b", "4b", "5b", "6b", "7b", "8b", "9b",
            "1f", "2f", "3f", "4f", "1j", "2j", "3j",
            "1h", "2h", "3h", "4h", "5h", "6h", "7h", "8h"
    };
    //梅兰竹菊、春夏秋冬

    public static final String TILE_STRING_WEILE[] = {
            "1w", "2w", "3w", "4w", "5w", "6w", "7w", "8w", "9w",
            "1t", "2t", "3t", "4t", "5t", "6t", "7t", "8t", "9t",
            "1b", "2b", "3b", "4b", "5b", "6b", "7b", "8b", "9b",
            "1f", "2f", "3f", "4f", "5f", "6f", "7f",
            "1h", "2h", "3h", "4h", "5h", "6h", "7h", "8h"
    };
    public static int[] CardHex = {
            0x001, 0x011, 0x021, 0x031, 0x041, 0x051, 0x061, 0x071, 0x081,
            0x101, 0x111, 0x121, 0x131, 0x141, 0x151, 0x161, 0x171, 0x181,
            0x201, 0x211, 0x221, 0x231, 0x241, 0x251, 0x261, 0x271, 0x281,
            0x301, 0x311, 0x321, 0x331,
            0x401, 0x411, 0x421,
            0x041, 0x141, 0x241

    };

    public static final int TILE_1 = 0;
    public static final int TILE_2 = 1;
    public static final int TILE_3 = 2;
    public static final int TILE_4 = 3;
    public static final int TILE_5 = 4;
    public static final int TILE_6 = 5;
    public static final int TILE_7 = 6;
    public static final int TILE_8 = 7;
    public static final int TILE_9 = 8;
    public static final int TILE_E = 9;
    public static final int TILE_S = 10;
    public static final int TILE_W = 11;
    public static final int TILE_N = 12;
    public static final int TILE_Z = 13;
    public static final int TILE_F = 14;
    public static final int TILE_B = 15;


    private static final Map<String, Integer> TILE_CODE = new HashMap<String, Integer>() {{
        for (int i = 0; i < TILE_STRING.length; i++) {
            put(TILE_STRING[i].toUpperCase(), i);
        }
    }};
    private static final Map<String, Integer> TILE_CODE_WEILE = new HashMap<String, Integer>() {{
        for (int i = 0; i < TILE_STRING.length; i++) {
            put(TILE_STRING_WEILE[i], i);
        }
    }};

    private static final Map<Integer, Integer> TILE_HEX = new HashMap<Integer, Integer>() {{
        for (int i = 0; i < CardHex.length; i++) {
            put(i, CardHex[i]);
        }
    }};

    /**
     * 由牌张Str获取下标
     *
     * @param tile
     * @return
     */
    public static int getTileCode(String tile) {
        if (tile != null && TILE_CODE.containsKey(tile.toUpperCase())) {
            return TILE_CODE.get(tile.toUpperCase());
        } else if (tile != null && TILE_CODE_WEILE.containsKey(tile)) {
            return TILE_CODE_WEILE.get(tile);
        } else {
            return -1;
        }
    }

    /**
     * 根据下标获取HEX牌张id
     *
     * @param tile
     * @return
     */
    public static int getTileHex(int tile) {
        if (TILE_HEX.containsKey(tile)) {
            return TILE_HEX.get(tile);
        } else {
            return -1;
        }
    }

    public static void getCardsList(List<Integer> cards, String str) throws Exception {
        int index = 0;
        int len = str.length();
        if(str.contains("PB")){
            return;
        }
        //兼容雀魂mspz等牌生成模式
        str = str.replace("m", "w");
        str = str.replace("p", "b");
        str = str.replace("s", "t");
        for (int i = 1; i < 8; i++) {
            str = str.replace(i + "f", TILE_STRING[27 + i - 1]);

        }
        for (int i = 1; i < 8; i++) {
            str = str.replace(i + "z", TILE_STRING[26 + i]);
        }
        while (index < len) {
            String find = str.substring(index, index + 2).toUpperCase();
            if (index + 2 <= len && TILE_CODE.containsKey(find)) {
                int code = TILE_CODE.get(str.substring(index, index + 2).toUpperCase());
                cards.add(code);
                Collections.sort(cards);
            } else {
                logger.error("存在非法字符,请检查 {}, 当前位置 {} 剩余字符串 {}", str, index, str.substring(index));
            }
            index = index + 2;
        }
    }

    public static boolean judgeIntersection(List<String> list1, List<String> list2) {
        boolean flag = false;
        // 使用retainAll会改变list1的值，所以写一个替代
        List<String> origin = new ArrayList<>();
        origin.addAll(list1);
        origin.retainAll(list2);
        // 有交集
        if (origin.size() > 0) {
            flag = true;
        }
        return flag;
    }

    public static boolean judgeIntersectionInt(List<Integer> list1, List<Integer> list2) {
        boolean flag = false;
        // 使用retainAll会改变list1的值，所以写一个替代
        List<Integer> origin = new ArrayList<>();
        origin.addAll(list1);
        origin.retainAll(list2);
        // 有交集
        if (origin.size() > 0) {
            flag = true;
        }
        return flag;
    }


    public static void getCardsListNoSort(List<Integer> cards, String str) throws Exception {
        int index = 0;
        int len = str.length();

        while (index < len) {
            if (index + 2 <= len && TILE_CODE.containsKey(str.substring(index, index + 2).toUpperCase())) {
                int code = TILE_CODE.get(str.substring(index, index + 2).toUpperCase());
                cards.add(code);
            } else {
                logger.error("存在非法字符,请检查 {}, 当前位置 {} 剩余字符串 {}", str, index, str.substring(index));
                throw new Exception("存在非法字符,请检查 " + str + " 当前位置 " + index + " 剩余字符串 " + str.substring(index));
            }
            index = index + 2;
        }
    }


    public static void main(String[] args) throws Exception {
//        List<Integer> cards = new ArrayList<>();
//        String str = "Ef1tSf2tWf3t4t5t6t7t8t9tWfSfNfZjFjBjNf";
//        getCardsList(cards, str);
//        StringBuilder stringBuilder = new StringBuilder();
//        for (int tile : cards) {
//            stringBuilder.append(TILE_STRING[tile]);
//        }
//        System.out.println(str.equals(stringBuilder.toString()));
//        System.out.println(str);
//        System.out.println(stringBuilder.toString());
//        String[] show = {"Zj","bj","fj","ef","sf","wf","nf","1w","9w"};
//        System.out.println( doraCount(new Stone(getTileHex(CodeUtil.getTileCode("BJ"))),show));
//        System.out.println( doraCount(new Stone(getTileHex(CodeUtil.getTileCode("Fj"))),show));
//        System.out.println( doraCount(new Stone(getTileHex(CodeUtil.getTileCode("zj"))),show));
//        System.out.println( doraCount(new Stone(getTileHex(CodeUtil.getTileCode("sf"))),show));
//        System.out.println( doraCount(new Stone(getTileHex(CodeUtil.getTileCode("wf"))),show));
//        System.out.println( doraCount(new Stone(getTileHex(CodeUtil.getTileCode("nf"))),show));
//        System.out.println( doraCount(new Stone(getTileHex(CodeUtil.getTileCode("ef"))),show));
//        System.out.println( doraCount(new Stone(getTileHex(CodeUtil.getTileCode("2w"))),show));
//        System.out.println( doraCount(new Stone(getTileHex(CodeUtil.getTileCode("3w"))),show));
//        System.out.println( doraCount(new Stone(getTileHex(CodeUtil.getTileCode("1w"))),show));
    }

    public static String genFulu(Integer tile, int action) {
        StringBuilder stringBuilder = new StringBuilder();
        switch (action) {
            case LOG_ACTION_DOWN_CHOW:
                stringBuilder.append(TILE_STRING[tile]).append(TILE_STRING[tile + 1]).append(TILE_STRING[tile + 2]);
                break;
            case LOG_ACTION_MIDDLE_CHOW:
                stringBuilder.append(TILE_STRING[tile]).append(TILE_STRING[tile - 1]).append(TILE_STRING[tile + 1]);
                break;
            case LOG_ACTION_UP_CHOW:
                stringBuilder.append(TILE_STRING[tile]).append(TILE_STRING[tile - 1]).append(TILE_STRING[tile - 2]);
                break;
            case LOG_ACTION_ANGANG:
                for (int i = 0; i < 5; i++) {
                    stringBuilder.append(TILE_STRING[tile]);


                }
                break;

            case LOG_ACTION_BUGANG:
            case LOG_ACTION_MINGGANG:
                for (int i = 0; i < 4; i++) {
                    stringBuilder.append(TILE_STRING[tile]);

                }
                break;

            case LOG_ACTION_PENG:
                for (int i = 0; i < 3; i++) {
                    stringBuilder.append(TILE_STRING[tile]);

                }
                break;

        }
        System.out.println(TILE_STRING[tile] + " " + action + " " + stringBuilder.toString());
        return stringBuilder.toString();
    }

    public static String getHandStr(int[] handTile, int hunNum) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < handTile.length; i++) {
            for (int j = 0; j < handTile[i]; j++) {
                sb.append(TILE_STRING_FAN[i]);
            }
        }
        for (int i = 0; i < hunNum; i++) {
            sb.append(TILE_STRING_FAN[34]);
        }
        return sb.toString().replaceAll("s", "t");
    }

    public static String getHandStrFromList(List<Integer> handList, int hunNum) {
        int[] handInt = new int[34];
        for (Integer i :
                handList) {
            handInt[i]++;
        }
        return getHandStr(handInt, hunNum);
    }

    public static MeldType getGzType(String set) {
        if (set.length() == 6) {
            return MeldType.PONG;
        }
        if (set.length() == 8) {
            return MeldType.PON_KONG;
        }
        if (set.length() == 10) {
            return MeldType.CON_KONG;
        }
        return null;
    }
}
