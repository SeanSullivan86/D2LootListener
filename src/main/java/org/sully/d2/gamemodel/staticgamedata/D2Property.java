package org.sully.d2.gamemodel.staticgamedata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.sully.d2.gamemodel.StatIdAndParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Value
@Builder
public class D2Property {
	private static final String COL_CODE = "code";

	
	private static Map<String, D2Property> propertiesByCode = new HashMap<>();
	
	String code;
	List<ItemStatModifier> statModifiers;
	
	public static D2Property fromCode(String code) {
		return propertiesByCode.get(code);
	}



	public static void loadData() {
    	GameDataTxtSpreadsheet data = GameDataTxtSpreadsheet.fromTxtFile(
    			"game_data/Properties.txt",
    			row -> !row.get("func1").isEmpty());
    	
    	for (GameDataTxtSpreadsheet.Row dataRow : data.getRows()) {
    		D2Property property = fromDataRow(dataRow);
    		propertiesByCode.put(property.code, property);
    	}
	}

	public static void linkData() {
		for (String code : propertiesByCode.keySet()) {
			for (ItemStatModifier statModifier : propertiesByCode.get(code).statModifiers) {
				statModifier.itemStat = D2ItemStat.fromCode(statModifier.itemStatCode);
				statModifier.statFuncBehavior = statFuncsById[statModifier.func];
			}
		}
	}
	
	static D2Property fromDataRow(GameDataTxtSpreadsheet.Row dataRow) {
		D2PropertyBuilder property = D2Property.builder();
		property.code = dataRow.get(COL_CODE);
		property.statModifiers(new ArrayList<>());
		for (int i = 1; i <= 7; i++) {
			if (dataRow.getNullable("func" + i) == null) continue;
			String statCode = dataRow.get("stat" + i);
			int func = dataRow.getInt("func" + i);
			Integer set = dataRow.getNullableInteger("set" + i);
			Integer val = dataRow.getNullableInteger("val" + i);
			property.statModifiers.add(new ItemStatModifier(statCode, set, val, func));
		}
		return property.build();
	}
	

	@Value
	@Builder
	@AllArgsConstructor
	public static class ItemStatModifier {
		String itemStatCode;
		Integer set;
		Integer val;
		int func;
		@NonFinal
		D2ItemStat itemStat;
		@NonFinal
		StatFuncBehavior statFuncBehavior;
		
		public ItemStatModifier(String itemStatCode, Integer set, Integer val, int func) {
			this.itemStatCode = itemStatCode;
			this.set = set;
			this.val = val;
			this.func = func;
		}
	}
	
	public static class StatFuncBehavior {
		int statFuncId;
		boolean statValueUsesRandomRoll;
		Function<StatFuncInputs, StatIdAndParam> statModifierValToImpactedStat;
		
		public StatFuncBehavior(int statFuncId, boolean statValueUsesRandomRoll,
				Function<StatFuncInputs, StatIdAndParam> statModifierValToImpactedStat) {
			this.statFuncId = statFuncId;
			this.statValueUsesRandomRoll = statValueUsesRandomRoll;
			this.statModifierValToImpactedStat = statModifierValToImpactedStat;
		}
		
		
	}
	
	public static class StatFuncInputs {
		int propertyMin;
		int propertyMax;
		String propertyParam;
		Integer itemStatModifierVal;
		String statCode;
		String itemTypeCode;
		
