package org.sully.d2.itemtracking;

import org.sully.d2.gamemodel.D2Item;
import org.sully.d2.gamemodel.DamageOption;
import org.sully.d2.gamemodel.derivedstats.SkillBonuses;
import org.sully.d2.gamemodel.enums.ItemQuality;
import org.sully.d2.gamemodel.enums.SkillTab;
import org.sully.d2.gamemodel.staticgamedata.D2ItemStats;
import org.sully.d2.gamemodel.staticgamedata.D2Skills;
import org.sully.d2.itemtracking.uniques.PerfectUniquesTracker;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class HardcodedTCDropConsumerConfiguration {

    public Map<DropContextEnum, List<D2TCDropConsumer>> initializeConsumers(Set<DropContextEnum> dropContexts) {
        Map<DropContextEnum,List<D2TCDropConsumer>> consumersByDropContext = new HashMap<>();
        for (DropContextEnum dropContext : dropContexts) {
            consumersByDropContext.put(dropContext, initializeConsumersForSingleDropContext(dropContext));
        }
        return consumersByDropContext;
    }

    public List<D2TCDropConsumer> initializeConsumersForSingleDropContext(DropContextEnum dropContext) {

        List<D2TCDropConsumer> allConsumers = new ArrayList<>();

        allConsumers.add(TopNConsumer.withId("GOLD")
                .addItemTypeCodes("gld")
                .allowItemQualities(ItemQuality.NORMAL)
                .withScoringFunction(item -> item.getGold())
                .withCountOfTopScoringItemsToKeepInEachCategory(1)
                .includeScoreDistribution(true)
                .build());

        allConsumers.add( new ItemGrid(
                "ITEM_COUNTS_BY_TYPE_AND_QUALITY",
                item -> item.getItemType().getName(),
                item -> item.getQuality().name(),
                item -> true,
                itemName -> itemName,
                quality -> quality,
                Comparator.naturalOrder(),
                Comparator.naturalOrder()));

        allConsumers.add(new ItemGrid(
                "COUNTS_OF_SET_AND_UNIQUES_BY_NAME",
                D2Item::getName,
                item -> item.isEthereal() ? "Ethereal" : "Non-Ethereal",
                item -> (item.getQuality() == ItemQuality.SET || item.getQuality() == ItemQuality.UNIQUE),
                name -> name,
                eth -> eth,
                Comparator.naturalOrder(),
                Comparator.naturalOrder()));

        allConsumers.add(new ItemGrid(
                "RARE_ENHANCED_DAMAGE_WEAPONS",
                item -> {
                    int ed = item.getStat(D2ItemStats.MAXDAMAGE_PERCENT.statId);
                    if (ed < 10) throw new RuntimeException("Unexpected low damage: " + item.toLongString());
                    if (ed <= 20) return "[A] Jagged [10-20%]";
                    if (ed <= 30) return "[B] Deadly [21-30%]";
                    if (ed <= 40) return "[C] Vicious [31-40%]";
                    if (ed <= 50) return "[D] Brutal [41-50%]";
                    if (ed <= 65) return "[E] Massive [51-65%]";
                    if (ed <= 80) return "[F] Savage [66-80%]";
                    if (ed <= 100) return "[G] Merciless [81-100%]";
                    if (ed <= 200) return "[H] Ferocious [101-200%]";
                    if (ed <= 300) return "[I] Cruel [201-300%]";
                    throw new RuntimeException("Unexpected high damage: " + item.toLongString());
                },
                item -> "Any",
                item -> (item.getQuality() == ItemQuality.RARE && item.getWeaponInfoForDamageCalc() != null &&
                        item.hasStat(D2ItemStats.MAXDAMAGE_PERCENT.statId) && (! item.hasStat(D2ItemStats.ATTACK_RATING.statId))),
                name -> name,
                name -> name,
                Comparator.naturalOrder(),
                Comparator.naturalOrder()));

        allConsumers.add(new BasicStatsConsumer("BASIC_STATS"));

        allConsumers.add(new AssortedMagicItemsConsumer("MAGIC_ITEMS"));

        allConsumers.add(new PerfectUniquesTracker());

        allConsumers.add(TopNConsumer.withId("TRI_RES_BOOTS")
                .addItemTypeTypeCodes("boot")
                .allowItemQualities(ItemQuality.RARE)
                .withAdditionalItemCriteria(item ->
                        (! item.isEthereal()) &&
                        item.hasStat(D2ItemStats.FIRE_RESIST.statId) &&
                        item.hasStat(D2ItemStats.LIGHTNING_RESIST.statId) &&
                        item.hasStat(D2ItemStats.COLD_RESIST.statId))
                .withScoringFunction(item ->
                                item.getStat(D2ItemStats.FIRE_RESIST.statId) +
                                item.getStat(D2ItemStats.LIGHTNING_RESIST.statId) +
                                item.getStat(D2ItemStats.COLD_RESIST.statId))
                .withCountOfTopScoringItemsToKeepInEachCategory(10)
                .build());

        allConsumers.add(TopNConsumer.withId("RARE_CASTER_BOOTS")
                .addItemTypeTypeCodes("boot")
                .allowItemQualities(ItemQuality.RARE)
                .withAdditionalItemCriteria(item -> (!item.isEthereal()))
                .withScoringFunction(D2Item::getCasterValueForRareArmorOrJewelry)
                .withCountOfTopScoringItemsToKeepInEachCategory(10)
                .build());

        allConsumers.add(TopNConsumer.withId("RARE_CASTER_RINGS")
                .addItemTypeTypeCodes("ring")
                .allowItemQualities(ItemQuality.RARE)
                .withScoringFunction(D2Item::getCasterValueForRareArmorOrJewelry)
                .withCountOfTopScoringItemsToKeepInEachCategory(10)
                .build());

        allConsumers.add(TopNConsumer.withId("RARE_CASTER_AMULETS")
                .addItemTypeTypeCodes("amul")
                .allowItemQualities(ItemQuality.RARE)
                .withScoringFunction(D2Item::getCasterValueForRareArmorOrJewelry)
                .withCountOfTopScoringItemsToKeepInEachCategory(10)
                .build());

        allConsumers.add(TopNConsumer.withId("RARE_CASTER_CIRCLETS")
                .addItemTypeTypeCodes("circ")
                .allowItemQualities(ItemQuality.RARE)
                .withAdditionalItemCriteria(item -> (!item.isEthereal()))
                .withScoringFunction(D2Item::getCasterValueForRareArmorOrJewelry)
                .withCountOfTopScoringItemsToKeepInEachCategory(10)
                .build());

        allConsumers.add(CategorizedTopN.withId("FIRE_SORC_ORBS")
                .addItemTypeTypeCodes("orb")
                .allowItemQualities(ItemQuality.RARE, ItemQuality.MAGIC, ItemQuality.NORMAL)
                .withCategorizer(item -> item.getQuality().name())
                .withAdditionalItemCriteria(item ->
                    item.getSkillTabBonus(SkillTab.SORC_FIRE_SPELLS) != null ||
                    item.getIndividualSkillBonusWithoutTabOrCharacterClassSkillAffixes(D2Skills.FIRE_BALL.get()) > 0 ||
                    item.getIndividualSkillBonusWithoutTabOrCharacterClassSkillAffixes(D2Skills.FIRE_MASTERY.get()) > 0 ||
                    item.getIndividualSkillBonusWithoutTabOrCharacterClassSkillAffixes(D2Skills.METEOR.get()) > 0)
                .withScoringFunction(item -> {
                    double result = item.getCasterValueForRareArmorOrJewelry();
                    result += Optional.ofNullable(item.getSkillTabBonus(SkillTab.SORC_FIRE_SPELLS)).map(SkillBonuses.SkillTabBonus::getSkillLevelBonus).orElse(0) * 90;
                    result += 30 * item.getIndividualSkillBonusWithoutTabOrCharacterClassSkillAffixes(D2Skills.FIRE_BALL.get());
                    result += 25 * item.getIndividualSkillBonusWithoutTabOrCharacterClassSkillAffixes(D2Skills.FIRE_MASTERY.get());
                    result += 30 * item.getIndividualSkillBonusWithoutTabOrCharacterClassSkillAffixes(D2Skills.METEOR.get());
                    return (int) result;
                })
                .withCountOfTopScoringItemsToKeepInEachCategory(10)
                .build());

        allConsumers.add(CategorizedTopN.withId("LIGHTNING_SORC_ORBS")
                .addItemTypeTypeCodes("orb")
                .allowItemQualities(ItemQuality.RARE, ItemQuality.MAGIC, ItemQuality.NORMAL)
                .withCategorizer(item -> item.getQuality().name())
                .withAdditionalItemCriteria(item ->
                        item.getSkillTabBonus(SkillTab.SORC_LIGHTNING_SPELLS) != null ||
                                item.getIndividualSkillBonusWithoutTabOrCharacterClassSkillAffixes(D2Skills.LIGHTNING.get()) > 0 ||
                                item.getIndividualSkillBonusWithoutTabOrCharacterClassSkillAffixes(D2Skills.CHAIN_LIGHTNING.get()) > 0 ||
                                item.getIndividualSkillBonusWithoutTabOrCharacterClassSkillAffixes(D2Skills.LIGHTNING_MASTERY.get()) > 0)
                .withScoringFunction(item -> {
                    double result = item.getCasterValueForRareArmorOrJewelry();
                    result += Optional.ofNullable(item.getSkillTabBonus(SkillTab.SORC_LIGHTNING_SPELLS)).map(SkillBonuses.SkillTabBonus::getSkillLevelBonus).orElse(0) * 90;
                    result += 30 * item.getIndividualSkillBonusWithoutTabOrCharacterClassSkillAffixes(D2Skills.LIGHTNING.get());
                    result += 25 * item.getIndividualSkillBonusWithoutTabOrCharacterClassSkillAffixes(D2Skills.CHAIN_LIGHTNING.get());
                    result += 30 * item.getIndividualSkillBonusWithoutTabOrCharacterClassSkillAffixes(D2Skills.LIGHTNING_MASTERY.get());
                    return (int) result;
                })
                .withCountOfTopScoringItemsToKeepInEachCategory(10)
                .build());

        allConsumers.add(CategorizedTopN.withId("COLD_SORC_ORBS")
                .addItemTypeTypeCodes("orb")
                .allowItemQualities(ItemQuality.RARE, ItemQuality.MAGIC, ItemQuality.NORMAL)
                .withCategorizer(item -> item.getQuality().name())
                .withAdditionalItemCriteria(item ->
                        item.getSkillTabBonus(SkillTab.SORC_COLD_SPELLS) != null ||
                                item.getIndividualSkillBonusWithoutTabOrCharacterClassSkillAffixes(D2Skills.BLIZZARD.get()) > 0 ||
                                item.getIndividualSkillBonusWithoutTabOrCharacterClassSkillAffixes(D2Skills.FROZEN_ORB.get()) > 0 ||
                                item.getIndividualSkillBonusWithoutTabOrCharacterClassSkillAffixes(D2Skills.COLD_MASTERY.get()) > 0)
                .withScoringFunction(item -> {
                    double result = item.getCasterValueForRareArmorOrJewelry();
                    result += Optional.ofNullable(item.getSkillTabBonus(SkillTab.SORC_COLD_SPELLS)).map(SkillBonuses.SkillTabBonus::getSkillLevelBonus).orElse(0) * 90;
                    result += 50 * item.getIndividualSkillBonusWithoutTabOrCharacterClassSkillAffixes(D2Skills.BLIZZARD.get());
                    result += 10 * item.getIndividualSkillBonusWithoutTabOrCharacterClassSkillAffixes(D2Skills.FROZEN_ORB.get());
                    result += 25 * item.getIndividualSkillBonusWithoutTabOrCharacterClassSkillAffixes(D2Skills.COLD_MASTERY.get());
                    return (int) result;
                })
                .withCountOfTopScoringItemsToKeepInEachCategory(10)
                .build());

        allConsumers.add(CategorizedTopN.withId("HIGH_DEFENSE_ARMOR")
                .addItemTypeTypeCodes("tors")
                .allowItemQualities(ItemQuality.NORMAL, ItemQuality.SUPERIOR)
                .withCategorizer(item -> item.isEthereal() ? "ethereal" : "non-ethereal")
                .withScoringFunction(item -> item.getDefense())
                .withCountOfTopScoringItemsToKeepInEachCategory(10)
                .build());

        allConsumers.add(new StaffmodTracker("STAFFMOD_TRACKER"));

        allConsumers.add(TopNConsumer.withId("RARE_MELEE_JEWELS")
                .addItemTypeTypeCodes("jewl")
                .withAdditionalItemCriteria(item -> item.getStat(D2ItemStats.MAXDAMAGE_PERCENT.statId) > 20)
                .allowItemQualities(ItemQuality.RARE)
                .withScoringFunction(item -> item.getStat(D2ItemStats.MAXDAMAGE_PERCENT.statId)
                        + item.getStat(D2ItemStats.FIRE_RESIST.statId)/3 + item.getStat(D2ItemStats.LIGHTNING_RESIST.statId)/3 + item.getStat(D2ItemStats.COLD_RESIST.statId)/3
                        + item.getStat(D2ItemStats.STRENGTH.statId) + item.getStat(D2ItemStats.DEXTERITY.statId) + item.getStat(D2ItemStats.ATTACK_RATING.statId)/5)
                .withCountOfTopScoringItemsToKeepInEachCategory(10)
                .build());

        allConsumers.add(CategorizedTopN.withId("WIND_DRUID_PELTS")
                .addItemTypeTypeCodes("pelt")
                .allowItemQualities(ItemQuality.RARE, ItemQuality.MAGIC, ItemQuality.NORMAL)
                .withCategorizer(item -> item.getQuality().name())
                .withAdditionalItemCriteria(item -> item.getTotalBonusIncludingSkillTabAndClassSkillBonuses(D2Skills.TORNADO.get()) >= 4)
                .withScoringFunction(item -> {
                    double result = item.getCasterValueForRareArmorOrJewelry();
                    result += Optional.ofNullable(item.getSkillTabBonus(SkillTab.DRUID_ELEMENTAL)).map(SkillBonuses.SkillTabBonus::getSkillLevelBonus).orElse(0) * 90;
                    result += 75 * item.getIndividualSkillBonusWithoutTabOrCharacterClassSkillAffixes(D2Skills.TORNADO.get());
                    result += 10 * item.getIndividualSkillBonusWithoutTabOrCharacterClassSkillAffixes(D2Skills.HURRICANE.get());
                    result += 5 * item.getIndividualSkillBonusWithoutTabOrCharacterClassSkillAffixes(D2Skills.CYCLONE_ARMOR.get());
                    if (item.isEthereal()) result -= 100;
                    return (int) result;
                })
                .withCountOfTopScoringItemsToKeepInEachCategory(10)
                .build());

        allConsumers.add(TopNConsumer.withId("ILLEGAL_BARB_STAFFMODS")
                .addItemTypeTypeCodes("phlm")
                .allowItemQualities(ItemQuality.INFERIOR, ItemQuality.NORMAL, ItemQuality.SUPERIOR, ItemQuality.MAGIC, ItemQuality.RARE)
                .withAdditionalItemCriteria(item -> !item.getIllegalStaffmods().isEmpty())
                .withScoringFunction(item -> item.getIllegalStaffmods().stream().mapToInt(SkillBonuses.IndividualSkillBonus::getSkillLevelBonus).sum())
                .withCountOfTopScoringItemsToKeepInEachCategory(10)
                .includeScoreDistribution(true)
                .build());

        Predicate<D2Item> weaponFilter_1Handed_Ethereal = item -> item.isOneHandableByBarbarian() && item.isEthereal();
        Predicate<D2Item> weaponFilter_2Handed_Ethereal = item -> item.getItemType().getWeaponInfo().isTwoHanded() && item.isEthereal();
        Predicate<D2Item> weaponFilter_1Handed_CapableOfLongLasting = item -> item.isOneHandableByBarbarian() && item.getWeaponInfoForDamageCalc().isAlreadyLongLastingOrCanBeFixedWithSocketingAndZod();
        Predicate<D2Item> weaponFilter_2Handed_CapableOfLongLasting  = item -> item.getItemType().getWeaponInfo().isTwoHanded() && item.getWeaponInfoForDamageCalc().isAlreadyLongLastingOrCanBeFixedWithSocketingAndZod();

        // Creates a CategorizedTopN consumer for each of the 48 combinations of ScoringOption, EtherealOption,
        // HandednessOption, and UpgradeOption
        for (RWScoringOption scoringOption : RWScoringOption.values()) {
            for (RWEtherealOption etherealOption : RWEtherealOption.values()) {
                for (RWHandednessOption handednessOption : RWHandednessOption.values()) {
                    for (RWUpgradeOption upgradeOption : RWUpgradeOption.values()) {

                        final Function<DamageOption,Integer> scoringFunction = switch(scoringOption) {
                            case MAX_DMG -> (x -> x.getMax());
                            case AVG_DMG -> (x -> x.getAverage());
                            case DPS -> (x -> x.getDps());
                        };

                        final Predicate<D2Item> weaponFilter;
                        if (etherealOption == RWEtherealOption.ETHEREAL) {
                            if (handednessOption == RWHandednessOption.ONE_HANDED) {
                                weaponFilter = weaponFilter_1Handed_Ethereal;
                            } else {
                                weaponFilter = weaponFilter_2Handed_Ethereal;
                            }
                        } else { // CAN_BE_MADE_LONG_LASTING
                            if (upgradeOption == RWUpgradeOption.NO_UPGRADE) {
                                // if no upgrades are allowed, then "can be made long-lasting" becomes "is already long-lasting"
                                if (handednessOption == RWHandednessOption.ONE_HANDED) {
                                    weaponFilter = item -> (item.getWeaponInfoForDamageCalc().isAlreadyLongLasting() && item.isOneHandableByBarbarian());
                                } else {
                                    weaponFilter = item -> (item.getWeaponInfoForDamageCalc().isAlreadyLongLasting() && item.getItemType().getWeaponInfo().isTwoHanded());
                                }
                            } else {
                                if (handednessOption == RWHandednessOption.ONE_HANDED) {
                                    weaponFilter = weaponFilter_1Handed_CapableOfLongLasting;
                                } else {
                                    weaponFilter = weaponFilter_2Handed_CapableOfLongLasting;
                                }
                            }

                        }

                        final Function<D2Item,DamageOption> damageOption;
                        if (etherealOption == RWEtherealOption.CAN_BE_MADE_LONG_LASTING) {
                            if (handednessOption == RWHandednessOption.TWO_HANDED) {
                                if (upgradeOption == RWUpgradeOption.NO_UPGRADE)
                                    damageOption = item -> item.getOriginalDmg();
                                else if (upgradeOption == RWUpgradeOption.ELITE_SOCKETS_ZOD_4015)
                                    damageOption = item -> item.getUpSocketZod4015();
                                else if (upgradeOption == RWUpgradeOption.ELITE_SOCKETS_ZOD_OHM)
                                    damageOption = item -> item.getUpSocketZodOhm();
                                else // ELITE_SOCKETS_ZOD
                                    damageOption = item -> item.getUpSocketZod();
                            } else { // 1 handed
                                if (upgradeOption == RWUpgradeOption.NO_UPGRADE)
                                    damageOption = item -> item.getOriginalDmg_1h() == null ? item.getOriginalDmg() : item.getOriginalDmg_1h();
                                else if (upgradeOption == RWUpgradeOption.ELITE_SOCKETS_ZOD_4015)
                                    damageOption = item -> item.getUpSocketZod4015_1h() == null ? item.getUpSocketZod4015() : item.getUpSocketZod4015_1h();
                                else if (upgradeOption == RWUpgradeOption.ELITE_SOCKETS_ZOD_OHM)
                                    damageOption = item -> item.getUpSocketZodOhm_1h() == null ? item.getUpSocketZodOhm() : item.getUpSocketZodOhm_1h();
                                else // ELITE_SOCKETS_ZOD
                                    damageOption = item -> item.getUpSocketZod_1h() == null ? item.getUpSocketZod() : item.getUpSocketZod_1h();
                            }
                        } else { // ETHEREAL
                            if (handednessOption == RWHandednessOption.TWO_HANDED) {
                                if (upgradeOption == RWUpgradeOption.NO_UPGRADE)
                                    damageOption = item -> item.getOriginalDmg();
                                else if (upgradeOption == RWUpgradeOption.ELITE_SOCKETS_ZOD_4015)
                                    damageOption = item -> item.getUpSocket4015_eth();
                                else if (upgradeOption == RWUpgradeOption.ELITE_SOCKETS_ZOD_OHM)
                                    damageOption = item -> item.getUpSocketOhm_eth();
                                else // ELITE_SOCKETS_ZOD
                                    damageOption = item -> item.getUpSocket_eth();
                            } else { // 1 handed
                                if (upgradeOption == RWUpgradeOption.NO_UPGRADE)
                                    damageOption = item -> item.getOriginalDmg_1h() == null ? item.getOriginalDmg() : item.getOriginalDmg_1h();
                                else if (upgradeOption == RWUpgradeOption.ELITE_SOCKETS_ZOD_4015)
                                    damageOption = item -> item.getUpSocket4015_eth_1h() == null ? item.getUpSocket4015_eth() : item.getUpSocket4015_eth_1h();
                                else if (upgradeOption == RWUpgradeOption.ELITE_SOCKETS_ZOD_OHM)
                                    damageOption = item -> item.getUpSocketOhm_eth_1h() == null ? item.getUpSocketOhm_eth() : item.getUpSocketOhm_eth_1h();
                                else // ELITE_SOCKETS_ZOD
                                    damageOption = item -> item.getUpSocket_eth_1h() == null ? item.getUpSocket_eth() : item.getUpSocket_eth_1h();
                            }
                        }

                        allConsumers.add(CategorizedTopN.withId("RARE_WEAPONS|" + scoringOption.name() + "|" +
                                        upgradeOption.name() + "|" + handednessOption.name() + "|" + etherealOption.name())
                                .addItemTypeTypeCodes("weap")
                                .excludeItemTypeTypeCodes("staf","wand","orb")
                                .allowItemQualities(ItemQuality.RARE)
                                .withAdditionalItemCriteria(weaponFilter)
                                .withCategorizer(item -> item.getItemTypeType().getCode())
                                .withScoringFunction(item ->  scoringFunction.apply(damageOption.apply(item)))
                                .withCountOfTopScoringItemsToKeepInEachCategory(5)
                                .includeScoreDistribution(true)
                                .build());
                    }
                }
            }

        }

        allConsumers.add(CategorizedTopN.withId("FOOLS_WEAPON|2_HANDED")
                .addItemTypeTypeCodes("weap")
                .excludeItemTypeTypeCodes("staf","wand","orb")
                .allowItemQualities(ItemQuality.RARE)
                .withAdditionalItemCriteria(item ->
                        item.getItemType().getWeaponInfo().isTwoHanded() &&
                        item.getStat(D2ItemStats.ATTACK_RATING_PER_LEVEL.statId) == 33 &&
                        item.getWeaponInfoForDamageCalc().isAlreadyLongLastingOrCanBeFixedWithSocketingAndZod())
                .withCategorizer(item -> item.getItemTypeType().getCode())
                .withScoringFunction(item -> item.getUpSocketZod4015().getDps())
                .withCountOfTopScoringItemsToKeepInEachCategory(5)
                .build());

        allConsumers.add(CategorizedTopN.withId("FOOLS_WEAPON|1_HANDED")
                .addItemTypeTypeCodes("weap")
                .excludeItemTypeTypeCodes("staf","wand","orb")
                .allowItemQualities(ItemQuality.RARE)
                .withAdditionalItemCriteria(item ->
                        item.isOneHandableByBarbarian() &&
                        item.getStat(D2ItemStats.ATTACK_RATING_PER_LEVEL.statId) == 33 &&
                        item.getWeaponInfoForDamageCalc().isAlreadyLongLastingOrCanBeFixedWithSocketingAndZod())
                .withCategorizer(item -> item.getItemTypeType().getCode())
                .withScoringFunction(item -> (item.getUpSocketZod4015_1h() == null ? item.getUpSocketZod4015() : item.getUpSocketZod4015_1h()).getDps())
                .withCountOfTopScoringItemsToKeepInEachCategory(5)
                .build());

        return allConsumers;
    }
}

