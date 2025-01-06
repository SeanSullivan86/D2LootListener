package org.sully.d2.gamemodel.staticgamedata;

import lombok.Builder;
import lombok.Value;

import java.util.HashMap;
import java.util.Map;

@Value
@Builder
public class D2ItemStat {
	private static final String COL_STAT_CODE = "Stat";
	private static final String COL_ID = "ID";
	private static final String COL_VAL_SHIFT = "ValShift";
	

	
	private static final Map<String, D2ItemStat> statsByCode = new HashMap<>();
	private static final Map<Integer, D2ItemStat> statsById = new HashMap<>();
	
	String code;
	int id;
	int valShift;
	
	public static D2ItemStat fromId(int id) {
		return statsById.get(id);
	}
	
	public static D2ItemStat fromCode(String code) {
		return statsByCode.get(code);
	}
	
	public static void loadData() {
		
    	GameDataTxtSpreadsheet data = GameDataTxtSpreadsheet.fromTxtFile(
    			"game_data/ItemStatCost.txt",
    			row -> !row.get(COL_ID).isEmpty());
    	
    	for (GameDataTxtSpreadsheet.Row dataRow : data.getRows()) {
    		D2ItemStat stat = fromDataRow(dataRow);
    		statsByCode.put(stat.code, stat);
    		statsById.put(stat.id, stat);
    	}
	}
	
	public static void linkData() {

	}
	
	private static D2ItemStat fromDataRow(GameDataTxtSpreadsheet.Row dataRow) {
		D2ItemStatBuilder stat = D2ItemStat.builder();
		stat.code(dataRow.get(COL_STAT_CODE));
		stat.id(dataRow.getInt(COL_ID));
		stat.valShift(dataRow.getIntOrZero(COL_VAL_SHIFT));
		return stat.build();
	}
}
