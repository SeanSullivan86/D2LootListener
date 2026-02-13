package org.sully.d2;

import lombok.Builder;
import lombok.Value;
import org.sully.d2.itemtracking.TCDropConsumerSnapshot;

import java.util.Map;

@Value
@Builder
public class SingleDropContextSnapshot {
    String dropContextName;

    Map<String, TCDropConsumerSnapshot> consumersById;
}
