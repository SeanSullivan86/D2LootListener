package org.sully.d2.itemtracking.uniques;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UniqueStatsSnapshot {
    String name;
    String itemTypeCode;
    double maxPerfection;
    double maxEthPerfection;
    long nonEthCount;
    long ethCount;
    long perfectCount;
    long perfectEthCount;
    Long bestItemId;
    Long bestEthItemId;
    boolean canBeEth;
    boolean canBeNonEth;
    long possibleRolls;
}
