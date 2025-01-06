package org.sully.d2.gamemodel.derivedstats;

import lombok.Value;

@Value
public class DamageAndDPS {
    int min;
    int max;
    int avg;
    int dps;
}