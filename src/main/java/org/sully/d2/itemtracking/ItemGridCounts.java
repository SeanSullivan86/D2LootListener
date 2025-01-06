package org.sully.d2.itemtracking;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class ItemGridCounts {
	List<String> rowNames;
	List<String> columnNames;
	long[][] counts;
	long[] rowTotals;
	long[] columnTotals;
}
