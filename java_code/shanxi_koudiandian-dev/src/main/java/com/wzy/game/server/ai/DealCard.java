package com.wzy.game.server.ai;

import com.wzy.game.server.constant.FanConst;
import com.wzy.game.server.util.CodeUtil;

import java.util.*;

import static com.wzy.game.server.util.CodeUtil.TILE_STRING;

public class DealCard implements DealCardInterface {
    private static DealCard instancce = new DealCard();
    private static Random random = new Random(System.nanoTime());

    private DealCard() {
    }

    public static DealCard getInstancce() {
        return instancce;
    }

    @Override
    public int getInit(List<Integer> allHands, int goodPos, int fan, int dealerPos) {
        if (fan <= 0) {
            if (goodPos >= 0 && goodPos < 4) {
                int betterHandsScore = 0; // 两个手牌的得分间距， 越大越好，超过20分就直接返回
                //挑选优势牌
                for (int roundNum = 0; roundNum < 50; roundNum++) {
                    List<Integer> randomGameHands = new ArrayList<>();
                    instancce.genRandomInitCards(randomGameHands);
                    //第一种牌//第二种牌
                    int[][] eachHands = new int[4][34];
                    for (int i = 0; i < 4; i++) {
                        for (int j = 0; j < 13; j++) {
                            eachHands[i][randomGameHands.get(i*13+j)]++;
                        }
                    }
                    int doraSign = randomGameHands.get(randomGameHands.size() - 5);
                    int UraDoraSign = randomGameHands.get(randomGameHands.size() - 6);
                    //评分
                    int scoreMax = -1000;
                    int betterSeat = -1;
                    for (int i = 0; i < 4; i++) {
                        int score = getDoraScore(eachHands[i], randomGameHands.subList(52,randomGameHands.size()));
                        if (score > scoreMax) {
                            scoreMax = score;
                            betterSeat = i;
                        }
                        StringBuilder builder = new StringBuilder();
                        builder.append(getDoraScore(eachHands[i], randomGameHands.subList(52, randomGameHands.size())));
                        builder.append('\n');
                        builder.append(roundNum + " max :" + scoreMax);
                        builder.append('\n');
                        System.out.println(builder);
                    }
                    //
                    allHands.clear();
                    if (roundNum > 48 || scoreMax >= 0) {
                        for (int i = 0; i < 4; i++) {
                            List<Integer> firstList = null;
                            List<Integer> secondList = null;
                            if (goodPos == i) {
                                firstList = new ArrayList<>(randomGameHands.subList(betterSeat * 13, (betterSeat + 1) * 13));


                            } else {
                                if (betterSeat == i) {
                                    firstList = new ArrayList<>(randomGameHands.subList(goodPos * 13, (goodPos + 1) * 13));
                                } else {
                                    firstList = new ArrayList<>(randomGameHands.subList(i * 13, (i + 1) * 13));
                                }
                            }
                            int[] handArr1 = new int[34];
                            StringBuilder builder = new StringBuilder();
                            firstList.sort(Integer::compareTo);
                            for (int c : firstList) {
                                handArr1[c]++;
                                builder.append(CodeUtil.TILE_STRING[c]).append(',');
                            }
                            builder.append(getDoraScore(handArr1, randomGameHands.subList(52, randomGameHands.size())));
                            builder.append('\n');
                            builder.append(roundNum + " max :" + scoreMax);
                            builder.append('\n');
                            System.out.println(builder);
                            allHands.addAll(firstList);
                        }
                        allHands.addAll(randomGameHands.subList(52, randomGameHands.size()));
                        if (scoreMax >= 0) {
                            break;
                        }
                    }


                }
                // 设置庄家位第一张手牌
                boolean isGood = dealerPos == goodPos;
                List<Integer> wallList = new ArrayList<>(allHands.subList(52, allHands.size()));
                boolean[] isTing = {false, false};
                int[][] playHands13 = new int[2][13];
                for (int player = 0; player < 2; player++) {
                    int startindex = player * 13;
                    for (int i = 0; i < 13; i++) {
                        playHands13[player][i] = allHands.get(startindex + i);
                    }
                }
//                int dealCard = nextCard(wallList, playHands13, Collections.emptyList(), isTing, dealerPos, isGood, 0);
//                wallList.remove(Integer.valueOf(dealCard));
                List<Integer> list_n = new ArrayList<>(allHands.size());
                list_n.addAll(allHands.subList(0, 52));
//                list_n.add(dealCard);
                list_n.addAll(wallList);
                allHands.clear();
                allHands.addAll(list_n);
            } else {//随机发牌
                instancce.genRandomInitCards(allHands);
            }
        } else {//指定番种，可以生成牌库
            instancce.genWithFan(allHands, goodPos, fan, dealerPos);
        }
        return random.nextInt(34);
    }

