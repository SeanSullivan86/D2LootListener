package org.sully.d2;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

public class SnapshotManager {

    private final File snapshotFolder;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmssSSS'Z'").withZone(ZoneId.of("UTC"));

    public SnapshotManager(String snapshotFolder) {
        this.snapshotFolder = new File(snapshotFolder);
    }

    public Optional<DataSnapshot> retrieveMostRecentSnapshot() {

        Optional<String> recentFilename = Arrays.stream(this.snapshotFolder.list())
                .filter(x -> x.startsWith("D2_ITEMS_"))
                .max(Comparator.naturalOrder());

        if (recentFilename.isEmpty()) return Optional.empty();

        try {
            return Optional.of(objectMapper.readValue(new File(snapshotFolder, recentFilename.get()), DataSnapshot.class));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    public void saveSnapshot(DataSnapshot snapshot) {
        File snapshotFile = new File(snapshotFolder, "D2_ITEMS_" + formatter.format(Instant.now()));
        try {
            objectMapper.writeValue(snapshotFile, snapshot);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
