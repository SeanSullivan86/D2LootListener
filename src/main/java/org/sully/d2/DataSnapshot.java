package org.sully.d2;

import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Map;

@Value
@Builder
public class DataSnapshot {

    Map<Long, SerializableD2Item> itemsById;
    List<SingleDropContextSnapshot> dropContexts;
    long nextItemId;
}

