package org.sully.d2.itemtracking.uniques;

import lombok.*;
import org.sully.d2.SerializableD2Item;
import org.sully.d2.gamemodel.D2Item;
import org.sully.d2.gamemodel.enums.ItemQuality;
import org.sully.d2.gamemodel.staticgamedata.D2UniqueItem;
import org.sully.d2.itemtracking.D2TCDrop;
import org.sully.d2.itemtracking.D2TCDropConsumer;
import org.sully.d2.itemtracking.DataReferencingItems;
import org.sully.d2.itemtracking.TCDropConsumerSnapshot;

import java.util.*;


public class PerfectUniquesTracker implements D2TCDropConsumer {

    @Getter
    long totalIterations = 0L;

    @Getter
    String id;

    private Map<String,UniqueStats> uniqueStatsByName;
    private Map<String,D2UniqueItem> uniqueItemsByName;

    public PerfectUniquesTracker() {
        id = "PERFECT_UNIQUES_TRACKER";
        uniqueItemsByName = new HashMap<>();
        uniqueStatsByName = new HashMap<>();

        for (D2UniqueItem unique : D2UniqueItem.getSpawnableUniquesBelowIlvl100()) {
            uniqueItemsByName.put(unique.getDisambiguatedName(), unique);
            UniqueStats stats = new UniqueStats();
            stats.name = unique.getDisambiguatedName();
            stats.itemTypeCode = unique.getItemTypeCode();
            stats.possibleRolls = unique.getCountOfPossibleRolls();
            stats.canBeEth = unique.canBeEthereal();
            stats.canBeNonEth = unique.canBeNonEthereal();

            uniqueStatsByName.put(unique.getDisambiguatedName(), stats);
        }
    }



    @Override
    public void consume(D2TCDrop tcDrop) {
        totalIterations++;
        for (D2Item item : tcDrop.getItems()) {
            consumeItem(item);
        }
    }

    private void consumeItem(D2Item item) {
        if (item.getQuality() != ItemQuality.UNIQUE) return;
        D2UniqueItem unique = item.getUniqueItem();
        String name = unique.getDisambiguatedName();

        UniqueStats stats = uniqueStatsByName.get(name);

        boolean isPerfect = unique.isPerfect(item);
        double perfectionScore = isPerfect ? 1.0 : unique.getPerfectionScore(item);

        if (item.isEthereal()) {
            stats.ethCount++;
            if (perfectionScore > stats.maxEthPerfection) {
                stats.maxEthPerfection = perfectionScore;
                stats.bestEthItem = item;
            }
            if (isPerfect) {
                stats.perfectEthCount++;
            }
        } else {
            stats.nonEthCount++;
            if (perfectionScore > stats.maxPerfection) {
                stats.maxPerfection = perfectionScore;
                stats.bestItem = item;
                if (perfectionScore == 1.0) {
                    System.out.println("NEW PERFECT UNIQUE : " + item.toLongString());
                }
            }
            if (isPerfect) {
                stats.perfectCount++;
            }
        }
    }

    @Override
    public void incrementFromSnapshot(TCDropConsumerSnapshot untypedSnapshot, Map<Long, SerializableD2Item> itemsById) {
        PerfectUniquesSnapshot snapshot = (PerfectUniquesSnapshot) untypedSnapshot;
        for (Map.Entry<String, UniqueStatsSnapshot> e : snapshot.getStatsByName().entrySet()) {
            if (uniqueStatsByName.containsKey(e.getKey())) {
                uniqueStatsByName.put(e.getKey(), UniqueStats.merge(
                        UniqueStats.fromSnapshot(e.getValue(), itemsById),
                        uniqueStatsByName.get(e.getKey())));
            } else {
                uniqueStatsByName.put(e.getKey(), UniqueStats.fromSnapshot(e.getValue(), itemsById));
            }
        }
        totalIterations += snapshot.getTotalIterations();
        if (! snapshot.getId().equals(id)) {
            throw new RuntimeException("Unexpected id : " + snapshot.getId());
        }
    }

