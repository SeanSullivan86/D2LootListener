package org.sully.d2.gamemodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import org.sully.d2.gamemodel.derivedstats.WeaponInfoForDamageCalc;
import org.sully.d2.gamemodel.derivedstats.SkillBonuses;
import org.sully.d2.gamemodel.enums.ItemQuality;
import org.sully.d2.gamemodel.enums.SkillTab;
import org.sully.d2.gamemodel.staticgamedata.D2ItemType;
import org.sully.d2.gamemodel.staticgamedata.D2ItemTypeType;
import org.sully.d2.gamemodel.staticgamedata.D2Skill;
import org.sully.d2.gamemodel.staticgamedata.D2UniqueItem;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Value
@Builder
public class D2Item {
	long dropIteration;

	ItemQuality quality;
	String name;
	String description;

	@Getter(onMethod_ = @JsonIgnore)
	byte[] d2sData;
	boolean ethereal;
	int sockets;
	int gold;
	int defense;

	@Getter(onMethod_ = @JsonIgnore)
	D2ItemType itemType;
	@Getter(onMethod_ = @JsonIgnore)
	D2ItemTypeType itemTypeType;
	long id;

	SkillBonuses skillBonuses;
	@Getter(onMethod_ = @JsonIgnore)
	D2UniqueItem uniqueItem;

	@Getter(onMethod_ = @JsonIgnore)
	WeaponInfoForDamageCalc weaponInfoForDamageCalc;
	StatList stats;




	public int getStat(int statId) {
		return stats.getStat(statId);
	}
	
	public boolean hasStat(int statId) {
		return stats.hasStat(statId);
	}
	
	public int getStat(int statId, int statParam) {
		return stats.getStat(statId, statParam);
	}

	public boolean hasAtLeastOneOfTheseStats(Set<Integer> desiredStatIds) {
		return stats.hasAtLeastOneOfTheseStats(desiredStatIds);
	}

	public boolean hasAtLeastOneSkillTabBonus() {
		return skillBonuses.hasAtLeastOneSkillTabBonus();
	}

	public int getTotalBonusIncludingSkillTabAndClassSkillBonuses(D2Skill skill) {
		return skillBonuses.getTotalBonusIncludingSkillTabAndClassSkillBonuses(skill);
	}
	
	public List<SkillBonuses.IndividualSkillBonus> getIllegalStaffmods() {
		return skillBonuses.getIllegalStaffmods(this.itemTypeType);
	}
	
	public SkillBonuses.SkillTabBonus getSkillTabBonus(SkillTab skillTab) {
		return skillBonuses.getSkillTabBonus(skillTab);
	}
	
	public static D2Item fromData(byte[] data, ByteBuffer buf) {

		D2ItemBuilder item = D2Item.builder();
		item.dropIteration = buf.getLong(2);

		String itemTypeCode = new String(data, 10, 3, StandardCharsets.UTF_8);
		item.itemType = D2ItemType.fromCode(itemTypeCode);

		String itemTypeTypeCode = new String(data, 13, 4, StandardCharsets.UTF_8);
		if (itemTypeTypeCode.endsWith(" ")) {
			itemTypeTypeCode = itemTypeTypeCode.substring(0,3); // todo : do it cleaner
		}
		D2ItemTypeType itemTypeType = D2ItemTypeType.fromCode(itemTypeTypeCode);
		if (item.itemType.getItemTypeType() != itemTypeType) {
			throw new RuntimeException("Expected ItemTypeType '" + item.itemType.getItemTypeTypeCode() + "' , but got '" + itemTypeType.getCode() + "'. ItemCode = " + item.itemType.getCode());
		}
		item.itemTypeType = item.itemType.getItemTypeType();
		
		item.quality = ItemQuality.fromId(buf.get(17));
		item.ethereal = buf.get(18) == 1;
		item.sockets = buf.get(19);
		item.gold = buf.getInt(20);
        item.defense = buf.getInt(24);
		
		int itemNameLength = buf.getShort(28);
		int itemDescriptionLength = buf.getShort(30);
		int d2sByteLength = buf.getShort(32);
		int statCount = buf.getShort(34);
		
		final int statsOffset = 36;
		int nameOffset = statsOffset + 8 * statCount;
		int descriptionOffset = nameOffset + itemNameLength;
		int d2sOffset = descriptionOffset + itemDescriptionLength;
		
		item.name = new String(data, nameOffset, itemNameLength, StandardCharsets.UTF_8);
		item.description = new String(data, descriptionOffset, itemDescriptionLength, StandardCharsets.UTF_8);
		item.d2sData = Arrays.copyOfRange(data, d2sOffset, d2sOffset  + d2sByteLength);
		


		StatValue[] stats = new StatValue[statCount];
		for (int i = 0; i < statCount; i++) {
			stats[i] = new StatValue(
					buf.getShort(statsOffset + 8*i + 2),
					buf.getShort(statsOffset + 8*i),
					buf.getInt(statsOffset + 8*i + 4));
		}
		item.stats = new StatList(stats);

		item.skillBonuses = SkillBonuses.deriveSkillBonusesFromStats(item.stats);


		if (item.quality == ItemQuality.UNIQUE) {
			item.uniqueItem = D2UniqueItem.getFromItem(item.itemType, item.stats, item.name);
			item.name = item.uniqueItem.getDisambiguatedName();
		}

		if (item.itemType.getWeaponInfo() != null) {
			item.weaponInfoForDamageCalc = new WeaponInfoForDamageCalc(item.itemType, item.quality, item.stats, item.ethereal, item.sockets);
		}
		
		item.id = ++nextId;
		return item.build();
	}
	static long nextId;
	
	public String toLongString() {
		return String.join("\t", ""+this.dropIteration,
				this.itemTypeType.getCode(),
				this.itemType.getName(),
				this.quality.name(), 
				this.name,
				this.ethereal ? "eth" : "",
				""+this.sockets,
				this.description);
	}


	public String getItemTypeCode() {
		return itemType.getCode();
	}

	public String getItemTypeName() {
		return itemType.getName();
	}
}
