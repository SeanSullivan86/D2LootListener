package org.sully.d2.gamemodel.derivedstats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.sully.d2.gamemodel.enums.CharacterClass;
import org.sully.d2.gamemodel.enums.ItemQuality;
import org.sully.d2.gamemodel.StatList;
import org.sully.d2.gamemodel.StatValue;
import org.sully.d2.gamemodel.staticgamedata.D2ItemType;

import java.util.List;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
public class WeaponInfoForDamageCalc {
    D2ItemType itemType;
    ItemQuality itemQuality;
    StatList magicStats;
    boolean ethereal;
    int totalSockets;
    int filledSockets;
    StatList extraStatsFromFilledSockets;
    boolean hasZodInSocket;

    public WeaponInfoForDamageCalc(D2ItemType itemType, ItemQuality itemQuality, StatList magicStats, boolean ethereal, int totalSockets) {
        this.itemType = itemType;
        this.itemQuality = itemQuality;
        this.magicStats = magicStats;
        this.ethereal = ethereal;
        this.totalSockets = totalSockets;
        this.filledSockets = 0;
        this.extraStatsFromFilledSockets = null;
        this.hasZodInSocket = false;
    }

    public WeaponInfoForDamageCalc upgradeRareOrUniqueToEliteAndAddSocketIfSocketableAndNotAlreadySocketed() {
        if (itemQuality != ItemQuality.RARE && itemQuality != ItemQuality.UNIQUE) {
            throw new RuntimeException("Item was supposed to be unique or rare: " + this);
        }

        int maxSockets = itemType.getMaxSocketsAtHighIlvl(); // TODO use ilvl rather than assuming high ilvl
        return this.toBuilder()
                .itemType(itemType.getEquipmentInfo().getEliteType())
                .totalSockets( (maxSockets > 0 && totalSockets == 0) ? 1 : totalSockets)
                .build();
    }

    public boolean isAlreadyLongLastingOrCanBeFixedWithAZodRune() {
        if (isAlreadyLongLasting()) {
            return true;
        }
        // can we put a zod in it?
        return filledSockets < totalSockets;
    }

    public boolean isAlreadyLongLasting() {
        if (!ethereal) return true;
        if (hasZodInSocket) return true;
        // TODO handle indestructible

        // so we know it's ethereal at this point
        if (itemType.isAThrowingWeapon()) {
            return magicStats.getStat(253) > 0; // self-replenishing
        } else {
            // self-repairing, or an item type that doesn't have durability (phase blade)
            return (magicStats.getStat(252) > 0) || (!itemType.isHasDurability());
        }
    }

    public WeaponInfoForDamageCalc addZodRuneIfItemIsNotAlreadyLongLasting() {
        if (isAlreadyLongLasting()) return this;
        if (filledSockets == totalSockets) {
            throw new RuntimeException("Cannot add Zod Rune since there are no sockets available : " + this);
        }

        return this.toBuilder()
                .filledSockets(filledSockets+1)
                .hasZodInSocket(true)
                .build();
    }

    public WeaponInfoForDamageCalc add_40ED_15IAS_JewelsToRemainingSockets() {
        int remainingSockets = totalSockets - filledSockets;
        if (remainingSockets == 0) return this;
        if (extraStatsFromFilledSockets != null) {
            throw new RuntimeException("Didn't implement merging stat lists yet");
        }

        StatValue ias = StatValue.of(93,0, 15*remainingSockets);
        StatValue maxDamagePct = StatValue.of(17, 0, 40*remainingSockets);
        StatValue minDamagePct = StatValue.of(18, 0, 40*remainingSockets);

        StatList statList = new StatList(List.of(ias, maxDamagePct, minDamagePct));

        return this.toBuilder()
                .filledSockets(totalSockets)
                .extraStatsFromFilledSockets(statList)
                .build();
    }

    public DamageAndDPS getDamage(AttackingContext attackingContext) {

        int minDamageBonus = Math.max(magicStats.getStat(21) /* "minDamage" */, magicStats.getStat(23) /* "secondaryMinDamage" */);
        int maxDamageBonus = Math.max(magicStats.getStat(22) /* "maxDamage" */, magicStats.getStat(24) /* "secondaryMaxDamage" */);

        int minDamage = itemType.getWeaponInfo().getDamageWithNormalHandedness().getMin() + (ethereal ? ((int)(itemType.getWeaponInfo().getDamageWithNormalHandedness().getMin() * 0.5)) : 0);
        int maxDamage = itemType.getWeaponInfo().getDamageWithNormalHandedness().getMax() + (ethereal ? ((int)(itemType.getWeaponInfo().getDamageWithNormalHandedness().getMax() * 0.5)) : 0);

        if ("swor".equals(itemType.getItemTypeTypeCode()) && (itemType.getWeaponInfo().isTwoHanded()) &&
                attackingContext.getCharacterClass() == CharacterClass.BARBARIAN &&
                attackingContext.getUseOneOrTwoHandsIfItsATwoHandedSwordAndYoureABarbarian() == 1) {
            minDamage = itemType.getWeaponInfo().getOneHandedDamageIfTwoHandedSwordWieldedByBarbarian().getMin() + (ethereal ? ((int)(itemType.getWeaponInfo().getOneHandedDamageIfTwoHandedSwordWieldedByBarbarian().getMin() * 0.5)) : 0);
            maxDamage = itemType.getWeaponInfo().getOneHandedDamageIfTwoHandedSwordWieldedByBarbarian().getMax() + (ethereal ? ((int)(itemType.getWeaponInfo().getOneHandedDamageIfTwoHandedSwordWieldedByBarbarian().getMax() * 0.5)) : 0);
        }

        int enhancedDamagePercent = magicStats.getStat(17);
        enhancedDamagePercent += (extraStatsFromFilledSockets == null ? 0 : extraStatsFromFilledSockets.getStat(17));

        int iasOnWeapon = magicStats.getStat(93);
        iasOnWeapon += (extraStatsFromFilledSockets == null ? 0 : extraStatsFromFilledSockets.getStat(93));

        double aps = attackingContext.getAttacksPerSecond(itemType, iasOnWeapon);

        int finalMinDamage = (int) ( minDamage * (1 + enhancedDamagePercent/100.0) + minDamageBonus );
        int finalMaxDamage = (int) ( maxDamage * (1 + enhancedDamagePercent/100.0) + maxDamageBonus );
        int finalAvgDamage = (finalMinDamage + finalMaxDamage)/2;
        int dps = (int) (aps * (finalMinDamage + finalMaxDamage)/2.0);
        return new DamageAndDPS(finalMinDamage, finalMaxDamage, finalAvgDamage, dps);
    }





}
