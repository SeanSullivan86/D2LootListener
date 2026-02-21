package org.sully.d2.itemtracking.uniques;

import lombok.Data;
import org.sully.d2.SerializableD2Item;
import org.sully.d2.gamemodel.D2Item;

import java.util.Map;

@Data
public class UniqueStats {
    public String name;
    public String itemTypeCode;
    public double maxPerfection;
    public double maxEthPerfection;
    public long nonEthCount;
    public long ethCount;
    public long perfectCount;
    public long perfectEthCount;
    public D2Item bestItem;
    public D2Item bestEthItem;
    public boolean canBeEth;
    public boolean canBeNonEth;
    public long possibleRolls;

    public static UniqueStats fromSnapshot(UniqueStatsSnapshot snapshot,
                                           Map<Long, SerializableD2Item> itemsById) {
        UniqueStats x = new UniqueStats();
        x.name = snapshot.getName();
        x.itemTypeCode = snapshot.getItemTypeCode();
        x.maxPerfection = snapshot.getMaxPerfection();
        x.maxEthPerfection = snapshot.getMaxEthPerfection();
        x.nonEthCount = snapshot.getNonEthCount();
        x.perfectCount = snapshot.getPerfectCount();
        x.perfectEthCount = snapshot.getPerfectEthCount();
        x.bestItem = snapshot.getBestItemId() == null ? null :
                D2Item.fromSerializableD2Item(itemsById.get(snapshot.getBestItemId()));
        x.bestEthItem = snapshot.getBestEthItemId() == null ? null :
                D2Item.fromSerializableD2Item(itemsById.get(snapshot.getBestEthItemId()));
        x.canBeEth = snapshot.isCanBeEth();
        x.canBeNonEth = snapshot.isCanBeNonEth();
        x.possibleRolls = snapshot.getPossibleRolls();
        return x;
    }

    public UniqueStatsSnapshot toSnapshot() {
        return UniqueStatsSnapshot.builder()
                .name(name)
                .itemTypeCode(itemTypeCode)
                .maxPerfection(maxPerfection)
                .maxEthPerfection(maxEthPerfection)
                .nonEthCount(nonEthCount)
                .ethCount(ethCount)
                .perfectCount(perfectCount)
                .perfectEthCount(perfectEthCount)
                .bestItemId(bestItem == null ? null : bestItem.getId())
                .bestEthItemId(bestEthItem == null ? null : bestEthItem.getId())
                .canBeEth(canBeEth)
                .canBeNonEth(canBeNonEth)
                .possibleRolls(possibleRolls)
                .build();
    }
}
