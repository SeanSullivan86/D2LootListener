package org.sully.d2.server;

import org.springframework.stereotype.Component;
import org.sully.d2.DataSnapshot;
import org.sully.d2.SnapshotManager;

@Component
public class SnapshotAccessor {
    
    private volatile DataSnapshot snapshot;

    private final SnapshotManager snapshotManager;

    public SnapshotAccessor(SnapshotManager snapshotManager) {
        this.snapshotManager = snapshotManager;

        this.snapshot = snapshotManager.retrieveMostRecentSnapshot().get();
        System.out.println("Loaded snapshot");
        this.snapshot.logSummary(System.out::println);
    }

    /* TODO : mechanism for refreshing the snapshot */

    public DataSnapshot getSnapshot() {
        return snapshot;
    }

}
