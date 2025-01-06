package org.sully.d2.itemtracking;

import lombok.Builder;
import lombok.Value;
import org.sully.d2.gamemodel.D2Item;
import org.sully.d2.util.Pair;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;


public class ItemGrid<RowValue,ColumnValue> implements ItemConsumer {

	private String name;
	private Function<D2Item,RowValue> rowValueFunc;
	private Function<D2Item,ColumnValue> columnValueFunc;
	private Predicate<D2Item> filter;
	private Function<RowValue,String> rowValueDisplayFunc;
	private Function<ColumnValue,String> columnValueDisplayFunc;
	private Comparator<RowValue> rowComparator;
	private Comparator<ColumnValue> columnComparator;
	private File outputLocation;
	
	private Map<Pair<RowValue,ColumnValue>,Long> counts;

	public ItemGridCounts getCounts() {
		NavigableSet<RowValue> rowValuesSet = new TreeSet<>(rowComparator);
		NavigableSet<ColumnValue> columnValuesSet = new TreeSet<>(columnComparator);

		for(Pair<RowValue,ColumnValue> key : counts.keySet()) {
			rowValuesSet.add(key.getA());
			columnValuesSet.add(key.getB());
		}

		List<RowValue> rowValues = List.copyOf(rowValuesSet);
		List<ColumnValue> columnValues = List.copyOf(columnValuesSet);
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
	
	

	public ItemGrid(String name, Function<D2Item, RowValue> rowValueFunc, Function<D2Item, ColumnValue> columnValueFunc,
			Predicate<D2Item> filter, Function<RowValue, String> rowValueDisplayFunc,
			Function<ColumnValue, String> columnValueDisplayFunc, Comparator<RowValue> rowComparator,
			Comparator<ColumnValue> columnComparator, File outputLocation) {
		this.name = name;
		this.rowValueFunc = rowValueFunc;
		this.columnValueFunc = columnValueFunc;
		this.filter = filter;
		this.rowValueDisplayFunc = rowValueDisplayFunc;
		this.columnValueDisplayFunc = columnValueDisplayFunc;
		this.rowComparator = rowComparator;
		this.columnComparator = columnComparator;
		this.outputLocation = outputLocation;
		this.counts = new ConcurrentHashMap<>();
	}

	@Override
	public void consume(D2ItemDrop itemDrop, ItemNotifier notifier) {
		D2Item item = itemDrop.getItem();
		if (filter != null && !filter.test(item)) {
			return;
		}
		incrementCount(rowValueFunc.apply(item), columnValueFunc.apply(item));
	}

	@Override
	public void closeAndGenerateOutput() {
		NavigableSet<RowValue> rowValues = new TreeSet<>(rowComparator); 
		NavigableSet<ColumnValue> columnValues = new TreeSet<>(columnComparator);
		
		for(Pair<RowValue,ColumnValue> key : counts.keySet()) {
			rowValues.add(key.getA());
			columnValues.add(key.getB());
		}
		
		try (PrintWriter out = new PrintWriter(new FileWriter(outputLocation))) {
			// Header row
			for (ColumnValue c : columnValues) {
				out.print('\t');
				out.print(columnValueDisplayFunc.apply(c));
			}
			out.print("\tTotal\n");
			
			long grandTotal = 0;
			long[] columnTotals = new long[columnValues.size()];
			for (RowValue r : rowValues) {
				long rowTotal = 0;
				out.print(rowValueDisplayFunc.apply(r));
				int columnIndex = 0;
				for (ColumnValue c : columnValues) {
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
	
	private void incrementCount(RowValue r, ColumnValue c) {
	    Pair<RowValue,ColumnValue> key = Pair.of(r, c);
	    if (counts.containsKey(key)) {
	    	counts.put(key,  counts.get(key) + 1);
	    } else {
	    	counts.put(key, 1L);
	    }
	}

	public String getName() {
		return name;
	}
	
}