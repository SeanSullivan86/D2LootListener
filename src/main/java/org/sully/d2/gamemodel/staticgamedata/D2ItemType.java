package org.sully.d2.gamemodel.staticgamedata;

import lombok.*;
import lombok.experimental.NonFinal;
import org.sully.d2.gamemodel.staticgamedata.strings.D2String;
import org.sully.d2.util.IntRange;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Value
@Builder
public class D2ItemType {
	private static final String COL_CODE = "code";
	private static final String COL_ITEM_TYPE_TYPE_CODE = "type";
	private static final String COL_GEMSOCKETS = "gemsockets";
	private static final String COL_SPAWNABLE = "spawnable";
	private static final String COL_NO_DURABILITY = "nodurability";
	
	String code;
	String nameStringCode;
	String itemTypeTypeCode;
	int gemSockets;
	boolean spawnable;
	boolean hasDurability;
	
	EquipmentInfo equipmentInfo;
	WeaponInfo weaponInfo;
	ArmorInfo armorInfo;

	@NonFinal
	String name;
	@NonFinal
	D2ItemTypeType itemTypeType;
	@NonFinal
	int maxSocketsAtHighIlvl;

	public boolean isAThrowingWeapon() {
		// This is equivalent to being within itemTypeType "thro"
		return this.weaponInfo != null && this.weaponInfo.getThrowDamage() != null;
	}
	
    private static Map<String, D2ItemType> itemTypesByCode;
    
    public static D2ItemType fromCode(String code) {
    	return itemTypesByCode.get(code);
    }
    
    public static Collection<D2ItemType> allItemTypes() {
    	return Collections.unmodifiableCollection(itemTypesByCode.values());
    }

	public static void linkData() {
    	for (String code : itemTypesByCode.keySet()) {
    		D2ItemType type = itemTypesByCode.get(code);
    		type.itemTypeType = D2ItemTypeType.fromCode(type.itemTypeTypeCode);
    		type.name = D2String.fromKey(type.nameStringCode);
    		type.maxSocketsAtHighIlvl = Math.min(type.gemSockets, type.itemTypeType.getMaxSock40());
    	}
    }

	public static void loadData() {
    	itemTypesByCode = new HashMap<>();
    	
    	GameDataTxtSpreadsheet weaponData = GameDataTxtSpreadsheet.fromTxtFile(
    			"game_data/Weapons.txt",
    			row -> (! row.get("code").isEmpty()));
    	
    	for (GameDataTxtSpreadsheet.Row dataRow : weaponData.getRows()) {
    		D2ItemTypeBuilder item = fromDataRow(dataRow);
    		item.equipmentInfo = EquipmentInfo.fromDataRow(dataRow);
    		item.weaponInfo = WeaponInfo.fromDataRow(dataRow);
    		itemTypesByCode.put(item.code, item.build());
    	}
    	
    	GameDataTxtSpreadsheet armorData = GameDataTxtSpreadsheet.fromTxtFile(
    			"game_data/Armor.txt",
    			row -> (! row.get("code").isEmpty()));
    	
    	for (GameDataTxtSpreadsheet.Row dataRow : armorData.getRows()) {
    		D2ItemTypeBuilder item = fromDataRow(dataRow);
    		item.equipmentInfo = EquipmentInfo.fromDataRow(dataRow);
    		item.armorInfo = ArmorInfo.fromDataRow(dataRow);
    		itemTypesByCode.put(item.code, item.build());
    	}
    	
    	GameDataTxtSpreadsheet miscItemData = GameDataTxtSpreadsheet.fromTxtFile(
    			"game_data/Misc.txt",
    			row -> (! row.get("code").isEmpty()));
    	
    	for (GameDataTxtSpreadsheet.Row dataRow : miscItemData.getRows()) {
    		D2ItemTypeBuilder item = fromDataRow(dataRow);
    		// TODO if any fields are needed that are unique to Misc.txt, create MiscItemInfo class and read them there
    		itemTypesByCode.put(item.code, item.build());
    	}
    	
    	// Link equipment types to normal/exceptional/elite versions
    	for (String code : itemTypesByCode.keySet()) {
    		D2ItemType type = itemTypesByCode.get(code);
    		if (type.equipmentInfo != null) {
    			type.equipmentInfo.normalType = itemTypesByCode.get(type.equipmentInfo.normalCode);
    			type.equipmentInfo.exceptionalType = itemTypesByCode.get(type.equipmentInfo.exceptionalCode);
    			type.equipmentInfo.eliteType = itemTypesByCode.get(type.equipmentInfo.eliteCode);
    		}
    	}
    	
    }
    
    private static D2ItemTypeBuilder fromDataRow(GameDataTxtSpreadsheet.Row data) {
    	D2ItemTypeBuilder item = D2ItemType.builder();
    	item.code = data.get(COL_CODE);
    	item.itemTypeTypeCode = data.get(COL_ITEM_TYPE_TYPE_CODE);
    	item.gemSockets = data.getIntOrZero(COL_GEMSOCKETS);
    	item.spawnable = data.getIntOrZero(COL_SPAWNABLE) == 1;
    	item.hasDurability = ! (data.getIntOrZero(COL_NO_DURABILITY) == 1);
		item.nameStringCode = data.get("namestr");
    	return item;
    }

    // Fields shared by both Weapons and Armors, but not Misc items
	@Data
	@Builder
	public static class EquipmentInfo {
		private static final String COL_NORM_CODE = "normcode";
		private static final String COL_EXCEPTIONAL_CODE = "ubercode";
		private static final String COL_ELITE_CODE = "ultracode";
		
