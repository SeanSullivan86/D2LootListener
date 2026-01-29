package org.sully.d2.itemtracking;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import org.sully.d2.gamemodel.D2Item;
import org.sully.d2.gamemodel.enums.ItemQuality;
import org.sully.d2.gamemodel.staticgamedata.D2ItemType;
import org.sully.d2.gamemodel.staticgamedata.D2ItemTypeType;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

@Value
@Builder
class CategorizedTopNSnapshot implements TCDropConsumerSnapshot {

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


	@Value
	@Builder
	static class TopNAndDistributionSnapshot {
		Map<Integer,Long> scoreDistribution;
		List<Long> topItemIds;
		List<Integer> topScores;
	}
}

public class CategorizedTopN implements D2TCDropConsumer {
	final Set<String> applicableItemCodes;
	final Set<ItemQuality> applicableItemQualities;
	final Predicate<D2Item> additionalCriteria;
	
	@Getter
    final String name;
	final Function<D2Item,String> categorizer;
	final Function<D2Item,Integer> scoringFunction;
	final int keepTopNItemsPerCategory;
	
	// stats

	long countMatchingItemCodeAndQuality = 0;
	long countMatchingAdditionalCriteria = 0;
	Map<String, TopNAndScoreDistribution> statsByCategory = new HashMap<>();
	@Getter
	long totalIterations = 0;

	@Override
	public void initializeFromSnapshot(TCDropConsumerSnapshot untypedSnapshot, Map<Long, D2Item> itemsById) {
		CategorizedTopNSnapshot snapshot = (CategorizedTopNSnapshot) untypedSnapshot;
		this.countMatchingItemCodeAndQuality = snapshot.getCountMatchingItemCodeAndQuality();
		this.countMatchingAdditionalCriteria = snapshot.getCountMatchingAdditionalCriteria();
		this.totalIterations = snapshot.getTotalIterations();
		this.statsByCategory = new HashMap<>();

		for (Map.Entry<String, CategorizedTopNSnapshot.TopNAndDistributionSnapshot> e : snapshot.getCategories().entrySet()) {
			String category = e.getKey();
			CategorizedTopNSnapshot.TopNAndDistributionSnapshot topNSnapshot = e.getValue();
			TopNAndScoreDistribution topN = new TopNAndScoreDistribution(this.keepTopNItemsPerCategory);
			int itemCount = topNSnapshot.getTopScores().size();
			for (int i = 0; i < itemCount; i++) {
				topN.consume(itemsById.get(topNSnapshot.getTopItemIds().get(i)), topNSnapshot.getTopScores().get(i));
			}
			topN.overrideScoreDistribution(topNSnapshot.getScoreDistribution());
			statsByCategory.put(category, topN);
		}

	}

	@Override
	public void consume(D2TCDrop tcDrop) {
		this.totalIterations++;

		for (D2Item item : tcDrop.getItems()) {
			if (this.applicableItemQualities.contains(item.getQuality()) && this.applicableItemCodes.contains(item.getItemTypeCode())) {
				this.countMatchingItemCodeAndQuality++;

				if (this.additionalCriteria.test(item)) {
					this.countMatchingAdditionalCriteria++;

					String category = categorizer.apply(item);
					int score = scoringFunction.apply(item);

					if (!statsByCategory.containsKey(category)) {
						statsByCategory.put(category, new TopNAndScoreDistribution(this.keepTopNItemsPerCategory));
					}
					statsByCategory.get(category).consume(item, score);
				}
			}
		}
	}

