package org.sully.d2.itemtracking;

public enum DropContextEnum {
    L85_UNIQUE_MOB,
    L85_NORMAL_MOB,
    HELL_BAAL,
    ALL;

    public static DropContextEnum getFromDropContextDetails(D2DropContext dropContext) {

        if (dropContext.getTreasureClassId() == 883 && dropContext.getUnitClassId() == 544 && dropContext.getGameDifficulty() == 2) {
            return HELL_BAAL;
        }

        if (dropContext.getTreasureClassId() == 808
                && dropContext.getUnitTypeId() == 1
                && dropContext.getGameDifficulty() == 2) {
            return L85_UNIQUE_MOB;
        }

        return L85_NORMAL_MOB;
        /*
        if (dropContext.getTreasureClassId() == 471
                && dropContext.getUnitTypeId() == 1
                && dropContext.getGameDifficulty() == 2) {
            return L85_NORMAL_MOB;
        }*/


        // throw new RuntimeException("Unexpected dropContext details : " + dropContext);
    }
}
