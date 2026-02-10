package org.sully.d2.itemtracking;

import org.sully.d2.gamemodel.D2Item;
import org.sully.d2.gamemodel.derivedstats.AttackingContext;
import org.sully.d2.gamemodel.derivedstats.SkillBonuses;
import org.sully.d2.gamemodel.derivedstats.WeaponInfoForDamageCalc;
import org.sully.d2.gamemodel.enums.CharacterClass;
import org.sully.d2.gamemodel.enums.ItemQuality;
import org.sully.d2.gamemodel.enums.SkillTab;
import org.sully.d2.gamemodel.staticgamedata.D2ItemStats;
import org.sully.d2.gamemodel.staticgamedata.D2Skills;

import java.util.*;
import java.util.stream.Collectors;

public class HardcodedTCDropConsumerConfiguration {

    public Map<DropContextEnum, List<D2TCDropConsumer>> initializeConsumers(Set<DropContextEnum> dropContexts) {
        Map<DropContextEnum,List<D2TCDropConsumer>> consumersByDropContext = new HashMap<>();
        for (DropContextEnum dropContext : dropContexts) {
            consumersByDropContext.put(dropContext, initializeConsumersForSingleDropContext(dropContext));
        }
        return consumersByDropContext;
    }

    private List<D2TCDropConsumer> initializeConsumersForSingleDropContext(DropContextEnum dropContext) {

        List<D2TCDropConsumer> allConsumers = new ArrayList<>();

        allConsumers.add( new ItemGrid(
                "Item Counts by Item Type and Quality",
                item -> item.getItemType().getName(),
                item -> item.getQuality().name(),
                item -> true,
                itemName -> itemName,
                quality -> quality,
                Comparator.naturalOrder(),
                Comparator.naturalOrder()));

        allConsumers.add(new ItemGrid(
                "Counts of Set and Unique Items by Name",
                D2Item::getName,
                item -> item.isEthereal() ? "Ethereal" : "Non-Ethereal",
                item -> (item.getQuality() == ItemQuality.SET || item.getQuality() == ItemQuality.UNIQUE),
                name -> name,
                eth -> eth,
                Comparator.naturalOrder(),
                Comparator.naturalOrder()));

        allConsumers.add(new BasicStatsConsumer("Basic Stats"));

        allConsumers.add(new AssortedMagicItemsConsumer("Magic Items"));


        allConsumers.add(CategorizedTopN.named("Tri-Res Boots")
                .addItemTypeTypeCodes("boot")
                .allowItemQualities(ItemQuality.RARE)
                .withAdditionalItemCriteria(item ->
                        item.hasStat(D2ItemStats.FIRE_RESIST.statId) &&
                        item.hasStat(D2ItemStats.LIGHTNING_RESIST.statId) &&
                        item.hasStat(D2ItemStats.COLD_RESIST.statId))
                .withCategorizer(item -> "All")
                .withScoringFunction(item ->
                                item.getStat(D2ItemStats.FIRE_RESIST.statId) +
                                item.getStat(D2ItemStats.LIGHTNING_RESIST.statId) +
                                item.getStat(D2ItemStats.COLD_RESIST.statId))
                .withCountOfTopScoringItemsToKeepInEachCategory(1)
                .build());

        allConsumers.add(CategorizedTopN.named("Rare Caster Boots")
                .addItemTypeTypeCodes("boot")
                .allowItemQualities(ItemQuality.RARE)
                .withCategorizer(item -> "All")
                .withScoringFunction(D2Item::getCasterValueForRareArmorOrJewelry)
                .withCountOfTopScoringItemsToKeepInEachCategory(1)
                .build());

        allConsumers.add(CategorizedTopN.named("Rare Caster Rings")
                .addItemTypeTypeCodes("ring")
                .allowItemQualities(ItemQuality.RARE)
                .withCategorizer(item -> "All")
                .withScoringFunction(D2Item::getCasterValueForRareArmorOrJewelry)
                .withCountOfTopScoringItemsToKeepInEachCategory(1)
                .build());

        allConsumers.add(CategorizedTopN.named("Rare Caster Amulets")
                .addItemTypeTypeCodes("amul")
                .allowItemQualities(ItemQuality.RARE)
                .withCategorizer(item -> "All")
                .withScoringFunction(D2Item::getCasterValueForRareArmorOrJewelry)
                .withCountOfTopScoringItemsToKeepInEachCategory(1)
                .build());

        allConsumers.add(CategorizedTopN.named("Rare Caster Circlets")
                .addItemTypeTypeCodes("circ")
                .allowItemQualities(ItemQuality.RARE)
                .withCategorizer(item -> "All")
                .withScoringFunction(D2Item::getCasterValueForRareArmorOrJewelry)
                .withCountOfTopScoringItemsToKeepInEachCategory(1)
                .build());

        allConsumers.add(CategorizedTopN.named("Fire Sorc Orbs")
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
                .withCountOfTopScoringItemsToKeepInEachCategory(1)
                .build());

        allConsumers.add(CategorizedTopN.named("Lightning Sorc Orbs")
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
                .withCountOfTopScoringItemsToKeepInEachCategory(1)
                .build());

        allConsumers.add(CategorizedTopN.named("Cold Sorc Orbs")
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
                .withCountOfTopScoringItemsToKeepInEachCategory(1)
                .build());





        allConsumers.add(CategorizedTopN.named("High Defense Armor")
                .addItemTypeTypeCodes("tors")
                .allowItemQualities(ItemQuality.NORMAL, ItemQuality.SUPERIOR)
                .withCategorizer(item -> item.isEthereal() ? "ethereal" : "non-ethereal")
                .withScoringFunction(item -> item.getDefense())
                .withCountOfTopScoringItemsToKeepInEachCategory(1)
                .build());


        /*



        allConsumers.add(CategorizedTopN.named("Rare Melee Jewels")
                .addItemTypeTypeCodes("jewl")
                .withAdditionalItemCriteria(item -> item.getStat(D2ItemStats.MAXDAMAGE_PERCENT.statId) > 20)
                .allowItemQualities(ItemQuality.RARE)
                .withCategorizer(item -> "Any")
                .withScoringFunction(item -> item.getStat(D2ItemStats.MAXDAMAGE_PERCENT.statId)
                        + item.getStat(D2ItemStats.FIRE_RESIST.statId)/3 + item.getStat(D2ItemStats.LIGHTNING_RESIST.statId)/3 + item.getStat(D2ItemStats.COLD_RESIST.statId)/3
                        + item.getStat(D2ItemStats.STRENGTH.statId) + item.getStat(D2ItemStats.DEXTERITY.statId) + item.getStat(D2ItemStats.ATTACK_RATING.statId)/5)
                .withCountOfTopScoringItemsToKeepInEachCategory(20)
                .build());


        allConsumers.add(CategorizedTopN.named("Illegal Barb Staffmods")
                .addItemTypeTypeCodes("phlm")
                .allowItemQualities(ItemQuality.INFERIOR, ItemQuality.NORMAL, ItemQuality.SUPERIOR, ItemQuality.MAGIC, ItemQuality.RARE)
                .withAdditionalItemCriteria(item -> !item.getIllegalStaffmods().isEmpty())
                .withCategorizer(item -> item.getIllegalStaffmods().stream().map(s -> s.getSkill().getName() + "(" + s.getSkillLevelBonus() + ")").sorted().collect(Collectors.joining(" ")))
                .withScoringFunction(item -> item.getQuality().id)
                .withCountOfTopScoringItemsToKeepInEachCategory(5)
                .build());

        allConsumers.add(CategorizedTopN.named("Wind Druid Pelts")
                .addItemTypeTypeCodes("pelt")
                .allowItemQualities(ItemQuality.MAGIC, ItemQuality.RARE)
                .withAdditionalItemCriteria(item -> item.getTotalBonusIncludingSkillTabAndClassSkillBonuses(D2Skills.TORNADO.get()) >= 4)
                .withCategorizer(item -> item.getQuality().name())
                .withScoringFunction(item -> item.getTotalBonusIncludingSkillTabAndClassSkillBonuses(D2Skills.TORNADO.get()))
                .withCountOfTopScoringItemsToKeepInEachCategory(5)
                .build());

        final AttackingContext barbTwoHandedSword = new AttackingContext(CharacterClass.BARBARIAN, 60, 2);
        allConsumers.add(CategorizedTopN.named("Rare Weapons|1 or 2 handed : Highest DPS")
                .addItemTypeTypeCodes("weap")
                .excludeItemTypeTypeCodes("staf","wand","orb")
                .allowItemQualities(ItemQuality.RARE)
                .withAdditionalItemCriteria(item -> item.getWeaponInfoForDamageCalc().isAlreadyLongLastingOrCanBeFixedWithAZodRune())
                .withCategorizer(item -> {
                    WeaponInfoForDamageCalc weapon = item.getWeaponInfoForDamageCalc()
                            .upgradeRareOrUniqueToEliteAndAddSocketIfSocketableAndNotAlreadySocketed()
                            .addZodRuneIfItemIsNotAlreadyLongLasting();
                    int remainingSocketsForJewels = weapon.getTotalSockets() - weapon.getFilledSockets();
                    // weapon = weapon.add_40ED_15IAS_JewelsToRemainingSockets();
                    return item.getItemTypeType().getCode();
                })
                .withScoringFunction(item -> {
                    WeaponInfoForDamageCalc weapon = item.getWeaponInfoForDamageCalc()
                            .upgradeRareOrUniqueToEliteAndAddSocketIfSocketableAndNotAlreadySocketed()
                            .addZodRuneIfItemIsNotAlreadyLongLasting()
                            .add_40ED_15IAS_JewelsToRemainingSockets();
                    return weapon.getDamage(barbTwoHandedSword).getDps();
                })
                .withCountOfTopScoringItemsToKeepInEachCategory(10)
                .build());

        final AttackingContext barbOneHandedSword = new AttackingContext(CharacterClass.BARBARIAN, 60, 1);
        allConsumers.add(CategorizedTopN.named("Rare Weapons|1 handed : Highest DPS")
                .addItemTypeTypeCodes("weap")
                .excludeItemTypeTypeCodes("staf","wand","orb")
                .allowItemQualities(ItemQuality.RARE)
                .withAdditionalItemCriteria(item -> item.getItemType().getWeaponInfo().isOneHandableByBarbarian() &&
                        item.getWeaponInfoForDamageCalc().isAlreadyLongLastingOrCanBeFixedWithAZodRune())
                .withCategorizer(item -> {
                    WeaponInfoForDamageCalc weapon = item.getWeaponInfoForDamageCalc()
                            .upgradeRareOrUniqueToEliteAndAddSocketIfSocketableAndNotAlreadySocketed()
                            .addZodRuneIfItemIsNotAlreadyLongLasting();
                    int remainingSocketsForJewels = weapon.getTotalSockets() - weapon.getFilledSockets();
                    // weapon = weapon.add_40ED_15IAS_JewelsToRemainingSockets();
                    return item.getItemTypeType().getCode() + "|" + remainingSocketsForJewels  + "|" + (item.isEthereal() ? "eth" : "non");
                })
                .withScoringFunction(item -> {
                    WeaponInfoForDamageCalc weapon = item.getWeaponInfoForDamageCalc()
                            .upgradeRareOrUniqueToEliteAndAddSocketIfSocketableAndNotAlreadySocketed()
                            .addZodRuneIfItemIsNotAlreadyLongLasting()
                            .add_40ED_15IAS_JewelsToRemainingSockets();
                    return weapon.getDamage(barbOneHandedSword).getDps();
                })
                .withCountOfTopScoringItemsToKeepInEachCategory(10)
                .build());


         */


        return allConsumers;
    }
}
