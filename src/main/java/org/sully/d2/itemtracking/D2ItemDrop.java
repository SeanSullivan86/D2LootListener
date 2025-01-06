package org.sully.d2.itemtracking;

import lombok.Builder;
import lombok.Value;
import org.sully.d2.gamemodel.D2Item;

import java.nio.ByteBuffer;

@Value
@Builder
public class D2ItemDrop {

    D2Item item;
    long dropIteration; // on which iteration of simulating killing the monster did this item drop
    // TODO which monster/object dropped the item

    public static D2ItemDrop fromData(byte[] data, ByteBuffer buf) {

        D2ItemDropBuilder itemDrop = D2ItemDrop.builder();
        itemDrop.dropIteration = buf.getLong(2);
        itemDrop.item = D2Item.fromData(data, buf);

        return itemDrop.build();
    }


}
