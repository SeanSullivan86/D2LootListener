package org.sully.d2.itemtracking;

import lombok.Getter;
import org.sully.d2.gamemodel.D2Item;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public class BasicStatsConsumer implements D2TCDropConsumer {

    @Getter
    long totalIterations = 0L;

    long[] countsByQuality = new long[10];
    D2Item[] mostRecentItemByQuality = new D2Item[10];

    @Getter
    String name;

    public BasicStatsConsumer(String name) {
        this.name = name;
    }

    @Override
    public void initializeFromSnapshot(TCDropConsumerSnapshot untypedSnapshot, Map<Long, D2Item> itemsById) {
        BasicStatsSnapshot snapshot = (BasicStatsSnapshot) untypedSnapshot;
        for (int i = 0; i < 10; i++) {
            countsByQuality[i] = snapshot.getCountsByQuality()[i];
            Long itemId = snapshot.getMostRecentItemIdsByQuality()[i];
            mostRecentItemByQuality[i] = itemId == null ? null : itemsById.get(itemId);
        }
        this.totalIterations = snapshot.getTotalIterations();
    }

    @Override
    public void consume(D2TCDrop tcDrop) {
        for (D2Item item : tcDrop.getItems()) {
            int quality = item.getQuality().id;
            countsByQuality[quality]++;
            mostRecentItemByQuality[quality] = item;
        }
        totalIterations++;
    }

    @Override
    public DataReferencingItems<TCDropConsumerSnapshot> takeSnapshot() {
        long[] countsByQuality = new long[10];
        Long[] itemIdsByQuality = new Long[10];
        for (int i = 0; i < 10; i++) {
            countsByQuality[i] = this.countsByQuality[i];
            itemIdsByQuality[i] = this.mostRecentItemByQuality[i] == null ? null : this.mostRecentItemByQuality[i].getId();
        }

        return DataReferencingItems.<TCDropConsumerSnapshot>builder()
                .data(BasicStatsSnapshot.builder()
                        .countsByQuality(countsByQuality)
                        .mostRecentItemIdsByQuality(itemIdsByQuality)
                        .name(name)
                        .build())
                .items(Arrays.stream(this.mostRecentItemByQuality).filter(Objects::nonNull).toList())
                .build();
    }

}