	@Override
	public DataReferencingItems<TCDropConsumerSnapshot> takeSnapshot() {
		List<D2Item> items = new ArrayList<>();
		Map<String, CategorizedTopNSnapshot.TopNAndDistributionSnapshot> categorySnapshots = new HashMap<>();
		for (String category : statsByCategory.keySet()) {
			List<Long> topItemIds = new ArrayList<>();
			List<Integer> topScores = new ArrayList<>();

			TopNAndScoreDistribution topN = statsByCategory.get(category);
			for (ItemAndScore item : topN.getTopN()) {
				topItemIds.add(item.getItem().getId());
				items.add(item.getItem());
				topScores.add(item.getScore());
			}
			categorySnapshots.put(category, CategorizedTopNSnapshot.TopNAndDistributionSnapshot.builder()
					.topItemIds(topItemIds)
					.topScores(topScores)
					.scoreDistribution(new HashMap<>(topN.getScoreDistribution()))
					.build());
		}
		return DataReferencingItems.<TCDropConsumerSnapshot>builder()
				.items(items)
				.data(CategorizedTopNSnapshot.builder()
						.categories(categorySnapshots)
						.totalIterations(totalIterations)
						.countMatchingItemCodeAndQuality(countMatchingItemCodeAndQuality)
						.countMatchingAdditionalCriteria(countMatchingAdditionalCriteria)
						.build())
				.build();
	}

    public List<String> getCategories() {
		return new ArrayList<>(statsByCategory.keySet());
	}

	/*
	public List<ItemAndScore<D2Item>> getTopN(String category) {
		if (statsByCategory.containsKey(category)) {
			return statsByCategory.get(category).getTopN();
		}
		return new ArrayList<>();
	}

	public ConcurrentHashMap<Integer,Long> getScoreDistribution(String category) {
		if (statsByCategory.containsKey(category)) {
			return statsByCategory.get(category).getScoreDistribution();
		}
		return null;
	} */
	
/*
	
	public void printOverallCounts(PrintWriter out) {
		out.println(String.join("\t", name, ""+this.countMatchingItemCodeAndQuality, ""+this.countMatchingAdditionalCriteria));
	}
	
	public void printTopItemsPerCategory(PrintWriter out) {
		for (String category : statsByCategory.keySet()) {
			TopNAndScoreDistribution<D2Item> topN = statsByCategory.get(category);
			topN.printTopItemsOnePerLine(out, name + "\t" + category + "\t", D2Item::toLongString);
		}
	}
	
	public void printScoreDistributionsByCategory(PrintWriter out) {
		for (String category : statsByCategory.keySet()) {
			TopNAndScoreDistribution<D2Item> topN = statsByCategory.get(category);
			topN.printScoreDistribution(out, name + "\t" + category + "\t");
		}
	} */

	
	private CategorizedTopN(Set<String> applicableItemCodes, Set<ItemQuality> applicableItemQualities,
							Predicate<D2Item> additionalCriteria, String name, Function<D2Item, String> categorizer,
							Function<D2Item, Integer> scoringFunction, int keepTopNItemsPerCategory) {
		this.applicableItemCodes = applicableItemCodes;
		this.applicableItemQualities = applicableItemQualities;
		this.additionalCriteria = additionalCriteria;
		this.name = name;
		this.categorizer = categorizer;
		this.scoringFunction = scoringFunction;
		this.keepTopNItemsPerCategory = keepTopNItemsPerCategory;
	}

	public static Builder named(String name) {
		return new Builder(name);
	}



	public static class Builder {
		Set<D2ItemTypeType> allowedItemTypeTypes = new HashSet<>();
		Set<String> allowedItemTypeCodes = new HashSet<>();
		Set<D2ItemTypeType> excludedItemTypeTypes = new HashSet<>();
		Set<String> excludedItemTypeCodes = new HashSet<>();
		
		Function<D2Item,String> categorizer = item -> ""; // default
		Function<D2Item,Integer> scoringFunction = item -> 0; // default
		Predicate<D2Item> additionalCriteria = item -> true; // default
		String name;
		Set<ItemQuality> applicableItemQualities = new HashSet<>();
		int keepTopNItemsPerCategory = 1;
		
		private Builder(String name) {
			this.name = name;
		}
		
