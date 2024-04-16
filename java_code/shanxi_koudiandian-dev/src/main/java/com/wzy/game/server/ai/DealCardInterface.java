package com.wzy.game.server.ai;

import java.util.List;

public interface DealCardInterface {
    /**
     * 发牌接口
     * @param allHands 返回所有的牌[0,13)张牌是位置0的牌，[13-26)张牌是位置1的牌，以此类推{52}是庄家牌，余下的是牌墙
     * @param goodPos   好牌的位置 {0,1,...} 其他任何值都是为随机发牌
     * @param fan       控制的AI的番种上限（-1表示不控制）
     * @param bankSeat       庄家位置
     * @return  返回幸运牌
     */
    int getInit(List<Integer> allHands, int goodPos, int fan, int bankSeat);

    /**
     * 获得牌墙的下一张牌
     * @param cardWall 剩余牌墙
     * @param playHands 两个玩家的手牌，牌背是-1
     * @param groups    两个玩家的碰杠数组，
     * @param curSeat   当前要拿牌的玩家
     * @param isGood    是否给这个玩家好牌
     * @return
     */
    int nextCard(List<Integer> cardWall, int playHands[/*2*/][/*13*/],
                    List<List<Group>> groups, boolean isTing[],
                        int curSeat, boolean isGood,
                        int quanfeng);
}
