package org.sully.d2.gamemodel.staticgamedata;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.sully.d2.gamemodel.D2Item;
import org.sully.d2.gamemodel.StatList;
import org.sully.d2.gamemodel.StatValue;
import org.sully.d2.gamemodel.staticgamedata.strings.D2String;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Value
@Builder
public class D2UniqueItem {
	private static final String COL_INDEX = "index";
	private static final String COL_ITEM_TYPE_CODE = "code";
	private static final String COL_ENABLED = "enabled";
	private static final String COL_QLVL = "lvl";
	
	private static List<D2UniqueItem> allUniques = new ArrayList<>();
	private static List<D2UniqueItem> spawnableUniquesBelowIlvl100 = new ArrayList<>();
	private static Map<String, List<D2UniqueItem>> uniquesByItemTypeCode = new HashMap<>();
	
	String uniqueItemCode;
	String itemTypeCode;
	int qlvl;

	@NonFinal
	D2ItemType itemType;
	@NonFinal
	String name;
	@NonFinal
	String disambiguatedName;
	@NonFinal
	List<D2PropertyValueRange> properties;
	
	public static List<D2UniqueItem> getSpawnableUniquesBelowIlvl100() {
		return Collections.unmodifiableList(spawnableUniquesBelowIlvl100);
	}
	
	public long getCountOfPossibleRolls() {
		long possibleOutcomes = 1L;
		for (D2PropertyValueRange prop : this.properties) {
			prop.property = D2Property.fromCode(prop.getPropertyCode());

			D2Property.ItemStatModifier statModifier = prop.property.getStatModifiers().get(0);
			if (statModifier.getStatFuncBehavior().statValueUsesRandomRoll && prop.min != prop.max) {
				D2ItemStat stat = D2ItemStat.fromId(prop.statImpactedByRoll.getStatId());
				int statMin = prop.min;
				int statMax = prop.max;
				if (stat.getId() == 194) {
					int maxSockets = this.itemType.getMaxSocketsAtHighIlvl();
					statMin = Math.min(maxSockets, prop.min == 0 ? Integer.parseInt(prop.getParam()) : prop.min);
					statMax = Math.min(maxSockets, prop.max == 0 ? Integer.parseInt(prop.getParam()) : prop.max);
				} 
				possibleOutcomes *= (statMax - statMin + 1);
			}
		}
		return possibleOutcomes;
	}
	
	public void printPerfectItemDetails(D2Item item) {
		System.out.println("---\n" + item.getName());
		long possibleOutcomes = 1L;
		for (D2PropertyValueRange prop : this.properties) {
			prop.property = D2Property.fromCode(prop.getPropertyCode());

			D2Property.ItemStatModifier statModifier = prop.property.getStatModifiers().get(0);
			if (statModifier.getStatFuncBehavior().statValueUsesRandomRoll && prop.min != prop.max) {
				D2ItemStat stat = D2ItemStat.fromId(prop.statImpactedByRoll.getStatId());
				int statMin = prop.min;
				int statMax = prop.max;
				int statValue = item.getStat(stat.getId(), prop.statImpactedByRoll.getParam()) >> stat.getValShift();
				if (stat.getId() == 194) {
					statValue = item.getSockets();
					int maxSockets = this.itemType.getMaxSocketsAtHighIlvl();
					statMin = Math.min(maxSockets, prop.min == 0 ? Integer.parseInt(prop.getParam()) : prop.min);
					statMax = Math.min(maxSockets, prop.max == 0 ? Integer.parseInt(prop.getParam()) : prop.max);
				} 
				possibleOutcomes *= (statMax - statMin + 1);
				boolean isStatBroken = statValue < statMin || statValue > statMax;
				System.out.println(prop.getPropertyCode() + "(" + prop.getParam() + ") --> " + stat.getCode() + "(" + prop.statImpactedByRoll.getParam() + ")  [" + statMin + " - " + statMax +"]  = " + statValue + (isStatBroken ? "   ?????????????????????????????????????????????????" : ""));
			}
		}
		System.out.println("Perfect !! (1 in " + possibleOutcomes + ")");
	}
	
