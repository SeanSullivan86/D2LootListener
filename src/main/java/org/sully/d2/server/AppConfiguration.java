package org.sully.d2.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.sully.d2.SnapshotManager;

@Configuration
public class AppConfiguration {

    @Bean
    public SnapshotManager createSnapshotManager() {
        return new SnapshotManager("C:\\Users\\sully\\D2LootSnapshots");
    }
}