		public CategorizedTopN build() {
			Set<String> finalizedItemTypeCodes = new HashSet<>();
			
			// iterate through item types to find out which ones are applicable to this use case
			for (D2ItemType itemType : D2ItemType.allItemTypes()) {
				boolean isAllowed = false;
				boolean isExcluded = false;
				for (D2ItemTypeType allowedType : allowedItemTypeTypes) {
					if (itemType.getItemTypeType().isEqualToOrASubtypeOf(allowedType)) {
						isAllowed = true;
						break;
					}
				}
				for (D2ItemTypeType excludedType : excludedItemTypeTypes) {
					if (itemType.getItemTypeType().isEqualToOrASubtypeOf(excludedType)) {
						isExcluded = true;
						break;
					}
				}
				isAllowed = isAllowed || allowedItemTypeCodes.contains(itemType.getCode());
				isExcluded = isExcluded || excludedItemTypeCodes.contains(itemType.getCode());
				
				if (isAllowed && (! isExcluded)) {
					finalizedItemTypeCodes.add(itemType.getCode());
				}
			}
			
			if (finalizedItemTypeCodes.isEmpty()) {
				throw new RuntimeException("No itemTypeCodes allowed for use case : " + name);
			}
			if (applicableItemQualities.isEmpty()) {
				throw new RuntimeException("No item qualities allowed for use case : " + name);
			}
			
			return new CategorizedTopN(finalizedItemTypeCodes, this.applicableItemQualities,
				this.additionalCriteria, this.name, this.categorizer, this.scoringFunction, this.keepTopNItemsPerCategory);
		}
		
		public Builder addItemTypeTypeCodes(String... itemTypeTypeCodes) {
			for (String code : itemTypeTypeCodes) {
				D2ItemTypeType type = D2ItemTypeType.fromCode(code);
				if (type == null) {
					throw new IllegalArgumentException("Unexpected itemTypeType : " + code);
				}
				this.allowedItemTypeTypes.add(type);
			}
			return this;
		}
		
		public Builder addItemTypeCodes(String... itemTypeCodes) {
			for (String code : itemTypeCodes) {
				D2ItemType type = D2ItemType.fromCode(code);
				if (type == null) {
					throw new IllegalArgumentException("Unexpected itemType : " + code);
				}
				this.allowedItemTypeCodes.add(code);
			}
			return this;
		}
		
		public Builder excludeItemTypeTypeCodes(String... itemTypeTypeCodes) {
			for (String code : itemTypeTypeCodes) {
				D2ItemTypeType type = D2ItemTypeType.fromCode(code);
				if (type == null) {
					throw new IllegalArgumentException("Unexpected itemTypeType : " + code);
				}
				this.excludedItemTypeTypes.add(type);
			}
			return this;
		}
		
		public Builder excludeItemTypeCodes(String... itemTypeCodes) {
			for (String code : itemTypeCodes) {
				D2ItemType type = D2ItemType.fromCode(code);
				if (type == null) {
					throw new IllegalArgumentException("Unexpected itemType : " + code);
				}
				this.excludedItemTypeCodes.add(code);
			}
			return this;
		}
		
		public Builder withCategorizer(Function<D2Item,String> categorizer) {
			this.categorizer = categorizer;
			return this;
		}
		
		public Builder withScoringFunction(Function<D2Item,Integer> scoringFunction) {
			this.scoringFunction = scoringFunction;
			return this;
		}
		
		public Builder withAdditionalItemCriteria(Predicate<D2Item> additionalCriteria) {
			this.additionalCriteria = additionalCriteria;
			return this;
		}
		
		public Builder allowItemQualities(ItemQuality... itemQualities) {
			Arrays.stream(itemQualities).forEach(this.applicableItemQualities::add);
			return this;
		}
		
		public Builder withCountOfTopScoringItemsToKeepInEachCategory(int n) {
			this.keepTopNItemsPerCategory = n;
			return this;
		}
		

	}
}