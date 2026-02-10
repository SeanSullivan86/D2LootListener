package org.sully.d2.itemtracking;

import lombok.Builder;
import lombok.Value;
import org.sully.d2.gamemodel.D2Item;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

@Value
@Builder(builderClassName = "Builder", toBuilder = true)
public class D2TCDrop {
    DropContextEnum dropContext;

    List<D2Item> items;

    public static D2TCDrop fromData(byte[] data, ByteBuffer buf, DropContextEnum dropContext, int itemCount) {

        // offset 0 has the singleItem messageLength
        int previousMessageLengths = 0;
        List<D2Item> items = new ArrayList<>();
        for (int i = 0; i < itemCount; i++) {
            int singleItemMessageLength = buf.getShort(previousMessageLengths);

            items.add(D2Item.fromData(data, buf, previousMessageLengths + 2)); // point offset at the first bytes after the singleItemMessageLength
            previousMessageLengths += singleItemMessageLength;
        }

        return D2TCDrop.builder()
                .dropContext(dropContext)
                .items(items)
                .build();
    }


}

