package com.wzy.game.server.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

class FanTiles {

    private Set<Integer> keyTiles;

    private Set<Integer> usefulTiles;

    public FanTiles() {
        keyTiles = new HashSet<>();
        usefulTiles = new HashSet<>();
    }

    static FanTiles fanArrayToFanTiles(int[] fanType) {
        FanTiles ft = new FanTiles();
        for (int i = 0; i < fanType.length; i++) {
            if (fanType[i] == 3 || fanType[i] == 2) {
                ft.insertKeyTiles(i);
            } else if (fanType[i] == 1) {
                ft.insertUsefulTiles(i);
            }
        }
        return ft;
    }

    public void insertKeyTiles(int t) {
        keyTiles.add(t);
    }

    public void insertUsefulTiles(int t) {
        usefulTiles.add(t);
    }

    public boolean isKeyTile(int t) {
        return keyTiles.contains(t);
    }

    public boolean isUsefulTile(int t) {
        return usefulTiles.contains(t);
    }

    public int usefulNum() {
        return 5 - keyTiles.size();
    }

    public Set<Integer> getKeyTiles() {
        return Collections.unmodifiableSet(keyTiles);
    }

    public Set<Integer> getUsefulTiles() {
        return Collections.unmodifiableSet(usefulTiles);
    }
}
