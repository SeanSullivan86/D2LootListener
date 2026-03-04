package org.sully.d2.itemtracking;

import lombok.Getter;
import org.sully.d2.SerializableD2Item;
import org.sully.d2.gamemodel.D2Item;
import org.sully.d2.gamemodel.derivedstats.SkillBonuses;
import org.sully.d2.gamemodel.enums.ItemQuality;
import org.sully.d2.gamemodel.staticgamedata.D2Skill;

import java.util.*;

public class StaffmodTracker implements D2TCDropConsumer {

    @Getter
    long totalIterations = 0L;

    @Getter
    String id;

    Map<Integer,Integer> countsBySkillId = new HashMap<>();

    public StaffmodTracker(String id) {
        this.id = id;
    }

    @Override
    public void incrementFromSnapshot(TCDropConsumerSnapshot untypedSnapshot, Map<Long, SerializableD2Item> itemsById) {
        StaffmodTrackerSnapshot snapshot = (StaffmodTrackerSnapshot) untypedSnapshot;

        for (Map.Entry<Integer, Integer> e : snapshot.getCountsBySkillId().entrySet()) {
            if (countsBySkillId.containsKey(e.getKey())) {
                countsBySkillId.put(e.getKey(), countsBySkillId.get(e.getKey()) + e.getValue());
            } else {
                countsBySkillId.put(e.getKey(), e.getValue());
            }
        }

        this.totalIterations += snapshot.getTotalIterations();
    }

    @Override
    public void consume(D2TCDrop tcDrop) {
        totalIterations++;
        for (D2Item item : tcDrop.getItems()) {
            consumeItem(item);
        }
    }

    private void consumeItem(D2Item item) {
        if (item.getQuality() == ItemQuality.SET || item.getQuality() == ItemQuality.UNIQUE) return;

        for (SkillBonuses.IndividualSkillBonus skillBonus : item.getSkillBonuses().getIndividualSkillBonuses()) {
            int skillId = skillBonus.getSkill().getId();
            if (countsBySkillId.containsKey(skillId)) {
                countsBySkillId.put(skillId, countsBySkillId.get(skillId) + 1);
            } else {
                countsBySkillId.put(skillId, 1);
                System.out.println("New Staffmod Skill : " + D2Skill.fromId(skillId).getName() + " : " + item.toLongString());
            }
        }
    }

    @Override
    public DataReferencingItems<TCDropConsumerSnapshot> takeSnapshot() {
        return DataReferencingItems.<TCDropConsumerSnapshot>builder()
                .items(List.of())
                .data(StaffmodTrackerSnapshot.builder()
                        .id(id)
                        .totalIterations(totalIterations)
                        .countsBySkillId(new HashMap<>(countsBySkillId))
                        .skillNamesById(D2Skill.skillIdToNameMap)
                        .build())
                .build();
    }
}
