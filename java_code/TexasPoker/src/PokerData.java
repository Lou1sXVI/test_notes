

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


class Card {
    private String suit;
    private String rank;


    public Card(String suit, String rank) {
        this.suit = suit;
        this.rank = rank;
    }



    public String toString() {
        return suit + " of " + rank;
    }

    public String getRank() {
        return rank;
    }

    public String getSuit() {
        return suit;
    }


    public int getValue() {
        if (getRank().equals("2")) {
            return 2;
        } else if (getRank().equals("3")) {
            return 3;
        } else if (getRank().equals("4")) {
            return 4;
        } else if (getRank().equals("5")) {
            return 5;
        } else if (getRank().equals("6")) {
            return 6;
        } else if (getRank().equals("7")) {
            return 7;
        } else if (getRank().equals("8")) {
            return 8;
        } else if (getRank().equals("9")) {
            return 9;
        } else if (getRank().equals("10")) {
            return 10;
        } else if (getRank().equals("Jack")) {
            return 11;
        } else if (getRank().equals("Queen")) {
            return 12;
        } else if (getRank().equals("King")) {
            return 13;
        } else  {
            return 14;
        }
    }

}

public class PokerData {
    private static List<Card> deck;

    static List<Card> tableCards;

    public PokerData() {
        initDeck();
    }



    private void initDeck() {
        String[] suits = {"Heart", "Diamond", "Club", "Spade"};
        String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "Jack", "Queen", "King", "Ace"};

        deck = new ArrayList<>();

        tableCards = new ArrayList<>();

        //把每个花色每个大小的牌都放入List
        for (String suit : suits) {
            for (String rank : ranks) {
                deck.add(new Card(suit, rank));
            }
        }

        //对List进行洗牌操作
        Collections.shuffle(deck);
    }


    //给每个玩家发自定义数量的牌
    public List<Card> dealHandCards(int numCards) {
        List<Card> hand = new ArrayList<>();

        for (int i = 0; i < numCards; i++) {
            hand.add(deck.remove(0));
        }

        return hand;
    }

    //给每个玩家发两张牌
    public void dealPlayerCards() {
        for (Player player : PlayerData.players) {
            for (int i = 0; i < 2; i++) {
                player.addCard(deck.remove(0));
            }
        }
    }

    //查看手中的牌
    public void printHand(List<Card> hand) {
        for (Card card : hand) {
            System.out.println(card);
        }
    }

    //给桌子上发自定义数量的牌
    public List<Card> dealTableCard(int numCards) {
        List<Card> table = new ArrayList<>();

        for (int i = 0; i < numCards; i++) {
            table.add(deck.remove(0));
        }

        return table;
    }

    //发翻牌圈(Flop)
    public void dealFlop() {
        for (int i = 0; i < 3; i++) {
            tableCards.add(deck.remove(0));
        }
        System.out.println("翻牌(FLOP):");
        printTable();
    }

    //发转牌圈(Turn)
    public void dealTurn() {
        for (int i = 0; i < 1; i++) {
            tableCards.add(deck.remove(0));
        }
        System.out.println("转牌(TURN):");
        printTable();
    }

    //发河牌圈(River)
    public void dealRiver() {
        for (int i = 0; i < 1; i++) {
            tableCards.add(deck.remove(0));
        }
        System.out.println("河牌(RIVER):");
        printTable();
    }


    //查看桌子上的牌
    public void printTable() {
        for (Card card : tableCards) {
            System.out.print(card + ", ");
        }
        System.out.println();
    }

//    public static void main(String[] args) {
//        PokerData game = new PokerData();
//
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
////        System.out.println(game.deck);
//    }
}