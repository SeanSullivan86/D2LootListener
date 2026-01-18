package org.sully.d2.itemtracking;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(builderClassName = "Builder", toBuilder = true)
public class D2DropContext {
    int treasureClassId;
    int magicFind;
    int unitTypeId;
    int unitClassId;
    int gameDifficulty;
}
