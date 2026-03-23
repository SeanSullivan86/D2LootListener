package org.sully.d2.itemtracking;

import lombok.Builder;
import lombok.Value;

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
	String id;

	@Override
	public Set<Long> getReferencedItemIds() {
		Set<Long> x = new HashSet<>();
		for (TopNAndDistributionSnapshot topN : categories.values()) {
			x.addAll(topN.getTopItemIds());
		}
		return x;
	}

	@Value
	@Builder
	static class TopNAndDistributionSnapshot {
		Map<Integer,Long> scoreDistribution;
		List<Long> topItemIds;
		List<Integer> topScores;
	}
}

