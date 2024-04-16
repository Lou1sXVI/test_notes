

import java.util.ArrayList;
import java.util.List;


public class Main {
    public static void main(String[] args) {
        //洗牌操作
        PokerData game = new PokerData();
        //对牌型进行判断的函数
        ComparePoker comparePoker = new ComparePoker();
        //定义玩家的名字和筹码
        PlayerData players = new PlayerData();
        Player player1 = new Player("Player 1", 100);
        Player player2 = new Player("Player 2", 100);
        //将玩家添加到List中
        players.addPlayer(player1);
        players.addPlayer(player2);
        //开始游戏，将洗牌和对牌型进行判断集合在一个函数中
        players.playGame();



//        //把给每个玩家发牌2张手牌的操作放在一个函数
//        game.dealPlayerCards();
//        players.printHandsAndChips();
//
//        //给桌子发牌
//        List<Card> tableCard1 = game.dealTableCard(3);
//        System.out.println("First Table Cards:");
//        game.printTable(tableCard1);
//        System.out.println();
//        List<Card> tableCard2 = game.dealTableCard(2);
//        System.out.println("Second Table Cards:");
//        game.printTable(tableCard2);


//        给玩家和桌子发牌
//        List<Card> player1Hand = game.dealHandCards(2);
//        List<Card> player2Hand = game.dealHandCards(2);
//        List<Card> tableCard1 = game.dealTableCard(3);
//        List<Card> tableCard2 = game.dealTableCard(2);
//
//        System.out.println("Player 1's hand:");
//        game.printHand(player1Hand);
//
//        System.out.println("Player 2's hand:");
//        game.printHand(player2Hand);
//
//        System.out.println("First Table Cards:");
//        game.printTable(tableCard1);
//
//        System.out.println("All Table Cards:");
//        game.printTable(tableCard1);
//        game.printTable(tableCard2);
//
//
//        把桌子上的牌合起来
//        List<Card> tableCards = new ArrayList<>();
//        tableCards.addAll(tableCard1);
//        tableCards.addAll(tableCard2);
//
//        把桌子上的牌和玩家的牌合起来
//        List<Card> player1Cards = new ArrayList<>();
//        player1Cards.addAll(player1Hand);
//        player1Cards.addAll(tableCards);
//        List<Card> player2Cards = new ArrayList<>();
//        player2Cards.addAll(player2Hand);
//        player2Cards.addAll(tableCards);
//
//        检查是否正确合并
//        System.out.println("Player1Cards:");
//        for(Card card : player1Cards) {
//            System.out.println(card);
//        }
//        System.out.println("Player2Cards:");
//        for(Card card : player2Cards) {
//            System.out.println(card);
//        }

//        测试同为顺子的A2345和23456牌型比大小时候的能否正确判断
//        List<Card> player1Cards = new ArrayList<>();
//        List<Card> player2Cards = new ArrayList<>();
//        player1Cards.add(new Card("Spade", "Ace"));
//        player1Cards.add(new Card("Club", "2"));
//        player1Cards.add(new Card("Heart", "3"));
//        player1Cards.add(new Card("Spade", "4"));
//        player1Cards.add(new Card("Spade", "5"));
//        player2Cards.add(new Card("Spade", "6"));
//        player2Cards.add(new Card("Club", "2"));
//        player2Cards.add(new Card("Heart", "3"));
//        player2Cards.add(new Card("Spade", "4"));
//        player2Cards.add(new Card("Spade", "5"));
//
//        检查是否正确输入牌型
//        System.out.println("Player1Cards:");
//        for(Card card : player1Cards) {
//            System.out.println(card);
//        }
//        System.out.println("Player2Cards:");
//        for(Card card : player2Cards) {
//            System.out.println(card);
//        }

//        判断两副牌的大小并输出结果
//        if(comparePoker.compareCards(player1Cards, player2Cards) == 1) {
//            System.out.println("Player1 win!");
//        } else if(comparePoker.compareCards(player1Cards, player2Cards) == -1) {
//            System.out.println("Player2 win!");
//        } else {
//            System.out.println("平局!");
//        }

    }
}