package org.sully.d2.itemtracking;

import lombok.Builder;
import lombok.Value;
import org.sully.d2.gamemodel.D2Item;

import java.util.Comparator;

@Value
@Builder
public class ItemAndScore {
    D2Item item;
    int score;

    public static Comparator<ItemAndScore> comparator = (a, b) -> {
        int scoreCompare = Integer.compare(b.getScore(), a.getScore());
        if (scoreCompare != 0) return scoreCompare;
        return Long.compare(a.getItem().getId(), b.getItem().getId());
    };
}