enum RWScoringOption {
    MAX_DMG,
    AVG_DMG,
    DPS;
}

/** For the options including _ZOD , only put zod rune in the weapon if RWEtherealOption is CAN_BE_MADE_LONG_LASTING
 * and zod is required in order for that item to be long-lasting
 *
 * ELITE means "upgrade the item to elite if possible" (ie. how it would be done through the crafting recipe)
 * SOCKETS means "give the item a socket if it is socketable and doesn't already have sockets" (ie. how it would be done with Larzuk quest reward)
 * 4015 means "put 40%ed/15ias jewels in the remaining sockets"
 * OHM means "put ohm runes (50% ed) in the remaining sockets"
 */
enum RWUpgradeOption {
    NO_UPGRADE,
    ELITE_SOCKETS_ZOD_4015,
    ELITE_SOCKETS_ZOD_OHM,
    ELITE_SOCKETS_ZOD;
}

enum RWHandednessOption {
    ONE_HANDED,
    TWO_HANDED;
}

enum RWEtherealOption {
    /** only consider Ethereal items, and don't bother putting Zod runes in them, let them remain short-lasting */
    ETHEREAL,

    /**
     * Consider any item that is either already long-lasting or could be made long-lasting by socketing it and putting
     * a zod rune in it. Exception: if RWUpgradeOption is NO_UPGRADE, then socketing is not allowed and this becomes
     * a filter checking whether the item is already long-lasting
     */
    CAN_BE_MADE_LONG_LASTING;
}