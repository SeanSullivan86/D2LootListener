package org.sully.d2.itemtracking;

import lombok.Builder;
import lombok.Value;
import org.sully.d2.server.ConsumerSummary;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Value
@Builder
public class AssortedMagicItemsSnapshot implements TCDropConsumerSnapshot {

    String name;
    long totalIterations;

    private long[][] counts;
    private Long[][] exampleItems;
    private List<String> categories;

    @Override
    public Set<Long> getReferencedItemIds() {
        Set<Long> result = new HashSet<>();
        for (int i = 0; i < exampleItems.length; i++) {
            for (int j = 0; j < 2; j++) {
                if (exampleItems[i][j] != null) {
                    result.add(exampleItems[i][j]);
                }
            }
        }
        return result;
    }

    @Override
    public ConsumerSummary toSummaryObject() {
        return ConsumerSummary.builder()
                .consumerName(name)
                .consumerType(this.getClass().getSimpleName())
                .additionalInfo(null)
                .build();
    }
}
