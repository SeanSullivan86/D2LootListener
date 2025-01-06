package org.sully.d2.itemtracking;

import org.sully.d2.gamemodel.D2Item;
import org.sully.d2.gamemodel.enums.ItemQuality;
import org.sully.d2.gamemodel.staticgamedata.D2UniqueItem;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PerfectUniquesTracker implements ItemConsumer {
    final Map<String, D2UniqueItem> remainingPerfectNonEthUniques = new HashMap<>();
    final Map<String, D2UniqueItem> remainingPerfectEthUniques = new HashMap<>();
    final Map<String, D2Item> foundPerfectNonEthUniques = new LinkedHashMap<>();
    final Map<String, D2Item> foundPerfectEthUniques = new LinkedHashMap<>();

    public PerfectUniquesTracker() {
        for (D2UniqueItem unique : D2UniqueItem.getSpawnableUniquesBelowIlvl100()) {
            if (unique.canBeNonEthereal()) {
                remainingPerfectNonEthUniques.put(unique.getDisambiguatedName(), unique);
            }
            if (unique.canBeEthereal()) {
                remainingPerfectEthUniques.put(unique.getDisambiguatedName(), unique);
            }
        }
        System.out.println("There are " + D2UniqueItem.getSpawnableUniquesBelowIlvl100().size() + " spawnable uniques below qlvl 100");
        System.out.println("There are " + remainingPerfectNonEthUniques.size() + " possible non-eth uniques");
        System.out.println("There are " + remainingPerfectEthUniques.size() + " possible ethereal uniques");
    }


    @Override
    public void consume(D2ItemDrop itemDrop, ItemNotifier notifier) {
        D2Item item = itemDrop.getItem();
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
}
