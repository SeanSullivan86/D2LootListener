package org.sully.d2.server;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class SummaryInfo {
    List<SingleDropContextSummary> dropContexts;
}
