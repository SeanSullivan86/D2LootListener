package org.sully.d2.itemtracking;

import lombok.Builder;
import lombok.Value;

import java.util.Map;
import java.util.Set;

@Value
@Builder
public class StaffmodTrackerSnapshot implements TCDropConsumerSnapshot {

    String id;
    long totalIterations;

    Map<Integer,Integer> countsBySkillId;
    Map<Integer,String> skillNamesById;

    @Override
    public Set<Long> getReferencedItemIds() {
        return Set.of();
    }

}
