package org.sully.d2.gamemodel.derivedstats;

import lombok.Value;
import org.sully.d2.gamemodel.enums.CharacterClass;
import org.sully.d2.gamemodel.staticgamedata.D2ItemType;

@Value
public class AttackingContext {
    CharacterClass characterClass;
    int additionalIasFromNonWeaponSources;
    int useOneOrTwoHandsIfItsATwoHandedSwordAndYoureABarbarian;

    public double getAttacksPerSecond(D2ItemType itemType, int iasOnWeapon) {
        // TODO determine animation length better...

        int animLength = itemType.getWeaponInfo().getBarbAnimLength();
        if (characterClass == CharacterClass.BARBARIAN && useOneOrTwoHandsIfItsATwoHandedSwordAndYoureABarbarian == 1 && "swor".equals(itemType.getItemTypeType().getCode())) {
            animLength = itemType.getWeaponInfo().getBarbAnimLength1Handed();
        }
        int ias = iasOnWeapon + additionalIasFromNonWeaponSources;
        return getAttacksPerSecondWithoutRounding(animLength, ias, 0, itemType.getWeaponInfo().getSpeed());
    }

    public static double getAttacksPerSecondWithoutRounding(int animLength, int ias, int sias, int wsm) {
        int eiasFromGear = (120*ias)/(120+ias);
        int eiasSum = sias + eiasFromGear - wsm;
        int cappedEiasSum = Math.min(75, eiasSum);
        double bestPossibleFpa = Math.ceil(animLength*256.0/448.0 - 1); // 448 = 256*(100+75)/100
        double fpaNoRounding = animLength*256.0/((256*(100+cappedEiasSum)/100.0)) - 1;

        double fpa = Math.max(bestPossibleFpa, fpaNoRounding);
        return 25.0/fpa;
    }

}