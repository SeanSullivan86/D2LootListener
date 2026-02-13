package org.sully.d2.itemtracking;

import lombok.Builder;
import lombok.Value;
import org.sully.d2.server.ConsumerSummary;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Value
@Builder
public class TopNSnapshot implements TCDropConsumerSnapshot {

	TopNAndDistributionSnapshot stats;
	long totalIterations;
	long countMatchingItemCodeAndQuality;
	long countMatchingAdditionalCriteria;
	String id;

	@Override
	public Set<Long> getReferencedItemIds() {
		return new HashSet<>(stats.getTopItemIds());
	}

	@Override
	public ConsumerSummary toSummaryObject() {
		return ConsumerSummary.builder()
				.consumerId(id)
				.consumerType(this.getClass().getSimpleName())
				.additionalInfo(null)
				.build();
	}


	@Value
	@Builder
	static class TopNAndDistributionSnapshot {
		Map<Integer,Long> scoreDistribution;
		List<Long> topItemIds;
		List<Integer> topScores;
	}
}
