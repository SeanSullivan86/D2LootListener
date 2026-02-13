package org.sully.d2;

import lombok.Builder;
import lombok.Value;
import org.sully.d2.itemtracking.DropContextEnum;
import org.sully.d2.itemtracking.TCDropConsumerSnapshot;

import java.util.Map;

@Value
@Builder
public class SingleConsumerDataWithItems {
    DropContextEnum dropContext;
    String consumerId;
    Map<Long,SerializableD2Item> items;
    TCDropConsumerSnapshot data;
}
