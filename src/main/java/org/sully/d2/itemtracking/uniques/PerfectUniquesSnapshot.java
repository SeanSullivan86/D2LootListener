package org.sully.d2.itemtracking.uniques;

import lombok.Builder;
import lombok.Value;
import org.sully.d2.itemtracking.TCDropConsumerSnapshot;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Value
@Builder
public class PerfectUniquesSnapshot implements TCDropConsumerSnapshot {

    String id;
    long totalIterations;
    Map<String,UniqueStatsSnapshot> statsByName;


    @Override
    public Set<Long> getReferencedItemIds() {
        Set<Long> itemIds = new HashSet<>();
        for (UniqueStatsSnapshot statsSnapshot : statsByName.values()) {
            if (statsSnapshot.getBestItemId() != null) {
                itemIds.add(statsSnapshot.getBestItemId());
            }
            if (statsSnapshot.getBestEthItemId() != null) {
                itemIds.add(statsSnapshot.getBestEthItemId());
            }
        }
        return itemIds;
    }

}
