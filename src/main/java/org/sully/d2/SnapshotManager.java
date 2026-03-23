package org.sully.d2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.sully.d2.gamemodel.D2Item;
import org.sully.d2.gamemodel.D2ItemMixinForWebsiteSerialization;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.zip.GZIPOutputStream;

public class SnapshotManager {

    private final File snapshotFolder;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ObjectMapper websiteSerializer = new ObjectMapper();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmssSSS'Z'").withZone(ZoneId.of("UTC"));

    public SnapshotManager(String snapshotFolder) {
        this.snapshotFolder = new File(snapshotFolder);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        websiteSerializer.addMixIn(SerializableD2Item.class, D2ItemMixinForWebsiteSerialization.class);
        websiteSerializer.setSerializationInclusion(JsonInclude.Include.NON_NULL);
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

    public static String generateSnapshotId() {
        return "D2_ITEMS_" + formatter.format(Instant.now());
    }

    public void saveSnapshot(DataSnapshot snapshot, R2ObjectStorageClient r2Client) {
        File snapshotFile = new File(snapshotFolder, snapshot.getId() + ".json");
        try {
            objectMapper.writeValue(snapshotFile, snapshot);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        File webSnapshot = new File(snapshotFolder, "WEB_" + snapshot.getId() + ".json.gz");
        try (FileOutputStream fos = new FileOutputStream(webSnapshot);
             GZIPOutputStream gzos = new GZIPOutputStream(fos)) {
            websiteSerializer.writeValue(gzos, snapshot);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (r2Client != null) {
            r2Client.upload("ITEM_SNAPSHOT.json.gz", webSnapshot,
                    Map.of("SNAPSHOT_HOUR", "" + (System.currentTimeMillis() / (3_600_000))));
        }
    }
}
