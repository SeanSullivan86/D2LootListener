package org.sully.d2.gamemodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import org.sully.d2.SerializableD2Item;
import org.sully.d2.gamemodel.derivedstats.AttackingContext;
import org.sully.d2.gamemodel.derivedstats.WeaponInfoForDamageCalc;
import org.sully.d2.gamemodel.derivedstats.SkillBonuses;
import org.sully.d2.gamemodel.enums.CharacterClass;
import org.sully.d2.gamemodel.enums.ItemQuality;
import org.sully.d2.gamemodel.enums.SkillTab;
import org.sully.d2.gamemodel.staticgamedata.*;
import org.sully.d2.itemtracking.DropContextEnum;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

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

	public int getSkillTabBonusLevel(SkillTab skillTab) {
		return Optional.ofNullable(skillBonuses.getSkillTabBonus(skillTab))
				.map(SkillBonuses.SkillTabBonus::getSkillLevelBonus).orElse(0);
	}

	public int getClassSkillBonusLevel(CharacterClass characterClass) {
		for (SkillBonuses.ClassSkillBonus bonus : skillBonuses.getClassSkillBonuses()) {
			if (bonus.getCharacterClass() == characterClass) {
				return bonus.getSkillLevelBonus();
			}
		}
		return 0;
	}

	public int getIndividualSkillBonusWithoutTabOrCharacterClassSkillAffixes(D2Skill skill) {
		return skillBonuses.getIndividualSkillBonusWithoutTabOrCharacterClassSkillAffixes(skill);
	}

	public SerializableD2Item toSerializableD2Item() {
		return SerializableD2Item.builder()
				.id(id)
				.dropContext(dropContext)
				.quality(quality)
				.name(name)
				.description(description)
				.ethereal(ethereal)
				.sockets(sockets)
				.gold(gold)
				.defense(defense)
				.itemTypeCode(itemType.getCode())
				.stats(stats.getStats())
				.originalDmg(originalDmg)
				.originalDmg_1h(originalDmg_1h)
				.upSocketZod4015(upSocketZod4015)
				.upSocketZod4015_1h(upSocketZod4015_1h)
				.upSocketZodOhm(upSocketZodOhm)
				.upSocketZodOhm_1h(upSocketZodOhm_1h)
				.upSocketZod(upSocketZod)
				.upSocketZod_1h(upSocketZod_1h)
				.upSocket4015_eth(upSocket4015_eth)
				.upSocket4015_eth_1h(upSocket4015_eth_1h)
				.upSocketOhm_eth(upSocketOhm_eth)
				.upSocketOhm_eth_1h(upSocketOhm_eth_1h)
				.upSocket_eth(upSocket_eth)
				.upSocket_eth_1h(upSocket_eth_1h)
				.build();
	}

	public static D2Item fromSerializableD2Item(SerializableD2Item input) {
		D2ItemBuilder item = D2Item.builder();
		item.id = input.getId();
		item.itemType = D2ItemType.fromCode(input.getItemTypeCode());

		item.dropContext = input.getDropContext();

		item.itemTypeType = item.itemType.getItemTypeType();
		item.quality = input.getQuality();
		item.ethereal = input.isEthereal();
		item.sockets = input.getSockets();
		item.gold = input.getGold();
		item.defense = input.getDefense();

		item.name = input.getName();
		item.description = input.getDescription();

		item.stats = new StatList(input.getStats());

		populateDerivedStats(item);

		return item.build();
	}

	static Set<Integer> statsSeen = new HashSet<>();

	// "1h" variants only populated for 2-handed swords, with damage when used 1-handed by barbarian

	DamageOption originalDmg, originalDmg_1h; // different Consumers will allow or disallow ethereal items
	// "Zod" means "fill with zod if necessary to make the item long-lasting". If it can't be made long-lasting, then these will be null
	DamageOption upSocketZod4015, upSocketZod4015_1h;
	DamageOption upSocketZodOhm, upSocketZodOhm_1h;
	DamageOption upSocketZod, upSocketZod_1h;

	// "eth" options are only populated for ethereal items
	DamageOption upSocket4015_eth, upSocket4015_eth_1h;
	DamageOption upSocketOhm_eth, upSocketOhm_eth_1h;
	DamageOption upSocket_eth, upSocket_eth_1h;


	
	public static D2Item fromData(byte[] data, ByteBuffer buf, int offset, DropContextEnum dropContext) {

		D2ItemBuilder item = D2Item.builder();

		String itemTypeCode = new String(data, offset, 3, StandardCharsets.UTF_8);
		item.itemType = D2ItemType.fromCode(itemTypeCode);
		item.dropContext = dropContext;

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

		populateDerivedStats(item);

		/*
		if (item.quality == ItemQuality.NORMAL || item.quality == ItemQuality.MAGIC || item.quality == ItemQuality.RARE) {
			for (StatValue stat : stats) {
				if (!statsSeen.contains(stat.statId)) {
					statsSeen.add(stat.statId);
					System.out.println(statsSeen.size() + " : New stat " + stat.statId + " param " + stat.statParam + " value = " + stat.statValue);
					System.out.println(String.join("\t",
							item.itemTypeType.getCode(),
							item.itemType.getName(),
							item.quality.name(),
							item.name,
							item.ethereal ? "eth" : "",
							""+item.sockets,
							item.description));
				}
			}
		} */
		
		item.id = ++nextId;
		return item.build();
	}
	public static long nextId;

	private static void populateDerivedStats(D2ItemBuilder item) {
		item.skillBonuses = SkillBonuses.deriveSkillBonusesFromStats(item.stats);

		if (item.quality == ItemQuality.UNIQUE) {
			item.uniqueItem = D2UniqueItem.getFromItem(item.itemType, item.stats, item.name);
			item.name = item.uniqueItem.getDisambiguatedName();
		}

		if (item.itemType.getWeaponInfo() != null) {
			item.weaponInfoForDamageCalc = new WeaponInfoForDamageCalc(item.itemType, item.quality, item.stats, item.ethereal, item.sockets);
		}

		if (item.itemType.getWeaponInfo() != null && item.quality == ItemQuality.RARE) {
			// originalDmg
			WeaponInfoForDamageCalc x = item.weaponInfoForDamageCalc;
			DamageOption damage;

			boolean isTwoHandedSword = "swor".equals(item.itemTypeType.getCode()) && item.itemType.getWeaponInfo().isTwoHanded();

			item.originalDmg = x.getDamage(AttackingContext.barbTwoHandedSword);
			if (isTwoHandedSword) {
				item.originalDmg_1h = x.getDamage(AttackingContext.barbOneHandedSword);
			}

			if (item.weaponInfoForDamageCalc.isAlreadyLongLastingOrCanBeFixedWithSocketingAndZod()) {
				// upSocketZod4015
				x = item.weaponInfoForDamageCalc
						.upgradeRareOrUniqueToEliteAndAddSocketIfSocketableAndNotAlreadySocketed()
						.addZodRuneIfItemIsNotAlreadyLongLasting()
						.add_40ED_15IAS_JewelsToRemainingSockets();
				item.upSocketZod4015 = x.getDamage(AttackingContext.barbTwoHandedSword);
				if (isTwoHandedSword) {
					item.upSocketZod4015_1h = x.getDamage(AttackingContext.barbOneHandedSword);
				}

				// upSocketZodOhm
				x = item.weaponInfoForDamageCalc
						.upgradeRareOrUniqueToEliteAndAddSocketIfSocketableAndNotAlreadySocketed()
						.addZodRuneIfItemIsNotAlreadyLongLasting()
						.addOhmRunesToRemainingSockets();
				item.upSocketZodOhm = x.getDamage(AttackingContext.barbTwoHandedSword);
				if (isTwoHandedSword) {
					item.upSocketZodOhm_1h = x.getDamage(AttackingContext.barbOneHandedSword);
				}



				// upSocketZod
				x = item.weaponInfoForDamageCalc
						.upgradeRareOrUniqueToEliteAndAddSocketIfSocketableAndNotAlreadySocketed()
						.addZodRuneIfItemIsNotAlreadyLongLasting();
				item.upSocketZod = x.getDamage(AttackingContext.barbTwoHandedSword);
				if (isTwoHandedSword) {
					item.upSocketZod_1h = x.getDamage(AttackingContext.barbOneHandedSword);
				}

			}

			if (item.ethereal) {
				// upSocket4015_eth
				x = item.weaponInfoForDamageCalc
						.upgradeRareOrUniqueToEliteAndAddSocketIfSocketableAndNotAlreadySocketed()
						.add_40ED_15IAS_JewelsToRemainingSockets();
				item.upSocket4015_eth = x.getDamage(AttackingContext.barbTwoHandedSword);
				if (isTwoHandedSword) {
					item.upSocket4015_eth_1h = x.getDamage(AttackingContext.barbOneHandedSword);
				}

				// upSocketOhm_eth
				x = item.weaponInfoForDamageCalc
						.upgradeRareOrUniqueToEliteAndAddSocketIfSocketableAndNotAlreadySocketed()
						.addOhmRunesToRemainingSockets();
				item.upSocketOhm_eth = x.getDamage(AttackingContext.barbTwoHandedSword);
				if (isTwoHandedSword) {
					item.upSocketOhm_eth_1h = x.getDamage(AttackingContext.barbOneHandedSword);
				}

				// upSocket_eth
				x = item.weaponInfoForDamageCalc
						.upgradeRareOrUniqueToEliteAndAddSocketIfSocketableAndNotAlreadySocketed();
				item.upSocket_eth = x.getDamage(AttackingContext.barbTwoHandedSword);
				if (isTwoHandedSword) {
					item.upSocket_eth_1h = x.getDamage(AttackingContext.barbOneHandedSword);
				}
			}

		}
	}


	public boolean isOneHandableByBarbarian() {
		return this.itemType.getWeaponInfo().isOneHandableByBarbarian();
	}

	
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

	public int getCasterValueForRareArmorOrJewelry() {
		double result = 0.0;
		if (! skillBonuses.getClassSkillBonuses().isEmpty()) {
			SkillBonuses.ClassSkillBonus classSkillBonus = skillBonuses.getClassSkillBonuses().getFirst();
			if (classSkillBonus.getCharacterClass() == CharacterClass.SORCERESS ||
					classSkillBonus.getCharacterClass() == CharacterClass.NECROMANCER ||
					classSkillBonus.getCharacterClass() == CharacterClass.DRUID) {
				result += 100.0 * classSkillBonus.getSkillLevelBonus();
			}
		}
		result += stats.getStat(D2ItemStats.LIGHTNING_RESIST.statId);
		result += stats.getStat(D2ItemStats.FIRE_RESIST.statId);
		result += stats.getStat(D2ItemStats.COLD_RESIST.statId);
		result += stats.getStat(D2ItemStats.POISON_RESIST.statId) * 0.3;
		result += stats.getStat(D2ItemStats.STRENGTH.statId) * 0.5;
		result += stats.getStat(D2ItemStats.DEXTERITY.statId) * 0.1;
		result += stats.getStat(D2ItemStats.VITALITY.statId) * 0.67;
		result += stats.getStat(D2ItemStats.ENERGY.statId) * 0.67;
		result += stats.getStat(D2ItemStats.LIFE.statId)/256.0 * 0.3;
		result += stats.getStat(D2ItemStats.MANA.statId)/256.0 * 0.3;
		result += stats.getStat(D2ItemStats.LIFE_PER_LEVEL.statId)/256.0/8.0*90.0 * 0.3;
		result += stats.getStat(D2ItemStats.MANA_PER_LEVEL.statId)/256.0/8.0*90.0 * 0.3;
		result += stats.getStat(D2ItemStats.MANA_AFTER_EACH_KILL.statId) * 4.0;
		result += stats.getStat(D2ItemStats.FASTER_HIT_RECOVERY.statId) * (20.0/24.0);
		result += stats.getStat(D2ItemStats.FASTER_CAST_RATE.statId) * 8.0;
		result += stats.getStat(D2ItemStats.MAGIC_FIND.statId) * 0.5;
		result += stats.getStat(D2ItemStats.DAMAGE_REDUCTION.statId) * 0.75;
		result += stats.getStat(D2ItemStats.MAGIC_DAMAGE_REDUCTION.statId) * 0.75;
		result += stats.getStat(D2ItemStats.FASTER_RUN_WALK_SPEED.statId) * 2.0;

		if (sockets > 1 && (quality == ItemQuality.RARE || quality == ItemQuality.SET || quality == ItemQuality.UNIQUE)) {
			result += (sockets - 1)*120.0;
		} else if (sockets > 2 && quality == ItemQuality.MAGIC) {
			result += (sockets - 2)*120.0;
		}

		// If normal items have 0 sockets, they can be socketed via Larzuk quest to get the max number of sockets for that item
		// so a normal-quality item is bad if it spawned with some number of sockets greater than 0 but less than the
		// max number of sockets for that item.
		if (quality == ItemQuality.NORMAL && sockets > 0) {
			int maxSockets = this.itemType.getEquipmentInfo().getEliteType().getMaxSocketsAtHighIlvl();
			if (sockets < maxSockets) {
				result -= (maxSockets - sockets) * 120.0;
			}
		}

		if (result < 0) result = 0;
		return (int) result;
	}

	public int getPlusLifeStat() {
		return getStat(D2ItemStats.LIFE.statId)/256;
	}
}

