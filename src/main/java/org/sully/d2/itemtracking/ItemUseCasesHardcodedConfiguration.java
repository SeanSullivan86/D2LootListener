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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ItemUseCasesHardcodedConfiguration {

    public static ItemUseCaseTracker createTrackerWithHardcodedItemUseCases() {
        Set<Integer> threeSocketCircletStats = D2ItemStats.getStatIds(D2ItemStats.FASTER_RUN_WALK_SPEED,
                D2ItemStats.DEXTERITY, D2ItemStats.LIFE, D2ItemStats.DAMAGE_REDUCTION, D2ItemStats.FASTER_CAST_RATE, D2ItemStats.STRENGTH);
        ItemUseCase threeSocketMagicCirclets = ItemUseCase.named("Three Socket Magic Circlets")
                .addItemTypeTypeCodes("circ")
                .allowItemQualities(ItemQuality.MAGIC)
                .withAdditionalItemCriteria(item -> item.getSockets() >= 3 && item.hasAtLeastOneOfTheseStats(threeSocketCircletStats))
                .withCategorizer(D2Item::getName)
                .withScoringFunction(item -> 1)
                .withCountOfTopScoringItemsToKeepInEachCategory(2)
                .build();

        ItemUseCase triResBoots = ItemUseCase.named("Tri-Res Boots")
                .addItemTypeTypeCodes("boot")
                .allowItemQualities(ItemQuality.RARE)
                .withAdditionalItemCriteria(item ->
                    ((item.hasStat(D2ItemStats.FIRE_RESIST.statId) ? 1 : 0) +
                    (item.hasStat(D2ItemStats.LIGHTNING_RESIST.statId) ? 1 : 0) +
                    (item.hasStat(D2ItemStats.COLD_RESIST.statId) ? 1 : 0)  /* +
                    (item.hasStat(D2ItemStats.POISON_RESIST.statId) ? 1 : 0) */) >= 3)
                .withCategorizer(item -> "All")
                .withScoringFunction(item ->
                        item.getStat(D2ItemStats.MAGIC_FIND.statId) +
                                item.getStat(D2ItemStats.FASTER_RUN_WALK_SPEED.statId) +
                        item.getStat(D2ItemStats.FIRE_RESIST.statId) +
                    item.getStat(D2ItemStats.LIGHTNING_RESIST.statId) +
                    item.getStat(D2ItemStats.COLD_RESIST.statId) /* +
                    item.getStat(D2ItemStats.POISON_RESIST.statId) */)
                .withCountOfTopScoringItemsToKeepInEachCategory(20)
                .build();


        ItemUseCase fourSocketArmors = ItemUseCase.named("Four Socket Armors")
                .addItemTypeTypeCodes("tors")
                .withAdditionalItemCriteria(item -> item.getSockets() == 4 && item.getStat(D2ItemStats.LIFE.statId) > 80*256)
                .allowItemQualities(ItemQuality.MAGIC)
                .withCategorizer(D2Item::getName)
                .withScoringFunction(item -> item.getStat(7)/256)
                .withCountOfTopScoringItemsToKeepInEachCategory(2)
                .build();

        ItemUseCase skillCharmsOfVita = ItemUseCase.named("Skill Grand Charms of Vita")
                .addItemTypeTypeCodes("lcha")
                .withAdditionalItemCriteria(item -> item.hasAtLeastOneSkillTabBonus() && item.getStat(7)/256 >= 36)
                .allowItemQualities(ItemQuality.MAGIC)
                .withCategorizer(D2Item::getName)
                .withScoringFunction(item -> item.getStat(7)/256)
                .withCountOfTopScoringItemsToKeepInEachCategory(2)
                .build();

        ItemUseCase iasJewels = ItemUseCase.named("IAS Jewels")
                .addItemTypeTypeCodes("jewl")
                .withAdditionalItemCriteria(item -> item.hasStat(D2ItemStats.INCREASED_ATTACK_SPEED.statId) &&
                        ((item.getStat(D2ItemStats.MAXDAMAGE_PERCENT.statId) > 30) ||
                                (item.hasStat(D2ItemStats.FIRE_RESIST.statId) && item.hasStat(D2ItemStats.LIGHTNING_RESIST.statId))))
                .allowItemQualities(ItemQuality.MAGIC)
                .withCategorizer(D2Item::getName)
                .withScoringFunction(item -> item.getStat(D2ItemStats.MAXDAMAGE_PERCENT.statId) + item.getStat(D2ItemStats.FIRE_RESIST.statId))
                .withCountOfTopScoringItemsToKeepInEachCategory(5)
                .build();

        ItemUseCase rareMeleeJewels = ItemUseCase.named("Rare Melee Jewels")
                .addItemTypeTypeCodes("jewl")
                .withAdditionalItemCriteria(item -> item.getStat(D2ItemStats.MAXDAMAGE_PERCENT.statId) > 20)
                .allowItemQualities(ItemQuality.RARE)
                .withCategorizer(item -> "Any")
                .withScoringFunction(item -> item.getStat(D2ItemStats.MAXDAMAGE_PERCENT.statId)
                        + item.getStat(D2ItemStats.FIRE_RESIST.statId)/3 + item.getStat(D2ItemStats.LIGHTNING_RESIST.statId)/3 + item.getStat(D2ItemStats.COLD_RESIST.statId)/3
                        + item.getStat(D2ItemStats.STRENGTH.statId) + item.getStat(D2ItemStats.DEXTERITY.statId) + item.getStat(D2ItemStats.ATTACK_RATING.statId)/5)
                .withCountOfTopScoringItemsToKeepInEachCategory(20)
                .build();

        ItemUseCase superiorItems = ItemUseCase.named("Superior Items")
                .addItemTypeTypeCodes("weap","armo")
                .allowItemQualities(ItemQuality.SUPERIOR)
                .withCategorizer(item -> item.getName() + "|" + (item.isEthereal() ? "eth" : "non") + "|" + item.getSockets())
                .withScoringFunction(item -> item.getStat(D2ItemStats.MAXDAMAGE_PERCENT.statId) + item.getStat(D2ItemStats.ENHANCED_DEFENSE_PERCENT.statId))
                .withCountOfTopScoringItemsToKeepInEachCategory(1)
                .build();

        ItemUseCase illegalBarbStaffmods = ItemUseCase.named("Illegal Barb Staffmods")
                .addItemTypeTypeCodes("phlm")
                .allowItemQualities(ItemQuality.INFERIOR, ItemQuality.NORMAL, ItemQuality.SUPERIOR, ItemQuality.MAGIC, ItemQuality.RARE)
                .withAdditionalItemCriteria(item -> !item.getIllegalStaffmods().isEmpty())
                .withCategorizer(item -> item.getIllegalStaffmods().stream().map(s -> s.getSkill().getName() + "(" + s.getSkillLevelBonus() + ")").sorted().collect(Collectors.joining(" ")))
                .withScoringFunction(item -> item.getQuality().id)
                .withCountOfTopScoringItemsToKeepInEachCategory(5)
                .build();

        ItemUseCase windDruidPelts = ItemUseCase.named("Wind Druid Pelts")
                .addItemTypeTypeCodes("pelt")
                .allowItemQualities(ItemQuality.MAGIC, ItemQuality.RARE)
                .withAdditionalItemCriteria(item -> item.getTotalBonusIncludingSkillTabAndClassSkillBonuses(D2Skills.TORNADO.get()) >= 4)
                .withCategorizer(item -> item.getQuality().name())
                .withScoringFunction(item -> item.getTotalBonusIncludingSkillTabAndClassSkillBonuses(D2Skills.TORNADO.get()))
                .withCountOfTopScoringItemsToKeepInEachCategory(5)
                .build();

        final AttackingContext barbTwoHandedSword = new AttackingContext(CharacterClass.BARBARIAN, 60, 2);
        ItemUseCase rareWeaponHighDps = ItemUseCase.named("Rare Weapons|1 or 2 handed : Highest DPS")
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
                    return item.getItemTypeType().getCode() /* + "|" + remainingSocketsForJewels  + "|" + (item.isEthereal() ? "eth" : "non") */;
                })
                .withScoringFunction(item -> {
                    WeaponInfoForDamageCalc weapon = item.getWeaponInfoForDamageCalc()
                            .upgradeRareOrUniqueToEliteAndAddSocketIfSocketableAndNotAlreadySocketed()
                            .addZodRuneIfItemIsNotAlreadyLongLasting()
                            .add_40ED_15IAS_JewelsToRemainingSockets();
                    return weapon.getDamage(barbTwoHandedSword).getDps();
                })
                .withCountOfTopScoringItemsToKeepInEachCategory(10)
                .build();

        final AttackingContext barbOneHandedSword = new AttackingContext(CharacterClass.BARBARIAN, 60, 1);
        ItemUseCase oneHandedRareWeaponByDPS = ItemUseCase.named("Rare Weapons|1 handed : Highest DPS")
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
                .build();

        ItemUseCase magicJavelins = ItemUseCase.named("Magic Skill/IAS Javelins")
                .addItemTypeTypeCodes("ajav")
                .allowItemQualities(ItemQuality.MAGIC)
                .withAdditionalItemCriteria(item -> {
                    SkillBonuses.SkillTabBonus s = item.getSkillTabBonus(SkillTab.AMAZON_JAVELIN_AND_SPEAR_SKILLS);
                    return s != null && s.getTotalBonusIncludingCharacterClassSkillBonuses() >= 2 && item.hasStat(D2ItemStats.INCREASED_ATTACK_SPEED.statId);
                })
                .withCategorizer(item -> item.getStat(D2ItemStats.INCREASED_ATTACK_SPEED.statId) + " IAS")
                .withScoringFunction(item -> item.getSkillTabBonus(SkillTab.AMAZON_JAVELIN_AND_SPEAR_SKILLS).getTotalBonusIncludingCharacterClassSkillBonuses())
                .withCountOfTopScoringItemsToKeepInEachCategory(5)
                .build();



        ItemUseCaseTracker useCaseTracker = new ItemUseCaseTracker(List.of(
                fourSocketArmors,
                threeSocketMagicCirclets,
                skillCharmsOfVita,
                triResBoots,
                iasJewels,
                rareMeleeJewels,
                superiorItems,
                illegalBarbStaffmods,
                rareWeaponHighDps,
                magicJavelins,
                oneHandedRareWeaponByDPS,
                windDruidPelts));
        return useCaseTracker;
    }

}
