package org.sully.d2.itemtracking;

import lombok.Builder;
import lombok.Value;
import org.sully.d2.server.ConsumerSummary;

import java.util.List;
import java.util.Set;

@Value
@Builder
public class ItemGridSnapshot implements TCDropConsumerSnapshot {
	List<String> rowValues;
	List<String> columnValues;

	long[][] counts;
	long totalIterations;

	String id;

	@Override
	public Set<Long> getReferencedItemIds() {
		return Set.of();
	}

	@Override
	public ConsumerSummary toSummaryObject() {
		return ConsumerSummary.builder()
				.consumerId(id)
				.consumerType(this.getClass().getSimpleName())
				.additionalInfo(null)
				.build();
	}
}
