package org.sully.d2.itemtracking;

import lombok.Getter;
import org.sully.d2.gamemodel.D2Item;
import org.sully.d2.util.Pair;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Uses the same format (ItemGridSnapshot) for both the full snapshots and incremental Updates.
 * When used as an update/checkpoint, ItemGridSnapshot still contains the cumulative totals,
 * not just the incremental numbers since the previous checkpoint. So applying the update
 * just means overwriting all the data in the Item Grid.
 */
public class ItemGrid implements D2TCDropConsumer {


	private Function<D2Item,String> rowValueFunc;
	private Function<D2Item,String> columnValueFunc;
	private Predicate<D2Item> filter;
	private Function<String,String> rowValueDisplayFunc;
	private Function<String,String> columnValueDisplayFunc;
	private Comparator<String> rowComparator;
	private Comparator<String> columnComparator;

	private Map<Pair<String,String>,Long> counts;

	@Getter
	private String name;

	@Getter
	private long totalIterations;


	@Override
	public void initializeFromSnapshot(TCDropConsumerSnapshot snapshotUntyped, Map<Long, D2Item> itemsById) {
		ItemGridSnapshot snapshot = (ItemGridSnapshot) snapshotUntyped;

		counts = new HashMap<>();
		int rowCount = snapshot.getRowValues().size();
		int columnCount = snapshot.getColumnValues().size();
		for (int row = 0; row < rowCount; row++) {
			for (int col = 0; col < columnCount; col++) {
				incrementCount(snapshot.getRowValues().get(row), snapshot.getColumnValues().get(col), snapshot.getCounts()[row][col]);
			}
		}
		this.totalIterations = snapshot.getTotalIterations();
	}

	@Override
	public void consume(D2TCDrop tcDrop) {
		this.totalIterations++;
		for (D2Item item : tcDrop.getItems()) {
			if (filter != null && !filter.test(item)) {
				continue;
			}
			incrementCount(rowValueFunc.apply(item), columnValueFunc.apply(item), 1L);
		}
	}

	@Override
	public DataReferencingItems<TCDropConsumerSnapshot> takeSnapshot() {
		NavigableSet<String> rowValueSet = new TreeSet<>(rowComparator);
		NavigableSet<String> columnValueSet = new TreeSet<>(columnComparator);

		for(Pair<String,String> key : counts.keySet()) {
			rowValueSet.add(key.getA());
			columnValueSet.add(key.getB());
		}
		List<String> rowValues = new ArrayList<>(rowValueSet);
		List<String> columnValues = new ArrayList<>(columnValueSet);

		long[][] countMatrix = new long[rowValues.size()][columnValues.size()];
		for (int r = 0; r < rowValues.size(); r++) {
			for (int c = 0; c < columnValues.size(); c++) {
				countMatrix[r][c] = counts.getOrDefault(Pair.of(rowValues.get(r), columnValues.get(c)), 0L);
			}
		}

		return DataReferencingItems.<TCDropConsumerSnapshot>builder()
				.items(List.of())
				.data(ItemGridSnapshot.builder()
						.rowValues(rowValues)
						.columnValues(columnValues)
						.counts(countMatrix)
						.totalIterations(totalIterations)
						.name(name)
						.build())
				.build();
	}

	public ItemGridCounts getCounts() {
		NavigableSet<String> rowValuesSet = new TreeSet<>(rowComparator);
		NavigableSet<String> columnValuesSet = new TreeSet<>(columnComparator);

		for(Pair<String,String> key : counts.keySet()) {
			rowValuesSet.add(key.getA());
			columnValuesSet.add(key.getB());
		}

		List<String> rowValues = List.copyOf(rowValuesSet);
		List<String> columnValues = List.copyOf(columnValuesSet);
		long[][] countMatrix = new long[rowValues.size()][columnValues.size()];
		long[] rowTotals = new long[rowValues.size()];
		long[] columnTotals = new long[columnValues.size()];
		for (int r = 0; r < rowValues.size(); r++) {
			for (int c = 0; c < columnValues.size(); c++) {
				countMatrix[r][c] = counts.getOrDefault(Pair.of(rowValues.get(r), columnValues.get(c)),0L);
				rowTotals[r] += countMatrix[r][c];
				columnTotals[c] += countMatrix[r][c];
			}
		}
		List<String> rowNames = rowValues.stream().map(rowValueDisplayFunc).toList();
		List<String> columnNames = columnValues.stream().map(columnValueDisplayFunc).toList();

		return ItemGridCounts.builder()
				.counts(countMatrix)
				.rowNames(rowNames)
				.columnNames(columnNames)
				.rowTotals(rowTotals)
				.columnTotals(columnTotals)
				.build();
	}
	
	

	public ItemGrid(String name, Function<D2Item, String> rowValueFunc, Function<D2Item, String> columnValueFunc,
			Predicate<D2Item> filter, Function<String, String> rowValueDisplayFunc,
			Function<String, String> columnValueDisplayFunc, Comparator<String> rowComparator,
			Comparator<String> columnComparator) {
		this.name = name;
		this.rowValueFunc = rowValueFunc;
		this.columnValueFunc = columnValueFunc;
		this.filter = filter;
		this.rowValueDisplayFunc = rowValueDisplayFunc;
		this.columnValueDisplayFunc = columnValueDisplayFunc;
		this.rowComparator = rowComparator;
		this.columnComparator = columnComparator;
		this.counts = new HashMap<>();
	}



	/*
	@Override
	public void closeAndGenerateOutput() {
		NavigableSet<String> rowValues = new TreeSet<>(rowComparator);
		NavigableSet<String> columnValues = new TreeSet<>(columnComparator);
		
		for(Pair<String,String> key : counts.keySet()) {
			rowValues.add(key.getA());
			columnValues.add(key.getB());
		}
		
		try (PrintWriter out = new PrintWriter(new FileWriter(outputLocation))) {
			// Header row
			for (String c : columnValues) {
				out.print('\t');
				out.print(columnValueDisplayFunc.apply(c));
			}
			out.print("\tTotal\n");
			
			long grandTotal = 0;
			long[] columnTotals = new long[columnValues.size()];
			for (String r : rowValues) {
				long rowTotal = 0;
				out.print(rowValueDisplayFunc.apply(r));
				int columnIndex = 0;
				for (String c : columnValues) {
					long val = counts.getOrDefault(Pair.of(r, c), 0L);
					rowTotal += val;
					columnTotals[columnIndex] += val;
					
					out.print('\t');
					out.print(val);
					
					columnIndex++;
				}
				out.print("\t" + rowTotal + "\n");
				grandTotal += rowTotal;
			}
			// Last Row for Column Totals 
			out.print("Total");
			for (int columnIndex = 0; columnIndex < columnValues.size(); columnIndex++) {
				out.print('\t');
				out.print(columnTotals[columnIndex]);
			}
			out.print("\t" + grandTotal + "\n");
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	*/

	private void incrementCount(String r, String c, long amount) {
		Pair<String,String> key = Pair.of(r, c);
		if (counts.containsKey(key)) {
			counts.put(key,  counts.get(key) + amount);
		} else {
			counts.put(key, amount);
		}
	}

}