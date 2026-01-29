package org.sully.d2.itemtracking;

public enum DropContextEnum {
    L85_UNIQUE_MOB,
    L85_NORMAL_MOB;

    public static DropContextEnum getFromDropContextDetails(D2DropContext dropContext) {
        if (/*dropContext.getTreasureClassId() == 471
                && */dropContext.getUnitTypeId() == 1
                && dropContext.getGameDifficulty() == 2) {
            return L85_NORMAL_MOB;
        }

        throw new RuntimeException("Unexpected dropContext details : " + dropContext);
    }
}
