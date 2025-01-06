package org.sully.d2.gamemodel;

import lombok.Value;

import java.util.Set;

@Value
public class StatList {
    StatValue[] stats;

    public int getStat(int statId, int statParam) {
        for (StatValue s : stats) {
            if (s.statId == statId && s.statParam == statParam) {
                return s.statValue;
            }
        }
        return 0;
    }

    public int getStat(int statId) {
        for (StatValue s : stats) {
            if (s.statId == statId && s.statParam == 0) {
                return s.statValue;
            }
        }
        return 0;
    }

    public boolean hasStat(int statId) {
        for (StatValue s : stats) {
            if (s.statId == statId && s.statParam == 0) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAtLeastOneOfTheseStats(Set<Integer> desiredStatIds) {
        for (StatValue stat : stats) {
            if (desiredStatIds.contains(stat.statId)) {
                return true;
            }
        }
        return false;
    }
}