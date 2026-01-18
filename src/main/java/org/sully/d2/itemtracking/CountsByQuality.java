package org.sully.d2.itemtracking;

import lombok.Value;
import org.sully.d2.gamemodel.D2Item;

import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class CountsByQuality implements ItemConsumer {

    AtomicLongArray counts = new AtomicLongArray(10);
    AtomicLongArray lastSavedCounts = new AtomicLongArray(10);
    AtomicReferenceArray<D2Item> mostRecentItemsByQuality = new AtomicReferenceArray<>(10);
    Long lastSavedTimestamp = null;

    @Override
    public void consume(D2Item item, ItemNotifier notifier) {
        counts.incrementAndGet(item.getQuality().id);
    }

    public AtomicReferenceArray<D2Item> getMostRecentItemsByQuality() {
        return mostRecentItemsByQuality;
    }

    public AtomicLongArray getCounts() {
        return counts;
    }

    public IntervalDurationAndCountsByQuality takeSnapshotOfCountsAndReportCountsSinceLastSnapshot() {
        long newTimestamp = System.nanoTime();

        long[] newTotalCounts = new long[counts.length()];
        long[] countDiff = new long[counts.length()];
        for (int i = 0; i < counts.length(); i++) {
            newTotalCounts[i] = counts.get(i);
            countDiff[i] = newTotalCounts[i] - lastSavedCounts.get(i);
            lastSavedCounts.set(i, newTotalCounts[i]);
        }

        if (lastSavedTimestamp == null) {
            lastSavedTimestamp = newTimestamp;
            return null;
        }

        double msSinceLastSnapshot = (newTimestamp - lastSavedTimestamp) / 1_000_000.0;
        lastSavedTimestamp = newTimestamp;

        return new IntervalDurationAndCountsByQuality(countDiff, newTotalCounts, msSinceLastSnapshot);
    }

    @Override
    public void closeAndGenerateOutput() {

    }

    @Value
    public static class IntervalDurationAndCountsByQuality {
        long[] countDiffsByQuality;
        long[] totalCountsByQuality;
        double millisecondsSinceLastSnapshot;
    }
}

