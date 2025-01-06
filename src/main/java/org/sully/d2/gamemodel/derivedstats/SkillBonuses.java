package org.sully.d2.gamemodel.derivedstats;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.sully.d2.gamemodel.enums.CharacterClass;
import org.sully.d2.gamemodel.enums.SkillTab;
import org.sully.d2.gamemodel.StatList;
import org.sully.d2.gamemodel.StatValue;
import org.sully.d2.gamemodel.staticgamedata.D2ItemTypeType;
import org.sully.d2.gamemodel.staticgamedata.D2Skill;

import java.util.ArrayList;
import java.util.List;

@Value
@Builder
public class SkillBonuses {
    List<IndividualSkillBonus> individualSkillBonuses;
    List<SkillTabBonus> skillTabBonuses;
    List<ClassSkillBonus> classSkillBonuses;

    public boolean hasAtLeastOneSkillTabBonus() {
        return !skillTabBonuses.isEmpty();
    }

    public SkillTabBonus getSkillTabBonus(SkillTab skillTab) {
        for (SkillBonuses.SkillTabBonus skillTabBonus : skillTabBonuses) {
            if (skillTabBonus.getSkillTab() == skillTab) {
                return skillTabBonus;
            }
        }
        return null;
    }

    public int getTotalBonusIncludingSkillTabAndClassSkillBonuses(D2Skill skill) {
        for (IndividualSkillBonus bonus : this.individualSkillBonuses) {
            if (bonus.getSkill() == skill) {
                return bonus.getTotalBonusIncludingSkillTabAndCharacterClassSkillBonuses();
            }
        }
        for (SkillTabBonus skillTabBonus : this.skillTabBonuses) {
            if (skillTabBonus.getSkillTab() == skill.getSkillTab()) {
                return skillTabBonus.getTotalBonusIncludingCharacterClassSkillBonuses();
            }
        }
        for (ClassSkillBonus classBonus : this.classSkillBonuses) {
            if (skill.getCharacterClass() == classBonus.getCharacterClass()) {
                return classBonus.getSkillLevelBonus();
            }
        }
        return 0;
    }

    public List<IndividualSkillBonus> getIllegalStaffmods(D2ItemTypeType itemTypeType) {
        List<IndividualSkillBonus> illegalMods = new ArrayList<>();
        for (IndividualSkillBonus skillBonus : this.individualSkillBonuses) {
            if (skillBonus.getSkill().getAllowedItemTypeTypeForStaffmod() != null) {
                if (! itemTypeType.isEqualToOrASubtypeOf(skillBonus.getSkill().getAllowedItemTypeTypeForStaffmod())) {
                    illegalMods.add(skillBonus);
                }
            }
        }
        return illegalMods;
    }

    public static SkillBonuses deriveSkillBonusesFromStats(StatList stats) {

        List<IndividualSkillBonus> individualSkillBonuses = new ArrayList<>();
        List<SkillTabBonus> skillTabBonuses = new ArrayList<>();
        List<ClassSkillBonus> classSkillBonuses = new ArrayList<>();

        for (StatValue stat : stats.getStats()) {
            if (stat.statId == 107) {
                individualSkillBonuses.add(IndividualSkillBonus.builder()
                        .skill(D2Skill.fromId(stat.statParam))
                        .skillLevelBonus(stat.statValue)
                        .build());
            } else if (stat.statId == 188) {
                skillTabBonuses.add(SkillTabBonus.builder()
                        .skillTab(SkillTab.fromItemStatParam(stat.statParam))
                        .skillLevelBonus(stat.statValue)
                        .build());
            } else if (stat.statId == 83) {
                classSkillBonuses.add(new ClassSkillBonus(CharacterClass.fromId(stat.statParam), stat.statValue));
            }
        }

        for (IndividualSkillBonus b : individualSkillBonuses) {
            b.assignTotalBonusFromOtherSkillBonuses(skillTabBonuses, classSkillBonuses);
        }
        for (SkillTabBonus b : skillTabBonuses) {
            b.assignTotalBonusFromOtherSkillBonuses(classSkillBonuses);
        }

        return SkillBonuses.builder()
                .individualSkillBonuses(individualSkillBonuses)
                .skillTabBonuses(skillTabBonuses)
                .classSkillBonuses(classSkillBonuses)
                .build();
    }


    @Value
    @Builder
    public static class IndividualSkillBonus {
        @Getter(onMethod_ = @JsonIgnore)
        D2Skill skill;

        int skillLevelBonus;

        @NonFinal
        int totalBonusIncludingSkillTabAndCharacterClassSkillBonuses;

        private void assignTotalBonusFromOtherSkillBonuses(List<SkillTabBonus> skillTabBonuses, List<ClassSkillBonus> classSkillBonuses) {
            int total = skillLevelBonus;
            for (SkillTabBonus tabBonus : skillTabBonuses) {
                if (skill.getSkillTab() == tabBonus.skillTab) {
                    total += tabBonus.skillLevelBonus;
                }
            }
            for (ClassSkillBonus classBonus : classSkillBonuses) {
                if (skill.getCharacterClass() == classBonus.characterClass) {
                    total += classBonus.skillLevelBonus;
                }
            }
            totalBonusIncludingSkillTabAndCharacterClassSkillBonuses = total;
        }

        public String getSkillName() {
            return skill.getName();
        }
    }

    @Value
    @Builder
    public static class SkillTabBonus {
        SkillTab skillTab;
        int skillLevelBonus;

        @NonFinal
        int totalBonusIncludingCharacterClassSkillBonuses;

        private void assignTotalBonusFromOtherSkillBonuses(List<ClassSkillBonus> classSkillBonuses) {
            int total = skillLevelBonus;
            for (ClassSkillBonus classBonus : classSkillBonuses) {
                if (skillTab.getCharacterClass() == classBonus.characterClass) {
                    total += classBonus.skillLevelBonus;
                }
            }
            totalBonusIncludingCharacterClassSkillBonuses = total;
        }
    }

    @Value
    @Builder
    public static class ClassSkillBonus {
        CharacterClass characterClass;
        int skillLevelBonus;

    }
}
