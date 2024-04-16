
import java.util.*;
class Player {
    private String name;
    private List<Card> hand;
    private int chips;


    public Player(String name, int chips) {
        this.name = name;
        this.hand = new ArrayList<>();
        this.chips = chips;
    }

    public String getName() {
        return name;
    }

    public void addCard(Card card) {
        hand.add(card);
    }
    
    public List<Card> getHand() {
        return hand;
    }
    
    public int getChips() {
        return chips;
    }
    
    public void addChips(int amount) {
        chips += amount;
    }
    
    public void subtractChips(int amount) {
        chips -= amount;
    }

    public String toString() {
        return name + "'s hand: " + hand + ",Chips: " + chips;
    }
}
public class PlayerData {
    public static List<Player> players;

    ComparePoker comparePoker = new ComparePoker();


    //这一轮中的投注金额
    private int currentBet;

    //表示当前玩家的位置
    private int currentPlayerIndex;

    private Scanner scanner;

    //记录轮次
    public int turn = 0;

    PokerData pokerData = new PokerData();

    public PlayerData() {
        players = new ArrayList<>();
        currentBet = 0;
        currentPlayerIndex = 0;
        scanner = new Scanner(System.in);
    }

    public void addPlayer(Player player) {

        players.add(player);
    }


    public void startGame() {
        pokerData.dealPlayerCards();

        currentPlayerIndex = 0;

    }

    private void nextPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        if (currentPlayerIndex == 0) {
            turn++;
            switch (turn) {
                case 0 :
                    break;
                case 1 :
                    pokerData.dealFlop();
                    break;
                case 2 :
                    pokerData.dealTurn();
                    break;
                case 3 :
                    pokerData.dealRiver();
                    break;
                default:
                    settle();
            }
        }

    }






    //投注自定义数量的筹码，筹码不足时报告
    public void bet(int amount) {
        Player currentPlayer = players.get(currentPlayerIndex);
        if (amount <= currentPlayer.getChips()) {
            currentPlayer.subtractChips(amount);
            currentBet += amount;
        } else {
            System.out.println(currentPlayer + " does not have enough chips to bet.");
        }
        nextPlayer();
    }

    //弃牌
    public void fold() {
        players.remove(currentPlayerIndex);
        nextPlayer();
    }

    //显示手中的牌
    public void showHand() {
        Player currentPlayer = players.get(currentPlayerIndex);
        System.out.println(currentPlayer.getName() + " is showing hand: " + currentPlayer.getHand());
    }

    //输出玩家的手牌和筹码
    public void printHandsAndChips() {
        for (Player player : players) {
            System.out.println(player);
        }
    }

    //输入投注的金额
    private int getUserBet() {
        System.out.print("Enter bet amount: ");
        return scanner.nextInt();
    }

    //找到胜利者
    private List<Player> findWinners() {
        List<Player> winners = new ArrayList<>();
        int maxRankValue = 0;
        for (Player player : players) {
            for (Card card : PokerData.tableCards) {
                player.addCard(card);
            }
            int rankValue = ComparePoker.evaluateHand(player.getHand()).getValue();
            //查看该玩家的牌型分数
//            System.out.println(player);
//            System.out.println(rankValue);
            if (rankValue > maxRankValue && maxRankValue == 0) {
                maxRankValue = rankValue;
                winners.add(player);
            } else if (rankValue > maxRankValue && maxRankValue != 0) {
                maxRankValue = rankValue;
                winners.clear();
                winners.add(player);
            } else if (rankValue == maxRankValue) {
                ComparePoker.compareCards(winners.get(0).getHand(), player.getHand());
                winners.add(player);
            }
        }
        return winners;
    }

    //结算
    public void settle() {
        List<Player> winners = findWinners();
        if (winners.size() == 0)
            return;
        if (winners.size() == 1) {
            //胜利者得到筹码
            winners.get(0).addChips(currentBet);
        } else if (winners.size() > 1) {
            //平局时平分奖池中的筹码
            int chipsPerWinner = currentBet / winners.size();
            for (Player winner : winners) {
                winner.addChips(chipsPerWinner);
            }
        }
        //结算后奖池中的筹码归零
        currentBet = 0;
        for (Player player : winners) {
            System.out.println(player.getName());
        }
        System.out.print("is winner!");
        for (Player player : winners) {
            System.out.println(player);
        }
        for (int i = 0; i <= players.size(); i++) {
            players.remove(0);
        }
        System.out.println("结束");
    }

    //主要游戏逻辑
    public void playGame() {
        startGame();
        while (players.size() > 1) {
            Player currentPlayer = players.get(currentPlayerIndex);
            System.out.println("Current player: " + currentPlayer.getName());
            showHand();

            //询问玩家下注，看牌或弃牌
            System.out.print("Enter action (bet/fold): ");
            String action = scanner.next();

            switch (action.toLowerCase()) {
                case "bet":
                    int betAmount = getUserBet();
                    bet(betAmount);
                    break;
                case "fold":
                    fold();
                    break;
                default:
                    System.out.println("Invalid action.");
            }
        }
        settle();
        //游戏结束时打印最后一位玩家的手牌和筹码
        printHandsAndChips();

    }


}
