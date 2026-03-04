package org.sully.d2;

import org.sully.d2.gamemodel.staticgamedata.D2ItemType;

import java.util.Arrays;

/**
 * Generates a TSV file containing data about each ItemType. The output from this utility got copied
 * into the website's source code package for use as a static data file on the website.
 */
public class ItemTypeStaticDataFileGenerator {

    public static void main(String[] args) {
        D2LootListener.loadAndLinkStaticGameData();

        for (D2ItemType itemType : D2ItemType.allItemTypes()) {
            String[] parts = new String[10];
            Arrays.fill(parts, "");
            parts[0] = itemType.getCode();
            parts[1] = itemType.getName();
            parts[2] = itemType.getItemTypeTypeCode();
            if (itemType.getWeaponInfo() != null) {
                parts[3] = ""+itemType.getWeaponInfo().getDamageWithNormalHandedness().getMin();
                parts[4] = ""+itemType.getWeaponInfo().getDamageWithNormalHandedness().getMax();
                if (itemType.getWeaponInfo().getOneHandedDamageIfTwoHandedSwordWieldedByBarbarian() != null) {
                    parts[5] = "" + itemType.getWeaponInfo().getOneHandedDamageIfTwoHandedSwordWieldedByBarbarian().getMin();
                    parts[6] = "" + itemType.getWeaponInfo().getOneHandedDamageIfTwoHandedSwordWieldedByBarbarian().getMax();
                }
            }
            if (itemType.getEquipmentInfo() != null) {
                parts[7] = itemType.getEquipmentInfo().getNormalCode();
                parts[8] = itemType.getEquipmentInfo().getExceptionalCode();
                parts[9] = itemType.getEquipmentInfo().getEliteCode();
            }
            System.out.println(String.join("\t", parts));
        }
    }
}
