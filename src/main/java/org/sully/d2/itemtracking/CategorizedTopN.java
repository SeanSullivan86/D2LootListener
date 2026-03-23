package org.sully.d2.itemtracking;

import lombok.Getter;

import org.sully.d2.SerializableD2Item;
import org.sully.d2.gamemodel.D2Item;
import org.sully.d2.gamemodel.enums.ItemQuality;
import org.sully.d2.gamemodel.staticgamedata.D2ItemType;
import org.sully.d2.gamemodel.staticgamedata.D2ItemTypeType;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class CategorizedTopN implements D2TCDropConsumer {
	final Set<String> applicableItemCodes;
	final Set<ItemQuality> applicableItemQualities;
	final Predicate<D2Item> additionalCriteria;
	final boolean includeScoreDistribution;
	
	@Getter
    final String id;
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
	public void incrementFromSnapshot(TCDropConsumerSnapshot untypedSnapshot, Map<Long, SerializableD2Item> itemsById) {
		CategorizedTopNSnapshot snapshot = (CategorizedTopNSnapshot) untypedSnapshot;
		this.countMatchingItemCodeAndQuality += snapshot.getCountMatchingItemCodeAndQuality();
		this.countMatchingAdditionalCriteria += snapshot.getCountMatchingAdditionalCriteria();
		this.totalIterations += snapshot.getTotalIterations();

		for (Map.Entry<String, CategorizedTopNSnapshot.TopNAndDistributionSnapshot> e : snapshot.getCategories().entrySet()) {
			String category = e.getKey();
			CategorizedTopNSnapshot.TopNAndDistributionSnapshot topNSnapshot = e.getValue();
			if (!statsByCategory.containsKey(category)) {
				statsByCategory.put(category, new TopNAndScoreDistribution(this.keepTopNItemsPerCategory, this.includeScoreDistribution));
			}
			TopNAndScoreDistribution topN = statsByCategory.get(category);
			int itemCount = topNSnapshot.getTopScores().size();
			for (int i = 0; i < itemCount; i++) {
				topN.consumeWithoutUpdatingScoreDistribution(
						D2Item.fromSerializableD2Item(itemsById.get(topNSnapshot.getTopItemIds().get(i))),
						topNSnapshot.getTopScores().get(i));
			}
			topN.incrementScoreDistribution(topNSnapshot.getScoreDistribution());
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
						statsByCategory.put(category, new TopNAndScoreDistribution(this.keepTopNItemsPerCategory, this.includeScoreDistribution));
					}
					int newRank = statsByCategory.get(category).consume(item, score);
					if (newRank == 1) {
						System.out.println("New Rank " + newRank + " for " + id + " " + category + " : " + item.toLongString() + " . Iterations= " + totalIterations + ", countMatchingCriteria=" + countMatchingAdditionalCriteria + " , score=" + score);
					}
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
					.scoreDistribution(this.includeScoreDistribution ? new HashMap<>(topN.getScoreDistribution()) : null)
					.build());
		}
		return DataReferencingItems.<TCDropConsumerSnapshot>builder()
				.items(items)
				.data(CategorizedTopNSnapshot.builder()
						.categories(categorySnapshots)
						.id(id)
						.totalIterations(totalIterations)
						.countMatchingItemCodeAndQuality(countMatchingItemCodeAndQuality)
						.countMatchingAdditionalCriteria(countMatchingAdditionalCriteria)
						.build())
				.build();
	}

    public List<String> getCategories() {
		return new ArrayList<>(statsByCategory.keySet());
	}
	
	private CategorizedTopN(Set<String> applicableItemCodes, Set<ItemQuality> applicableItemQualities,
							Predicate<D2Item> additionalCriteria, String id, Function<D2Item, String> categorizer,
							Function<D2Item, Integer> scoringFunction, int keepTopNItemsPerCategory, boolean includeScoreDistribution) {
		this.applicableItemCodes = applicableItemCodes;
		this.applicableItemQualities = applicableItemQualities;
		this.additionalCriteria = additionalCriteria;
		this.id = id;
		this.categorizer = categorizer;
		this.scoringFunction = scoringFunction;
		this.keepTopNItemsPerCategory = keepTopNItemsPerCategory;
		this.includeScoreDistribution = includeScoreDistribution;
	}

	public static Builder withId(String id) {
		return new Builder(id);
	}



	public static class Builder {
		Set<D2ItemTypeType> allowedItemTypeTypes = new HashSet<>();
		Set<String> allowedItemTypeCodes = new HashSet<>();
		Set<D2ItemTypeType> excludedItemTypeTypes = new HashSet<>();
		Set<String> excludedItemTypeCodes = new HashSet<>();
		
		Function<D2Item,String> categorizer = item -> ""; // default
		Function<D2Item,Integer> scoringFunction = item -> 0; // default
		Predicate<D2Item> additionalCriteria = item -> true; // default
		String id;
		Set<ItemQuality> applicableItemQualities = new HashSet<>();
		int keepTopNItemsPerCategory = 1;
		boolean includeScoreDistribution = false;
		
		private Builder(String id) {
			this.id = id;
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
				throw new RuntimeException("No itemTypeCodes allowed for use case : " + id);
			}
			if (applicableItemQualities.isEmpty()) {
				throw new RuntimeException("No item qualities allowed for use case : " + id);
			}
			
			return new CategorizedTopN(finalizedItemTypeCodes, this.applicableItemQualities,
				this.additionalCriteria, this.id, this.categorizer, this.scoringFunction, this.keepTopNItemsPerCategory, this.includeScoreDistribution);
		}

		public Builder includeScoreDistribution(boolean val) {
			this.includeScoreDistribution = val;
			return this;
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