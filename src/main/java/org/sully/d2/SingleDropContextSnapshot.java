package org.sully.d2;

import lombok.Builder;
import lombok.Value;
import org.sully.d2.itemtracking.TCDropConsumerSnapshot;

import java.util.List;

@Value
@Builder
public class SingleDropContextSnapshot {
    String dropContextName;

    List<TCDropConsumerSnapshot> consumers;
}
