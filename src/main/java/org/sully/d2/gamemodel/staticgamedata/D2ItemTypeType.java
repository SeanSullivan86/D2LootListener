package org.sully.d2.gamemodel.staticgamedata;

import lombok.*;
import lombok.experimental.NonFinal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

@Value
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // only use 'code' for equality check
public class D2ItemTypeType {
	private static final String COL_CODE = "Code";
	private static final String COL_EQUIV1 = "Equiv1";
	private static final String COL_EQUIV2 = "Equiv2";
	private static final String COL_MAX_SOCK_40 = "MaxSock40";
	
	private static Map<String, D2ItemTypeType> itemTypeTypesByCode;
	
	// Extracted from Game Data
	@EqualsAndHashCode.Include String code;
	String equiv1Code;
	String equiv2Code;
	int maxSock40;
	
	// Linked to other instances of this class
	@NonFinal
	D2ItemTypeType equiv1;
	@NonFinal D2ItemTypeType equiv2;
	
	// Nested Type Membership
	@NonFinal Set<String> parentTypeCodesIncludingSelf;
	
	public boolean isEqualToOrASubtypeOf(D2ItemTypeType other) {
		return parentTypeCodesIncludingSelf.contains(other.code);
	}
	
	public static D2ItemTypeType fromCode(String itemTypeTypeCode) {
		return itemTypeTypesByCode.get(itemTypeTypeCode);
	}


	public static void loadData() {
		itemTypeTypesByCode = new HashMap<>();
		
    	GameDataTxtSpreadsheet data = GameDataTxtSpreadsheet.fromTxtFile(
    			"game_data/ItemTypes.txt",
    			row -> (! row.get("Code").isEmpty()));
    	
    	for (GameDataTxtSpreadsheet.Row dataRow : data.getRows()) {
    		D2ItemTypeType item = fromDataRow(dataRow);
    		itemTypeTypesByCode.put(item.code, item);
    	}
    	
    	for (String code : itemTypeTypesByCode.keySet()) {
    		D2ItemTypeType type = itemTypeTypesByCode.get(code);
    		if (type.equiv1Code != null) {
    			type.equiv1 = itemTypeTypesByCode.get(type.equiv1Code);
    		}
    		if (type.equiv2Code != null) {
    			type.equiv2 = itemTypeTypesByCode.get(type.equiv2Code);
    		}
    	}
    	
    	// pre-populate the list of all parent types for each type, so we wont have to do tree traversals later for each item type membership lookup
    	for (String code : itemTypeTypesByCode.keySet()) {
    		D2ItemTypeType type = itemTypeTypesByCode.get(code);
    		type.parentTypeCodesIncludingSelf = new HashSet<>();
    		type.parentTypeCodesIncludingSelf.add(type.code);
    		
    		LinkedList<D2ItemTypeType> parents = new LinkedList<>();
    		parents.addLast(type.equiv1);
    		parents.addLast(type.equiv2);;
    		
    		while (!parents.isEmpty()) {
    			D2ItemTypeType p = parents.removeFirst();
    			if (p != null) {
    				type.parentTypeCodesIncludingSelf.add(p.code);
    				parents.addLast(p.equiv1);
    				parents.addLast(p.equiv2);
    			}
    		}
    	}
    	
    	
		
	}
	
	static D2ItemTypeType fromDataRow(GameDataTxtSpreadsheet.Row dataRow) {
		D2ItemTypeTypeBuilder result = D2ItemTypeType.builder();
		result.code = dataRow.get(COL_CODE);
		result.equiv1Code = dataRow.getNullable(COL_EQUIV1);
		result.equiv2Code = dataRow.getNullable(COL_EQUIV2);
		result.maxSock40 = dataRow.getIntOrZero(COL_MAX_SOCK_40);
		return result.build();
	}


	
}