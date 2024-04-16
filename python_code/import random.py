import random

class Mahjong:
    def __init__(self):
        self.tiles = []
        self.players = []
        self.initialize_tiles()
    
    def initialize_tiles(self):
        # 初始化麻将牌，四副牌，每副牌有1-9万、1-9条、1-9筒以及东南西北中发白
        self.tiles = [str(i) for i in range(1, 10)] * 3
        self.tiles.extend(['东', '南', '西', '北', '中', '发', '白'] * 4)
        random.shuffle(self.tiles)
    
    def add_player(self, player):
        # 添加玩家
        self.players.append(player)
    
    def deal_tiles(self):
        # 发牌
        for _ in range(13):
            for player in self.players:
                tile = self.tiles.pop(0)
                player.add_tile(tile)
    
    def play_game(self):
        # 开始游戏
        for player in self.players:
            print(f"{player.name}的手牌: {player.tiles}")
            if player.is_win():
                print(f"{player.name}胡牌了！")
                break
            else:
                print(f"{player.name}没有胡牌")

class Player:
    def __init__(self, name):
        self.name = name
        self.tiles = []
    
    def add_tile(self, tile):
        # 玩家获得一张牌
        self.tiles.append(tile)
    
    def is_win(self):
        # 判断玩家是否胡牌（这里简化为手牌为13张即胡牌）
        return len(self.tiles) == 13

# 测试
mahjong = Mahjong()
player1 = Player("玩家1")
player2 = Player("玩家2")
mahjong.add_player(player1)
mahjong.add_player(player2)
mahjong.deal_tiles()
mahjong.play_game()
