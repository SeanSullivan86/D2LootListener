package org.sully.d2.server;

import lombok.Builder;
import lombok.Value;
import org.sully.d2.itemtracking.DropContextEnum;

import java.util.List;

@Value
@Builder
public class SingleDropContextSummary {
    DropContextEnum dropContext;
    List<ConsumerSummary> consumers;
}
