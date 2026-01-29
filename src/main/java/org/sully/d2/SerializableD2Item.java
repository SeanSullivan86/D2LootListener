package org.sully.d2;

import lombok.Builder;
import lombok.Value;
import org.sully.d2.gamemodel.StatValue;
import org.sully.d2.gamemodel.enums.ItemQuality;
import org.sully.d2.itemtracking.DropContextEnum;

import java.util.List;

@Value
@Builder
public class SerializableD2Item {
    long id;
    DropContextEnum dropContext;
    long dcIteration;

    ItemQuality quality;
    String name;
    String description;
    boolean ethereal;
    int sockets;
    int gold;
    int defense;

    String itemTypeCode;
    List<StatValue> stats;

    // derived fields
    // skillBonuses;
    // weaponInfoForDamageCalc
    // uniqueItem
    // itemTypeType

}
