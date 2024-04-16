package com.wzy.game.server.ai;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class Group {

    public final static GroupComparator comparator = new GroupComparator();

    private List<Integer> cards = new ArrayList<>();

    private int from;

    boolean isKe(){
        if(cards.size()<3){
            return false;
        }
        return cards.get(0) == cards.get(1) && cards.get(0) == cards.get(2);
    }

    public int getColor(){
        return AIUtils.color(cards.get(0));
    }

    public boolean isGang(){
        return cards.size() == 4;
    }

    public static class GroupComparator implements Comparator<Group>{

        private GroupComparator(){}

        @Override
        public int compare(Group g1, Group g2) {
            if(g1 == null && g2 == null) return 0;
            if(g1 == null) return -1;
            if(g2 == null) return 1;
            int compare = Integer.compare(g1.getCards().size(), g2.getCards().size());
            if (compare != 0) return compare;
            compare = Integer.compare(g1.getColor(), g2.getColor());
            if (compare != 0) return compare;
            for (int i = 0; i < g1.getCards().size(); i++) {
                compare = Integer.compare(g1.getCards().get(i), g2.getCards().get(i));
                if (compare != 0) return compare;
            }
            return Integer.compare(g1.hashCode(), g2.hashCode());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Group group = (Group) o;
        return from == group.from && Objects.equals(cards, group.cards);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cards, from);
    }
}
