package org.sully.d2.itemtracking;

import org.sully.d2.gamemodel.D2Item;
import org.sully.d2.gamemodel.enums.ItemQuality;
import org.sully.d2.gamemodel.staticgamedata.D2ItemType;
import org.sully.d2.gamemodel.staticgamedata.D2ItemTypeType;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

public class ItemUseCase {
	final List<String> applicableItemCodes;
	final List<ItemQuality> applicableItemQualities;
	final Predicate<D2Item> additionalCriteria;
	
	final String name;
	final Function<D2Item,String> categorizer;
	final Function<D2Item,Integer> scoringFunction;
	final int keepTopNItemsPerCategory;
	
	// stats
	long countMatchingItemCodeAndQuality = 0;
	long countMatchingAdditionalCriteria = 0;
	Map<String, TopNAndScoreDistribution<D2Item>> statsByCategory = new ConcurrentHashMap<>();

	public String getName() {
		return name;
	}

	public List<String> getCategories() {
		return new ArrayList<>(statsByCategory.keySet());
	}

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
	}
	
	public void consumeItemKnownToMatchItemCodeAndQuality(D2Item item) {
		this.countMatchingItemCodeAndQuality++;
		
		if (this.additionalCriteria.test(item)) {
			this.countMatchingAdditionalCriteria++;
			
			String category = categorizer.apply(item);
			int score = scoringFunction.apply(item);
			
			if (!statsByCategory.containsKey(category)) {
				statsByCategory.put(category, new TopNAndScoreDistribution<D2Item>(this.keepTopNItemsPerCategory));
			}
			statsByCategory.get(category).consume(item, score);
		}
	}
	
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
	}

	
	private ItemUseCase(List<String> applicableItemCodes, List<ItemQuality> applicableItemQualities,
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
		
		public ItemUseCase build() {
			List<String> finalizedItemTypeCodes = new ArrayList<>();
			
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
			
			return new ItemUseCase(finalizedItemTypeCodes, new ArrayList<ItemQuality>(this.applicableItemQualities),
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