    private void genWithFan(List<Integer> allHands, int pos, int fan, int dealerPos) {
        List<Integer> goodHand = new ArrayList<>(13);
        List<Integer> keyTiles = new ArrayList<>(13);
        int wd = random.nextInt(3);
        int fanNeedTile = 0;
        int totalTile = 13;
        int[] handInt = new int[16];
        allHands.clear();

        switch (fan) {
            case 88:
                int type = random.nextInt(6);
                switch (type) {
                    case 0:
                        //百万石
                        fanNeedTile = 13;
                        for (int i = 4; i < 9; i++) {
                            int cardNum = random.nextInt(2) + 2;
                            if (totalTile - cardNum >= 0) {
                                totalTile -= cardNum;
                            } else {
                                cardNum = totalTile;
                                totalTile = 0;
                            }
                            for (int j = 0; j < cardNum; j++) {
                                goodHand.add(i);
                            }
                        }
                        break;
                    case 1:
                        for (int i = 9; i < 13; i++) {
                            int cardNum = random.nextInt(2) + 2;
                            if (totalTile - cardNum >= 0) {
                                totalTile -= cardNum;
                            } else {
                                cardNum = totalTile;
                                totalTile = 0;
                            }

                            for (int j = 0; j < cardNum; j++) {
                                goodHand.add(i);
                                handInt[i]++;
                            }
                        }
                        for (int i = 0; i < wd; i++) {
                            int remove = random.nextInt(goodHand.size());
                            while (handInt[goodHand.get(remove)] <= 2 && goodHand.size() > 8) {
                                remove = random.nextInt(goodHand.size());
                            }
                            goodHand.remove(remove);
                        }
                        break;

                    case 2:
                        for (int i = 13; i < 16; i++) {
                            int cardNum = random.nextInt(2) + 2;
                            if (totalTile - cardNum >= 0) {
                                totalTile -= cardNum;
                            } else {
                                cardNum = totalTile;
                                totalTile = 0;
                            }
                            for (int j = 0; j < cardNum; j++) {
                                goodHand.add(i);
                                handInt[i]++;
                            }
                        }
                        for (int i = 0; i < wd; i++) {
                            if (goodHand.size() > 6) {
                                int remove = random.nextInt(goodHand.size());
                                while (handInt[goodHand.get(remove)] <= 2) {
                                    remove = random.nextInt(goodHand.size());
                                }
                                goodHand.remove(remove);
                            }
                        }
                        break;

                    case 3:
                        int h = random.nextInt(FanConst.nine().size());
                        List<Integer> tileCount = FanConst.nine().get(h);
                        for (int i = 0; i < 9; i++) {
                            for (int j = 0; j < tileCount.get(i); j++) {
                                goodHand.add(i);
                            }
                        }
                        for (int i = 0; i < 2; i++) {
                            if (goodHand.size() > 12) {
                                int remove = random.nextInt(goodHand.size());
                                keyTiles.add(goodHand.get(remove));
                                goodHand.remove(remove);
                            }
                        }
                        break;
                    case 4:
                        boolean ziWith = false;
                        for (int i = 0; i < 4; i++) {
                            int cardNum = random.nextInt(2) + 3;

                            int gangTile = random.nextInt(16);
                            while (goodHand.contains(gangTile)) {
                                gangTile = random.nextInt(16);
                            }
                            if (gangTile > 8) {
                                ziWith = true;
                            }
                            for (int j = 0; j < cardNum; j++) {
                                goodHand.add(gangTile);
                                handInt[gangTile]++;
                            }

                        }
                        for (int i = 0; i < wd + (goodHand.size() - 13); i++) {
                            boolean ok = true;

                            int remove = random.nextInt(goodHand.size());
                            int gangTile = goodHand.get(remove);
                            if (goodHand.size() < 13 && !ziWith) {
                                break;
                            }
                            while (ok && goodHand.size() > 13 - wd) {
                                if (gangTile >= 9 && handInt[gangTile] > 2) {
                                    ok = false;
                                } else if (gangTile < 9 && handInt[gangTile] > 3) {
                                    ok = false;
                                }
                                remove = random.nextInt(goodHand.size());
                                gangTile = goodHand.get(remove);
                            }
                            keyTiles.add(goodHand.get(remove));
                            handInt[gangTile]--;
                            goodHand.remove(remove);
                        }
                        break;
                    case 5://连七对
                        h = random.nextInt(FanConst.lianSevenPair().size());
                        tileCount = FanConst.lianSevenPair().get(h);
                        for (int i = 0; i < 9; i++) {
                            for (int j = 0; j < tileCount.get(i); j++) {
                                goodHand.add(i);
                            }
                        }
                        for (int i = 0; i < random.nextInt(3); i++) {
                            if (goodHand.size() > 12) {
                                int remove = random.nextInt(goodHand.size());
                                keyTiles.add(goodHand.get(remove));
                                goodHand.remove(remove);
                            }
                        }
                        break;
                }

                break;
            case 64:
                type = random.nextInt(5);
                switch (type) {
                    case 0://小四喜
                        for (int i = 9; i < 13; i++) {
                            int cardNum = random.nextInt(3) + 1;
                            if (totalTile - cardNum >= 0) {
                                totalTile -= cardNum;
                            } else {
                                cardNum = totalTile;
                                totalTile = 0;
                            }
                            for (int j = 0; j < cardNum; j++) {
                                goodHand.add(i);
                                handInt[i]++;
                            }
                        }
                        for (int i = 0; i < wd; i++) {
                            int remove = random.nextInt(goodHand.size());
                            while (handInt[goodHand.get(remove)] <= 2 && goodHand.size() > 7) {
                                remove = random.nextInt(goodHand.size());
                            }
                            goodHand.remove(remove);
                        }
                        break;
                    case 1:

                        for (int i = 13; i < 16; i++) {
                            int cardNum = random.nextInt(3) + 1;
                            if (totalTile - cardNum >= 0) {
                                totalTile -= cardNum;
                            } else {
                                cardNum = totalTile;
                                totalTile = 0;
                            }
                            for (int j = 0; j < cardNum; j++) {
                                goodHand.add(i);
                                handInt[i]++;

                            }
                        }
                        for (int i = 0; i < wd; i++) {
                            if (goodHand.size() > 5) {
                                int remove = random.nextInt(goodHand.size());
                                while (handInt[goodHand.get(remove)] <= 1) {
                                    remove = random.nextInt(goodHand.size());
                                }
                                goodHand.remove(remove);
                            }
                        }
                        break;
                    case 2://字一色

                        for (int i = 9; i < 16; i++) {
                            int cardNum = random.nextInt(3) + 1;
                            if (totalTile - cardNum >= 0) {
                                totalTile -= cardNum;
                            } else {
                                cardNum = totalTile;
                                totalTile = 0;
                            }
                            for (int j = 0; j < cardNum; j++) {
                                goodHand.add(i);
                                handInt[i]++;
                            }
                        }
                        for (int i = 0; i < wd; i++) {
                            if (goodHand.size() > 9) {
                                int remove = random.nextInt(goodHand.size());
                                while (handInt[goodHand.get(remove)] <= 1) {
                                    remove = random.nextInt(goodHand.size());
                                }
                                goodHand.remove(remove);
                            }
                        }
                        break;
                    case 3://四暗刻
                        for (int i = 0; i < 4; i++) {
                            int cardNum = random.nextInt(2) + 3;
                            int gangTile = random.nextInt(16);
                            while (goodHand.contains(gangTile)) {
                                gangTile = random.nextInt(16);
                            }
                            for (int j = 0; j < cardNum; j++) {
                                goodHand.add(gangTile);
                                handInt[gangTile]++;
                            }
                        }
                        for (int i = 0; i < wd + (goodHand.size() - 13); i++) {
                            boolean ok = true;
                            int remove = random.nextInt(goodHand.size());
                            int gangTile = goodHand.get(remove);
                            while (ok && goodHand.size() > 13 - wd) {
                                if (gangTile >= 9 && handInt[gangTile] > 2) {
                                    ok = false;
                                } else if (gangTile < 9 && handInt[gangTile] > 2) {
                                    ok = false;
                                }
                                remove = random.nextInt(goodHand.size());
                                gangTile = goodHand.get(remove);
                            }
                            keyTiles.add(goodHand.get(remove));
                            goodHand.remove(remove);
                        }
                        break;
                    case 4://双龙会
                        List<Integer> tileCount = FanConst.twoDragonMeet().get(0);
                        for (int i = 0; i < 9; i++) {
                            for (int j = 0; j < tileCount.get(i); j++) {
                                goodHand.add(i);
                            }
                        }
                        for (int i = 0; i < wd + goodHand.size() - 13; i++) {
                            if (goodHand.size() > 13 - wd) {
                                int remove = random.nextInt(goodHand.size());
                                while (remove < 2) {
                                    remove = random.nextInt(goodHand.size());
                                }
                                keyTiles.add(goodHand.get(remove));
                                goodHand.remove(remove);
                            }
                        }
                        break;
                }
                break;
            case 48:
                type = random.nextInt(2);
                switch (type) {
                    case 0://四同顺
                        int h = random.nextInt(FanConst.fourSameShun().size());
                        List<Integer> tileCount = FanConst.fourSameShun().get(h);
                        for (int i = 0; i < 9; i++) {
                            for (int j = 0; j < tileCount.get(i); j++) {
                                goodHand.add(i);
                                handInt[i]++;
                            }
                        }
                        for (int i = 0; i < wd; i++) {
                            if (goodHand.size() > 8) {
                                int remove = random.nextInt(goodHand.size());
                                while (handInt[goodHand.get(remove)] <= 2) {
                                    remove = random.nextInt(goodHand.size());
                                }
                                handInt[goodHand.get(remove)]--;
                                keyTiles.add(goodHand.get(remove));
                                goodHand.remove(remove);
                            }
                        }
                        break;
                    case 1:
                        h = random.nextInt(FanConst.fourLianKe().size());
                        tileCount = FanConst.fourLianKe().get(h);
                        for (int i = 0; i < 9; i++) {
                            for (int j = 0; j < tileCount.get(i); j++) {
                                goodHand.add(i);
                            }
                        }
                        for (int i = 0; i < wd; i++) {
                            if (goodHand.size() > 8) {
                                int remove = random.nextInt(goodHand.size());
                                while (goodHand.get(remove) <= 2) {
                                    remove = random.nextInt(goodHand.size());
                                }
                                keyTiles.add(goodHand.get(remove));
                                goodHand.remove(remove);
                            }
                        }
                        break;
                }
                break;
            case 32:
                type = random.nextInt(3);
                switch (type) {
                    case 0://四步
                        int h = random.nextInt(FanConst.fourLianShun().size());
                        List<Integer> tileCount = FanConst.fourLianShun().get(h);
                        for (int i = 0; i < 9; i++) {
                            for (int j = 0; j < tileCount.get(i); j++) {
                                goodHand.add(i);
                            }
                        }
                        for (int i = 0; i < wd; i++) {
                            if (goodHand.size() > 8) {
                                int remove = random.nextInt(goodHand.size());
                                while (goodHand.get(remove) <= 1) {
                                    remove = random.nextInt(goodHand.size());
                                }
                                keyTiles.add(goodHand.get(remove));
                                goodHand.remove(remove);
                            }
                        }
                        break;
                    case 1://三杠
                        for (int i = 0; i < 3; i++) {
                            int gangTile = random.nextInt(16);
                            while (goodHand.contains(gangTile)) {
                                gangTile = random.nextInt(16);
                            }
                            for (int j = 0; j < 4; j++) {
                                goodHand.add(gangTile);
                                handInt[gangTile]++;
                            }
                        }
                        for (int i = 0; i < wd; i++) {
                            boolean ok = true;
                            int remove = random.nextInt(goodHand.size());
                            int gangTile = goodHand.get(remove);
                            while (ok && goodHand.size() > 9) {
                                if (gangTile >= 9 && handInt[gangTile] > 2) {
                                    ok = false;
                                } else if (gangTile < 9 && handInt[gangTile] > 3) {
                                    ok = false;
                                }
                                remove = random.nextInt(goodHand.size());
                                gangTile = goodHand.get(remove);
                            }
                            keyTiles.add(goodHand.get(remove));
                            goodHand.remove(remove);
                        }
                        break;
                    case 2://混幺九
                        int used = 0;
                        for (int i = 7; i < 16; i++) {
                            int count = random.nextInt(4);
                            if (i < 9) {
                                count = random.nextInt(1) + 2;
                            }
                            int gangTile = i;
                            if (used + count > 13) {
                                count = 13 - used;
                                used = 13;
                            }
                            while (goodHand.contains(gangTile)) {
                                gangTile = random.nextInt(16);
                            }
                            for (int j = 0; j < count; j++) {
                                goodHand.add(gangTile);
                                handInt[gangTile]++;
                            }
                        }
                        if (handInt[7] > 0) {
                            handInt[0] = handInt[7];
                            handInt[7] = 0;
                            for (int i = 0; i < handInt[0]; i++) {
                                goodHand.remove(goodHand.lastIndexOf(7));
                                goodHand.add(0);
                            }
                        }
                        for (int i = 0; i < wd; i++) {
                            boolean ok = true;
                            int remove = random.nextInt(goodHand.size());
                            int gangTile = goodHand.get(remove);
                            while (ok && goodHand.size() > 9) {
                                if (handInt[0] + handInt[8] < 4 || goodHand.get(remove) < 9) {
                                    break;
                                } else {
                                    if (handInt[goodHand.get(remove)] < 2) {
                                        remove = random.nextInt(goodHand.size());
                                        ok = true;
                                    } else {
                                        ok = false;
                                    }
                                }

                            }
                            keyTiles.add(goodHand.get(remove));
                            handInt[goodHand.get(remove)]--;
                            goodHand.remove(remove);
                        }
                        break;
                }
                break;
            case 24:

                type = random.nextInt(4);
                switch (type) {
                    case 0://七对
                        Arrays.fill(handInt, 0);
                        for (int i = 0; i < 7; i++) {
                            int tile = random.nextInt(16);
                            if (goodHand.size() < 14 && handInt[tile] < 4) {
                                goodHand.add(tile);
                                goodHand.add(tile);
                                handInt[tile] = handInt[tile] + 2;
                            }
                        }
                        for (int i = 0; i < wd + 1; i++) {
                            int remove = random.nextInt(goodHand.size());
                            while (handInt[goodHand.get(remove)] < 1) {
                                remove = random.nextInt(goodHand.size());
                            }
                            int tile = goodHand.get(remove);
                            handInt[tile]--;
                            handInt[tile]--;
                            goodHand.remove(goodHand.lastIndexOf(tile));
                            goodHand.remove(goodHand.lastIndexOf(tile));
                            i++;
                        }
                        break;
                    case 1:
                        Arrays.fill(handInt, 4);
                        for (int i = 0; i < 14; i++) {
                            int tile = random.nextInt(9);
                            if (goodHand.size() < 14 && handInt[tile] > 0) {
                                goodHand.add(tile);
                                handInt[tile]--;
                            }
                        }
                        for (int i = 0; i < wd + 1; i++) {
                            int remove = random.nextInt(9);
                            while (handInt[remove] == 4) {
                                remove = random.nextInt(goodHand.size());
                            }
                            handInt[remove]++;
                            goodHand.remove(goodHand.lastIndexOf(remove));
                        }
                        break;
                    case 2:
                        int h = random.nextInt(FanConst.threeLianKe().size());
                        List<Integer> tiles = FanConst.threeLianKe().get(h);
                        for (int i = 0; i < 9; i++) {
                            for (int j = 0; j < tiles.get(i); j++) {
                                goodHand.add(i);
                                handInt[i]++;
                            }
                        }
                        for (int i = 0; i < wd; i++) {
                            int remove = random.nextInt(goodHand.size());
                            if (tiles.size() <= 7) {
                                break;
                            }
                            while (handInt[goodHand.get(remove)] < 2) {
                                remove = random.nextInt(goodHand.size());
                            }
                            handInt[goodHand.get(remove)]--;
                            goodHand.remove(remove);
                        }

                        break;

                }
                break;
            case 16:
                type = random.nextInt(2);
                switch (type) {
                    case 0:
                        int h = random.nextInt(FanConst.threeLianShun().size());
                        List<Integer> tiles = new ArrayList<>();
                        tiles = FanConst.threeLianShun().get(h);
                        for (int i = 0; i < 9; i++) {
                            for (int j = 0; j < tiles.get(i); j++) {
                                goodHand.add(i);


                                handInt[i]++;
                            }
                        }
                        for (int i = 0; i < wd; i++) {
                            int remove = random.nextInt(goodHand.size());
                            if (tiles.size() <= 7) {
                                break;
                            }
                            while (wd > 0 && tiles.size() > 6 && handInt[goodHand.get(remove)] < 1) {
                                remove = random.nextInt(goodHand.size());
                            }
                            keyTiles.add(goodHand.get(remove));
                            handInt[goodHand.get(remove)]--;
                            goodHand.remove(remove);
                        }
                        break;
                    case 1:
                        for (int i = 0; i < 3; i++) {
                            int keTile = random.nextInt(16);
                            while (handInt[keTile] > 0) {
                                keTile = random.nextInt(16);
                            }
                            int tileCount = random.nextInt(2) + 3;
                            for (int j = 0; j < tileCount; j++) {
                                goodHand.add(keTile);
                                handInt[keTile]++;
                            }
                        }
                        for (int i = 0; i < wd; i++) {
                            int remove = random.nextInt(goodHand.size());
                            if (goodHand.size() > 8) {
                                break;
                            }
                            while (wd > 0 && (handInt[goodHand.get(remove)] < 2 || handInt[goodHand.get(remove)] == 0)) {
                                remove = random.nextInt(goodHand.size());
                            }
                            keyTiles.add(goodHand.get(remove));
                            handInt[goodHand.get(remove)]--;
                            goodHand.remove(remove);
                        }
                        break;
                }
                break;
            default:
                getInit(allHands, pos, 0, dealerPos);
                return;
        }
        int[] tiles = new int[16];
        Arrays.fill(tiles, 4);
        for (int i = 0; i < goodHand.size(); i++) {
            tiles[goodHand.get(i)]--;
        }
        for (int i = 0; i < keyTiles.size(); i++) {
            tiles[keyTiles.get(i)]--;
        }
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < tiles[i]; j++) {
                allHands.add(i);
            }
        }
        Collections.shuffle(allHands, random);
        allHands.addAll(pos * 13, goodHand);
        allHands.addAll(28, keyTiles);
    }


    public int getInit_bak(List<Integer> allHands, int goodPos, int fan) {
        if (fan <= 0) {
            if (goodPos == 0 || goodPos == 1) {
                int betterHandsScore = 0; // 两个手牌的得分间距， 越大越好，超过20分就直接返回
                //挑选优势牌
                for (int roundNum = 0; roundNum < 50; roundNum++) {
                    //初始发牌
                    allHands.clear();
                    List<Integer> randomGameHands = new ArrayList<>();
                    instancce.genRandomInitCards(randomGameHands);
                    //第一种牌
                    int firstHands[] = new int[16];//16种牌的数量，数量
                    int indexGameSequence = 0;
                    for (int i = 0; i < 13; i++) {
                        firstHands[randomGameHands.get(i)]++;
                    }
                    //第二种牌
                    int secondHands[] = new int[16];//16种牌的数量，数量
                    for (int i = 0; i < 13; i++) {
                        secondHands[randomGameHands.get(i + 13)]++;
                    }
                    //评分
                    int valueFirstHands = countDealerValue(firstHands);
                    int valueSecondHands = countDealerValue(secondHands);

                    if (valueFirstHands - valueSecondHands < 20) {//第一种牌好
                        if (goodPos == 1) {//第二个位置牌好
                            allHands.addAll(randomGameHands.subList(13, 26));
                            allHands.addAll(randomGameHands.subList(0, 13));
                            allHands.addAll(randomGameHands.subList(26, 64));
                            break;
                        } else {
                            allHands.addAll(randomGameHands);
                            break;
                        }
                    } else if (valueSecondHands - valueFirstHands > 20) {//第二种牌好
                        if (goodPos == 0) {//第一个位置牌好
                            allHands.addAll(randomGameHands.subList(13, 26));
                            allHands.addAll(randomGameHands.subList(0, 13));
                            allHands.addAll(randomGameHands.subList(26, 64));
                            break;
                        } else {//第二个位置牌好
                            allHands.addAll(randomGameHands);
                            break;
                        }
                    }
                }
            } else {//随机发牌
                instancce.genRandomInitCards(allHands);
            }
        } else {//指定番种，可以生成牌库
            instancce.genRandomInitCards(allHands);
        }
        return random.nextInt(16);
    }

    private void genRandomInitCards(List<Integer> allHands) {
        allHands.clear();
        for (int i = 0; i < 34; i++) {
            for (int j = 1; j < 5; j++) {
                allHands.add(i);
            }
        }
        Collections.shuffle(allHands, random);
    }

    /**
     * panjy 注释
     * 给手牌打分，数值越小牌越好
     *
     * @param hands
     * @return
     */
    private int countDealerValue(int[] hands) {
        //上听分
        List<AIResponse.SplitGroupsHelp> result = new ArrayList<>();
        AIResponse.splitHands(hands, result);
        int minWinDistance = 7;
        for (AIResponse.SplitGroupsHelp sg : result) {
            if (sg.getWinDistance() < minWinDistance) {
                minWinDistance = sg.getWinDistance();
            }
        }
        int value = minWinDistance * 10;
        //字牌分
        int ziValue = 0;
        List<Integer> ziSingle = new ArrayList<>();
        List<Integer> ziDouble = new ArrayList<>();
        List<Integer> ziKe = new ArrayList<>();
        List<Integer> ziGang = new ArrayList<>();
        int pairCount = 0;
        for (int i = 0; i < 16; i++) {
            if (hands[i] == 2) pairCount++;
            if (i >= 9) {
                if (hands[i] == 1)
                    ziSingle.add(i);
                else if (hands[i] == 2)
                    ziDouble.add(i);
                else if (hands[i] == 3)
                    ziKe.add(i);
                else if (hands[i] == 4)
                    ziGang.add(i);
            }
        }

        //单张字牌：每张5分
        //???为啥乘以7
        ziValue += ziSingle.size() * 7;

        if (pairCount > 1) {
            if (pairCount < 4) {
                //字对：第1对7分，第2对起每对10分
                if (ziDouble.size() >= 1 && ziDouble.size() < 3)
                    ziValue += 7 + 10 * (ziDouble.size() - 1);
            }
        }


        //字刻或者杠，0分
        //如果没有字，-10分。
        if (ziSingle.size() == 0 && ziDouble.size() == 0 && ziKe.size() == 0 && ziGang.size() == 0) {
            ziValue -= 10;
        }
        value += ziValue;

        //长度分：万牌的最长连续长度打分。大于等于7，0分；等于6，5分；等于5，10分；小于等于4，20分。
        int maxSequen = 0;
        int maxSequenBegin = 0;
        int maxSequenEnd = 0;
        for (int i = 0; i < 9; i++) {
            int tmpBegin = i;
            int tmpEnd = -1;
            int tmpLen = 1;
            if (hands[i] != 0) {
                int j = i + 1;
                for (; j < 9; j++) {
                    if (hands[j] == 0) {
                        break;
                    }
                    tmpLen++;
                }
                tmpEnd = j;
            }
            if (tmpLen > maxSequen) {
                maxSequen = tmpLen;
                maxSequenBegin = tmpBegin;
                maxSequenEnd = tmpEnd;
            }
        }

        //长度分：万牌的最长连续长度打分。大于等于7，0分；等于6，5分；等于5，10分；小于等于4，20分。
        if (maxSequen >= 7)
            value += 0;
        else if (maxSequen >= 6)
            value += 7;
        else if (maxSequen >= 5)
            value += 14;
        else if (maxSequen >= 4)
            value += 21;
        else if (maxSequen >= 3)
            value += 28;
        else if (maxSequen >= 2)
            value += 35;
        else if (maxSequen >= 1)
            value += 42;
        else
            value += 48;

        //暗刻分：如果万牌存在暗刻，且有暗刻在最长连续牌中间，-10分；如果所有暗刻都不在最长连续牌中间，-5分。
        int anKeValue = 0;
        for (int i = 0; i < 9; i++) {
            if (hands[i] >= 3) {
                if (i > maxSequenBegin && i < maxSequenEnd)
                    anKeValue -= 10;
                else
                    anKeValue -= 5;
            }
        }
        value += anKeValue;
        return value;
    }


    public int nextCard(List<Integer> cardWall, int playHands13[/*2*/][/*13*/],
                        List<List<Group>> groups, boolean isTing[],
                        int curSeat, boolean isGood, int quanfeng) {
        int playHands[][] = new int[4][34];
        playHands[0] = new int[34];
        playHands[1] = new int[34];
        playHands[3] = new int[34];
        playHands[2] = new int[34];
        for (int i = 0; i < 13; i++) {
            if (playHands13[0][i] >= 0) playHands[0][playHands13[0][i]]++;
            if (playHands13[1][i] >= 0) playHands[1][playHands13[1][i]]++;
            if (playHands13[2][i] >= 0) playHands[2][playHands13[2][i]]++;
            if (playHands13[3][i] >= 0) playHands[3][playHands13[3][i]]++;
        }

        int ret = cardWall.get(0);      //拿到当前牌
        int oppoSeat = curSeat == 0 ? 1 : 0; //获取对手位置


        CardValue cards[] = new CardValue[3];//获得牌墙中三张牌
        if (cardWall.size() >= 3) {
            for (int i = 0; i < 3; i++) {
                cards[i] = new CardValue(cardWall.get(i), 30);//初始价值都是30
            }
        } else {//剩下少于三张牌，不进行控制
            return cardWall.remove(0);
        }

        //拆分
        List<AIResponse.SplitGroupsHelp> myResult = new ArrayList<>();//声明自己的手牌拆分
        List<AIResponse.SplitGroupsHelp> otherResult = new ArrayList<>();//声明对手的手牌拆分
        AIResponse.splitHands(playHands[curSeat], myResult);//获得自己的拆分
        AIResponse.splitHands(playHands[oppoSeat], otherResult);//获得自己的拆分


        int wd = AIResponse.getMinWinDistance(myResult);//自己当前上听距离
        int remain[] = new int[34];//剩余牌张,记录的是数量，初始值为0
        for (int stone : cardWall) {
            remain[stone]++;
        }

        Set<Integer> myTing = new TreeSet<>();//声明自己上听集合
        Set<Integer> otherTing = new TreeSet<>();//声明对手上听集合
        int tingCount = 0;//自己上听张数

        if (isTing[curSeat]) {//当前玩家是否报听了
            AIResponse.findTingSet(myTing, myResult,0);//统计自己上听的牌
            for (int tile : myTing) {//统计上听的张数
                tingCount += remain[tile];
            }
        }
        if (isTing[oppoSeat]) {//对手报听过
            AIResponse.findTingSet(otherTing, otherResult,0);//统计对手上听的牌
        }


        int shangzhangshu = -1;//统计自己最短上听距离拆分种最大的上张数
        for (AIResponse.SplitGroupsHelp sg : myResult) {
            if (sg.getWinDistance() == wd) {
                int tmpBu = getBuZhangShu(sg, remain);
                if (tmpBu > shangzhangshu) {
                    shangzhangshu = tmpBu;
                }
            }
        }
        //价值评估
        for (int i = 0; i < 3; i++) {
            if (isTing[curSeat]) {
                if (myTing.contains(cards[i].card)) {
                    if (otherTing.contains(cards[i].getCard())) {
                        cards[i].value = 2;//2.	和牌但打出去点炮
                    } else {
                        cards[i].value = 1;//1.	和牌且打出去不点炮
                    }
                } else {
                    if (otherTing.contains(cards[i].getCard())) {
                        cards[i].value = 4;//4.	不和牌打出去点炮
                    } else {
                        cards[i].value = 3;//3.	不和牌打出去不点炮
                    }
                }
            } else if (myTing.size() > 0) {//如果上听且未报听：
                if (myTing.contains(cards[i].card)) {//1.	和牌
                    if (otherTing.contains(cards[i].getCard())) {
                        cards[i].value = 6;//都能和牌
                    } else {
                        cards[i].value = 5;
                    }
                } else {//2.不和牌
                    if (otherTing.contains(cards[i].getCard())) {
                        cards[i].value = 9;//点炮的
                    } else {
                        //更新抓牌后拆分
                        playHands[curSeat][cards[i].card]++;
                        List<AIResponse.SplitGroupsHelp> tmpResult = new ArrayList<>();
                        AIResponse.splitHands(playHands[curSeat], tmpResult);
                        Set<Integer> newTing = new TreeSet<>();
                        AIResponse.findTingSet(newTing, tmpResult,0);
                        playHands[curSeat][cards[i].card]--;

                        //更新上听数
                        int tmpTingCount = 0;
                        for (int tile : newTing) {
                            tmpTingCount += remain[tile];
                        }
                        //更新价值
                        if (tingCount < tmpTingCount) {
                            cards[i].value = 7;//能使听牌张数变得更多
                        } else {
                            cards[i].value = 8;//不点炮的
                        }
                    }
                }
            } else {
                //更新抓牌后拆分
                playHands[curSeat][cards[i].card]++;
                List<AIResponse.SplitGroupsHelp> tmpResult = new ArrayList<>();
                AIResponse.splitHands(playHands[curSeat], tmpResult);
                int newWd = AIResponse.getMinWinDistance(tmpResult);
                playHands[curSeat][cards[i].card]--;

                if (newWd < wd) {
                    cards[i].value = 10; //1.能使和牌距离减少的
                } else {
                    //重新计算上张数
                    int tmpShangZhangShu = 0;
                    for (AIResponse.SplitGroupsHelp sg : tmpResult) {
                        if (sg.getWinDistance() == newWd) {
                            int tmpBu = getBuZhangShu(sg, remain);
                            if (tmpBu > tmpShangZhangShu) {
                                tmpShangZhangShu = tmpBu;
                            }
                        }
                    }
                    if (tmpShangZhangShu > shangzhangshu) {
                        cards[i].value = 11;//2.	不减少和牌距离，但是增加上张最多的牌
                    } else if (otherTing.contains(cards[i].card)) {
                        cards[i].value = 12;//3.	没用但是不点炮的
                    } else {
                        cards[i].value = 13;//4.	没用但是点炮的
                    }
                }
            }
        }

        if (isGood) {//控制好牌
            int betterValue = 30;
            for (int i = 0; i < 3; i++) {
                if (cards[i].getValue() <= betterValue) {
                    betterValue = cards[i].getValue();
                    ret = cards[i].getCard();
                }
            }
        } else {//控制破牌
            int betterValue = -1;
            for (int i = 0; i < 3; i++) {
                if (cards[i].getValue() >= betterValue) {
                    betterValue = cards[i].getValue();
                    ret = cards[i].getCard();
                }
            }
        }
        cardWall.remove(Integer.valueOf(ret));
        return ret;
    }


    /*
     * 要摸的牌的价值
     */
    private class CardValue {
        int card;
        int value;

        public CardValue(int stone, int value) {
            this.card = stone;
            this.value = value;
        }

        public int getCard() {
            return card;
        }

        public void setCard(int card) {
            this.card = card;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }


    private int getBuZhangShu(AIResponse.SplitGroupsHelp group, int remain[]) {
        int ret = 0;
        for (AIResponse.SplitGroup sg : group.getCurSplitGroup()) {
            switch (sg.getType()) {
                case SPLIT_GROUP_TYPE_PAIR: //对
                    ret += remain[sg.getTile()];
                    break;
                case SPLIT_GROUP_TYPE_LIANZHANG: //连张
                    if (sg.getTile() == 0) {
                        ret += remain[2];
                    } else if (sg.getTile() == 7) {
                        ret += remain[6];
                    } else {
                        ret += remain[sg.getTile() - 1];
                        ret += remain[sg.getTile() + 2];
                    }
                    break;
                case SPLIT_GROUP_TYPE_KANZHANG: //隔张
                    ret += remain[sg.getTile() + 1];
                    break;
                default:
                    ret += 0;
                    break;
            }
        }
        return ret;
    }

    /**
     * 增加计算dora分数
     *
     * @param tiles
     * @return
     */
    static int GetScore(int[] tiles, List<Integer> wall) {
        int windis = GetWindis(tiles);
        int colorScore = GetColorScore(tiles);
        int sanyuanScore = GetSanYuanScore(tiles);
        int ankeScore = GetAnkeScore(tiles);
        int qlscore = GetQingLong(tiles);
        int morethan24score = GetMoreThan24Score(tiles);
        int doraScore = getDoraScore(tiles, wall);
        return windis * (-100) + colorScore + sanyuanScore + ankeScore + qlscore + morethan24score;
    }

    private static int getDoraScore(int[] tiles, List<Integer> wall) {
        int score = 0;
        for (int i = 0; i < 8; i++) {
            int doraSign = wall.get(wall.size() - 5 - i);
            int dora = -1;
            if (doraSign < 27) {
                dora = doraSign / 9 * 9 + (doraSign % 9 + 1) % 9;
            } else if (doraSign < 31) {
                dora = (doraSign - 27 + 1) % 4 + 27;
            } else {
                dora = (dora - 31 + 1) % 3 + 31;
            }
            if (tiles[dora] > 0) {
                if (i < 7) {
                    if (dora < 27 || (dora > 26 && tiles[dora] > 1)) {
                        score += 1 << (6 - i) * tiles[dora];
                    }
                } else {
                    score += tiles[dora];
                }
            }
        }
        return score;
    }

    private static int GetMoreThan24Score(int[] hands) {
        //TO-DO
        //增加大于24番牌打分
        return 0;
    }

    static int GetWindis(int[] hands) {
        List<AIResponse.SplitGroupsHelp> result = new ArrayList<>();
        AIResponse.splitHands(hands, result);
        int minWinDistance = 7;
        for (AIResponse.SplitGroupsHelp sg : result) {
            if (sg.getWinDistance() < minWinDistance) {
                minWinDistance = sg.getWinDistance();
            }
        }
        return minWinDistance;
    }

    static int GetColorScore(int[] tiles) {
        int ziDui = 0;
        int danzi = 0;
        int score = 0;
        int dui = 0;

        for (int i = 9; i < 16; ++i) {
            if (tiles[i] >= 2) {
                ziDui++;
            }
            if (tiles[i] == 1) {
                danzi++;
            }
        }
        for (int i = 0; i < 16; ++i) {
            if (tiles[i] >= 2 && tiles[i] <= 3) {
                dui++;
            }
            if (tiles[i] == 4) {
                dui += 2;
            }
        }
        //没有字对 清一色
        //每张单字-20
        if (ziDui == 0) {
            score += 50;
            score = score - danzi * 20;
        } else if (ziDui >= 1 && ziDui <= 2) {
            //混一色
            score -= danzi * 20;
        } else if (ziDui >= 3) {
            //字一色
            score += 50;
            score = score - danzi * 20;
        }
        //对于非对子牌型
        if (dui < 4 && ziDui <= 2) {
            //万牌如果是连续的 +50
            boolean con = false;
            int wan = 0;
            for (int i = 0; i < 9; ++i) {
                if (tiles[i] > 0 && !con) {
                    con = true;
                    wan++;
                }
                if (con && tiles[i] == 0) {
                    con = false;
                }
            }
            if (wan == 1) {
                score += 50;
            }

        }
        return score;
    }

    static int GetSanYuanScore(int[] tiles) {
        //三种箭牌都有
        int score = 0;
        if (tiles[13] > 0 && tiles[14] > 0 && tiles[15] > 0) {
            int ke = 0;
            int dui = 0;
            int dan = 0;
            for (int i = 13; i < 16; ++i) {
                //同花色算刻则不算对
                if (tiles[i] == 1) {
                    dan += 1;
                } else if (tiles[i] == 2) {
                    dui += 1;
                } else if (tiles[i] >= 3) {
                    ke += 1;
                }
            }
            if (dui == 2 && dan == 1) {
                score += 50;
            } else if (ke == 1 && dui == 1 && dan == 1) {
                score += 100;
            } else if (dui >= 3) {
                score += 300;
            }
        }
        return score;
    }

    static int GetAnkeScore(int[] tiles) {
        int score = 0;
        int ke = 0;
        int dui = 0;
        for (int i = 0; i < 16; ++i) {
            if (tiles[i] >= 3) {
                ke++;
            } else if (tiles[i] == 2) {
                dui++;
            }
        }
        if (ke == 4) {
            score += 500;
        } else if (ke == 3) {
            score += 200;
        } else if (ke >= 2 && dui >= 2) {
            score += 100;
        }
        return score;
    }

    static int GetQingLong(int[] tiles) {
        boolean qlong = true;
        for (int i = 0; i < 9; i++) {
            if (tiles[i] == 0) {
                qlong = false;
                return 0;
            }
        }
        return qlong ? 50 : 0;
    }

    public static void main(String args[]) {

        List<Integer> allHands = new ArrayList<>();
        DealCard.getInstancce().getInit(allHands, 0, -1, 1);
//        DealCard.getInstancce().getInit_bak(allHands, 0, -1);
        long minTime = 100000;
        long maxTime = 0;
        long totalTime = 0;
        for (int i = 0; i < 100000; i++) {
            long startTime = System.currentTimeMillis();
            DealCard.getInstancce().getInit(allHands, 1, 0, 0);
//            DealCard.getInstancce().getInit_bak(allHands, 0, -1);
            long time = System.currentTimeMillis() - startTime;
            if (time > maxTime) maxTime = time;
            if (time < minTime) minTime = time;
            totalTime += time;
            if (i % 1000 == 0 && i > 0) {
                System.out.println(i + "," + minTime + "," + maxTime + "," + totalTime / i);
            }

//        System.out.println("," + minTime + "," + maxTime + "," + totalTime / 100000);
            StringBuilder builder = new StringBuilder();

            for (int j = 0; j < 4; j++) {
                int[] handArr1 = new int[34];
                List<Integer> list = new ArrayList<>(allHands.subList(j * 13, (j + 1) * 13));
                list.sort(Integer::compareTo);
                for (int c : list) {
                    handArr1[c]++;
                    builder.append(CodeUtil.TILE_STRING[c]).append(',');
                }
                builder.append('\n');
                builder.append(getDoraScore(handArr1, allHands.subList(52, allHands.size())));
                builder.append('\n');

            }
            builder.append("dora is :" + TILE_STRING[doraCal(allHands.get(allHands.size() - 5))]);
            builder.append("uradora is :" + TILE_STRING[doraCal(allHands.get(allHands.size() - 6))]);
            System.out.println(builder.toString());
            System.out.println(1);
        }
    }

    private static int doraCal(int doraSign) {
        int dora = -1;
        if (doraSign < 27) {
            dora = doraSign / 9 * 9 + (doraSign % 9 + 1) % 9;
        } else if (doraSign < 31) {
            dora = (doraSign - 27 + 1) % 4 + 27;
        } else {
            dora = (doraSign - 31 + 1) % 3 + 31;
        }
//        System.out.println("指示牌:" + com.wzy.game.server.fanCal.CodeUtil.TILE_STRING[doraSign] + " DORA is" + TILE_STRING[dora]
//        );
        return dora;
    }
}
