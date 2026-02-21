package org.sully.d2;

import lombok.Builder;
import lombok.Value;
import org.sully.d2.gamemodel.DamageOption;
import org.sully.d2.gamemodel.StatValue;
import org.sully.d2.gamemodel.enums.ItemQuality;
import org.sully.d2.itemtracking.DropContextEnum;

import java.util.List;

@Value
@Builder
public class SerializableD2Item {
    long id;
    DropContextEnum dropContext;

    ItemQuality quality;
    String name;
    String description;
    boolean ethereal;
    int sockets;
    int gold;
    int defense;

    String itemTypeCode;
    List<StatValue> stats;

    DamageOption originalDmg, originalDmg_1h; // different Consumers will allow or disallow ethereal items
    // "Zod" means "fill with zod if necessary to make the item long-lasting". If it can't be made long-lasting, then these will be null
    DamageOption upSocketZod4015, upSocketZod4015_1h;
    DamageOption upSocketZodOhm, upSocketZodOhm_1h;
    DamageOption upSocketZod, upSocketZod_1h;

    // "eth" options are only populated for ethereal items
    DamageOption upSocket4015_eth, upSocket4015_eth_1h;
    DamageOption upSocketOhm_eth, upSocketOhm_eth_1h;
    DamageOption upSocket_eth, upSocket_eth_1h;

    // derived fields
    // skillBonuses;
    // weaponInfoForDamageCalc
    // uniqueItem
    // itemTypeType
}
