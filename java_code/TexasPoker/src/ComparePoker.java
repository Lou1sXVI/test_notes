
import java.util.*;

public class ComparePoker {



    //比较两手牌的大小
    public static int compareCards(List<Card> play1, List<Card> play2) {
        //给两组牌的牌型进行打分
        HandRank rank1 = evaluateHand(play1);
        HandRank rank2 = evaluateHand(play2);

        if (rank1.getValue() > rank2.getValue()) {
            return 1;
        } else if (rank1.getValue() < rank2.getValue()) {
            return -1;
        } else {
            //牌型相同时，对牌组进行排序后比较最大牌的大小
            List<Integer> play1Values = getSortedValues(play1);
            List<Integer> play2Values = getSortedValues(play2);

            for (int i = play1Values.size() - 1; i>=0; i--) {
                if (play1Values.get(i) > play2Values.get(i)) {
                    return 1;
                } else if (play1Values.get(i) < play2Values.get(i)) {
                    return -1;
                }
            }
            //牌型相同且最大牌也相同
            return 0;
        }
    }


    public static HandRank evaluateHand(List<Card> hand) {
        if (isRoyalFlush(hand)) {
            return HandRank.ROYAL_FLUSH;
        } else if (isStraightFlush(hand)) {
            return HandRank.STRAIGHT_FLUSH;
        } else if (isFourOfAKind(hand)) {
            return HandRank.FOUR_OF_A_KIND;
        } else if (isFullHouse(hand)) {
            return HandRank.FULL_HOUSE;
        } else if (isFlush(hand)) {
            return HandRank.FLUSH;
        } else if (isStraight(hand)) {
            //顺子中存在特殊情况最小顺子A2345在判断最大牌时会比所有顺子都大，实际为最小顺子
            List<Integer> values = getSortedValues(hand);
            if(values.contains(1) && values.contains(2) && values.contains(3) && values.contains(4) && values.contains(5)) {
                return HandRank.STRAIGHTHALF;
            }
            return HandRank.STRAIGHT;
        } else if (isThreeOfAKind(hand)) {
            return HandRank.THREE_OF_A_KIND;
        } else if (isTwoPair(hand)) {
            return HandRank.TWO_PAIR;
        } else if (isPair(hand)) {
            return HandRank.PAIR;
        } else {
            return HandRank.HIGH_CARD;
        }
    }

    private static boolean isRoyalFlush(List<Card> hand) {
        return isStraightFlush(hand) && hasAce(hand) && hasKing(hand);
    }

    private static boolean isStraightFlush(List<Card> hand) {
        return isFlush(hand) && isStraight(hand);
    }

    private static boolean isFourOfAKind(List<Card> hand) {
        Map<Integer, Integer> rankCounts = countRanks(hand);
        return rankCounts.containsValue(4);
    }

    private static boolean isFullHouse(List<Card> hand) {
        Map<Integer, Integer> rankCounts = countRanks(hand);
        return rankCounts.containsValue(3) && rankCounts.containsValue(2);
    }

    private static boolean isFlush(List<Card> hand) {
        String suit = hand.get(0).getSuit();
        for (Card card : hand) {
            if (!card.getSuit().equals(suit)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isStraight(List<Card> hand) {
        List<Integer> values = getSortedValues(hand);
        //如果有A，判断将A视为1时候为顺子的情况
        if (values.contains(14)) {
            values.add(1);
        }
        for (int i = 0; i < values.size() - 1; i++) {
            if(values.get(i + 1) - values.get(i) != 1) {
                return false;
            }

        }
        return true;
    }
    private static boolean isThreeOfAKind(List<Card> hand) {
        Map<Integer, Integer> rankCounts = countRanks(hand);
        return rankCounts.containsValue(3);
    }

    private static boolean isTwoPair(List<Card> hand) {
        Map<Integer, Integer> rankCounts = countRanks(hand);
        int pairCount = 0;
        for (int count : rankCounts.values()) {
            if (count == 2) {
                pairCount++;
            }
        }
        return pairCount == 2;
    }

    private static boolean isPair(List<Card> hand) {
        Map<Integer, Integer> rankCounts = countRanks(hand);
        return rankCounts.containsValue(2);
    }

    private static boolean hasAce(List<Card> hand) {
        for (Card card : hand) {
            if (card.getRank().equals("Ace")) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasKing(List<Card> hand) {
        for (Card card : hand) {
            if (card.getRank().equals("King")) {
                return true;
            }
        }
        return false;
    }

    //统计手牌中每种点数的数量
    private static Map<Integer, Integer> countRanks(List<Card> hand) {
        Map<Integer, Integer> rankCounts = new HashMap<>();
        for (Card card : hand) {
            int value = card.getValue();
            rankCounts.put(value, rankCounts.getOrDefault(value, 0) + 1);
        }
        return rankCounts;
    }

    //获取手牌中的点数并排序
    private static List<Integer> getSortedValues(List<Card> hand) {
        List<Integer> values = new ArrayList<>();
        for (Card card : hand) {
            values.add(card.getValue());
        }
        Collections.sort(values);
        return values;
    }





    enum HandRank {
        HIGH_CARD(1),
        PAIR(2),
        TWO_PAIR(3),
        THREE_OF_A_KIND(4),
        STRAIGHT(6),
        //将A2345这种特殊情况牌型列为单独的分值，即比所有顺子的分数更低
        STRAIGHTHALF(5),
        FLUSH(7),
        FULL_HOUSE(8),
        FOUR_OF_A_KIND(9),
        STRAIGHT_FLUSH(10),
        ROYAL_FLUSH(11);

        private final int value;

        HandRank(int value) {
            this.value = value;
        }

        //得到牌型的分值
        public int getValue() {
            return value;
        }
    }
}
