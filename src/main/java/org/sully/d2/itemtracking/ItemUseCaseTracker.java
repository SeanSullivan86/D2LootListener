package org.sully.d2.itemtracking;

import lombok.Getter;
import org.sully.d2.gamemodel.D2Item;
import org.sully.d2.gamemodel.enums.ItemQuality;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ItemUseCaseTracker implements ItemConsumer {

	@Getter
	List<ItemUseCase> allUseCases;
	Map<String,List<ItemUseCase>[]> useCasesByItemTypeAndQuality = new HashMap<>();

	Map<String, ItemUseCase> useCasesByName = new HashMap<>();

	public ItemUseCaseTracker(List<ItemUseCase> useCases) {
		this.allUseCases = List.copyOf(useCases);

		for (ItemUseCase useCase : useCases) {
			addUseCase(useCase);
		}
	}

	// index the use case as an applicable use case for each (itemCode,quality) pair that is valid for
	private void addUseCase(ItemUseCase useCase) {
		useCasesByName.put(useCase.getName(), useCase);
		
		for (String itemCode : useCase.applicableItemCodes) {
			if (!useCasesByItemTypeAndQuality.containsKey(itemCode)) {
				List<ItemUseCase>[] useCasesByQuality = new List[ItemQuality.values().length+1];
				for (ItemQuality q : ItemQuality.values()) {
					useCasesByQuality[q.id] = new ArrayList<ItemUseCase>();
				}
				useCasesByItemTypeAndQuality.put(itemCode, useCasesByQuality);
			}
			for (ItemQuality q : useCase.applicableItemQualities) {
				useCasesByItemTypeAndQuality.get(itemCode)[q.id].add(useCase);
			}
		}
	}

	public ItemUseCase getUseCaseByName(String name) {
		return useCasesByName.get(name);
	}

	public List<ItemAndScore<D2Item>> getTopNItems(String useCaseName, String category) {
		if (useCasesByName.containsKey(useCaseName)) {
			return useCasesByName.get(useCaseName).getTopN(category);
		}
		return List.of();
	}

	public ConcurrentHashMap<Integer,Long> getScoreDistribution(String useCaseName, String category) {
		if (useCasesByName.containsKey(useCaseName)) {
			return useCasesByName.get(useCaseName).getScoreDistribution(category);
		}
		return null;
	}

	public Map<String, List<String>> getUseCasesAndCategories() {
		Map<String, List<String>> result = new HashMap<>();
		allUseCases.forEach(useCase -> {
			result.put(useCase.getName(), useCase.getCategories());
		});
		return result;
	}

	@Override
	public void consume(D2Item item, ItemNotifier notifier) {
		if (useCasesByItemTypeAndQuality.containsKey(item.getItemType().getCode())) {
			for (ItemUseCase useCase : useCasesByItemTypeAndQuality.get(item.getItemType().getCode())[item.getQuality().id]) {
				useCase.consumeItemKnownToMatchItemCodeAndQuality(item);
			}
		}
	}

	@Override
	public void closeAndGenerateOutput() {
		try (PrintWriter out = new PrintWriter(new FileWriter("output/itemUseCaseTotalCounts.csv"))) {
			for (ItemUseCase useCase : allUseCases) {
				useCase.printOverallCounts(out);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		try (PrintWriter out = new PrintWriter(new FileWriter("output/itemUseCaseTopItems.csv"))) {
			for (ItemUseCase useCase : allUseCases) {
				useCase.printTopItemsPerCategory(out);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		try (PrintWriter out = new PrintWriter(new FileWriter("output/itemUseCaseScoreDistributions.csv"))) {
			for (ItemUseCase useCase : allUseCases) {
				useCase.printScoreDistributionsByCategory(out);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}
	
	

}