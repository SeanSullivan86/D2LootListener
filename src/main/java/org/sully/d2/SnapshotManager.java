package org.sully.d2;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class SnapshotManager {

    private File snapshotFolder;
    private ObjectMapper objectMapper = new ObjectMapper();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmssSSS'Z'").withZone(ZoneId.of("UTC"));

    public SnapshotManager(String snapshotFolder) {
        this.snapshotFolder = new File(snapshotFolder);
    }

    public DataSnapshot retrieveMostRecentSnapshot() {
        return null;
    }

    public void saveSnapshot(DataSnapshot snapshot) {
        File snapshotFile = new File(snapshotFolder, formatter.format(Instant.now()));
        try {
            objectMapper.writeValue(snapshotFile, snapshot);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
