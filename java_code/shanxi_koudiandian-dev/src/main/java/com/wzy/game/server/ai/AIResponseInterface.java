package com.wzy.game.server.ai;

public interface AIResponseInterface {
    /**
     * 推荐打牌，AI托管真人，AI打牌 调用这个
     * @param act
     */
    void doAction(ParamMode act);
}
