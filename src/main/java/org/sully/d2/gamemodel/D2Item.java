package org.sully.d2.gamemodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import org.sully.d2.SerializableD2Item;
import org.sully.d2.gamemodel.derivedstats.WeaponInfoForDamageCalc;
import org.sully.d2.gamemodel.derivedstats.SkillBonuses;
import org.sully.d2.gamemodel.enums.ItemQuality;
import org.sully.d2.gamemodel.enums.SkillTab;
import org.sully.d2.gamemodel.staticgamedata.D2ItemType;
import org.sully.d2.gamemodel.staticgamedata.D2ItemTypeType;
import org.sully.d2.gamemodel.staticgamedata.D2Skill;
import org.sully.d2.gamemodel.staticgamedata.D2UniqueItem;
import org.sully.d2.itemtracking.DropContextEnum;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Value
@Builder
public class D2Item {
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

	DropContextEnum dropContext;
	long dcIteration;



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

	public SerializableD2Item toSerializableD2Item() {
		return SerializableD2Item.builder()
				.id(id)
				.dropContext(dropContext)
				.dcIteration(dcIteration)
				.quality(quality)
				.name(name)
				.description(description)
				.ethereal(ethereal)
				.sockets(sockets)
				.gold(gold)
				.defense(defense)
				.itemTypeCode(itemType.getCode())
				.stats(stats.getStats())
				.build();
	}

	public static D2Item fromSerializableD2Item(SerializableD2Item input) {
		D2ItemBuilder item = D2Item.builder();
		item.itemType = D2ItemType.fromCode(input.getItemTypeCode());


		item.itemTypeType = item.itemType.getItemTypeType();
		item.quality = input.getQuality();
		item.ethereal = input.isEthereal();
		item.sockets = input.getSockets();
		item.gold = input.getGold();
		item.defense = input.getDefense();

		item.name = input.getName();
		item.description = input.getDescription();

		item.stats = new StatList(input.getStats());

		item.skillBonuses = SkillBonuses.deriveSkillBonusesFromStats(item.stats);

		if (item.quality == ItemQuality.UNIQUE) {
			item.uniqueItem = D2UniqueItem.getFromItem(item.itemType, item.stats, item.name);
		}

		if (item.itemType.getWeaponInfo() != null) {
			item.weaponInfoForDamageCalc = new WeaponInfoForDamageCalc(item.itemType, item.quality, item.stats, item.ethereal, item.sockets);
		}
		return item.build();
	}
	
	public static D2Item fromData(byte[] data, ByteBuffer buf, int offset) {

		D2ItemBuilder item = D2Item.builder();

		String itemTypeCode = new String(data, offset, 3, StandardCharsets.UTF_8);
		item.itemType = D2ItemType.fromCode(itemTypeCode);

		String itemTypeTypeCode = new String(data, offset+3, 4, StandardCharsets.UTF_8);
		if (itemTypeTypeCode.endsWith(" ")) {
			itemTypeTypeCode = itemTypeTypeCode.substring(0,3); // todo : do it cleaner
		}
		D2ItemTypeType itemTypeType = D2ItemTypeType.fromCode(itemTypeTypeCode);
		if (item.itemType == null) {
			throw new RuntimeException("Unexpected null item type");
		}
		if (item.itemType.getItemTypeType() != itemTypeType) {
			throw new RuntimeException("Expected ItemTypeType '" + item.itemType.getItemTypeTypeCode() + "' , but got '" + itemTypeType.getCode() + "'. ItemCode = " + item.itemType.getCode());
		}
		item.itemTypeType = item.itemType.getItemTypeType();
		
		item.quality = ItemQuality.fromId(buf.get(offset + 7));
		item.ethereal = buf.get(offset + 8) == 1;
		item.sockets = buf.get(offset + 9);
		item.gold = buf.getInt(offset + 10);
        item.defense = buf.getInt(offset + 14);
		
		int itemNameLength = buf.getShort(offset + 18);
		int itemDescriptionLength = buf.getShort(offset + 20);
		int d2sByteLength = buf.getShort(offset + 22);
		int statCount = buf.getShort(offset + 24);

		int statsOffset = offset + 26;
		if (item.quality == ItemQuality.MAGIC || item.quality == ItemQuality.RARE) {
			statsOffset += 12;
			/*
			for (int i = 0; i < 6; i++) {
				System.out.println("Affix ID : " + buf.getShort(offset + 26 + 2*i));
			} */
		}


		int nameOffset = statsOffset + 8 * statCount;
		int descriptionOffset = nameOffset + itemNameLength;
		int d2sOffset = descriptionOffset + itemDescriptionLength;
		
		item.name = new String(data, nameOffset, itemNameLength, StandardCharsets.UTF_8);
		item.description = new String(data, descriptionOffset, itemDescriptionLength, StandardCharsets.UTF_8);
		item.d2sData = Arrays.copyOfRange(data, d2sOffset, d2sOffset  + d2sByteLength);

		/*
		if (item.quality == ItemQuality.MAGIC || item.quality == ItemQuality.RARE) {
			System.out.println(item.name + " : " + item.description);
			System.out.println("---");
		} */


		List<StatValue> stats = new ArrayList<>(statCount);
		for (int i = 0; i < statCount; i++) {
			stats.add(new StatValue(
					buf.getShort(statsOffset + 8*i + 2),
					buf.getShort(statsOffset + 8*i),
					buf.getInt(statsOffset + 8*i + 4)));
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
	public static long nextId;
	
	public String toLongString() {
		return String.join("\t",
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
