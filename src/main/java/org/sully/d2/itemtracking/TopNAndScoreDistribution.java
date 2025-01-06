package org.sully.d2.itemtracking;

import lombok.Getter;

import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Stream;

public class TopNAndScoreDistribution<T> {

	@Getter
	private int n;

	@Getter
	private ConcurrentHashMap<Integer,Long> scoreDistribution;
	
	private TreeSet<ItemAndScore<T>> topN;
	private int minScoreForTopN;
	private volatile TopNSuperset<T> topNForReadAccess;
	
	public TopNAndScoreDistribution(int n) {
		this.n = n;
		this.topNForReadAccess = TopNSuperset.newList(n);

		// max score is first set item (it's in descending order)
		this.topN = new TreeSet<>(ItemAndScore.comparator);
		this.scoreDistribution = new ConcurrentHashMap<>();
	}

	public List<ItemAndScore<T>> getTopN() {
		return topNForReadAccess.getTopN();
	}

	long sequenceNumber = 0;
	public void consume(T item, int value) {

		if (topN.size() < this.n || value > minScoreForTopN) {
			ItemAndScore<T> newEntry = new ItemAndScore<>(item, value, ++sequenceNumber);

			topN.add(newEntry);
			if (topN.size() > this.n) {
				topN.pollLast();
			}

			this.minScoreForTopN = topN.last().getScore();

			TopNSuperset<T> maybeNewCompactedList = topNForReadAccess.add(newEntry, newEntry.getSequenceNumber());
			if (maybeNewCompactedList != null) {
				topNForReadAccess = maybeNewCompactedList;
			}
		}

		if (scoreDistribution.containsKey(value)) {
			scoreDistribution.put(value, 1 + scoreDistribution.get(value));
		} else {
			scoreDistribution.put(value, 1L);
		}

	}
	
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
		
	}



}

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


}