	public boolean isPerfect(D2Item item) {
		boolean isPerfect = true;
		for (D2PropertyValueRange prop : this.properties) {
			prop.property = D2Property.fromCode(prop.getPropertyCode());

			D2Property.ItemStatModifier statModifier = prop.property.getStatModifiers().get(0);
			if (statModifier.getStatFuncBehavior().statValueUsesRandomRoll && prop.min != prop.max) {
				D2ItemStat stat = D2ItemStat.fromId(prop.statImpactedByRoll.getStatId());
				int statMin = prop.min;
				int statMax = prop.max;
				int statValue = item.getStat(stat.getId(), prop.statImpactedByRoll.getParam()) >> stat.getValShift();
				if (stat.getId() == 194) {
					statValue = item.getSockets();
					int maxSockets = this.itemType.getMaxSocketsAtHighIlvl();
					statMin = Math.min(maxSockets, prop.min == 0 ? Integer.parseInt(prop.getParam()) : prop.min);
					statMax = Math.min(maxSockets, prop.max == 0 ? Integer.parseInt(prop.getParam()) : prop.max);
				} 
				
				boolean isStatBroken = statValue < statMin || statValue > statMax;
				if (isStatBroken) {
					System.out.println("Broken Stat " + stat.getCode() + " : " + item.toLongString());
				}
				
				if (statValue < statMax) {
					isPerfect = false;
				}
			}
		}
		return isPerfect;
	}
	
	public static D2UniqueItem getFromItem(D2ItemType itemType, StatList stats, String name) {
		List<D2UniqueItem> uniques = uniquesByItemTypeCode.get(itemType.getCode());
		if (uniques.size() == 1) return uniques.get(0);

		
		if ("jewl".equals(itemType.getItemTypeTypeCode())) {
			String elem = null, procType = null;
			for (StatValue stat : stats.getStats()) {
				if (stat.statId == 197) procType = "death";
				if (stat.statId == 199) procType = "level";
				if (stat.statId == 333) elem = "fire";
				if (stat.statId == 334) elem = "ltng";
				if (stat.statId == 335) elem = "cold";
				if (stat.statId == 336) elem = "pois";
			}
			if (elem == null) {
				throw new RuntimeException("Couldn't figure out which Element the Rainbow Facet was : " + stats);
			}
			if (procType == null) {
				throw new RuntimeException("Couldn't figure out which Proc Type the Rainbow Facet was : " + stats);
			}
			String disambiguator = elem + "/" + procType;
			
			for (D2UniqueItem unique : uniques) {
				if (unique.disambiguatedName.contains(disambiguator)) {
					return unique;
				}
			}
			throw new RuntimeException("No Rainbow Facet found for name disambiguator : " + disambiguator + " : " + stats);
		}
		
		for (D2UniqueItem unique : uniques) {
			if (name.endsWith(unique.name)) {
				return unique;
			}
		}
		throw new RuntimeException("Couldn't find unique item : " + name);
	}

	public static void loadData() {
		
    	GameDataTxtSpreadsheet data = GameDataTxtSpreadsheet.fromTxtFile(
    			"game_data/UniqueItems.txt",
    			row -> ("1".equals(row.get(COL_ENABLED))));
    	
    	for (GameDataTxtSpreadsheet.Row dataRow : data.getRows()) {
    		D2UniqueItem unique = fromDataRow(dataRow);
    		allUniques.add(unique);
    		
    	    if (uniquesByItemTypeCode.containsKey(unique.itemTypeCode)) {
    	    	uniquesByItemTypeCode.get(unique.itemTypeCode).add(unique);
    	    } else {
    	    	List<D2UniqueItem> uniques = new ArrayList<>();
    	    	uniques.add(unique);
    	    	uniquesByItemTypeCode.put(unique.itemTypeCode, uniques);
    	    }
    	}
	}
	
	// "Swordback Hold" has 'thorns' property listed twice, each with min=max=5
	// this will squish them together into one property with min=max=10
	private static void consolidateDuplicatePropertiesInList(D2UniqueItem unique) {
		Set<Integer> indexesToRemove = new HashSet<>();
		for (int i = 0; i < unique.properties.size(); i++) {
			D2PropertyValueRange iProp = unique.properties.get(i);
			for (int j = 0; j < i; j++) {
				D2PropertyValueRange jProp = unique.properties.get(j);
				D2Property.ItemStatModifier statModifier = jProp.property.getStatModifiers().get(0);
				if (iProp.property == jProp.property && nullSafeEquals(iProp.getParam(), jProp.getParam())) {
					if (statModifier.getStatFuncBehavior().statValueUsesRandomRoll) {
						if (iProp.getMin() == iProp.getMax() && jProp.getMin() == jProp.getMax()) {
							jProp.min = jProp.getMin() + iProp.getMin();
							jProp.max = jProp.getMax() + iProp.getMax();
							indexesToRemove.add(i);
						}
					}
				}
			}
		}
		if (indexesToRemove.isEmpty()) {
			return;
		}
		// this is dumb, should just use Iterator with remove() when going through the list the first time
		List<D2PropertyValueRange> newList = new ArrayList<>();
		for (int i = 0; i < unique.properties.size(); i++) {
			if (!indexesToRemove.contains(i)) {
				newList.add(unique.properties.get(i));
			}
		}
		unique.properties = newList;
	}
	
