package org.sully.d2;

import lombok.Builder;
import lombok.Value;
import org.sully.d2.itemtracking.D2TCDropConsumer;
import org.sully.d2.itemtracking.DropContextEnum;
import org.sully.d2.itemtracking.TCDropConsumerSnapshot;
import org.sully.d2.server.ConsumerSummary;
import org.sully.d2.server.SingleDropContextSummary;
import org.sully.d2.server.SummaryInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Value
@Builder
public class DataSnapshot {

    Map<Long, SerializableD2Item> itemsById;
    List<SingleDropContextSnapshot> dropContexts;
    long nextItemId;

    public void logSummary(Consumer<String> printer) {
        printer.accept("Data Snapshot contains " + itemsById.size() + " items");
        printer.accept("nextItemId = " + nextItemId);
        for (SingleDropContextSnapshot dropContext : dropContexts) {
            printer.accept("DropContext " + dropContext.getDropContextName());
            for (TCDropConsumerSnapshot consumer : dropContext.getConsumers()) {
                printer.accept("  Consumer " + consumer.getName() + " consumed " + consumer.getTotalIterations() + " drops, type = " + consumer.getClass().getSimpleName());
            }

        }
        printer.accept("-- end of data snapshot summary --");
    }

    public void addSnapshotDataToConsumers(Map<DropContextEnum,List<D2TCDropConsumer>> consumersByDropContext) {

        for (SingleDropContextSnapshot dropContextSnapshot : dropContexts) {
            DropContextEnum dropContext = DropContextEnum.valueOf(dropContextSnapshot.getDropContextName());
            if (!consumersByDropContext.containsKey(dropContext)) {
                System.out.println("Warning : Found DropContext " + dropContext.name() + " in snapshot, but there are no consumers configured for it in current run");
                continue;
            }

            Map<String, TCDropConsumerSnapshot> consumerSnapshotsByName = dropContextSnapshot.getConsumers().stream()
                    .collect(Collectors.toMap(TCDropConsumerSnapshot::getName, Function.identity()));

            for (D2TCDropConsumer consumer : consumersByDropContext.get(dropContext)) {
                if (consumerSnapshotsByName.containsKey(consumer.getName())) {
                    consumer.initializeFromSnapshot(consumerSnapshotsByName.get(consumer.getName()), itemsById);
                }
            }
        }
    }

    public SummaryInfo toSummaryObject() {
        List<SingleDropContextSummary> dropContextSummaries = new ArrayList<>();
        for (SingleDropContextSnapshot dropContextSnapshot : dropContexts) {

            List<ConsumerSummary> consumerSummaries = new ArrayList<>();
            for (TCDropConsumerSnapshot consumer : dropContextSnapshot.getConsumers()) {
                consumerSummaries.add(consumer.toSummaryObject());
            }
            dropContextSummaries.add(SingleDropContextSummary.builder()
                    .dropContext(DropContextEnum.valueOf(dropContextSnapshot.getDropContextName()))
                    .consumers(consumerSummaries)
                    .build());
        }
        return SummaryInfo.builder().dropContexts(dropContextSummaries).build();
    }
}

