package org.sully.d2.itemtracking;

import lombok.Builder;
import lombok.Value;

import java.util.Comparator;

@Value
@Builder
public class ItemAndScore<T> {
    T item;
    int score;
    long sequenceNumber;

    public static Comparator<ItemAndScore<?>> comparator = (a, b) -> {
        int scoreCompare = Integer.compare(b.getScore(), a.getScore());
        if (scoreCompare != 0) return scoreCompare;
        return Long.compare(a.getSequenceNumber(), b.getSequenceNumber());
    };
}