	static boolean nullSafeEquals(Object a, Object b) {
		if (a == null && b == null) return true;
		if ((a == null) ^ (b == null)) return false;
		return a.equals(b);
	}

	public static void linkData() {
		for (D2UniqueItem unique : allUniques) {
			unique.name = D2String.fromKey(unique.uniqueItemCode);
			// Special case for Rainbow Facet (8 Uniques with the same name)
			if ("jew".equals(unique.itemTypeCode)) {
				// like "ltng/death" or "cold/level"
				String disambiguator = unique.properties.get(0).getPropertyCode().substring(4) + "/" + unique.properties.get(3).getPropertyCode().substring(0,5);
			    unique.disambiguatedName = unique.name + " (" + disambiguator +")";
			} else {
				unique.disambiguatedName = unique.name;
			}
			
			unique.itemType = D2ItemType.fromCode(unique.itemTypeCode);
			for (D2PropertyValueRange prop : unique.properties) {
				prop.property = D2Property.fromCode(prop.getPropertyCode());
			}
			
			consolidateDuplicatePropertiesInList(unique);
			
			for (D2PropertyValueRange prop : unique.properties) {
				D2Property.ItemStatModifier statModifier = prop.property.getStatModifiers().get(0);

				if (statModifier.getStatFuncBehavior().statValueUsesRandomRoll) {
					prop.statImpactedByRoll = statModifier.getStatFuncBehavior().statModifierValToImpactedStat.apply(
							new D2Property.StatFuncInputs(
									prop.getMin(), prop.getMax(), prop.getParam(), statModifier.getVal(), statModifier.getItemStatCode(), unique.itemTypeCode));
				}
			}
			
			System.out.println("---\n" + unique.disambiguatedName);
			for (D2PropertyValueRange prop : unique.properties) {
				prop.property = D2Property.fromCode(prop.getPropertyCode());
				D2Property.ItemStatModifier statModifier = prop.property.getStatModifiers().get(0);
				if (statModifier.getStatFuncBehavior().statValueUsesRandomRoll /* && prop.min != prop.max */) {
					D2ItemStat stat = D2ItemStat.fromId(prop.statImpactedByRoll.getStatId());
					int statMin = prop.getMin() << stat.getValShift();
					int statMax = prop.getMax() << stat.getValShift();
					System.out.println(prop.getPropertyCode() + "(" + prop.getParam() + ") --> " + stat.getCode() + "(" + prop.statImpactedByRoll.getParam() + ")  [" + statMin + " - " + statMax +"]");
				}
			}
			
			if (unique.itemType.isSpawnable() && unique.qlvl < 100) {
				spawnableUniquesBelowIlvl100.add(unique);
			}
			
		}
	}
	
	private static D2UniqueItem fromDataRow(GameDataTxtSpreadsheet.Row dataRow) {
		D2UniqueItemBuilder item = D2UniqueItem.builder();
		item.uniqueItemCode = dataRow.get(COL_INDEX);
		item.itemTypeCode = dataRow.get(COL_ITEM_TYPE_CODE);
		item.qlvl = dataRow.getInt(COL_QLVL);
		item.properties = new ArrayList<>();
		
		for (int i = 1; i <= 12; i++) {
			String prop = dataRow.getNullable("prop" + i);
			if (prop == null) continue;
			if (prop.startsWith("*")) continue;
			
			String param = dataRow.getNullable("par" + i);
			int min = dataRow.getIntOrZero("min" + i);
			int max = dataRow.getIntOrZero("max" + i);
			item.properties.add(new D2PropertyValueRange(prop, param, min, max));
		}
		return item.build();
	}

	public boolean isAlwaysEthereal() {
		for (D2PropertyValueRange prop : this.properties) {
			if (prop.getPropertyCode().equals("ethereal") && prop.getMin() >= 1) {
				return true;
			}
		}
		return false;
	}
	
	public boolean canBeEthereal() {
		return this.itemType.canBeEthereal();
	}
	
	public boolean canBeNonEthereal() {
		return !this.isAlwaysEthereal();
	}
}
