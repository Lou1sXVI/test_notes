package com.wzy.game.server.util;

import java.util.Random;

public class RandomUtil {
    private final static Random random = new Random(System.currentTimeMillis());

    private static RandomUtil instance = new RandomUtil();

    private RandomUtil(){}

    public static int nextInt(int bound){
        return random.nextInt(bound);
    }
}

