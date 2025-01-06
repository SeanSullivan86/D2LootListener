package org.sully.d2.websocketserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.sully.d2.itemtracking.CountsByQuality;
import org.sully.d2.websocketserver.responses.LiveItemStatsResponse;

public class PeriodicItemBroadcaster implements Runnable {

    private final CountsByQuality countsByQuality;
    private final ObjectMapper jackson = new ObjectMapper();

    public PeriodicItemBroadcaster(CountsByQuality countsByQuality) {
        this.countsByQuality = countsByQuality;
    }

    @Override
    public void run() {

        while (true) {
            try { Thread.sleep(1000); } catch (InterruptedException e) { throw new RuntimeException(e); }

            CountsByQuality.IntervalDurationAndCountsByQuality counts = countsByQuality.takeSnapshotOfCountsAndReportCountsSinceLastSnapshot();

            // on the first call it just saves the first snapshot and doesnt have a diff to report yet
            if (counts == null) continue;

            LiveItemStatsResponse response = LiveItemStatsResponse.builder()
                    .countDiffsByQuality(counts.getCountDiffsByQuality())
                    .totalCountsByQuality(counts.getTotalCountsByQuality())
                    .millisecondsSinceLastSnapshot(counts.getMillisecondsSinceLastSnapshot())
                    .build();

            try {
                String responseString = jackson.writeValueAsString(response);
                System.out.println("Sending Broadcast : " + responseString);
                LootEndpoint.broadcast(responseString);
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
    }
}
