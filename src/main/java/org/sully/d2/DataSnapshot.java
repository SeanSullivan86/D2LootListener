package org.sully.d2;

import lombok.Builder;
import lombok.Value;
import org.sully.d2.itemtracking.D2TCDropConsumer;
import org.sully.d2.itemtracking.DropContextEnum;
import org.sully.d2.itemtracking.TCDropConsumerSnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Value
@Builder
public class DataSnapshot {

    String id;
    Map<Long, SerializableD2Item> itemsById;
    Map<DropContextEnum, SingleDropContextSnapshot> dropContexts;
    SingleDropContextSnapshot aggregatesOverAllDropContexts;
    long nextItemId;

    public void logSummary(Consumer<String> printer) {
        printer.accept("Data Snapshot contains " + itemsById.size() + " items");
        printer.accept("nextItemId = " + nextItemId);
        for (SingleDropContextSnapshot dropContext : dropContexts.values()) {
            printer.accept("DropContext " + dropContext.getDropContextName());
            for (TCDropConsumerSnapshot consumer : dropContext.getConsumersById().values()) {
                printer.accept("  Consumer " + consumer.getId() + " consumed " + consumer.getTotalIterations() + " drops, type = " + consumer.getClass().getSimpleName());
            }

        }
        printer.accept("-- end of data snapshot summary --");
    }

    public void addSnapshotDataToConsumers(Map<DropContextEnum,List<D2TCDropConsumer>> consumersByDropContext) {

        for (SingleDropContextSnapshot dropContextSnapshot : dropContexts.values()) {
            DropContextEnum dropContext = DropContextEnum.valueOf(dropContextSnapshot.getDropContextName());
            if (!consumersByDropContext.containsKey(dropContext)) {
                System.out.println("Warning : Found DropContext " + dropContext.name() + " in snapshot, but there are no consumers configured for it in current run");
                continue;
            }

            Map<String, TCDropConsumerSnapshot> consumerSnapshotsById = dropContextSnapshot.getConsumersById();

            for (D2TCDropConsumer consumer : consumersByDropContext.get(dropContext)) {
                if (consumerSnapshotsById.containsKey(consumer.getId())) {
                    consumer.incrementFromSnapshot(consumerSnapshotsById.get(consumer.getId()), itemsById);
                }
            }
        }
    }

    public SingleConsumerDataWithItems getSingleConsumerData(DropContextEnum dropContext, String consumerId) {
        if (dropContexts.containsKey(dropContext)) {
            SingleDropContextSnapshot singleDropContextSnapshot = dropContexts.get(dropContext);

            if (singleDropContextSnapshot.getConsumersById().containsKey(consumerId)) {
                TCDropConsumerSnapshot consumerSnapshot = singleDropContextSnapshot.getConsumersById().get(consumerId);
                Map<Long, SerializableD2Item> itemsToReturn = new HashMap<>();
                for (Long itemId : consumerSnapshot.getReferencedItemIds()) {
                    itemsToReturn.put(itemId, itemsById.get(itemId));
                }

                return SingleConsumerDataWithItems.builder()
                        .dropContext(dropContext)
                        .consumerId(consumerId)
                        .data(consumerSnapshot)
                        .items(itemsToReturn)
                        .build();
            }
        }
        return null;
    }
}

