package com.wzy.game.server.ai;

import com.wzy.game.server.base.Hand;
import com.wzy.game.server.base.Meld;
import com.wzy.game.server.base.MeldType;
import com.wzy.game.server.base.Tile;
import com.wzy.game.server.caculator.HuResultInterface;
import com.wzy.game.server.caculator.guizhou.GuizhouCheckParam;
import com.wzy.game.server.caculator.guizhou.GuizhouHuResult;
import com.wzy.game.server.caculator.guizhou.GuizhouJudge;

import java.util.*;

public class AIUtils {

    public static HuResultInterface countWinNowResult(String hStr, List<String> sets, int winMode, String winTiles) {
        int score = 0;
        Hand hand = Hand.parseString(hStr);
        winTiles = winTiles.replaceAll("s","t");

        List<Meld> melds = new ArrayList<>();
        if(sets!=null) {
            for (int i = 0; i < sets.size(); i++) {
                String set = sets.get(i);
                MeldType mt = getGzType(set);
                if (mt != null) {
                    melds.add(new Meld(Tile.parseTiles(set), mt, 0));
                }
            }
        }
        GuizhouCheckParam gzParam = new GuizhouCheckParam(hand.getTiles(), Tile.parseString(winTiles), melds,
                winMode);
        GuizhouJudge gzJudge = new GuizhouJudge();
        HuResultInterface gzResult = gzJudge.checkWin(gzParam);
        return gzResult;
    }

    public static int countWinNowScore(String hStr, List<String> sets, int winMode, String winTiles) {
        int score = 0;
        Hand hand = Hand.parseString(hStr);
        winTiles = winTiles.replaceAll("s","t");
        List<Meld> melds = new ArrayList<>();
        if(sets!=null) {
            for (int i = 0; i < sets.size(); i++) {
                String set = sets.get(i);
                MeldType mt = getGzType(set);
                if (mt != null) {
                    melds.add(new Meld(Tile.parseTiles(set), mt, 0));
                }
            }
        }
        GuizhouCheckParam gzParam = new GuizhouCheckParam(hand.getTiles(), Tile.parseString(winTiles), melds,
                winMode);
        GuizhouJudge gzJudge = new GuizhouJudge();
        HuResultInterface gzResult = gzJudge.checkWin(gzParam);
        GuizhouHuResult gzHuResult = (GuizhouHuResult) gzJudge.checkWin(gzParam);
        return gzHuResult.getMaxFans();
    }
    public static MeldType getGzType(String set) {
        if (set.length() == 6) {
            return MeldType.PONG;
        }
        if (set.length() == 8) {
            return MeldType.PON_KONG;
        }
        if(set.length() == 10){
            return MeldType.CON_KONG;
        }
        return null;
    }

    public static int color(int index){
        return index / 9;
    }

    public static int[] listToArray(List<Integer> cards){
        int [] hands = new int[27];
        for (Integer i : cards) {
            hands[i] ++;
        }
        return hands;
    }


    public static String listToString(List<Integer> cards){
        Collections.sort(cards);
        StringBuilder builder = new StringBuilder();
        for (Integer card : cards) {
            builder.append(AIConstant.CARD_NAME[card]);
        }
        return builder.toString();
    }

    public static List<Integer> stringToList(String cardStr){
        List<Integer> cards = new ArrayList<>(16);
        for (int i = 0; i < cardStr.length(); i+=2) {
            cards.add(AIConstant.NAME_INDEX.get(cardStr.substring(i,i+2)));
        }
        return cards;
    }

    public static int[] getColorSize(int[] hands){
        int[] colorSize = new int[4];
        for (int i = 0; i < 9; i++) {
            if (hands[i] > 0) colorSize[0]+=hands[i];
        }
        for (int i = 9; i < 18; i++) {
            if (hands[i] > 0) colorSize[1]+=hands[i];
        }
        for (int i = 18; i < 27; i++) {
            if (hands[i] > 0) colorSize[2]+=hands[i];
        }
        for (int i = 27; i < 34; i++) {
            if (hands[i] > 0) colorSize[3]+=hands[i];
        }
        return colorSize;
    }
}