    @Override
    public DataReferencingItems<TCDropConsumerSnapshot> takeSnapshot() {

        Set<Long> itemIds = new HashSet<>();
        List<D2Item> itemsToSave = new ArrayList<>();
        Map<String,UniqueStatsSnapshot> snapshotsByName = new HashMap<>();

        for (UniqueStats stats : uniqueStatsByName.values()) {
            if (stats.bestItem != null && (!itemIds.contains(stats.bestItem.getId()))) {
                itemIds.add(stats.bestItem.getId());
                itemsToSave.add(stats.bestItem);
            }
            if (stats.bestEthItem != null && (!itemIds.contains(stats.bestEthItem.getId()))) {
                itemIds.add(stats.bestEthItem.getId());
                itemsToSave.add(stats.bestEthItem);
            }
            snapshotsByName.put(stats.name, stats.toSnapshot());
        }


        return DataReferencingItems.<TCDropConsumerSnapshot>builder()
                .data(PerfectUniquesSnapshot.builder()
                        .statsByName(snapshotsByName)
                        .id(id)
                        .totalIterations(totalIterations)
                        .build())
                .items(itemsToSave)
                .build();
    }


    /*
    @Override
    public void consume(D2Item item, ItemNotifier notifier) {
        if (item.getQuality() != ItemQuality.UNIQUE) return;
        D2UniqueItem unique = item.getUniqueItem();
        String name = unique.getDisambiguatedName();
        if (item.isEthereal() && remainingPerfectEthUniques.containsKey(name)) {
            if (item.getUniqueItem().isPerfect(item)) {
                System.out.println(item.toLongString());
                foundPerfectEthUniques.put(name, item);
                item.getUniqueItem().printPerfectItemDetails(item);
                remainingPerfectEthUniques.remove(name);
                System.out.println(remainingPerfectEthUniques.size() + " Ethereal remaining ...");
                if (remainingPerfectEthUniques.size() < 100) {
                    System.out.println("Remaining Ethereal Perfect Uniques : " + remainingPerfectEthUniques.entrySet().stream()
                            .map(e -> e.getKey() + "(1 in " + e.getValue().getCountOfPossibleRolls() + ")")
                            .collect(Collectors.joining(", ")));
                }
            }
        } else if ( (!item.isEthereal()) && remainingPerfectNonEthUniques.containsKey(name)) {
            if (item.getUniqueItem().isPerfect(item)) {
                foundPerfectNonEthUniques.put(name, item);
                System.out.println(item.toLongString());
                item.getUniqueItem().printPerfectItemDetails(item);
                remainingPerfectNonEthUniques.remove(name);
                System.out.println(remainingPerfectNonEthUniques.size() + " Non-Ethereal remaining ...");
                if (remainingPerfectNonEthUniques.size() < 100) {
                    System.out.println("Remaining Non-Ethereal Perfect Uniques : " + remainingPerfectNonEthUniques.entrySet().stream()
                            .map(e -> e.getKey() + "(1 in " + e.getValue().getCountOfPossibleRolls() + ")")
                            .collect(Collectors.joining(", ")));
                }
            }
        }
    }

    @Override
    public void closeAndGenerateOutput() {
        try (PrintWriter out = new PrintWriter(new FileWriter("output/perfectUniques.txt"))) {
            out.println("Found " + foundPerfectEthUniques.size() + " different perfect ethereal uniques");
            for (Map.Entry<String, D2Item> entry : foundPerfectEthUniques.entrySet()) {
                out.println(entry.getValue().toLongString());
            }
            out.println("Found " + foundPerfectNonEthUniques.size() + " different perfect non-ethereal uniques");
            for (Map.Entry<String, D2Item> entry : foundPerfectNonEthUniques.entrySet()) {
                out.println(entry.getValue().toLongString());
            }

            System.out.println("Remaining Ethereal Perfect Uniques ( " + remainingPerfectEthUniques.size() + " ) :\n" + remainingPerfectEthUniques.entrySet().stream()
                    .map(e -> e.getKey() + "(1 in " + e.getValue().getCountOfPossibleRolls() + ")")
                    .collect(Collectors.joining("\n")));
            System.out.println("Remaining Non-Ethereal Perfect Uniques ( " + remainingPerfectNonEthUniques.size() + " ) :\n" + remainingPerfectNonEthUniques.entrySet().stream()
                    .map(e -> e.getKey() + "(1 in " + e.getValue().getCountOfPossibleRolls() + ")")
                    .collect(Collectors.joining("\n")));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

     */


}
