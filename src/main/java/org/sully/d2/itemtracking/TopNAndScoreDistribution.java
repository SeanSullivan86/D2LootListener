package org.sully.d2.itemtracking;

import lombok.Getter;
import org.sully.d2.gamemodel.D2Item;
import java.util.*;

public class TopNAndScoreDistribution {

	@Getter
	private int n;

	@Getter
	private Map<Integer,Long> scoreDistribution;

	@Getter
	private TreeSet<ItemAndScore> topN;
	private int minScoreForTopN;
	
	public TopNAndScoreDistribution(int n) {
		this.n = n;

		// max score is first set item (it's in descending order)
		this.topN = new TreeSet<>(ItemAndScore.comparator);
		this.scoreDistribution = new HashMap<>();
	}

	public void overrideScoreDistribution(Map<Integer,Long> scores) {
		this.scoreDistribution = new HashMap<>(scores);
	}

	public void incrementScoreDistribution(Map<Integer,Long> newScores) {
		for (Map.Entry<Integer, Long> e : newScores.entrySet()) {
			int score = e.getKey();
			long frequency = e.getValue();
			if (scoreDistribution.containsKey(score)) {
				scoreDistribution.put(score, scoreDistribution.get(score) + frequency);
			} else {
				scoreDistribution.put(score, frequency);
			}
		}
	}

	public void consumeWithoutUpdatingScoreDistribution(D2Item item, int value) {
		consume(item, value, false);
	}

	public void consume(D2Item item, int value) {
		consume(item, value, true);
	}

	public void consume(D2Item item, int value, boolean updateScoreDistribution) {

		if (topN.size() < this.n || value > minScoreForTopN) {
			ItemAndScore newEntry = new ItemAndScore(item, value);

			topN.add(newEntry);
			if (topN.size() > this.n) {
				topN.pollLast();
			}

			this.minScoreForTopN = topN.last().getScore();
		}

		if (updateScoreDistribution) {
			if (scoreDistribution.containsKey(value)) {
				scoreDistribution.put(value, 1 + scoreDistribution.get(value));
			} else {
				scoreDistribution.put(value, 1L);
			}
		}


	}

	/*
	public void printTopItemsOnePerLine(PrintWriter out, String prefix, Function<T,String> itemToString) {
		if (prefix != null && !prefix.isEmpty() && !prefix.endsWith("\t")) {
			throw new RuntimeException("line prefix must end with a tab character if non-null/non-empty");
		}
		int rankWithoutTiesAllowed = 0;
		for (ItemAndScore<T> entry : topN.descendingSet()) {
			rankWithoutTiesAllowed++;
			long rank = 1 + topN.stream().filter(x -> x.getScore() > entry.getScore()).count();
			out.println((prefix == null ? "" : prefix) + rank + "\t" + rankWithoutTiesAllowed + "\t" + entry.getScore() + "\t" + itemToString.apply(entry.getItem()));
		}
	}
	
	public void printScoreDistribution(PrintWriter out, String prefix) {
		if (prefix != null && !prefix.isEmpty() && !prefix.endsWith("\t")) {
			throw new RuntimeException("line prefix must end with a tab character if non-null/non-empty");
		}
		
		List<Map.Entry<Integer, Long>> scoresAndCounts = new ArrayList<>();
		scoresAndCounts.addAll(scoreDistribution.entrySet());
		Collections.sort(scoresAndCounts, (e1,e2) -> Long.compare(e1.getValue(), e2.getValue()));
		
		for (Map.Entry<Integer, Long> scoreAndCount : scoresAndCounts) {
			out.println((prefix == null ? "" : prefix) + scoreAndCount.getKey() + "\t" + scoreAndCount.getValue());
		}
	}

	public void closeAndGenerateOutput() {
		System.out.println(" ----- Printing Top N ------ ");
		for (ItemAndScore<T> entry : topN.descendingSet()) {
			T item = entry.getItem();
			int value = entry.getScore();
			long rank = 1 + topN.stream().filter(x -> x.getScore() > value).count();
			// TODO print
		}
		
	} */



}

/*
class TopNSuperset<T> {
	final int n;
	final long revisionNumberAtLastCompaction;
	final List<ItemAndScore<T>> oldList;

	volatile int newAdditionCount;
	final Object[] newAdditions;
	final long[] revisionNumbers;

	public static <T> TopNSuperset<T> newList(int n) {
		return new TopNSuperset<>(n, List.of(), -1);
	}

	private TopNSuperset(int n, List<ItemAndScore<T>> oldList, long revisionNumberAtLastCompaction) {
		this.n = n;
		this.oldList = oldList;
		this.revisionNumberAtLastCompaction = revisionNumberAtLastCompaction;
		this.revisionNumbers = new long[n];
		this.newAdditionCount = 0;
		this.newAdditions = new Object[n];
	}

	public List<ItemAndScore<T>> getTopN() {
		List<ItemAndScore<T>> mergedList = new ArrayList<>(oldList);
		int newElements = newAdditionCount;
		for (int i = 0; i < newElements; i++) {
			mergedList.add((ItemAndScore<T>) newAdditions[i]);
		}
		Collections.sort(mergedList, ItemAndScore.comparator);
		if (mergedList.size() > n) {
			return mergedList.subList(0,n);
		}
		return mergedList;
	}

	public TopNSuperset<T> add(ItemAndScore<T> item, long revisionNumber) {
		newAdditions[newAdditionCount] = item;
		revisionNumbers[newAdditionCount] = revisionNumber;
		newAdditionCount++;

		if (newAdditionCount < newAdditions.length) {
			return null;
		}

		List<ItemAndScore<T>> mergedList = Stream.concat(oldList.stream(), Arrays.stream(newAdditions))
				.map(x -> (ItemAndScore<T>) x)
				.sorted(ItemAndScore.comparator)
				.limit(n)
				.toList();

		return new TopNSuperset<>(n, mergedList, revisionNumber);
	}


} */