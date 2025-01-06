package org.sully.d2.itemtracking;

import lombok.Builder;
import lombok.Value;
import org.sully.d2.gamemodel.D2Item;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class BasicStatsConsumer implements ItemConsumer {

    AtomicLongArray counts = new AtomicLongArray(10);
    AtomicReferenceArray<D2Item> mostRecentItemsByQuality = new AtomicReferenceArray<>(10);
    AtomicLong minDropCount;

    @Override
    public void consume(D2ItemDrop item, ItemNotifier notifier) {
        counts.incrementAndGet(item.getItem().getQuality().id);
        mostRecentItemsByQuality.set(item.getItem().getQuality().id, item.getItem());
    }

    public BasicStats getStats() {
        D2Item[] x = new D2Item[10];
        long[] c = new long[10];
        for (int i = 0; i < 10; i++) {
            x[i] = mostRecentItemsByQuality.get(i);
            c[i] = counts.get(i);
        }

        return BasicStats.builder()
                .countsByQuality(c)
                .mostRecentItemsByQuality(x)
                .build();
    }

    @Override
    public void closeAndGenerateOutput() {

    }

}