		public StatFuncInputs(int propertyMin, int propertyMax, String propertyParam, Integer itemStatModifierVal,
				String statCode, String itemTypeCode) {
			this.propertyMin = propertyMin;
			this.propertyMax = propertyMax;
			this.propertyParam = propertyParam;
			this.itemStatModifierVal = itemStatModifierVal;
			this.statCode = statCode;
			this.itemTypeCode = itemTypeCode;
		}
	}
	
	
	// Given a D2Item and its List<D2PropertyValueRange>, determine which of those propertyValueRanges
	// have some randomness to roll a result
	// for each propertyValueRange, determine what the outcome of the roll was compared to the min/max
	// evaluate whether the item rolled "perfectly"
	
	
	// TODO check which functions sometimes have propertyParam populated
	static StatFuncBehavior[] statFuncsById = new StatFuncBehavior[50];
	static {
		// Lightsabre uses ignore-ac with param 1 and min=max=1, but it seems to be an error, should just ignore param
		statFuncsById[1] = new StatFuncBehavior(1, true, x -> new StatIdAndParam(x.statCode, 0 ));
		
		statFuncsById[2] = new StatFuncBehavior(2, true, x -> new StatIdAndParam(x.statCode, 0)); // used for enhanced defense %
		
		// 3 copies whatever value was rolled for the first stat onto another stat
		
		// 5 dmg-min
		statFuncsById[5] = new StatFuncBehavior(5, true, x -> {

			D2ItemType itemType = D2ItemType.fromCode(x.itemTypeCode);
			if (itemType.getWeaponInfo() != null && itemType.getWeaponInfo().isTwoHanded()) {
				return new StatIdAndParam(D2ItemStats.SECONDARY_MIN_DAMAGE.statId, 0);
			} else {
				return new StatIdAndParam(D2ItemStats.MIN_DAMAGE.statId, 0);
			}
			
			});

		// 6 dmg-max
		statFuncsById[6] = new StatFuncBehavior(6, true, x -> {
			D2ItemType itemType = D2ItemType.fromCode(x.itemTypeCode);
			if (itemType.getWeaponInfo() != null && itemType.getWeaponInfo().isTwoHanded()) {
				return new StatIdAndParam(D2ItemStats.SECONDARY_MAX_DAMAGE.statId, 0);
			} else {
				return new StatIdAndParam(D2ItemStats.MAX_DAMAGE.statId, 0);
			}  });
		
		// 7 dmg%
		statFuncsById[7] = new StatFuncBehavior(7, true, x -> new StatIdAndParam(D2ItemStats.MAXDAMAGE_PERCENT.statId, 0));
		
		statFuncsById[8] = new StatFuncBehavior(8, true, x -> new StatIdAndParam(x.statCode, 0)); // used for + ias / fcr / fbr / fhr
		statFuncsById[10] = new StatFuncBehavior(10, true, x -> { // used for + to skilltab
			int propertyParam = Integer.parseInt(x.propertyParam);
			int itemStatParam = (propertyParam / 3) * 8 + (propertyParam % 3);
			return new StatIdAndParam(x.statCode, itemStatParam);
		});
		statFuncsById[11] = new StatFuncBehavior(11, false, null); // skill proc
		// 12 is for ormus robes, uses property Min/Max as skill Id range
		statFuncsById[12] = new StatFuncBehavior(12, false, null);
		
		// 13 is for max_durability_percent, not needed ?
		
		// 14 is for sockets, uses property min/max (but min/max should be subject to ilvl/itemType limits?)
		// sometimes uses min/max, sometimes uses a single value in the 'param' instead ????
		statFuncsById[14] = new StatFuncBehavior(14, true, x -> new StatIdAndParam(x.statCode, 
				(x.propertyParam == null || x.propertyParam.isEmpty()) ? 0 : Integer.parseInt(x.propertyParam)));
		
		
		// 15 apply input.propertyMin to statCode
		// 16 apply input.propertyMax to statCode
		statFuncsById[15] = new StatFuncBehavior(15, false, null);
		statFuncsById[16] = new StatFuncBehavior(16, false, null);
		
		// 17 takes value from propertyParam, not min/max (TODO: verify that min/max is never populated)
		statFuncsById[17] = new StatFuncBehavior(17, false, null);
		
		// 18 used for time-of-day varying properties (not used in game)
		// 19 charged skill, doesnt use min/max for roll (param=skill , min=charges, max=level)
		statFuncsById[19] = new StatFuncBehavior(19, false, null);
		
		// 20 indestruct, doesnt use min/max
		statFuncsById[20] = new StatFuncBehavior(20, false, null);
		// 21 for item_addclassskills / item_elemskill
		statFuncsById[21] = new StatFuncBehavior(21, true, x -> new StatIdAndParam(x.statCode, x.itemStatModifierVal));
		
		// 22 for item_singleskill / item_aura / item_nonclassskill (properties skill / aura / oskill)
		// sometimes the param is a skill id (integer) and sometimes a skill name or skill desc id
		statFuncsById[22] = new StatFuncBehavior(22, true, x -> 
		    new StatIdAndParam(x.statCode, D2Skill.fromAmbiguousSkillIdentifier(x.propertyParam).getId()));
		
		// 23 ethereal , doesnt use min/max
		statFuncsById[23] = new StatFuncBehavior(23, false, null);
		
		// 24 used for Reanimate As Returned, no randomness
		statFuncsById[24] = new StatFuncBehavior(24, false, null);
		
		// 36 used for hellfire torch charm
		statFuncsById[36] = new StatFuncBehavior(36, false, null);
		
	}
	
	
}
