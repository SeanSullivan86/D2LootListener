package org.sully.d2.gamemodel;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class StatValue {
    public int statId;
    public int statParam;
    public int statValue;

    public static StatValue of(int id, int param, int value) {
        return new StatValue(id, param, value);
    }
}
