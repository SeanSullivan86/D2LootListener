package org.sully.d2.gamemodel.staticgamedata;

import org.sully.d2.util.ResourceFileReader;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;


public class GameDataTxtSpreadsheet {
	private String[] columnNames;
	private Map<String,Integer> columnNameToIndex;
	private List<Row> rows;
	
	public List<Row> getRows() {
		return rows;
	}
	
	public static GameDataTxtSpreadsheet fromTxtFile(String path, Predicate<Map<String,String>> rowValidator) {
		GameDataTxtSpreadsheet result = new GameDataTxtSpreadsheet();
		result.rows = new ArrayList<>();
		result.columnNameToIndex = new HashMap<>();

		try (BufferedReader in = new BufferedReader(new InputStreamReader(ResourceFileReader.getProjectResourceFileAsInputStream(path)))) {
			
			String line = in.readLine();
			//System.out.println(line.length() + " : " + line);
			result.columnNames = line.split("\\t", -1);
			
			for (int i = 0; i < result.columnNames.length; i++) {
				if (result.columnNameToIndex.containsKey(result.columnNames[i])) {
					System.out.println("Duplicate column name in " + path + " : " + result.columnNames[i]);
				}
				result.columnNameToIndex.put(result.columnNames[i], i);
			}
			
			while ( (line = in.readLine()) != null) {
				//System.out.println(line.length() + " : " + line);
				
				String[] parts = line.split("\\t", -1);
				Map<String,String> columnValues = new HashMap<>();
				for (int i = 0; i < result.columnNames.length; i++) {
					columnValues.put(result.columnNames[i], (i < parts.length) ? parts[i] : "");
				}
				
				if (rowValidator.test(columnValues)) {
					result.rows.add(new Row(parts, result));
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return result;
	}
	
	public int getColumnIndex(String columnName) {
		if (!columnNameToIndex.containsKey(columnName)) {
			throw new IllegalArgumentException("Unexpected column name : " + columnName);
		}
		return columnNameToIndex.get(columnName);
	}
	
	public static class Row {
		String[] data;
		GameDataTxtSpreadsheet parent;
		
		private Row(String[] data, GameDataTxtSpreadsheet parent) {
			this.data = data;
			this.parent = parent;
		}

		public String get(String columnName) {
			int columnIndex = parent.getColumnIndex(columnName);
			if (columnIndex >= data.length) {
				return "";
			}
			return data[columnIndex];
		}
		
		public String getNullable(String columnName) {
			String val = get(columnName);
			if (val.isEmpty()) {
				return null;
			}
			return val;
		}
		
		public int getInt(String columnName) {
			return Integer.parseInt(get(columnName));
		}
		
		public int getIntOrZero(String columnName) {
			String val = get(columnName);
			if (val.isEmpty()) {
				return 0;
			}
			return Integer.parseInt(val);
		}
		
		public Integer getNullableInteger(String columnName) {
			String value = get(columnName);
			if (value.isEmpty()) {
				return null;
			}
			return Integer.parseInt(value);
		}
		
		public boolean getBoolean(String columnName) {
			String value = get(columnName);
			return "1".equals(value) || "Y".equals(value) || "y".equals(value);
		}
		
		public boolean isEmpty(String columnName) {
			return get(columnName).isEmpty();
		}
	}
}
