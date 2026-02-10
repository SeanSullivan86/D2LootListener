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
public class CategorizedTopNSnapshot implements TCDropConsumerSnapshot {

	Map<String, TopNAndDistributionSnapshot> categories;
	long totalIterations;
	long countMatchingItemCodeAndQuality;
	long countMatchingAdditionalCriteria;
	String name;

	@Override
	public Set<Long> getReferencedItemIds() {
		Set<Long> x = new HashSet<>();
		for (TopNAndDistributionSnapshot topN : categories.values()) {
			x.addAll(topN.getTopItemIds());
		}
		return x;
	}

	@Override
	public ConsumerSummary toSummaryObject() {
		return ConsumerSummary.builder()
				.consumerName(name)
				.consumerType(this.getClass().getSimpleName())
				.additionalInfo(List.copyOf(categories.keySet()))
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