		final String normalCode;
		final String exceptionalCode;
		final String eliteCode;
		@ToString.Exclude D2ItemType normalType;
		@ToString.Exclude D2ItemType exceptionalType;
		@ToString.Exclude D2ItemType eliteType;
		
		static EquipmentInfo fromDataRow(GameDataTxtSpreadsheet.Row data) {
		    EquipmentInfoBuilder item = EquipmentInfo.builder();
		    item.normalCode = data.get(COL_NORM_CODE);
		    item.exceptionalCode = data.get(COL_EXCEPTIONAL_CODE);
		    item.eliteCode = data.get(COL_ELITE_CODE);
		    
		    return item.build();
		}
	}

	@Value
	@Builder
	public static class WeaponInfo {
		
		private static final String COL_MIN_DAMAGE = "mindam";
		private static final String COL_MAX_DAMAGE = "maxdam";
		private static final String COL_2_HANDED = "2handed";
		private static final String COL_CAN_BE_EITHER_1_OR_2_HANDED = "1or2handed";
		private static final String COL_2_HANDED_MIN_DAMAGE = "2handmindam";
		private static final String COL_2_HANDED_MAX_DAMAGE = "2handmaxdam";
		private static final String COL_MIN_THROW_DAMAGE = "minmisdam";
		private static final String COL_MAX_THROW_DAMAGE = "maxmisdam";
		private static final String COL_SPEED = "speed";
		private static final String COL_WEAPON_CLASS = "wclass";
		private static final String COL_2_HANDED_WEAPON_CLASS = "2handedwclass";
		
		public boolean isOneHandableByBarbarian() {
			return (!this.isTwoHanded) || this.oneHandedDamageIfTwoHandedSwordWieldedByBarbarian != null;
		}
		
		private static WeaponInfo fromDataRow(GameDataTxtSpreadsheet.Row data) {
			WeaponInfoBuilder weapon = WeaponInfo.builder();
			weapon.isTwoHanded = data.getBoolean(COL_2_HANDED);
			if (weapon.isTwoHanded) {
				weapon.damageWithNormalHandedness = IntRange.withMinAndMax(data.getInt(COL_2_HANDED_MIN_DAMAGE), data.getInt(COL_2_HANDED_MAX_DAMAGE));
			} else {
				weapon.damageWithNormalHandedness = IntRange.withMinAndMax(data.getInt(COL_MIN_DAMAGE), data.getInt(COL_MAX_DAMAGE));
			}
			
			if (! data.isEmpty(COL_MIN_THROW_DAMAGE)) {
				weapon.throwDamage = IntRange.withMinAndMax(data.getInt(COL_MIN_THROW_DAMAGE), data.getInt(COL_MAX_THROW_DAMAGE));
			}
			if (data.getBoolean(COL_CAN_BE_EITHER_1_OR_2_HANDED)) {
				weapon.oneHandedDamageIfTwoHandedSwordWieldedByBarbarian = IntRange.withMinAndMax(data.getInt(COL_MIN_DAMAGE), data.getInt(COL_MAX_DAMAGE));
			}
			
			weapon.speed = data.getIntOrZero(COL_SPEED);
			weapon.wClass = data.get(COL_WEAPON_CLASS);
			weapon.twoHandedWClass = data.get(COL_2_HANDED_WEAPON_CLASS);
			
			weapon.barbAnimLength = barbAnimLengthByWeaponClass.get(weapon.twoHandedWClass);
			weapon.barbAnimLength1Handed = barbAnimLengthByWeaponClass.get(weapon.wClass);
			return weapon.build();
		}

		boolean isTwoHanded; // "2handed" column in weapons.txt
		
		IntRange damageWithNormalHandedness;
		IntRange throwDamage;
		IntRange oneHandedDamageIfTwoHandedSwordWieldedByBarbarian;
		
		int speed;
		String wClass;
		String twoHandedWClass;
		int barbAnimLength;
		int barbAnimLength1Handed;
		
	    static Map<String,Integer> barbAnimLengthByWeaponClass = new HashMap<>();
	    static {
	    	barbAnimLengthByWeaponClass.put("1hs", 16);
	    	barbAnimLengthByWeaponClass.put("1ht", 16);
	    	barbAnimLengthByWeaponClass.put("1js", 16);
	    	barbAnimLengthByWeaponClass.put("1jt", 16);
	    	barbAnimLengthByWeaponClass.put("1ss", 16);
	    	barbAnimLengthByWeaponClass.put("1st", 16);
	    	barbAnimLengthByWeaponClass.put("2hs", 18);
	    	barbAnimLengthByWeaponClass.put("2ht", 19);
	    	barbAnimLengthByWeaponClass.put("bow", 15);
	    	barbAnimLengthByWeaponClass.put("hth", 12);
	    	barbAnimLengthByWeaponClass.put("stf", 19);
	    	barbAnimLengthByWeaponClass.put("xbw", 20);
	    	barbAnimLengthByWeaponClass.put("ht1", 16); // claws, cant actually be used by barb
	    	
	    }
		
	}

	@Value
	@Builder
	public static class ArmorInfo {
		private static final String COL_MIN_DEFENSE = "minac";
		private static final String COL_MAX_DEFENSE = "maxac";
		
		public int minDefense;
		public int maxDefense;
		
		private static ArmorInfo fromDataRow(GameDataTxtSpreadsheet.Row data) {
			ArmorInfoBuilder item = ArmorInfo.builder();
		    item.minDefense = data.getInt(COL_MIN_DEFENSE);
		    item.maxDefense = data.getInt(COL_MAX_DEFENSE);
		    
		    return item.build();
		}
	}
	
	public boolean canBeEthereal() {
		return this.hasDurability;
	}
}